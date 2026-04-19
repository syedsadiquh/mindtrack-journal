// Lightweight frontend logger.
// - `debug` only in dev, or when VITE_LOG_LEVEL=debug is set (prod diagnostics).
// - `info`, `warn`, `error` always emit.
// - Never log tokens, passwords, or full request/response bodies — only metadata.
// - In prod, point `VITE_LOG_SINK_URL` at a collector (e.g. /api/v1/client-logs) to forward warn+error.

type Level = "debug" | "info" | "warn" | "error";

const env = (import.meta as { env?: Record<string, unknown> }).env;
const isDev = env?.DEV === true || env?.MODE === "development";
const configuredLevel = ((env?.VITE_LOG_LEVEL as string | undefined) ||
  (isDev ? "debug" : "info")) as Level;
const sinkUrl = env?.VITE_LOG_SINK_URL as string | undefined;

const ORDER: Record<Level, number> = {
  debug: 10,
  info: 20,
  warn: 30,
  error: 40,
};

function shouldEmit(level: Level): boolean {
  return ORDER[level] >= ORDER[configuredLevel];
}

function emit(level: Level, msg: string, meta?: Record<string, unknown>) {
  if (!shouldEmit(level)) return;
  const entry = {
    ts: new Date().toISOString(),
    level,
    msg,
    ...(meta ?? {}),
  };
  const line = `[mtj:${level}] ${msg}`;
  switch (level) {
    case "debug":
      console.debug(line, meta ?? "");
      break;
    case "info":
      console.info(line, meta ?? "");
      break;
    case "warn":
      console.warn(line, meta ?? "");
      break;
    case "error":
      console.error(line, meta ?? "");
      break;
  }
  if (sinkUrl && (level === "warn" || level === "error")) {
    try {
      const body = JSON.stringify(entry);
      if (typeof navigator !== "undefined" && "sendBeacon" in navigator) {
        const blob = new Blob([body], { type: "application/json" });
        navigator.sendBeacon(sinkUrl, blob);
      } else {
        fetch(sinkUrl, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body,
          keepalive: true,
        }).catch(() => {});
      }
    } catch {
      // swallow — logger must never throw
    }
  }
}

export const logger = {
  debug: (msg: string, meta?: Record<string, unknown>) =>
    emit("debug", msg, meta),
  info: (msg: string, meta?: Record<string, unknown>) =>
    emit("info", msg, meta),
  warn: (msg: string, meta?: Record<string, unknown>) =>
    emit("warn", msg, meta),
  error: (msg: string, meta?: Record<string, unknown>) =>
    emit("error", msg, meta),
};
