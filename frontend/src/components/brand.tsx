import { Link } from "@tanstack/react-router";
import { Feather } from "lucide-react";
import { cn } from "@/lib/utils";

interface BrandProps {
  className?: string;
  asLink?: boolean;
  size?: "sm" | "md" | "lg";
}

export function Brand({ className, asLink = true, size = "md" }: BrandProps) {
  const sizes = {
    sm: { icon: "h-4 w-4", text: "text-base" },
    md: { icon: "h-5 w-5", text: "text-lg" },
    lg: { icon: "h-6 w-6", text: "text-2xl" },
  } as const;
  const inner = (
    <span
      className={cn(
        "inline-flex items-center gap-2 font-serif font-medium tracking-tight",
        sizes[size].text,
        className,
      )}
    >
      <span className="grid h-8 w-8 place-items-center rounded-full bg-gradient-bloom text-primary-foreground shadow-soft">
        <Feather className={sizes[size].icon} strokeWidth={2.2} />
      </span>
      <span>MindTrack</span>
    </span>
  );
  if (!asLink) return inner;
  return (
    <Link to="/" className="transition-opacity hover:opacity-80">
      {inner}
    </Link>
  );
}
