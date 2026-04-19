import { cn } from "@/lib/utils";
import {
  sentimentColor,
  sentimentLabelText,
  getEmotionEmoji,
} from "@/lib/sentiment";
import type { SentimentLabel } from "@/lib/types";

interface SentimentPillProps {
  label?: SentimentLabel;
  emotion?: string;
  score?: number;
  className?: string;
  variant?: "soft" | "bold";
}

export function SentimentPill({
  label,
  emotion,
  score,
  className,
  variant = "soft",
}: SentimentPillProps) {
  if (!label) {
    return (
      <span
        className={cn(
          "inline-flex items-center gap-1.5 rounded-full border border-dashed border-border px-2.5 py-0.5 text-[11px] uppercase tracking-wider text-muted-foreground",
          className,
        )}
      >
        <span className="h-1.5 w-1.5 rounded-full bg-muted-foreground/40" />{" "}
        awaiting
      </span>
    );
  }
  const color = sentimentColor[label];
  const styleSoft = {
    backgroundColor: `color-mix(in oklab, ${color} 14%, transparent)`,
    color: `color-mix(in oklab, ${color} 85%, var(--foreground))`,
  };
  const styleBold = {
    backgroundColor: color,
    color: "white",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-medium uppercase tracking-wider",
        className,
      )}
      style={variant === "bold" ? styleBold : styleSoft}
    >
      <span className="text-sm leading-none">{getEmotionEmoji(emotion)}</span>
      <span>
        {sentimentLabelText[label]}
        {typeof score === "number" && score > 0
          ? ` · ${(score * 100).toFixed(0)}`
          : ""}
      </span>
    </span>
  );
}
