import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { analyticsApi } from "@/lib/api";
import { Button } from "@/components/ui/inputs/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/navigation/tabs";
import { SentimentPill } from "@/components/sentiment-pill";
import {
  Area,
  AreaChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  Loader2,
  RefreshCw,
  Sparkles,
  Flame,
  BookOpen,
  TrendingUp,
} from "lucide-react";
import { toast } from "sonner";
import { sentimentColor } from "@/lib/sentiment";

export const Route = createFileRoute("/app/analytics")({
  head: () => ({ meta: [{ title: "Insights - MindTrack" }] }),
  component: AnalyticsPage,
});

function AnalyticsPage() {
  const qc = useQueryClient();
  const [period, setPeriod] = useState<"DAILY" | "WEEKLY" | "MONTHLY">("DAILY");
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);

  const summary = useQuery({
    queryKey: ["analytics", "summary"],
    queryFn: analyticsApi.summary,
  });
  const trends = useQuery({
    queryKey: ["analytics", "trends", period],
    queryFn: () => analyticsApi.trends({ period }),
  });
  const calendar = useQuery({
    queryKey: ["analytics", "calendar", year, month],
    queryFn: () => analyticsApi.moodCalendar({ year, month }),
  });
  const emotions = useQuery({
    queryKey: ["analytics", "emotions"],
    queryFn: () => analyticsApi.emotions(),
  });

  const refresh = useMutation({
    mutationFn: analyticsApi.refresh,
    onSuccess: () => {
      toast.success("Insights refreshed.");
      qc.invalidateQueries({ queryKey: ["analytics"] });
    },
    onError: () => toast.error("Could not refresh insights."),
  });

  const trendData = (trends.data ?? []).map((t) => ({
    period: t.period,
    score: Number((t.avgSentimentScore * 100).toFixed(1)),
    entries: t.entryCount,
  }));

  const emotionData = emotions.data ?? [];

  return (
    <div className="space-y-10 animate-bloom-in">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-[0.2em] text-bloom">
            Insights
          </p>
          <h1 className="mt-2 font-serif text-4xl font-medium md:text-5xl">
            Your inner weather.
          </h1>
          <p className="mt-2 max-w-xl text-muted-foreground">
            A gentle map of your emotions over time - visible only to you.
          </p>
        </div>
        <Button
          variant="outline"
          disabled={refresh.isPending}
          onClick={() => refresh.mutate()}
          className="rounded-full"
        >
          {refresh.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <>
              <RefreshCw className="mr-2 h-4 w-4" /> Refresh
            </>
          )}
        </Button>
      </header>

      {/* Summary band */}
      <section className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <SummaryStat
          icon={BookOpen}
          label="Entries"
          value={summary.data?.totalEntries ?? "-"}
        />
        <SummaryStat
          icon={TrendingUp}
          label="Avg sentiment"
          value={
            summary.data
              ? `${(summary.data.avgSentimentScore * 100).toFixed(0)}%`
              : "-"
          }
        />
        <SummaryStat
          icon={Sparkles}
          label="Most felt"
          value={summary.data?.mostFrequentEmotion ?? "-"}
          capitalize
        />
        <SummaryStat
          icon={Flame}
          label="Longest streak"
          value={summary.data?.longestStreak ?? "-"}
          suffix="days"
        />
      </section>

      {/* Sentiment count band */}
      {summary.data && (
        <section className="grid grid-cols-3 overflow-hidden rounded-2xl border border-border">
          <SentimentBar
            label="POSITIVE"
            count={summary.data.positiveCount}
            total={summary.data.totalEntries}
          />
          <SentimentBar
            label="NEUTRAL"
            count={summary.data.neutralCount}
            total={summary.data.totalEntries}
          />
          <SentimentBar
            label="NEGATIVE"
            count={summary.data.negativeCount}
            total={summary.data.totalEntries}
          />
        </section>
      )}

      {/* Trends */}
      <section className="rounded-3xl border border-border bg-card p-6 shadow-soft md:p-8">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="font-serif text-2xl font-medium">
              Sentiment over time
            </h2>
            <p className="text-sm text-muted-foreground">
              Average score per period (0 = heavy, 100 = bright)
            </p>
          </div>
          <Tabs
            value={period}
            onValueChange={(v) => setPeriod(v as typeof period)}
          >
            <TabsList className="rounded-full">
              <TabsTrigger value="DAILY" className="rounded-full text-xs">
                Daily
              </TabsTrigger>
              <TabsTrigger value="WEEKLY" className="rounded-full text-xs">
                Weekly
              </TabsTrigger>
              <TabsTrigger value="MONTHLY" className="rounded-full text-xs">
                Monthly
              </TabsTrigger>
            </TabsList>
          </Tabs>
        </div>

        {trends.isLoading ? (
          <div className="grid place-items-center py-16">
            <Loader2 className="h-5 w-5 animate-spin text-primary" />
          </div>
        ) : trendData.length === 0 ? (
          <EmptyChart message="No entries to chart yet." />
        ) : (
          <div className="h-72">
            <ResponsiveContainer>
              <AreaChart
                data={trendData}
                margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
              >
                <defs>
                  <linearGradient id="sentArea" x1="0" y1="0" x2="0" y2="1">
                    <stop
                      offset="0%"
                      stopColor="var(--bloom)"
                      stopOpacity={0.5}
                    />
                    <stop
                      offset="100%"
                      stopColor="var(--bloom)"
                      stopOpacity={0}
                    />
                  </linearGradient>
                </defs>
                <CartesianGrid
                  stroke="var(--border)"
                  strokeDasharray="3 3"
                  vertical={false}
                />
                <XAxis
                  dataKey="period"
                  stroke="var(--muted-foreground)"
                  fontSize={11}
                />
                <YAxis
                  domain={[0, 100]}
                  stroke="var(--muted-foreground)"
                  fontSize={11}
                />
                <Tooltip
                  contentStyle={{
                    background: "var(--popover)",
                    border: "1px solid var(--border)",
                    borderRadius: 12,
                    fontSize: 12,
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="score"
                  stroke="var(--bloom)"
                  strokeWidth={2.5}
                  fill="url(#sentArea)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      {/* Mood calendar + emotion distribution */}
      <section className="grid gap-6 lg:grid-cols-5">
        <div className="rounded-3xl border border-border bg-card p-6 shadow-soft lg:col-span-3 md:p-8">
          <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="font-serif text-2xl font-medium">Mood calendar</h2>
              <p className="text-sm text-muted-foreground">
                A day-by-day glance
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Button
                size="sm"
                variant="ghost"
                className="h-8"
                onClick={() => {
                  if (month === 1) {
                    setMonth(12);
                    setYear(year - 1);
                  } else setMonth(month - 1);
                }}
              >
                ‹
              </Button>
              <span className="min-w-[110px] text-center text-sm font-medium">
                {new Date(year, month - 1, 1).toLocaleString(undefined, {
                  month: "long",
                  year: "numeric",
                })}
              </span>
              <Button
                size="sm"
                variant="ghost"
                className="h-8"
                onClick={() => {
                  if (month === 12) {
                    setMonth(1);
                    setYear(year + 1);
                  } else setMonth(month + 1);
                }}
              >
                ›
              </Button>
            </div>
          </div>
          <MoodCalendar
            year={year}
            month={month}
            entries={calendar.data ?? []}
            loading={calendar.isLoading}
          />
        </div>

        <div className="rounded-3xl border border-border bg-card p-6 shadow-soft lg:col-span-2 md:p-8">
          <h2 className="font-serif text-2xl font-medium">Emotion palette</h2>
          <p className="text-sm text-muted-foreground">Last 30 days</p>
          {emotions.isLoading ? (
            <div className="grid place-items-center py-16">
              <Loader2 className="h-5 w-5 animate-spin text-primary" />
            </div>
          ) : emotionData.length === 0 ? (
            <div className="py-16">
              <EmptyChart message="No emotion data yet." />
            </div>
          ) : (
            <>
              <div className="h-52">
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={emotionData}
                      dataKey="count"
                      nameKey="emotion"
                      innerRadius={45}
                      outerRadius={75}
                      paddingAngle={2}
                    >
                      {emotionData.map((_, i) => (
                        <Cell key={i} fill={`var(--chart-${(i % 5) + 1})`} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{
                        background: "var(--popover)",
                        border: "1px solid var(--border)",
                        borderRadius: 12,
                        fontSize: 12,
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <ul className="mt-2 space-y-1.5 text-sm">
                {emotionData.slice(0, 6).map((e, i) => (
                  <li
                    key={e.emotion}
                    className="flex items-center justify-between"
                  >
                    <span className="flex items-center gap-2 capitalize">
                      <span
                        className="h-2.5 w-2.5 rounded-full"
                        style={{
                          backgroundColor: `var(--chart-${(i % 5) + 1})`,
                        }}
                      />
                      {e.emotion}
                    </span>
                    <span className="text-muted-foreground">
                      {e.percentage.toFixed(1)}%
                    </span>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      </section>
    </div>
  );
}

interface SummaryStatProps {
  icon: React.ComponentType<{ className?: string; strokeWidth?: number }>;
  label: string;
  value: string | number;
  suffix?: string;
  capitalize?: boolean;
}
function SummaryStat({
  icon: Icon,
  label,
  value,
  suffix,
  capitalize,
}: SummaryStatProps) {
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
            {value}
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

function SentimentBar({
  label,
  count,
  total,
}: {
  label: "POSITIVE" | "NEUTRAL" | "NEGATIVE";
  count: number;
  total: number;
}) {
  const pct = total > 0 ? (count / total) * 100 : 0;
  return (
    <div className="border-r border-border bg-card p-4 last:border-r-0">
      <div className="mb-2">
        <SentimentPill label={label} variant="soft" />
      </div>
      <p className="font-serif text-2xl font-medium">{count}</p>
      <p className="text-xs text-muted-foreground">
        {pct.toFixed(0)}% of entries
      </p>
    </div>
  );
}

function MoodCalendar({
  year,
  month,
  entries,
  loading,
}: {
  year: number;
  month: number;
  entries: Array<{
    date: string;
    sentimentScore: number;
    sentimentLabel: "POSITIVE" | "NEUTRAL" | "NEGATIVE";
    entryCount: number;
    dominantEmotion: string;
  }>;
  loading: boolean;
}) {
  if (loading)
    return (
      <div className="grid place-items-center py-12">
        <Loader2 className="h-5 w-5 animate-spin text-primary" />
      </div>
    );
  const map = new Map(entries.map((e) => [e.date, e]));
  const firstDay = new Date(year, month - 1, 1);
  const startDow = firstDay.getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const cells: Array<{
    date?: string;
    data?: (typeof entries)[number];
  } | null> = [];
  for (let i = 0; i < startDow; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) {
    const ds = `${year}-${String(month).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
    cells.push({ date: ds, data: map.get(ds) });
  }
  const dows = ["S", "M", "T", "W", "T", "F", "S"];
  return (
    <div>
      <div className="mb-2 grid grid-cols-7 gap-1 text-center text-[10px] uppercase tracking-wider text-muted-foreground">
        {dows.map((d, i) => (
          <div key={i}>{d}</div>
        ))}
      </div>
      <div className="grid grid-cols-7 gap-1.5">
        {cells.map((c, i) => {
          if (!c) return <div key={i} />;
          const has = !!c.data;
          const color = has
            ? sentimentColor[c.data!.sentimentLabel]
            : undefined;
          const intensity = has
            ? Math.max(0.25, Math.min(1, c.data!.sentimentScore))
            : 0;
          return (
            <div
              key={i}
              title={c.data ? `${c.date} · ${c.data.dominantEmotion}` : c.date}
              className="group relative aspect-square rounded-lg border border-border/60 bg-background/60 transition-all hover:border-bloom/60"
              style={
                has
                  ? {
                      backgroundColor: `color-mix(in oklab, ${color} ${intensity * 60}%, var(--background))`,
                    }
                  : undefined
              }
            >
              <span className="absolute left-1.5 top-1 text-[10px] text-muted-foreground/70">
                {Number(c.date!.slice(-2))}
              </span>
              {has && c.data!.entryCount > 0 && (
                <span
                  className="absolute bottom-1 right-1 h-1.5 w-1.5 rounded-full"
                  style={{ backgroundColor: color }}
                />
              )}
            </div>
          );
        })}
      </div>
      <div className="mt-4 flex items-center gap-3 text-[11px] uppercase tracking-wider text-muted-foreground">
        <span className="flex items-center gap-1.5">
          <span
            className="h-2 w-2 rounded-full"
            style={{ backgroundColor: sentimentColor.POSITIVE }}
          />{" "}
          Bright
        </span>
        <span className="flex items-center gap-1.5">
          <span
            className="h-2 w-2 rounded-full"
            style={{ backgroundColor: sentimentColor.NEUTRAL }}
          />{" "}
          Even
        </span>
        <span className="flex items-center gap-1.5">
          <span
            className="h-2 w-2 rounded-full"
            style={{ backgroundColor: sentimentColor.NEGATIVE }}
          />{" "}
          Heavy
        </span>
      </div>
    </div>
  );
}

function EmptyChart({ message }: { message: string }) {
  return (
    <div className="grid h-48 place-items-center rounded-2xl border border-dashed border-border text-sm text-muted-foreground">
      {message}
    </div>
  );
}
