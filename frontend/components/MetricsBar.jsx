import { StatusBadge, statusTone } from "@/components/StatusBadge";

export default function MetricsBar({ metrics, source }) {
  if (!metrics) {
    return (
      <div className="rounded-lg border border-dashed border-line bg-white px-5 py-3 text-sm text-ink-muted">
        Run an algorithm to see performance results here.
      </div>
    );
  }

  const items = [
    { label: "Algorithm", value: metrics.algorithm_used },
    { label: "Execution time", value: `${metrics.execution_time_ms} ms` },
    { label: "Memory", value: `${metrics.memory_allocated_kb} KB` },
    { label: "Hard violations", value: String(metrics.violations ?? metrics.hard_constraint_violations ?? 0) },
    ...(metrics.extra ?? []),
  ];

  return (
    <div className="rounded-lg border border-line bg-white">
      <div className="flex items-center justify-between border-b border-line px-5 py-2.5">
        <h3 className="text-xs font-semibold uppercase tracking-wide text-ink-muted">Benchmark metrics</h3>
        <div className="flex items-center gap-2">
          <StatusBadge label={metrics.status} tone={statusTone(metrics.status)} />
          <StatusBadge label={source === "gateway" ? "Live gateway" : "Sample contract"} tone={source === "gateway" ? "accent" : "neutral"} />
        </div>
      </div>
      <dl className="grid grid-cols-2 divide-line md:grid-cols-3 xl:grid-cols-4">
        {items.map((item) => (
          <div key={item.label} className="border-b border-r border-line px-5 py-3">
            <dt className="text-[11px] uppercase tracking-wide text-ink-faint">{item.label}</dt>
            <dd className="mono mt-1 text-sm font-medium text-ink">{item.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
