import type { BaseResponse, AuthTokens } from "./types";
import { logger } from "./logger";

const STORAGE_KEYS = {
  tenant: "mtj.tenant_slug",
  role: "mtj.role", // "USER" | "ADMIN" | "SYS_ADMIN"
} as const;

// Access token held in-memory only. Refresh token lives in httpOnly cookie (server-set).
let accessTokenMem: string | null = null;

export const tokenStore = {
  getAccess(): string | null {
    return accessTokenMem;
  },
  getTenant(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(STORAGE_KEYS.tenant);
  },
  getRole(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(STORAGE_KEYS.role);
  },
  set(tokens: AuthTokens, role?: string) {
    accessTokenMem = tokens.access_token;
    if (tokens.defaultTenantSlug) {
      localStorage.setItem(STORAGE_KEYS.tenant, tokens.defaultTenantSlug);
    }
    if (role) localStorage.setItem(STORAGE_KEYS.role, role);
  },
  setRole(role: string) {
    localStorage.setItem(STORAGE_KEYS.role, role);
  },
  clear() {
    accessTokenMem = null;
    localStorage.removeItem(STORAGE_KEYS.tenant);
    localStorage.removeItem(STORAGE_KEYS.role);
  },
};

export function getApiBase(): string {
  const env = (import.meta as { env?: Record<string, string | undefined> }).env;
  return env?.VITE_API_BASE_URL || "http://localhost:2000";
}

export class ApiError extends Error {
  status: number;
  data?: unknown;
  constructor(message: string, status: number, data?: unknown) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

interface RequestOptions {
  method?: string;
  body?: unknown;
  query?: Record<string, string | number | boolean | undefined | null>;
  headers?: Record<string, string>;
  raw?: boolean; // skip BaseResponse unwrap
  isForm?: boolean;
}

function buildUrl(path: string, query?: RequestOptions["query"]): string {
  const url = new URL(
    path.replace(/^\//, ""),
    getApiBase().replace(/\/?$/, "/"),
  );
  if (query) {
    Object.entries(query).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== "") {
        url.searchParams.set(k, String(v));
      }
    });
  }
  return url.toString();
}

export async function tryRefresh(): Promise<boolean> {
  const url = buildUrl("/api/v1/auth/refresh-token");
  try {
    logger.debug("auth.refresh: start", { url });
    const res = await fetch(url, { method: "POST", credentials: "include" });
    logger.debug("auth.refresh: response", { status: res.status });
    if (!res.ok) {
      logger.warn("auth.refresh: non-ok", { status: res.status });
      return false;
    }
    const json = (await res.json()) as AuthTokens;
    if (!json.access_token) {
      logger.warn("auth.refresh: no access_token in body");
      return false;
    }
    tokenStore.set(json, tokenStore.getRole() ?? undefined);
    return true;
  } catch (e) {
    logger.error("auth.refresh: network error", { error: describeError(e) });
    return false;
  }
}

async function rawRequest<T>(
  path: string,
  opts: RequestOptions = {},
  retried = false,
): Promise<T> {
  const url = buildUrl(path, opts.query);
  const headers: Record<string, string> = { ...(opts.headers || {}) };
  const access = tokenStore.getAccess();
  if (access) headers["Authorization"] = `Bearer ${access}`;

  let body: BodyInit | undefined;
  if (opts.body !== undefined && opts.body !== null) {
    if (opts.isForm && opts.body instanceof FormData) {
      body = opts.body;
    } else {
      headers["Content-Type"] = "application/json";
      body = JSON.stringify(opts.body);
    }
  }

  const method = opts.method || "GET";
  const started = performance.now();
  logger.debug("api: request", { method, url, retried });

  let res: Response;
  try {
    res = await fetch(url, { method, headers, body, credentials: "include" });
  } catch (e) {
    logger.error("api: network failure", {
      method,
      url,
      error: describeError(e),
    });
    throw new ApiError("Network error — check connection or CORS/config", 0, e);
  }

  const elapsedMs = Math.round(performance.now() - started);
  logger.debug("api: response", { method, url, status: res.status, elapsedMs });

  if (res.status === 401 && !retried) {
    logger.info("api: 401, attempting refresh", { url });
    const ok = await tryRefresh();
    if (ok) return rawRequest<T>(path, opts, true);
    tokenStore.clear();
  }

  if (res.status === 204) return undefined as T;

  let json: unknown = null;
  const text = await res.text();
  if (text) {
    try {
      json = JSON.parse(text);
    } catch {
      json = text;
    }
  }

  if (!res.ok) {
    const rawMsg =
      (json &&
      typeof json === "object" &&
      "message" in json &&
      typeof (json as { message: unknown }).message === "string"
        ? (json as { message: string }).message
        : null) ||
      res.statusText ||
      "Request failed";
    const msg = humanizeErrorMessage(rawMsg, res.status);
    logger.warn("api: error response", {
      method,
      url,
      status: res.status,
      message: msg,
    });
    throw new ApiError(msg, res.status, json);
  }

  return json as T;
}

