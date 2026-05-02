import { Link, useRouter, useRouterState } from "@tanstack/react-router";
import { useAuth } from "@/lib/use-auth";
import { Brand } from "@/components/brand";
import { Button } from "@/components/ui/inputs/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/navigation/dropdown-menu";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/display/avatar";
import {
  BookOpen,
  BarChart3,
  Settings,
  LogOut,
  Plus,
  ShieldCheck,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface AppShellProps {
  children: React.ReactNode;
}

const nav = [
  { to: "/app", label: "Journal", icon: BookOpen },
  { to: "/app/analytics", label: "Insights", icon: BarChart3 },
  { to: "/app/settings", label: "Settings", icon: Settings },
] as const;

export function AppShell({ children }: AppShellProps) {
  const { user, logout, role } = useAuth();
  const router = useRouter();
  const path = useRouterState({ select: (s) => s.location.pathname });
  const initials = (user?.firstName || user?.username || "·")
    .split(" ")
    .map((s) => s[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <div className="min-h-screen bg-background">
      {/* Sidebar (desktop) */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-border/50 bg-sidebar/80 px-5 py-6 backdrop-blur-xl md:flex">
        <Brand />
        <Button
          asChild
          className="mt-7 w-full justify-start gap-2 rounded-full shadow-bloom"
        >
          <Link to="/app/new">
            <Plus className="h-4 w-4" /> New entry
          </Link>
        </Button>
        <nav className="mt-8 flex flex-col gap-1">
          {nav.map((n) => {
            const active =
              n.to === "/app" ? path === "/app" : path.startsWith(n.to);
            return (
              <Link
                key={n.to}
                to={n.to}
                className={cn(
                  "group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition-colors",
                  active
                    ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground"
                    : "text-sidebar-foreground/70 hover:bg-sidebar-accent/60 hover:text-sidebar-foreground",
                )}
              >
                <n.icon className="h-4 w-4" strokeWidth={1.8} />
                {n.label}
              </Link>
            );
          })}
          {(role === "ADMIN" || role === "SYS_ADMIN") && (
            <Link
              to="/admin"
              className="mt-2 flex items-center gap-3 rounded-xl border border-dashed border-bloom/30 px-3 py-2.5 text-sm text-bloom transition-colors hover:bg-bloom/5"
            >
              <ShieldCheck className="h-4 w-4" /> Admin console
            </Link>
          )}
        </nav>

        <div className="mt-auto">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className="flex w-full items-center gap-3 rounded-2xl border border-border bg-card/70 p-3 text-left transition-colors hover:bg-card">
                <Avatar className="h-9 w-9">
                  {user?.avatarUrl && (
                    <AvatarImage src={user.avatarUrl} alt={user.username} />
                  )}
                  <AvatarFallback className="bg-gradient-bloom text-xs text-primary-foreground">
                    {initials}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">
                    {user?.firstName || user?.username}
                  </p>
                  <p className="truncate text-xs text-muted-foreground">
                    {user?.email}
                  </p>
                </div>
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>Account</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem asChild>
                <Link to="/app/settings">
                  <Settings className="mr-2 h-4 w-4" /> Settings
                </Link>
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => {
                  logout();
                  router.navigate({ to: "/" });
                }}
              >
                <LogOut className="mr-2 h-4 w-4" /> Sign out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </aside>

      {/* Mobile top bar */}
      <header className="sticky top-0 z-20 flex items-center justify-between border-b border-border/50 bg-background/80 px-4 py-3 backdrop-blur-xl md:hidden">
        <Brand size="sm" />
        <div className="flex items-center gap-2">
          <Button asChild size="sm" className="rounded-full">
            <Link to="/app/new">
              <Plus className="h-4 w-4" />
            </Link>
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Avatar className="h-8 w-8 cursor-pointer">
                {user?.avatarUrl && (
                  <AvatarImage src={user.avatarUrl} alt={user.username} />
                )}
                <AvatarFallback className="bg-gradient-bloom text-xs text-primary-foreground">
                  {initials}
                </AvatarFallback>
              </Avatar>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              {nav.map((n) => (
                <DropdownMenuItem key={n.to} asChild>
                  <Link to={n.to}>
                    <n.icon className="mr-2 h-4 w-4" /> {n.label}
                  </Link>
                </DropdownMenuItem>
              ))}
              {(role === "ADMIN" || role === "SYS_ADMIN") && (
                <DropdownMenuItem asChild>
                  <Link to="/admin">
                    <ShieldCheck className="mr-2 h-4 w-4" /> Admin
                  </Link>
                </DropdownMenuItem>
              )}
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => {
                  logout();
                  router.navigate({ to: "/" });
                }}
              >
                <LogOut className="mr-2 h-4 w-4" /> Sign out
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>

      <main className="md:pl-64">
        <div className="mx-auto max-w-5xl px-4 py-8 md:px-10 md:py-12">
          {children}
        </div>
      </main>
    </div>
  );
}
