import { CheckCircle2, Clock, Cpu, AlertTriangle, Activity } from "lucide-react";

export default function MetricsBar({ metrics }) {
  if (!metrics) {
    return (
      <div className="flex items-center gap-3 rounded-xl bg-muted/50 border border-border px-5 py-3 text-sm text-muted-foreground">
        <Activity className="w-4 h-4" />
        <span>Run an algorithm to see performance results here.</span>
      </div>
    );
  }

  return (
    <div className="flex flex-wrap items-center gap-x-6 gap-y-2 rounded-xl bg-card border border-border px-5 py-3 text-sm shadow-sm">
      <div className="flex items-center gap-2">
        <Cpu className="w-4 h-4 text-primary" />
        <span className="text-muted-foreground">Algorithm:</span>
        <span className="font-semibold">{metrics.algorithm_used ?? "—"}</span>
      </div>
      <div className="flex items-center gap-2">
        <Clock className="w-4 h-4 text-primary" />
        <span className="text-muted-foreground">Time:</span>
        <span className="font-semibold">{metrics.execution_time_ms ?? "—"} ms</span>
      </div>
      <div className="flex items-center gap-2">
        <Activity className="w-4 h-4 text-primary" />
        <span className="text-muted-foreground">Memory:</span>
        <span className="font-semibold">{metrics.memory_allocated_kb ?? "—"} KB</span>
      </div>
      <div className="flex items-center gap-2">
        {metrics.hard_constraint_violations === 0 ? (
          <CheckCircle2 className="w-4 h-4 text-green-500" />
        ) : (
          <AlertTriangle className="w-4 h-4 text-amber-500" />
        )}
        <span className="text-muted-foreground">Violations:</span>
        <span className={`font-semibold ${metrics.hard_constraint_violations === 0 ? "text-green-500" : "text-amber-500"}`}>
          {metrics.hard_constraint_violations ?? "—"}
        </span>
      </div>
      <div className="flex items-center gap-2">
        <span className="text-muted-foreground">Status:</span>
        <span className={`font-semibold px-2 py-0.5 rounded-full text-xs ${
          metrics.status === "OPTIMAL" ? "bg-green-500/15 text-green-500" :
          metrics.status === "FEASIBLE" ? "bg-amber-500/15 text-amber-500" :
          "bg-red-500/15 text-red-500"
        }`}>
          {metrics.status ?? "—"}
        </span>
      </div>
    </div>
  );
}
