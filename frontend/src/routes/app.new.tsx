import {
  createFileRoute,
  Link,
  useNavigate,
  useRouter,
} from "@tanstack/react-router";
import { useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { journalApi, analyticsApi } from "@/lib/api";
import { useAuth } from "@/lib/use-auth";
import { Button } from "@/components/ui/inputs/button";
import { Input } from "@/components/ui/inputs/input";
import { Textarea } from "@/components/ui/inputs/textarea";
import { Label } from "@/components/ui/inputs/label";
import { Switch } from "@/components/ui/inputs/switch";
import { DatePicker } from "@/components/ui/inputs/date-picker";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/inputs/select";
import { ApiError } from "@/lib/api-client";
import { cn } from "@/lib/utils";
import { toast } from "sonner";
import { Loader2, ArrowLeft, Plus, Trash2, GripVertical } from "lucide-react";
import { todayISO } from "@/lib/sentiment";
import type { BlockType, CreateJournalPageRequest } from "@/lib/types";

export const Route = createFileRoute("/app/new")({
  head: () => ({ meta: [{ title: "New entry - MindTrack" }] }),
  component: NewEntryPage,
});

interface DraftBlock {
  id: string;
  type: BlockType;
  text: string;
  url?: string;
  caption?: string;
}

const blockTypeOptions: { value: BlockType; label: string }[] = [
  { value: "TEXT", label: "Paragraph" },
  { value: "HEADING", label: "Heading" },
  { value: "QUOTE", label: "Quote" },
  { value: "IMAGE", label: "Image" },
  { value: "VIDEO", label: "Video" },
  { value: "AUDIO", label: "Audio" },
];

function newId() {
  return Math.random().toString(36).slice(2, 10);
}

function NewEntryPage() {
  const navigate = useNavigate();
  const router = useRouter();
  const qc = useQueryClient();
  const { user } = useAuth();
  const tenantId = user?.defaultTenant?.tenantId;

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [entryDate, setEntryDate] = useState(todayISO());
  const [isPrivate, setIsPrivate] = useState(true);
  const [blocks, setBlocks] = useState<DraftBlock[]>([
    { id: newId(), type: "TEXT", text: "" },
  ]);

  const addBlock = (type: BlockType) =>
    setBlocks((b) => [...b, { id: newId(), type, text: "" }]);
  const removeBlock = (id: string) =>
    setBlocks((b) => b.filter((x) => x.id !== id));
  const updateBlock = (id: string, patch: Partial<DraftBlock>) =>
    setBlocks((b) => b.map((x) => (x.id === id ? { ...x, ...patch } : x)));

  const reorderBlock = (fromId: string, toId: string) => {
    if (fromId === toId) return;
    setBlocks((b) => {
      const from = b.findIndex((x) => x.id === fromId);
      const to = b.findIndex((x) => x.id === toId);
      if (from < 0 || to < 0) return b;
      const copy = [...b];
      const [moved] = copy.splice(from, 1);
      copy.splice(to, 0, moved);
      return copy;
    });
  };

  const dragId = useRef<string | null>(null);
  const [dragOverId, setDragOverId] = useState<string | null>(null);

  const create = useMutation({
    mutationFn: async () => {
      if (!tenantId) throw new Error("No tenant available");
      const payload: CreateJournalPageRequest = {
        tenantId,
        title: title.trim(),
        description: description.trim() || undefined,
        entryDate,
        isPrivate,
        blocks: blocks
          .filter((b) => {
            if (b.type === "IMAGE" || b.type === "VIDEO" || b.type === "AUDIO")
              return !!b.url;
            return !!b.text.trim();
          })
          .map((b, i) => ({
            type: b.type,
            orderIndex: i,
            content:
              b.type === "IMAGE" || b.type === "VIDEO" || b.type === "AUDIO"
                ? { url: b.url || "", caption: b.caption || "" }
                : { text: b.text },
          })),
      };
      return journalApi.create(payload);
    },
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ["journal"] });
      analyticsApi
        .refresh()
        .catch(() => {})
        .finally(() => {
          qc.invalidateQueries({ queryKey: ["analytics"] });
        });
      toast.success("Entry saved. Sentiment is being analysed quietly.");
      navigate({ to: "/app/entry/$pageId", params: { pageId: data.id } });
    },
    onError: (err) => {
      const msg =
        err instanceof ApiError ? err.message : err.message || "Could not save";
      toast.error(msg);
    },
  });

  const canSubmit =
    title.trim().length > 0 && blocks.some((b) => b.text.trim() || b.url);

  return (
    <div className="mx-auto max-w-3xl space-y-8 animate-bloom-in">
      <div>
        <Button
          variant="ghost"
          size="sm"
          className="mb-3 -ml-2 text-muted-foreground"
          onClick={() => router.history.back()}
        >
          <ArrowLeft className="mr-1 h-4 w-4" /> Back to journal
        </Button>
        <p className="text-xs uppercase tracking-[0.2em] text-bloom">
          A new entry
        </p>
        <h1 className="mt-1 font-serif text-4xl font-medium md:text-5xl">
          Write what you feel.
        </h1>
      </div>

      <div className="rounded-3xl border border-border bg-card p-6 shadow-soft md:p-8">
        <div className="space-y-5">
          <div>
            <Input
              placeholder="A title for this entry…"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="h-auto border-0 bg-transparent p-0 font-serif text-3xl font-medium shadow-none placeholder:text-muted-foreground/40 focus-visible:ring-0"
            />
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div className="space-y-1.5">
              <Label
                htmlFor="entryDate"
                className="text-xs uppercase tracking-wider text-muted-foreground"
              >
                Entry date
              </Label>
              <DatePicker
                id="entryDate"
                value={entryDate}
                max={todayISO()}
                onChange={setEntryDate}
              />
            </div>
            <div className="space-y-1.5 sm:col-span-2">
              <Label
                htmlFor="description"
                className="text-xs uppercase tracking-wider text-muted-foreground"
              >
                A short note (optional)
              </Label>
              <Input
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="A line that captures the moment"
                className="h-10 rounded-xl bg-background"
              />
            </div>
          </div>

          <div className="flex items-center justify-between rounded-xl border border-border bg-secondary/40 px-4 py-3">
            <div>
              <p className="text-sm font-medium">Private</p>
              <p className="text-xs text-muted-foreground">
                Only you can read this entry
              </p>
            </div>
            <Switch checked={isPrivate} onCheckedChange={setIsPrivate} />
          </div>
        </div>

        {/* Blocks */}
        <div className="mt-8 space-y-3">
          {blocks.map((b, i) => (
            <BlockEditor
              key={b.id}
              block={b}
              index={i}
              onChange={(patch) => updateBlock(b.id, patch)}
              onRemove={() => removeBlock(b.id)}
              canRemove={blocks.length > 1}
              isDragOver={dragOverId === b.id}
              onDragStart={() => {
                dragId.current = b.id;
              }}
              onDragOver={(e) => {
                e.preventDefault();
                if (dragId.current && dragId.current !== b.id) {
                  setDragOverId(b.id);
                }
              }}
              onDragLeave={() => {
                if (dragOverId === b.id) setDragOverId(null);
              }}
              onDrop={(e) => {
                e.preventDefault();
                if (dragId.current) reorderBlock(dragId.current, b.id);
                dragId.current = null;
                setDragOverId(null);
              }}
              onDragEnd={() => {
                dragId.current = null;
                setDragOverId(null);
              }}
            />
          ))}
        </div>

        <div className="mt-5 flex flex-wrap items-center gap-2 border-t border-border pt-5">
          <span className="mr-2 text-xs uppercase tracking-wider text-muted-foreground">
            Add
          </span>
          {blockTypeOptions.map((opt) => (
            <Button
              key={opt.value}
              type="button"
              variant="outline"
              size="sm"
              className="h-8 rounded-full text-xs"
              onClick={() => addBlock(opt.value)}
            >
              <Plus className="mr-1 h-3 w-3" /> {opt.label}
            </Button>
          ))}
        </div>
      </div>

      <div className="flex items-center justify-end gap-3 pb-10">
        <Button asChild variant="ghost">
          <Link to="/app">Discard</Link>
        </Button>
        <Button
          disabled={!canSubmit || create.isPending}
          onClick={() => create.mutate()}
          className="rounded-full px-6 shadow-bloom"
        >
          {create.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            "Save entry"
          )}
        </Button>
      </div>
    </div>
  );
}