const KEYCLOAK_ERROR_MAP: Record<string, string> = {
  "error-username-invalid-character":
    "Username has invalid characters. Use lowercase letters, numbers, dot, underscore or hyphen.",
  "error-invalid-email": "Email address is not valid.",
  "error-email-exists": "An account with this email already exists.",
  "error-username-exists": "That username is already taken.",
  "error-user-exists": "An account with these details already exists.",
  "error-invalid-password": "Password does not meet requirements.",
  "error-password-too-short": "Password is too short.",
  "error-password-min-length": "Password is too short.",
  invalid_grant: "Incorrect username or password.",
};

function humanizeErrorMessage(raw: string, status: number): string {
  // Try to pull a JSON blob out of the message (Keycloak/backend embed patterns)
  const jsonMatch = raw.match(/\{[\s\S]*\}/);
  if (jsonMatch) {
    try {
      const parsed = JSON.parse(jsonMatch[0]) as {
        errorMessage?: string;
        error_description?: string;
        error?: string;
        field?: string;
      };
      const code = parsed.errorMessage || parsed.error || "";
      if (code && KEYCLOAK_ERROR_MAP[code]) return KEYCLOAK_ERROR_MAP[code];
      if (parsed.error_description) return parsed.error_description;
      if (code) return humanizeCode(code);
    } catch {
      // fall through
    }
  }
  if (KEYCLOAK_ERROR_MAP[raw]) return KEYCLOAK_ERROR_MAP[raw];
  if (status === 401) return "Incorrect username or password.";
  if (status === 403) return "You don't have permission to do that.";
  if (status === 404) return "Not found.";
  if (status === 409) return "That already exists.";
  if (status >= 500) return "Server error. Please try again.";
  return raw;
}

function humanizeCode(code: string): string {
  return code
    .replace(/^error-/, "")
    .replace(/[-_]/g, " ")
    .replace(/^./, (c) => c.toUpperCase());
}

function describeError(e: unknown): Record<string, unknown> {
  if (e instanceof Error) {
    return { name: e.name, message: e.message };
  }
  return { value: String(e) };
}

export async function api<T>(
  path: string,
  opts: RequestOptions = {},
): Promise<T> {
  const json = await rawRequest<BaseResponse<T> | T>(path, opts);
  if (opts.raw) return json as T;
  if (
    json &&
    typeof json === "object" &&
    "success" in (json as object) &&
    "data" in (json as object)
  ) {
    const env = json as BaseResponse<T>;
    if (env.success === false) {
      throw new ApiError(env.message || "Request failed", 200, env);
    }
    return env.data;
  }
  return json as T;
}

// Auth endpoints (raw response — no envelope)
export async function loginRaw(body: {
  username: string;
  password: string;
}): Promise<AuthTokens> {
  return rawRequest<AuthTokens>("/api/v1/auth/login", { method: "POST", body });
}

export async function logoutRaw(): Promise<void> {
  try {
    await fetch(buildUrl("/api/v1/auth/logout"), {
      method: "POST",
      credentials: "include",
    });
  } catch {
    // ignore — still clear local state
  }
}
