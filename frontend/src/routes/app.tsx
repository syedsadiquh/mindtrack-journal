import { createFileRoute, Outlet } from "@tanstack/react-router";
import { RouteGuard } from "@/components/route-guard";
import { AppShell } from "@/components/app-shell";

export const Route = createFileRoute("/app")({
  component: AppLayout,
});

function AppLayout() {
  return (
    <RouteGuard requireAuth>
      <AppShell>
        <Outlet />
      </AppShell>
    </RouteGuard>
  );
}
