import * as React from "react";
import { DayPicker } from "react-day-picker";
import "react-day-picker/style.css";
import { format, parseISO, isValid } from "date-fns";
import { Calendar as CalendarIcon } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/inputs/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/overlays/popover";

interface DatePickerProps {
  value: string; // ISO yyyy-MM-dd
  onChange: (iso: string) => void;
  max?: string;
  min?: string;
  id?: string;
  className?: string;
  placeholder?: string;
}

function toDate(iso: string | undefined): Date | undefined {
  if (!iso) return undefined;
  const d = parseISO(iso);
  return isValid(d) ? d : undefined;
}

function toISO(d: Date): string {
  return format(d, "yyyy-MM-dd");
}

export function DatePicker({
  value,
  onChange,
  max,
  min,
  id,
  className,
  placeholder = "Pick a date",
}: DatePickerProps) {
  const [open, setOpen] = React.useState(false);
  const selected = toDate(value);
  const maxDate = toDate(max);
  const minDate = toDate(min);
  const today = new Date();

  const disabled = [
    ...(maxDate ? [{ after: maxDate }] : []),
    ...(minDate ? [{ before: minDate }] : []),
  ];

  const isTodayDisabled =
    (maxDate && today > maxDate) || (minDate && today < minDate);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          id={id}
          type="button"
          variant="outline"
          className={cn(
            "h-10 w-full justify-between rounded-xl bg-background px-3 font-normal",
            !selected && "text-muted-foreground",
            className,
          )}
        >
          <span>
            {selected ? format(selected, "dd / MM / yyyy") : placeholder}
          </span>
          <CalendarIcon className="h-4 w-4 text-muted-foreground" />
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className="w-auto rounded-2xl border-border bg-card p-3 shadow-elevated"
        align="start"
      >
        <DayPicker
          mode="single"
          selected={selected}
          onSelect={(d) => {
            if (d) {
              onChange(toISO(d));
              setOpen(false);
            }
          }}
          defaultMonth={selected ?? today}
          disabled={disabled}
          showOutsideDays
          className="[--rdp-accent-color:var(--color-primary)] [--rdp-accent-background-color:color-mix(in_oklab,var(--color-primary)_15%,transparent)]"
          classNames={{
            month: "relative",
            month_caption:
              "flex items-center justify-center py-2 font-serif text-base",
            caption_label: "font-medium",
            nav: "absolute right-0 top-1 z-10 flex items-center gap-0.5",
            button_previous:
              "h-7 w-7 rounded-full hover:bg-muted inline-flex items-center justify-center",
            button_next:
              "h-7 w-7 rounded-full hover:bg-muted inline-flex items-center justify-center",
            weekday:
              "text-xs font-medium text-muted-foreground uppercase tracking-wider",
            day: "h-9 w-9 p-0 text-sm",
            day_button:
              "h-9 w-9 rounded-full hover:bg-secondary aria-selected:bg-primary aria-selected:text-primary-foreground disabled:opacity-30 disabled:pointer-events-none",
            today: "font-semibold text-primary",
            outside: "text-muted-foreground/40",
          }}
        />
        <div className="mt-2 flex justify-end border-t border-border pt-2">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="h-8 rounded-full text-xs"
            disabled={isTodayDisabled}
            onClick={() => {
              onChange(toISO(today));
              setOpen(false);
            }}
          >
            Today
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  );
}
