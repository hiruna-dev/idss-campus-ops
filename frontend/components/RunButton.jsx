import { Loader2Icon, PlayIcon } from "lucide-react";

export function RunButton({ label, onClick, running, disabled, variant = "primary" }) {
  const base =
    "inline-flex items-center justify-center gap-2 rounded px-3.5 py-2 text-sm font-semibold transition-colors duration-150 ease-out focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent disabled:cursor-not-allowed disabled:opacity-60";
  const styles =
    variant === "primary"
      ? "bg-primary text-white hover:bg-primary-dark"
      : "border border-line bg-white text-primary hover:bg-primary-soft";

  return (
    <button type="button" onClick={onClick} disabled={disabled || running} className={`${base} ${styles}`}>
      {running ? <Loader2Icon className="h-4 w-4 animate-spin" aria-hidden="true" /> : <PlayIcon className="h-4 w-4" aria-hidden="true" />}
      {running ? "Running…" : label}
    </button>
  );
}
