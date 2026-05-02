import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { journalApi, analyticsApi } from "@/lib/api";
import { useAuth } from "@/lib/use-auth";
import { Button } from "@/components/ui/inputs/button";
import { SentimentPill } from "@/components/sentiment-pill";
import { formatRelativeDate, formatLongDate } from "@/lib/sentiment";
import {
  Plus,
  Flame,
  Sparkles,
  BookOpen,
  Loader2,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";

type JournalSearch = {
  page?: number;
};

export const Route = createFileRoute("/app/")({
  head: () => ({ meta: [{ title: "Journal - MindTrack" }] }),
  validateSearch: (search: Record<string, unknown>): JournalSearch => {
    return {
      page: search.page ? Number(search.page) : undefined,
    };
  },
  component: JournalListPage,
});

function JournalListPage() {
  const { user } = useAuth();

  const { page = 1 } = Route.useSearch();
  const navigate = useNavigate({ from: Route.id });
  const pageSize = 10;

  const pages = useQuery({
    queryKey: ["journal", "list", page],
    queryFn: () =>
      journalApi.list({
        page: page - 1,
        size: pageSize,
        sort: "entryDate,desc",
      }),
  });

  const streak = useQuery({
    queryKey: ["analytics", "streak"],
    queryFn: async () => {
      await analyticsApi.refreshStreak().catch(() => {});
      return analyticsApi.streak();
    },
    refetchOnMount: "always",
    staleTime: 0,
  });

  const summary = useQuery({
    queryKey: ["analytics", "summary"],
    queryFn: () => analyticsApi.summary(),
    refetchOnMount: "always",
    staleTime: 0,
  });

  const greeting = (() => {
    const h = new Date().getHours();
    if (h < 5) return "Late evening";
    if (h < 12) return "Good morning";
    if (h < 18) return "Good afternoon";
    return "Good evening";
  })();

  const totalPages = pages.data?.page?.totalPages ?? 1;
  const isFirstPage = page <= 1;
  const isLastPage = page >= totalPages;

  const handlePageChange = (newPage: number) => {
    navigate({
      search: { page: newPage === 1 ? undefined : newPage },
    });

    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <div className="space-y-10">
      {/* Hero / greeting */}
      <section className="animate-bloom-in">
        <p className="text-xs uppercase tracking-[0.2em] text-bloom">
          {formatLongDate(new Date().toISOString())}
        </p>
        <h1 className="mt-2 font-serif text-4xl font-medium leading-tight md:text-5xl">
          {greeting},{" "}
          <span className="text-bloom">
            {user?.firstName || user?.username || "friend"}
          </span>
        </h1>
        <p className="mt-2 max-w-xl text-muted-foreground">
          What is your inner weather today? A few minutes of writing, kept
          private and yours.
        </p>
        <div className="mt-6 flex flex-wrap gap-2">
          <Button asChild className="rounded-full shadow-bloom">
            <Link to="/app/new">
              <Plus className="mr-1 h-4 w-4" /> Today's entry
            </Link>
          </Button>
        </div>
      </section>

      {/* Stat band */}
      <section className="grid gap-3 sm:grid-cols-3">
        <StatCard
          icon={Flame}
          label="Current streak"
          value={streak.data?.currentStreak ?? "-"}
          suffix={streak.data?.currentStreak ? "days" : ""}
          loading={streak.isLoading}
        />
        <StatCard
          icon={BookOpen}
          label="Total entries"
          value={pages.data?.page?.totalElements ?? "-"}
          loading={pages.isLoading}
        />
        <StatCard
          icon={Sparkles}
          label="Most felt"
          value={summary.data?.mostFrequentEmotion || "-"}
          loading={summary.isLoading}
          capitalize
        />
      </section>

      {/* Entries */}
      <section>
        <div className="mb-4">
          <h2 className="font-serif text-2xl font-medium">Your entries</h2>
        </div>
        {pages.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-5 w-5 animate-spin text-primary" />
          </div>
        ) : pages.data && pages.data.content.length > 0 ? (
          <div className="space-y-6">
            <div className="space-y-3">
              {pages.data.content.map((p) => (
                <Link
                  key={p.id}
                  to="/app/entry/$pageId"
                  params={{ pageId: p.id }}
                  className="group block rounded-2xl border border-border bg-card p-5 shadow-soft transition-all hover:-translate-y-0.5 hover:border-rose/60 hover:shadow-bloom"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0 flex-1">
                      <div className="mb-1 flex items-center gap-2 text-xs uppercase tracking-wider text-muted-foreground">
                        <span>{formatRelativeDate(p.entryDate)}</span>
                        <span className="h-1 w-1 rounded-full bg-muted-foreground/40" />
                        <span>
                          {p.blockCount}{" "}
                          {p.blockCount === 1 ? "block" : "blocks"}
                        </span>
                      </div>
                      <h3 className="font-serif text-xl font-medium leading-snug text-foreground transition-colors group-hover:text-bloom">
                        {p.title}
                      </h3>
                      {p.description && (
                        <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                          {p.description}
                        </p>
                      )}
                      {p.tags.length > 0 && (
                        <div className="mt-3 flex flex-wrap gap-1.5">
                          {p.tags.map((t) => (
                            <span
                              key={t.id}
                              className="inline-flex items-center gap-1 rounded-full border border-border bg-secondary/60 px-2 py-0.5 text-[11px] text-secondary-foreground"
                            >
                              <span
                                className="h-1.5 w-1.5 rounded-full"
                                style={{
                                  backgroundColor: t.color || "var(--bloom)",
                                }}
                              />
                              {t.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                    <SentimentPill
                      label={p.sentimentLabel}
                      emotion={p.dominantEmotion}
                      score={p.sentimentScore}
                    />
                  </div>
                </Link>
              ))}
            </div>

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between pt-4 border-t border-border/50">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handlePageChange(page - 1)}
                  disabled={isFirstPage}
                  className="rounded-full shadow-sm"
                >
                  <ChevronLeft className="mr-1 h-4 w-4" /> Previous
                </Button>

                <span className="text-sm text-muted-foreground">
                  Page{" "}
                  <span className="font-medium text-foreground">{page}</span> of{" "}
                  {totalPages}
                </span>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handlePageChange(page + 1)}
                  disabled={isLastPage}
                  className="rounded-full shadow-sm"
                >
                  Next <ChevronRight className="ml-1 h-4 w-4" />
                </Button>
              </div>
            )}
          </div>
        ) : (
          <EmptyState />
        )}
      </section>
    </div>
  );
}

interface StatCardProps {
  icon: React.ComponentType<{ className?: string; strokeWidth?: number }>;
  label: string;
  value: string | number;
  suffix?: string;
  loading?: boolean;
  capitalize?: boolean;
}

function StatCard({
  icon: Icon,
  label,
  value,
  suffix,
  loading,
  capitalize,
}: StatCardProps) {
  return (
    <div className="rounded-2xl border border-border bg-card p-5 shadow-soft">
      <div className="flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-xl bg-secondary text-bloom">
          <Icon className="h-4 w-4" strokeWidth={2} />
        </div>
        <div>
          <p className="text-xs uppercase tracking-wider text-muted-foreground">
            {label}
          </p>
          <p
            className={`font-serif text-2xl font-medium leading-none ${capitalize ? "capitalize" : ""}`}
          >
            {loading ? "…" : value}
            {suffix && (
              <span className="ml-1 text-sm text-muted-foreground">
                {suffix}
              </span>
            )}
          </p>
        </div>
      </div>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="rounded-3xl border border-dashed border-border bg-card/40 px-6 py-16 text-center">
      <div className="mx-auto mb-5 grid h-14 w-14 place-items-center rounded-full bg-gradient-bloom text-primary-foreground shadow-bloom">
        <BookOpen className="h-6 w-6" strokeWidth={1.8} />
      </div>
      <h3 className="font-serif text-2xl font-medium">
        Your first entry awaits.
      </h3>
      <p className="mx-auto mt-2 max-w-sm text-muted-foreground">
        Begin gently. A sentence, a thought, a feeling. The shape of your
        weather will emerge.
      </p>
      <Button asChild className="mt-6 rounded-full shadow-bloom">
        <Link to="/app/new">
          <Plus className="mr-1 h-4 w-4" /> Write today's entry
        </Link>
      </Button>
    </div>
  );
}
