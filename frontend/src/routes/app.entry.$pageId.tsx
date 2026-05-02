import { createFileRoute, Link, useRouter } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { journalApi } from "@/lib/api";
import { Button } from "@/components/ui/inputs/button";
import { SentimentPill } from "@/components/sentiment-pill";
import { formatLongDate } from "@/lib/sentiment";
import { ArrowLeft, Loader2, Lock, Sparkles } from "lucide-react";
import type { JournalBlock } from "@/lib/types";
import { toast } from "sonner";
import { decode } from "he";

export const Route = createFileRoute("/app/entry/$pageId")({
  head: () => ({ meta: [{ title: "Entry - MindTrack" }] }),
  component: EntryView,
});

function EntryView() {
  const { pageId } = Route.useParams();
  const router = useRouter();
  const qc = useQueryClient();
  const page = useQuery({
    queryKey: ["journal", "page", pageId],
    queryFn: () => journalApi.get(pageId),
  });

  const analyze = useMutation({
    mutationFn: () => journalApi.analyze(pageId),
    onSuccess: () => {
      toast.success("Re-analysis complete.");
      qc.invalidateQueries({ queryKey: ["journal", "page", pageId] });
      qc.invalidateQueries({ queryKey: ["analytics"] });
    },
    onError: () => toast.error("Could not analyse right now."),
  });

  if (page.isLoading) {
    return (
      <div className="grid place-items-center py-24">
        <Loader2 className="h-5 w-5 animate-spin text-primary" />
      </div>
    );
  }
  if (!page.data) {
    return (
      <div className="py-24 text-center">
        <p className="text-muted-foreground">This entry could not be loaded.</p>
        <Button
          variant="ghost"
          className="mt-4"
          onClick={() => router.history.back()}
        >
          Back to journal
        </Button>
      </div>
    );
  }

  const p = page.data;
  const sortedBlocks = [...p.blocks].sort(
    (a, b) => a.orderIndex - b.orderIndex,
  );

  return (
    <article className="mx-auto max-w-3xl animate-bloom-in pb-16">
      <Button
        variant="ghost"
        size="sm"
        className="mb-6 -ml-2 text-muted-foreground"
        onClick={() => router.history.back()}
      >
        <ArrowLeft className="mr-1 h-4 w-4" /> All entries
      </Button>

      <header className="mb-10">
        <div className="flex flex-wrap items-center gap-2 text-xs uppercase tracking-[0.2em] text-muted-foreground">
          <span>{formatLongDate(p.entryDate)}</span>
          {p.isPrivate && (
            <>
              <span className="h-1 w-1 rounded-full bg-muted-foreground/40" />
              <span className="inline-flex items-center gap-1">
                <Lock className="h-3 w-3" /> Private
              </span>
            </>
          )}
        </div>
        <h1 className="mt-3 font-serif text-4xl font-medium leading-tight md:text-5xl">
          {p.title}
        </h1>
        {p.description && (
          <p className="mt-3 text-lg text-muted-foreground">{p.description}</p>
        )}

        <div className="mt-5 flex flex-wrap items-center gap-3">
          <SentimentPill
            label={p.sentiment?.sentimentLabel}
            emotion={p.sentiment?.dominantEmotion}
            score={p.sentiment?.sentimentScore}
            variant="bold"
          />
          {p.tags.map((t) => (
            <span
              key={t.id}
              className="inline-flex items-center gap-1 rounded-full border border-border bg-secondary/60 px-2.5 py-0.5 text-xs"
            >
              <span
                className="h-1.5 w-1.5 rounded-full"
                style={{ backgroundColor: t.color || "var(--bloom)" }}
              />
              {t.name}
            </span>
          ))}
          <Button
            size="sm"
            variant="ghost"
            className="ml-auto h-8 rounded-full"
            disabled={analyze.isPending}
            onClick={() => analyze.mutate()}
          >
            {analyze.isPending ? (
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
            ) : (
              <>
                <Sparkles className="mr-1 h-3.5 w-3.5" /> Re-analyse
              </>
            )}
          </Button>
        </div>
      </header>

      <div className="prose prose-stone max-w-none">
        {sortedBlocks.map((b) => (
          <BlockView key={b.id} block={b} />
        ))}
      </div>

      {p.sentiment?.emotionVector &&
        Object.keys(p.sentiment.emotionVector).length > 0 && (
          <section className="mt-12 rounded-3xl border border-border bg-gradient-dusk p-6 shadow-soft">
            <p className="text-xs uppercase tracking-[0.2em] text-bloom">
              Emotional palette
            </p>
            <h2 className="mt-1 font-serif text-2xl">What this entry held</h2>
            <div className="mt-5 space-y-2.5">
              {Object.entries(p.sentiment.emotionVector)
                .sort(([, a], [, b]) => Number(b) - Number(a))
                .slice(0, 6)
                .map(([emotion, value]) => {
                  const pct = Math.max(2, Math.min(100, Number(value) * 100));
                  return (
                    <div key={emotion}>
                      <div className="mb-1 flex justify-between text-xs">
                        <span className="capitalize text-foreground">
                          {emotion}
                        </span>
                        <span className="text-muted-foreground">
                          {pct.toFixed(0)}%
                        </span>
                      </div>
                      <div className="h-1.5 overflow-hidden rounded-full bg-secondary">
                        <div
                          className="h-full rounded-full bg-gradient-bloom"
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
            </div>
          </section>
        )}

      <p className="mt-12 text-center text-xs text-muted-foreground">
        Entries are immutable - to record a different perspective, write a new
        entry for the same date.
      </p>
    </article>
  );
}

function BlockView({ block }: { block: JournalBlock }) {
  const text = decode((block.content?.text as string | undefined) || "");
  const url = decode((block.content?.url as string | undefined) || "");
  const caption = decode((block.content?.caption as string | undefined) || "");
  switch (block.type) {
    case "HEADING":
      return (
        <h2 className="mt-8 font-serif text-2xl font-medium md:text-3xl">
          {text}
        </h2>
      );
    case "QUOTE":
      return (
        <blockquote className="my-6 border-l-4 border-bloom bg-secondary/40 px-5 py-4 font-serif text-lg italic text-foreground">
          {text}
        </blockquote>
      );
    case "IMAGE":
      return (
        <figure className="my-6">
          <img
            src={url}
            alt={caption}
            className="w-full rounded-2xl object-cover"
          />
          {caption && (
            <figcaption className="mt-2 text-center text-sm text-muted-foreground">
              {caption}
            </figcaption>
          )}
        </figure>
      );
    case "VIDEO":
      return (
        <figure className="my-6">
          <video controls src={url} className="w-full rounded-2xl" />
          {caption && (
            <figcaption className="mt-2 text-center text-sm text-muted-foreground">
              {caption}
            </figcaption>
          )}
        </figure>
      );
    case "AUDIO":
      return (
        <figure className="my-6">
          <audio controls src={url} className="w-full" />
          {caption && (
            <figcaption className="mt-2 text-sm text-muted-foreground">
              {caption}
            </figcaption>
          )}
        </figure>
      );
    default:
      return (
        <p className="my-4 text-base leading-relaxed text-foreground/90 whitespace-pre-wrap">
          {text}
        </p>
      );
  }
}
