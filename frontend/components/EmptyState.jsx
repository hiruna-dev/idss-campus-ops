import { TerminalIcon } from "lucide-react";

export function EmptyState({ title, description }) {
  return (
    <div className="flex h-full min-h-[260px] flex-col items-center justify-center rounded border border-dashed border-line px-6 py-10 text-center">
      <TerminalIcon className="h-5 w-5 text-ink-faint" aria-hidden="true" />
      <p className="mt-3 text-sm font-semibold text-ink">{title}</p>
      <p className="mt-1 max-w-sm text-xs text-ink-muted">{description}</p>
    </div>
  );
}