interface BlockEditorProps {
  block: DraftBlock;
  index: number;
  onChange: (p: Partial<DraftBlock>) => void;
  onRemove: () => void;
  canRemove: boolean;
  isDragOver: boolean;
  onDragStart: () => void;
  onDragOver: (e: React.DragEvent) => void;
  onDragLeave: () => void;
  onDrop: (e: React.DragEvent) => void;
  onDragEnd: () => void;
}

function BlockEditor({
  block,
  onChange,
  onRemove,
  canRemove,
  isDragOver,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
  onDragEnd,
}: BlockEditorProps) {
  const [isDragging, setIsDragging] = useState(false);
  const isMedia =
    block.type === "IMAGE" || block.type === "VIDEO" || block.type === "AUDIO";
  return (
    <div
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}
      className={cn(
        "group relative rounded-2xl border bg-background pl-8 pr-4 py-4 transition-all",
        isDragOver
          ? "border-primary bg-primary/5"
          : "border-border/70 hover:border-border",
        isDragging && "opacity-50",
      )}
    >
      <button
        type="button"
        draggable
        onDragStart={(e) => {
          setIsDragging(true);
          e.dataTransfer.effectAllowed = "move";
          onDragStart();
        }}
        onDragEnd={() => {
          setIsDragging(false);
          onDragEnd();
        }}
        className="absolute left-1.5 top-1/2 -translate-y-1/2 cursor-grab rounded p-1 text-muted-foreground/40 opacity-0 transition-opacity hover:bg-muted hover:text-foreground group-hover:opacity-100 active:cursor-grabbing"
        aria-label="Drag to reorder"
      >
        <GripVertical className="h-4 w-4" />
      </button>

      <div className="mb-2 flex items-center justify-between">
        <Select
          value={block.type}
          onValueChange={(v) => onChange({ type: v as BlockType })}
        >
          <SelectTrigger className="h-7 w-auto gap-1 border-0 bg-transparent px-1 text-xs uppercase tracking-wider text-muted-foreground shadow-none focus:ring-0">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {blockTypeOptions.map((o) => (
              <SelectItem key={o.value} value={o.value}>
                {o.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {canRemove && (
          <button
            type="button"
            onClick={onRemove}
            className="rounded p-1 text-muted-foreground/60 hover:bg-destructive/10 hover:text-destructive"
          >
            <Trash2 className="h-3.5 w-3.5" />
          </button>
        )}
      </div>

      {block.type === "HEADING" && (
        <Input
          value={block.text}
          onChange={(e) => onChange({ text: e.target.value })}
          placeholder="A heading…"
          className="mt-1 h-auto border-0 bg-transparent p-0 py-1 font-serif text-2xl font-medium leading-tight shadow-none placeholder:text-muted-foreground/40 focus-visible:ring-0"
        />
      )}

      {block.type === "QUOTE" && (
        <Textarea
          value={block.text}
          onChange={(e) => onChange({ text: e.target.value })}
          placeholder="Words worth keeping…"
          rows={2}
          className="resize-none border-0 border-l-4 border-l-bloom bg-transparent pl-4 font-serif text-lg italic shadow-none placeholder:text-muted-foreground/40 focus-visible:ring-0"
        />
      )}

      {block.type === "TEXT" && (
        <Textarea
          value={block.text}
          onChange={(e) => onChange({ text: e.target.value })}
          placeholder="Begin here…"
          rows={3}
          className="min-h-[80px] resize-none border-0 bg-transparent p-0 leading-relaxed shadow-none placeholder:text-muted-foreground/40 focus-visible:ring-0"
        />
      )}

      {isMedia && (
        <div className="space-y-2">
          <Input
            value={block.url || ""}
            onChange={(e) => onChange({ url: e.target.value })}
            placeholder={`${block.type.toLowerCase()} URL`}
            className="h-9 rounded-lg bg-card text-sm"
          />
          <Input
            value={block.caption || ""}
            onChange={(e) => onChange({ caption: e.target.value })}
            placeholder="Caption (optional)"
            className="h-9 rounded-lg bg-card text-sm"
          />
          {block.type === "IMAGE" && block.url && (
            <img
              src={block.url}
              alt={block.caption || ""}
              className="mt-2 max-h-64 rounded-xl object-cover"
            />
          )}
        </div>
      )}
    </div>
  );
}
