import { createFileRoute, useNavigate, Link } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/use-auth";
import { Button } from "@/components/ui/inputs/button";
import { Input } from "@/components/ui/inputs/input";
import { Label } from "@/components/ui/inputs/label";
import { ApiError } from "@/lib/api-client";
import { toast } from "sonner";
import { Eye, EyeOff, Loader2, ShieldCheck } from "lucide-react";
import { AuthShell } from "./login";

export const Route = createFileRoute("/admin/login")({
  head: () => ({ meta: [{ title: "Admin sign in - MindTrack" }] }),
  component: AdminLoginPage,
});

function AdminLoginPage() {
  const { login, isAuthenticated, isLoading, role } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const onPasswordKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.currentTarget.form?.requestSubmit();
    }
  };

  useEffect(() => {
    if (
      !isLoading &&
      isAuthenticated &&
      (role === "ADMIN" || role === "SYS_ADMIN")
    ) {
      navigate({ to: "/admin" });
    }
  }, [isLoading, isAuthenticated, role, navigate]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const profile = await login(username, password, "ADMIN");
      const roles = profile.roles ?? [];
      if (roles.includes("ADMIN") || roles.includes("SYS_ADMIN")) {
        toast.success("Welcome back, admin.");
        navigate({ to: "/admin" });
      } else {
        // Server didn't return admin role - proceed anyway based on intent, but warn
        toast.warning("Signed in. Some admin actions may be restricted.");
        navigate({ to: "/admin" });
      }
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : "Could not sign in";
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Admin console"
      subtitle="Privileged access - sign in to continue."
    >
      <div className="mb-5 flex items-center gap-2 rounded-xl border border-border bg-secondary/40 px-3 py-2 text-xs text-secondary-foreground">
        <ShieldCheck className="h-4 w-4 text-bloom" /> Admin & System Admin only
      </div>
      <form onSubmit={onSubmit} className="space-y-5">
        <div className="space-y-1.5">
          <Label
            htmlFor="username"
            className="text-xs uppercase tracking-wider text-muted-foreground"
          >
            Username
          </Label>
          <Input
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            autoComplete="username"
            className="h-11 rounded-xl border-border bg-card/60"
          />
        </div>
        <div className="space-y-1.5">
          <Label
            htmlFor="password"
            className="text-xs uppercase tracking-wider text-muted-foreground"
          >
            Password
          </Label>
          <div className="relative">
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={onPasswordKeyDown}
              required
              autoComplete="current-password"
              className="h-11 rounded-xl border-border bg-card/60 pr-11"
            />
            <div className="absolute right-2 top-1/2 -translate-y-1/2">
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                className="inline-flex h-8 w-8 items-center justify-center rounded-md text-muted-foreground transition hover:text-foreground"
              >
                {showPassword ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </button>
            </div>
          </div>
        </div>
        <Button
          type="submit"
          disabled={submitting}
          className="w-full rounded-full shadow-bloom"
        >
          {submitting ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            "Enter console"
          )}
        </Button>
      </form>
      <p className="mt-7 text-center text-sm text-muted-foreground">
        Not an admin?{" "}
        <Link
          to="/login"
          className="text-primary underline-offset-4 hover:underline"
        >
          User sign in
        </Link>
      </p>
    </AuthShell>
  );
}
