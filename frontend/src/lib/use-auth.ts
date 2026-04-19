import { createContext, useContext } from "react";
import type { UserProfile, UserRole } from "./types";

export interface AuthState {
  user: UserProfile | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  role: UserRole;
  login: (
    username: string,
    password: string,
    intendedRole?: UserRole,
  ) => Promise<UserProfile>;
  logout: () => void;
  refresh: () => Promise<void>;
}

export const AuthContext = createContext<AuthState | null>(null);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
