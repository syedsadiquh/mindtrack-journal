import type { SentimentLabel } from "@/lib/types";

export const sentimentColor: Record<SentimentLabel, string> = {
  POSITIVE: "var(--sentiment-positive)",
  NEGATIVE: "var(--sentiment-negative)",
  NEUTRAL: "var(--sentiment-neutral)",
};

export const sentimentLabelText: Record<SentimentLabel, string> = {
  POSITIVE: "Bright",
  NEGATIVE: "Heavy",
  NEUTRAL: "Even",
};

export const emotionEmoji: Record<string, string> = {
  joy: "✨",
  happiness: "😊",
  love: "💗",
  surprise: "🌟",
  calm: "🌿",
  contentment: "🍵",
  sadness: "🌧️",
  grief: "🥀",
  anger: "🔥",
  frustration: "💢",
  fear: "🌫️",
  anxiety: "🫧",
  disgust: "🌑",
  neutral: "·",
};

export function getEmotionEmoji(emotion?: string): string {
  if (!emotion) return "·";
  return emotionEmoji[emotion.toLowerCase()] ?? "·";
}

export function formatRelativeDate(iso: string): string {
  const d = new Date(iso);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(d);
  target.setHours(0, 0, 0, 0);
  const diff = Math.round((target.getTime() - today.getTime()) / 86_400_000);
  if (diff === 0) return "Today";
  if (diff === -1) return "Yesterday";
  if (diff > 0 && diff <= 7) return `In ${diff} days`;
  if (diff < 0 && diff >= -7) return `${Math.abs(diff)} days ago`;
  return d.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export function formatLongDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export function todayISO(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}
