import { useEffect, useState, useCallback, type ReactNode } from "react";
import {
  tokenStore,
  loginRaw,
  logoutRaw,
  tryRefresh,
  ApiError,
} from "./api-client";
import { logger } from "./logger";
import { userApi } from "./api";
import type { UserRole } from "./types";
import { AuthContext } from "./use-auth";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<import("./types").UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [role, setRoleState] = useState<UserRole>(
    (tokenStore.getRole() as UserRole) || "USER",
  );

  const loadUser = useCallback(async () => {
    if (!tokenStore.getAccess()) {
      // Page reload wipes in-memory access token. Try silent refresh via httpOnly cookie.
      const refreshed = await tryRefresh();
      if (!refreshed) {
        setUser(null);
        setIsLoading(false);
        return;
      }
    }
    try {
      const profile = await userApi.me();
      setUser(profile);
      if (profile.roles && profile.roles.length > 0) {
        const r = profile.roles.includes("SYS_ADMIN")
          ? "SYS_ADMIN"
          : profile.roles.includes("ADMIN")
            ? "ADMIN"
            : "USER";
        setRoleState(r);
        tokenStore.setRole(r);
      }
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        logger.info("auth.loadUser: unauthenticated, clearing");
        tokenStore.clear();
        setUser(null);
      } else {
        logger.error("auth.loadUser: failed", {
          status: e instanceof ApiError ? e.status : undefined,
          message: e instanceof Error ? e.message : String(e),
        });
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUser();
  }, [loadUser]);

  const login = useCallback(
    async (
      username: string,
      password: string,
      intendedRole: UserRole = "USER",
    ) => {
      logger.info("auth.login: start", { intendedRole });
      const tokens = await loginRaw({ username, password });
      if (!tokens.access_token) {
        logger.error("auth.login: no access_token in response");
        throw new ApiError("Login response missing access token", 200);
      }
      tokenStore.set(tokens, intendedRole);
      setRoleState(intendedRole);
      logger.debug("auth.login: token set, fetching profile");
      const profile = await userApi.me();
      setUser(profile);
      if (profile.roles && profile.roles.length > 0) {
        const r = profile.roles.includes("SYS_ADMIN")
          ? "SYS_ADMIN"
          : profile.roles.includes("ADMIN")
            ? "ADMIN"
            : "USER";
        setRoleState(r);
        tokenStore.setRole(r);
      }
      return profile;
    },
    [],
  );

  const logout = useCallback(() => {
    logoutRaw();
    tokenStore.clear();
    setUser(null);
    setRoleState("USER");
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        role,
        login,
        logout,
        refresh: loadUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
