import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { authApi } from "@/lib/api";
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
import { Loader2, UserMinus, Users } from "lucide-react";

export const Route = createFileRoute("/admin/users")({
  head: () => ({ meta: [{ title: "Users - Admin" }] }),
  component: UsersAdminPage,
});

function UsersAdminPage() {
  const qc = useQueryClient();
  const [userId, setUserId] = useState("");

  const del = useMutation({
    mutationFn: (id: string) => authApi.deleteUser(id),
    onSuccess: () => {
      toast.success("User removed.");
      setUserId("");
      qc.invalidateQueries({ queryKey: ["admin"] });
    },
    onError: (e) =>
      toast.error(e instanceof ApiError ? e.message : "Delete failed"),
  });

  return (
    <div className="mx-auto max-w-2xl space-y-8 animate-bloom-in">
      <header>
        <p className="text-xs uppercase tracking-[0.2em] text-bloom">
          User management
        </p>
        <h1 className="mt-2 font-serif text-4xl font-medium">Users</h1>
        <p className="mt-2 text-muted-foreground">
          The API exposes user removal by ID. List endpoints aren't available in
          v1, so manage by ID.
        </p>
      </header>

      <section className="rounded-3xl border border-border bg-card p-6 shadow-soft md:p-8">
        <div className="flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-2xl bg-secondary text-bloom">
            <Users className="h-5 w-5" />
          </div>
          <div>
            <h2 className="font-serif text-xl font-medium">Remove a user</h2>
            <p className="text-sm text-muted-foreground">
              Deleting a user erases all their journal data.
            </p>
          </div>
        </div>

        <div className="mt-6 space-y-3">
          <Label
            htmlFor="userId"
            className="text-xs uppercase tracking-wider text-muted-foreground"
          >
            User ID (UUID)
          </Label>
          <Input
            id="userId"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="00000000-0000-0000-0000-000000000000"
            className="h-11 rounded-xl"
          />

          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button
                variant="destructive"
                disabled={!userId.trim()}
                className="rounded-full"
              >
                <UserMinus className="mr-2 h-4 w-4" /> Delete user
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Delete this user?</AlertDialogTitle>
                <AlertDialogDescription>
                  This will permanently erase the user and their journal
                  entries. This cannot be undone.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Cancel</AlertDialogCancel>
                <AlertDialogAction
                  onClick={() => del.mutate(userId.trim())}
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                >
                  {del.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    "Yes, delete"
                  )}
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
      </section>
    </div>
  );
}
