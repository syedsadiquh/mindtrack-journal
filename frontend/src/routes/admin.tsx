import {
  createFileRoute,
  Outlet,
  Link,
  useRouter,
} from "@tanstack/react-router";
import { RouteGuard } from "@/components/route-guard";
import { Brand } from "@/components/brand";
import { useAuth } from "@/lib/use-auth";
import { Button } from "@/components/ui/inputs/button";
import { CreditCard, Users, LogOut, ArrowLeft } from "lucide-react";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/admin")({
  component: AdminLayout,
});

const adminNav = [
  { to: "/admin", label: "Plans", icon: CreditCard, exact: true },
  { to: "/admin/users", label: "Users", icon: Users, exact: false },
] as const;

function AdminLayout() {
  return (
    <RouteGuard
      requireAuth
      requireRole={["ADMIN", "SYS_ADMIN"]}
      redirectTo="/admin/login"
    >
      <AdminShell />
    </RouteGuard>
  );
}

function AdminShell() {
  const { user, logout, role } = useAuth();
  const router = useRouter();
  const path = router.state.location.pathname;

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-30 border-b border-border/50 bg-background/80 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-6 py-4">
          <div className="flex items-center gap-6">
            <Brand size="sm" />
            <span className="hidden rounded-full bg-bloom/15 px-2.5 py-0.5 text-[11px] font-medium uppercase tracking-wider text-bloom md:inline-flex">
              {role === "SYS_ADMIN" ? "System Admin" : "Admin"}
            </span>
          </div>
          <nav className="flex items-center gap-1">
            {adminNav.map((n) => {
              const active = n.exact ? path === n.to : path.startsWith(n.to);
              return (
                <Link
                  key={n.to}
                  to={n.to}
                  className={cn(
                    "flex items-center gap-2 rounded-full px-4 py-1.5 text-sm transition-colors",
                    active
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:text-foreground",
                  )}
                >
                  <n.icon className="h-4 w-4" /> {n.label}
                </Link>
              );
            })}
          </nav>
          <div className="flex items-center gap-2">
            <span className="hidden text-sm text-muted-foreground md:inline">
              {user?.firstName || user?.username}
            </span>
            <Button asChild variant="ghost" size="sm" className="rounded-full">
              <Link to="/app">
                <ArrowLeft className="mr-1 h-4 w-4" /> App
              </Link>
            </Button>
            <Button
              variant="ghost"
              size="sm"
              className="rounded-full"
              onClick={() => {
                logout();
                router.navigate({ to: "/" });
              }}
            >
              <LogOut className="mr-1 h-4 w-4" /> Sign out
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-6 py-10">
        <Outlet />
      </main>
    </div>
  );
}
