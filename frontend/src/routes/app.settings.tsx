import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { userApi, authApi } from "@/lib/api";
import { useAuth } from "@/lib/use-auth";
import { Button } from "@/components/ui/inputs/button";
import { Input } from "@/components/ui/inputs/input";
import { Label } from "@/components/ui/inputs/label";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/overlays/alert-dialog";
import { ApiError } from "@/lib/api-client";
import { toast } from "sonner";
import { Loader2, Trash2 } from "lucide-react";
import { isValidEmail, isValidUsername } from "@/lib/utils";

export const Route = createFileRoute("/app/settings")({
  head: () => ({ meta: [{ title: "Settings - MindTrack" }] }),
  component: SettingsPage,
});

function SettingsPage() {
  const { user, refresh, logout } = useAuth();
  const qc = useQueryClient();
  const navigate = useNavigate();
  const me = useQuery({
    queryKey: ["user", "me"],
    queryFn: userApi.me,
    initialData: user ?? undefined,
  });
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    phone: "",
    timezone: "",
    avatarUrl: "",
  });

  useEffect(() => {
    if (me.data) {
      setForm({
        firstName: me.data.firstName ?? "",
        lastName: me.data.lastName ?? "",
        username: me.data.username ?? "",
        email: me.data.email ?? "",
        phone: me.data.phone ?? "",
        timezone: me.data.timezone ?? "",
        avatarUrl: me.data.avatarUrl ?? "",
      });
    }
  }, [me.data]);

  const update = useMutation({
    mutationFn: async () => {
      if (!isValidUsername(form.username)) {
        throw new ApiError(
          "Username must be 3-30 chars: lowercase letters, numbers, dot, underscore or hyphen.",
          400,
        );
      }
      if (!isValidEmail(form.email)) {
        throw new ApiError("Provided Email contains invalid characters", 400);
      }
      return await userApi.update(form);
    },
    onSuccess: () => {
      toast.success("Profile updated.");
      qc.invalidateQueries({ queryKey: ["user", "me"] });
      refresh();
    },
    onError: (e) =>
      toast.error(e instanceof ApiError ? e.message : "Update failed"),
  });

  const del = useMutation({
    mutationFn: () => authApi.deleteUser(me.data!.id),
    onSuccess: () => {
      toast.success("Your account has been erased.");
      logout();
      navigate({ to: "/" });
    },
    onError: (e) =>
      toast.error(
        e instanceof ApiError ? e.message : "Could not delete account",
      ),
  });

  const set =
    <K extends keyof typeof form>(k: K) =>
    (v: string) =>
      setForm((p) => ({ ...p, [k]: v }));

  return (
    <div className="mx-auto max-w-2xl space-y-10 animate-bloom-in">
      <header>
        <p className="text-xs uppercase tracking-[0.2em] text-bloom">
          Settings
        </p>
        <h1 className="mt-2 font-serif text-4xl font-medium">Your account</h1>
      </header>

      <section className="rounded-3xl border border-border bg-card p-6 shadow-soft md:p-8">
        <h2 className="font-serif text-xl font-medium">Profile</h2>
        <div className="mt-5 grid gap-4 sm:grid-cols-2">
          <Field
            label="First name"
            id="firstName"
            value={form.firstName}
            onChange={set("firstName")}
          />
          <Field
            label="Last name"
            id="lastName"
            value={form.lastName}
            onChange={set("lastName")}
          />
          <Field
            label="Username"
            id="username"
            value={form.username}
            onChange={set("username")}
          />
          <Field
            label="Email"
            id="email"
            type="email"
            value={form.email}
            onChange={set("email")}
          />
          <Field
            label="Phone"
            id="phone"
            value={form.phone}
            onChange={set("phone")}
          />
          <Field
            label="Timezone"
            id="timezone"
            value={form.timezone}
            onChange={set("timezone")}
          />
          <Field
            label="Avatar URL"
            id="avatarUrl"
            value={form.avatarUrl}
            onChange={set("avatarUrl")}
          />
        </div>
        <div className="mt-6 flex justify-end">
          <Button
            disabled={update.isPending}
            onClick={() => update.mutate()}
            className="rounded-full px-6 shadow-bloom"
          >
            {update.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              "Save changes"
            )}
          </Button>
        </div>
      </section>

      <section className="rounded-3xl border border-destructive/30 bg-destructive/5 p-6 shadow-soft md:p-8">
        <h2 className="font-serif text-xl font-medium text-destructive">
          The right to be forgotten
        </h2>
        <p className="mt-2 text-sm text-muted-foreground">
          Permanently erase your account and every entry you've written. This
          cannot be undone.
        </p>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button variant="destructive" className="mt-5 rounded-full">
              <Trash2 className="mr-2 h-4 w-4" /> Delete my account
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>Erase your journal?</AlertDialogTitle>
              <AlertDialogDescription>
                Every entry, every insight, every byte associated with your
                account will be removed. This action is permanent.
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Keep my account</AlertDialogCancel>
              <AlertDialogAction
                onClick={() => del.mutate()}
                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              >
                {del.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  "Yes, erase forever"
                )}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </section>
    </div>
  );
}

interface FieldProps {
  label: string;
  id: string;
  value: string;
  onChange: (v: string) => void;
  type?: string;
}
function Field({ label, id, value, onChange, type = "text" }: FieldProps) {
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
        className="h-10 rounded-xl bg-background"
      />
    </div>
  );
}
