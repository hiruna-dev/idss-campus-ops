"use client";

import { useState } from "react";
import MetricsBar from "@/components/MetricsBar";

export default function Task2Page() {
  const [result, setResult] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const runAlgorithm = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("http://localhost:8080/api/task2/assign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({}),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json();
      setResult(json.data ?? json);
      setMetrics(json.metrics ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    } finally {
      setLoading(false);
    }
  };

  const exportJson = () => {
    if (!result) return;
    const blob = new Blob([JSON.stringify(result, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "output_proctor_rosters.json";
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold text-primary">
        Task 2 — Invigilator Assignment (Hungarian)
      </h2>
      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-lg bg-card p-4 shadow">
          <h3 className="font-semibold">Input</h3>
          <p className="mt-2 text-sm text-gray-500">
            Master schedule + invigilators
          </p>
          <div className="mt-4 flex gap-2">
            <button
              onClick={runAlgorithm}
              disabled={loading}
              className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-white transition hover:bg-accent disabled:opacity-50"
            >
              {loading ? "Running..." : "Run Algorithm"}
            </button>
            <button
              onClick={exportJson}
              className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium transition hover:bg-gray-50"
            >
              Export JSON
            </button>
          </div>
        </div>
        <div className="rounded-lg bg-card p-4 shadow">
          <h3 className="font-semibold">Output</h3>
          {error && <p className="mt-2 text-sm text-red-500">{error}</p>}
          {result && (
            <pre className="mt-2 max-h-64 overflow-auto text-xs">
              {JSON.stringify(result, null, 2)}
            </pre>
          )}
        </div>
      </div>
      <MetricsBar metrics={metrics} />
    </div>
  );
}
