import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/use-auth";
import { Brand } from "@/components/brand";
import { Button } from "@/components/ui/inputs/button";
import { Input } from "@/components/ui/inputs/input";
import { Label } from "@/components/ui/inputs/label";
import { ApiError } from "@/lib/api-client";
import { logger } from "@/lib/logger";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";

interface LoginSearch {
  redirect?: string;
}

export const Route = createFileRoute("/login")({
  head: () => ({ meta: [{ title: "Sign in — MindTrack" }] }),
  validateSearch: (s: Record<string, unknown>): LoginSearch => ({
    redirect: typeof s.redirect === "string" ? s.redirect : undefined,
  }),
  component: LoginPage,
});

function LoginPage() {
  const { login, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();
  const search = Route.useSearch();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      navigate({ to: search.redirect ?? "/app" });
    }
  }, [isLoading, isAuthenticated, navigate, search.redirect]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await login(username, password, "USER");
      toast.success("Welcome back.");
      navigate({ to: search.redirect ?? "/app" });
    } catch (err) {
      const status = err instanceof ApiError ? err.status : undefined;
      const msg =
        err instanceof ApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : "Could not sign in";
      logger.error("login: submit failed", {
        status,
        message: msg,
        errorType: err instanceof ApiError ? "ApiError" : typeof err,
      });
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell title="Welcome back" subtitle="Slip back into your quiet space.">
      <form onSubmit={onSubmit} className="space-y-5">
        <Field
          label="Username"
          id="username"
          value={username}
          onChange={setUsername}
          autoComplete="username"
          required
        />
        <Field
          label="Password"
          id="password"
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="current-password"
          required
        />
        <Button
          type="submit"
          className="w-full rounded-full shadow-bloom"
          disabled={submitting}
        >
          {submitting ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            "Sign in"
          )}
        </Button>
      </form>
      <p className="mt-7 text-center text-sm text-muted-foreground">
        New here?{" "}
        <Link
          to="/register"
          className="text-primary underline-offset-4 hover:underline"
        >
          Create your journal
        </Link>
      </p>
    </AuthShell>
  );
}

interface FieldProps {
  label: string;
  id: string;
  value: string;
  onChange: (v: string) => void;
  type?: string;
  required?: boolean;
  autoComplete?: string;
}

function Field({
  label,
  id,
  value,
  onChange,
  type = "text",
  required,
  autoComplete,
}: FieldProps) {
  return (
    <div className="space-y-1.5">
      <Label
        htmlFor={id}
        className="text-xs uppercase tracking-wider text-muted-foreground"
      >
        {label}
      </Label>
      <Input
        id={id}
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        autoComplete={autoComplete}
        className="h-11 rounded-xl border-border bg-card/60"
      />
    </div>
  );
}

interface AuthShellProps {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
}

export function AuthShell({ title, subtitle, children }: AuthShellProps) {
  return (
    <div className="relative min-h-screen overflow-hidden bg-background">
      <div className="pointer-events-none absolute -top-40 right-1/2 h-[600px] w-[800px] translate-x-1/2 rounded-full bg-rose/25 blur-[160px]" />
      <header className="relative mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
        <Brand />
        <Link
          to="/"
          className="text-sm text-muted-foreground hover:text-foreground"
        >
          ← Back
        </Link>
      </header>
      <main className="relative mx-auto flex max-w-md flex-col px-6 pb-16 pt-10">
        <div className="rounded-3xl border border-border bg-card/80 p-8 shadow-elevated backdrop-blur md:p-10">
          <h1 className="font-serif text-3xl font-medium leading-tight">
            {title}
          </h1>
          {subtitle && <p className="mt-2 text-muted-foreground">{subtitle}</p>}
          <div className="mt-8">{children}</div>
        </div>
      </main>
    </div>
  );
}
