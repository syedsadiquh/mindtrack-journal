import { useEffect, useState } from "react";
import { useNavigate, useRouter } from "@tanstack/react-router";
import { useAuth } from "@/lib/use-auth";
import type { UserRole } from "@/lib/types";
import { Loader2 } from "lucide-react";

interface RouteGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireRole?: UserRole[];
  redirectTo?: string;
}

export function RouteGuard({
  children,
  requireAuth = true,
  requireRole,
  redirectTo,
}: RouteGuardProps) {
  const { isLoading, isAuthenticated, role } = useAuth();
  const navigate = useNavigate();
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (isLoading) return;
    if (requireAuth && !isAuthenticated) {
      const target = redirectTo ?? "/login";
      navigate({
        to: target,
        search: { redirect: router.state.location.href } as never,
      });
      return;
    }
    if (requireRole && !requireRole.includes(role)) {
      navigate({ to: requireRole.includes("ADMIN") ? "/admin/login" : "/" });
      return;
    }
    setReady(true);
  }, [
    isLoading,
    isAuthenticated,
    role,
    requireAuth,
    requireRole,
    navigate,
    router,
    redirectTo,
  ]);

  if (isLoading || !ready) {
    return (
      <div className="grid min-h-screen place-items-center">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    );
  }
  return <>{children}</>;
}
