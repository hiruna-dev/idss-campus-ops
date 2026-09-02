const toneClasses = {
  success: "bg-success-soft text-success border-success/20",
  warning: "bg-warning-soft text-warning border-warning/20",
  danger: "bg-danger-soft text-danger border-danger/20",
  neutral: "bg-canvas text-ink-muted border-line",
  accent: "bg-accent-soft text-primary border-accent/25",
};

export function StatusBadge({ label, tone = "neutral", icon }) {
  return (
    <span className={`inline-flex items-center gap-1.5 rounded border px-2 py-0.5 text-xs font-semibold ${toneClasses[tone]}`}>
      {icon}
      {label}
    </span>
  );
}

export function statusTone(status) {
  if (status === "OPTIMAL" || status === "VALID") return "success";
  if (status === "FEASIBLE") return "accent";
  if (status === "INFEASIBLE") return "danger";
  return "neutral";
}
