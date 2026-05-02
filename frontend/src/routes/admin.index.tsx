import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { planApi } from "@/lib/api";
import { Button } from "@/components/ui/inputs/button";
import { Input } from "@/components/ui/inputs/input";
import { Label } from "@/components/ui/inputs/label";
import { Switch } from "@/components/ui/inputs/switch";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/overlays/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/inputs/select";
import { ApiError } from "@/lib/api-client";
import { toast } from "sonner";
import { Loader2, Plus, Power, PowerOff } from "lucide-react";
import type { Plan, PlanTier } from "@/lib/types";

export const Route = createFileRoute("/admin/")({
  head: () => ({ meta: [{ title: "Plans - Admin" }] }),
  component: PlansAdminPage,
});

const emptyPlan: Partial<Plan> = {
  tier: "FREE",
  displayName: "",
  description: "",
  maxPages: -1,
  maxBlocksPerPage: -1,
  maxTags: -1,
  maxMembers: 1,
  aiEnrichmentEnabled: false,
  sentimentAnalysisEnabled: true,
  sharingEnabled: false,
  exportEnabled: false,
  priceMonthly: 0,
  priceYearly: 0,
  currency: "USD",
};

function PlansAdminPage() {
  const qc = useQueryClient();
  const plans = useQuery({
    queryKey: ["admin", "plans"],
    queryFn: planApi.listAdmin,
  });
  const [editing, setEditing] = useState<Partial<Plan> | null>(null);
  const [open, setOpen] = useState(false);

  const save = useMutation({
    mutationFn: async () => {
      if (!editing) throw new Error("No plan");
      if (editing.id) return planApi.update(editing.id, editing);
      return planApi.create(editing);
    },
    onSuccess: () => {
      toast.success(editing?.id ? "Plan updated." : "Plan created.");
      qc.invalidateQueries({ queryKey: ["admin", "plans"] });
      setOpen(false);
      setEditing(null);
    },
    onError: (e) =>
      toast.error(e instanceof ApiError ? e.message : "Save failed"),
  });

  const toggle = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      active ? planApi.deactivate(id) : planApi.reactivate(id),
    onSuccess: () => {
      toast.success("Plan updated.");
      qc.invalidateQueries({ queryKey: ["admin", "plans"] });
    },
    onError: (e) =>
      toast.error(e instanceof ApiError ? e.message : "Toggle failed"),
  });

  const openNew = () => {
    setEditing({ ...emptyPlan });
    setOpen(true);
  };
  const openEdit = (p: Plan) => {
    setEditing({ ...p });
    setOpen(true);
  };
  const set = <K extends keyof Plan>(k: K, v: Plan[K]) =>
    setEditing((prev) => (prev ? { ...prev, [k]: v } : prev));

  return (
    <div className="space-y-8 animate-bloom-in">
      <header className="flex items-end justify-between">
        <div>
          <p className="text-xs uppercase tracking-[0.2em] text-bloom">
            Subscription plans
          </p>
          <h1 className="mt-2 font-serif text-4xl font-medium">
            Manage offerings
          </h1>
        </div>
        <Button onClick={openNew} className="rounded-full shadow-bloom">
          <Plus className="mr-1 h-4 w-4" /> New plan
        </Button>
      </header>

      {plans.isLoading ? (
        <div className="grid place-items-center py-24">
          <Loader2 className="h-5 w-5 animate-spin text-primary" />
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {(plans.data ?? []).map((p) => (
            <PlanCard
              key={p.id}
              plan={p}
              onEdit={() => openEdit(p)}
              onToggle={() => toggle.mutate({ id: p.id, active: p.active })}
            />
          ))}
          {(plans.data?.length ?? 0) === 0 && (
            <div className="col-span-full rounded-2xl border border-dashed border-border p-10 text-center text-muted-foreground">
              No plans yet - create your first one.
            </div>
          )}
        </div>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle className="font-serif text-2xl">
              {editing?.id ? "Edit plan" : "New plan"}
            </DialogTitle>
          </DialogHeader>
          {editing && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label className="text-xs uppercase tracking-wider text-muted-foreground">
                  Tier
                </Label>
                <Select
                  value={editing.tier}
                  onValueChange={(v) => set("tier", v as PlanTier)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="FREE">Free</SelectItem>
                    <SelectItem value="PRO">Pro</SelectItem>
                    <SelectItem value="ENTERPRISE">Enterprise</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <NumField
                label="Currency"
                value={editing.currency ?? "USD"}
                onChange={(v) => set("currency", v)}
                text
              />
              <TextField
                label="Display name"
                value={editing.displayName ?? ""}
                onChange={(v) => set("displayName", v)}
                className="sm:col-span-2"
              />
              <TextField
                label="Description"
                value={editing.description ?? ""}
                onChange={(v) => set("description", v)}
                className="sm:col-span-2"
              />
              <NumField
                label="Price / month"
                value={String(editing.priceMonthly ?? 0)}
                onChange={(v) => set("priceMonthly", Number(v))}
              />
              <NumField
                label="Price / year"
                value={String(editing.priceYearly ?? 0)}
                onChange={(v) => set("priceYearly", Number(v))}
              />
              <NumField
                label="Max pages (-1 = ∞)"
                value={String(editing.maxPages ?? -1)}
                onChange={(v) => set("maxPages", Number(v))}
              />
              <NumField
                label="Max blocks/page"
                value={String(editing.maxBlocksPerPage ?? -1)}
                onChange={(v) => set("maxBlocksPerPage", Number(v))}
              />
              <NumField
                label="Max tags"
                value={String(editing.maxTags ?? -1)}
                onChange={(v) => set("maxTags", Number(v))}
              />
              <NumField
                label="Max members"
                value={String(editing.maxMembers ?? 1)}
                onChange={(v) => set("maxMembers", Number(v))}
              />
              <SwitchRow
                label="Sentiment analysis"
                checked={!!editing.sentimentAnalysisEnabled}
                onChange={(v) => set("sentimentAnalysisEnabled", v)}
              />
              <SwitchRow
                label="AI enrichment"
                checked={!!editing.aiEnrichmentEnabled}
                onChange={(v) => set("aiEnrichmentEnabled", v)}
              />
              <SwitchRow
                label="Sharing"
                checked={!!editing.sharingEnabled}
                onChange={(v) => set("sharingEnabled", v)}
              />
              <SwitchRow
                label="Export"
                checked={!!editing.exportEnabled}
                onChange={(v) => set("exportEnabled", v)}
              />
            </div>
          )}
          <DialogFooter>
            <Button variant="ghost" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={() => save.mutate()}
              disabled={save.isPending}
              className="rounded-full shadow-bloom"
            >
              {save.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : editing?.id ? (
                "Save changes"
              ) : (
                "Create plan"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function PlanCard({
  plan,
  onEdit,
  onToggle,
}: {
  plan: Plan;
  onEdit: () => void;
  onToggle: () => void;
}) {
  const tierBadge: Record<PlanTier, string> = {
    FREE: "bg-secondary text-secondary-foreground",
    PRO: "bg-bloom/15 text-bloom",
    ENTERPRISE: "bg-primary text-primary-foreground",
  };
  return (
    <div
      className={`rounded-2xl border p-6 shadow-soft transition-all ${plan.active ? "border-border bg-card" : "border-border/50 bg-card/40 opacity-70"}`}
    >
      <div className="flex items-start justify-between">
        <div>
          <span
            className={`inline-flex rounded-full px-2.5 py-0.5 text-[11px] font-medium uppercase tracking-wider ${tierBadge[plan.tier]}`}
          >
            {plan.tier}
          </span>
          <h3 className="mt-3 font-serif text-2xl font-medium">
            {plan.displayName}
          </h3>
          {plan.description && (
            <p className="mt-1 text-sm text-muted-foreground">
              {plan.description}
            </p>
          )}
        </div>
      </div>
      <p className="mt-4 font-serif text-3xl font-medium">
        {plan.priceMonthly === 0
          ? "Free"
          : `${plan.currency} ${plan.priceMonthly}`}
        {plan.priceMonthly > 0 && (
          <span className="text-sm font-normal text-muted-foreground">/mo</span>
        )}
      </p>
      <ul className="mt-4 space-y-1 text-sm text-muted-foreground">
        <li>• {plan.maxPages === -1 ? "Unlimited" : plan.maxPages} pages</li>
        <li>• {plan.maxTags === -1 ? "Unlimited" : plan.maxTags} tags</li>
        {plan.sentimentAnalysisEnabled && <li>• Sentiment analysis</li>}
        {plan.aiEnrichmentEnabled && <li>• AI enrichment</li>}
        {plan.sharingEnabled && <li>• Sharing</li>}
        {plan.exportEnabled && <li>• Export</li>}
      </ul>
      <div className="mt-5 flex gap-2">
        <Button variant="outline" size="sm" className="flex-1" onClick={onEdit}>
          Edit
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={onToggle}
          className={plan.active ? "text-destructive" : "text-bloom"}
        >
          {plan.active ? (
            <PowerOff className="h-4 w-4" />
          ) : (
            <Power className="h-4 w-4" />
          )}
        </Button>
      </div>
    </div>
  );
}

function TextField({
  label,
  value,
  onChange,
  className,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  className?: string;
}) {
  return (
    <div className={`space-y-1.5 ${className || ""}`}>
      <Label className="text-xs uppercase tracking-wider text-muted-foreground">
        {label}
      </Label>
      <Input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="h-10 rounded-xl"
      />
    </div>
  );
}
function NumField({
  label,
  value,
  onChange,
  text,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  text?: boolean;
}) {
  return (
    <div className="space-y-1.5">
      <Label className="text-xs uppercase tracking-wider text-muted-foreground">
        {label}
      </Label>
      <Input
        type={text ? "text" : "number"}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="h-10 rounded-xl"
      />
    </div>
  );
}
function SwitchRow({
  label,
  checked,
  onChange,
}: {
  label: string;
  checked: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <div className="flex items-center justify-between rounded-xl border border-border bg-secondary/40 px-3 py-2.5">
      <span className="text-sm">{label}</span>
      <Switch checked={checked} onCheckedChange={onChange} />
    </div>
  );
}
