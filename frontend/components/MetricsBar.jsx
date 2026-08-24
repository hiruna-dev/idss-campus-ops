export default function MetricsBar({ metrics }) {
  if (!metrics) {
    return (
      <div className="flex gap-4 rounded-lg bg-card px-4 py-2 text-sm text-gray-400 shadow">
        <span>No metrics yet — run an algorithm to see results.</span>
      </div>
    );
  }

  const items = [
    { label: "Algorithm", value: metrics.algorithm_used ?? "—" },
    { label: "Time", value: `${metrics.execution_time_ms ?? "—"} ms` },
    { label: "Memory", value: `${metrics.memory_allocated_kb ?? "—"} KB` },
    {
      label: "Violations",
      value: metrics.hard_constraint_violations ?? "—",
      highlight: metrics.hard_constraint_violations === 0 ? "success" : "warning",
    },
    { label: "Status", value: metrics.status ?? "—" },
  ];

  return (
    <div className="flex flex-wrap gap-4 rounded-lg bg-card px-4 py-2 text-sm shadow">
      {items.map((item) => (
        <span key={item.label}>
          <span className="font-semibold text-primary">{item.label}:</span>{" "}
          <span
            className={
              item.highlight === "success"
                ? "text-idss-success"
                : item.highlight === "warning"
                  ? "text-idss-warning"
                  : ""
            }
          >
            {item.value}
          </span>
        </span>
      ))}
    </div>
  );
}
