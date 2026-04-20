import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { Button } from "@/components/ui/inputs/button";
import { Input } from "@/components/ui/inputs/input";
import { Label } from "@/components/ui/inputs/label";
import { ApiError } from "@/lib/api-client";
import { authApi } from "@/lib/api";
import { useAuth } from "@/lib/use-auth";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { AuthShell } from "./login";

export const Route = createFileRoute("/register")({
  head: () => ({ meta: [{ title: "Create your journal — MindTrack" }] }),
  component: RegisterPage,
});

function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    password: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const set =
    <K extends keyof typeof form>(k: K) =>
    (v: string) =>
      setForm((p) => ({ ...p, [k]: v }));

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedUsername = form.username.trim();
    const username = form.username.trim().toLowerCase();
    if (!/^[a-z0-9._-]{3,30}$/.test(trimmedUsername)) {
      toast.error(
        "Username must be 3–30 chars: lowercase letters, numbers, dot, underscore or hyphen.",
      );
      return;
    }
    if (form.password.length < 8) {
      toast.error("Password must be at least 8 characters.");
      return;
    }
    setSubmitting(true);
    try {
      await authApi.register({
        username,
        email: form.email,
        password: form.password,
        firstName: form.firstName,
        lastName: form.lastName || undefined,
      });
      // Auto-login
      await login(username, form.password, "USER");
      toast.success("Your journal is ready.");
      navigate({ to: "/app" });
    } catch (err) {
      const msg =
        err instanceof ApiError ? err.message : "Could not create account";
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Begin your journal"
      subtitle="A few details and you're in."
    >
      <form onSubmit={onSubmit} className="space-y-5">
        <div className="grid grid-cols-2 gap-3">
          <FieldInline
            label="First name"
            id="firstName"
            value={form.firstName}
            onChange={set("firstName")}
            required
          />
          <FieldInline
            label="Last name"
            id="lastName"
            value={form.lastName}
            onChange={set("lastName")}
          />
        </div>
        <FieldInline
          label="Username"
          id="username"
          value={form.username}
          onChange={set("username")}
          required
        />
        <FieldInline
          label="Email"
          id="email"
          type="email"
          value={form.email}
          onChange={set("email")}
          required
        />
        <FieldInline
          label="Password"
          id="password"
          type="password"
          value={form.password}
          onChange={set("password")}
          required
        />
        <Button
          type="submit"
          disabled={submitting}
          className="w-full rounded-full shadow-bloom"
        >
          {submitting ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            "Create my journal"
          )}
        </Button>
      </form>
      <p className="mt-7 text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link
          to="/login"
          className="text-primary underline-offset-4 hover:underline"
        >
          Sign in
        </Link>
      </p>
    </AuthShell>
  );
}

interface InlineProps {
  label: string;
  id: string;
  value: string;
  onChange: (v: string) => void;
  type?: string;
  required?: boolean;
}

function FieldInline({
  label,
  id,
  value,
  onChange,
  type = "text",
  required,
}: InlineProps) {
  return (
    <div className="space-y-1.5">
      <Label
        htmlFor={id}
        className="text-xs uppercase tracking-wider text-muted-foreground"
      >
        {label}
        {required && <span className="ml-0.5 text-destructive">*</span>}
      </Label>
      <Input
        id={id}
        type={type}
        value={value}
        required={required}
        onChange={(e) => onChange(e.target.value)}
        className="h-11 rounded-xl border-border bg-card/60"
      />
    </div>
  );
}
