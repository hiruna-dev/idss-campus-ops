"use client";

import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { api } from "@/lib/api/index";

const initialRuns = {
  task1: { state: "idle" },
  task2: { state: "idle" },
  task3: { state: "idle" },
  task4: { state: "idle" },
  task5: { state: "idle" },
};

const callers = {
  task1: (body) => api.task1.route(body),
  task2: (body) => api.task2.assign(body),
  task3: (body) => api.task3.detect(body),
  task4: (body) => api.task4.rank(body),
  task5: (body) => api.task5.generate(body),
};

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const PipelineContext = createContext(null);

export function PipelineProvider({ children }) {
  const [runs, setRuns] = useState(initialRuns);

  const run = useCallback(async (id, body = {}, fallback = null) => {
    setRuns((current) => ({ ...current, [id]: { state: "running" } }));
    const started = performance.now();
    try {
      const data = await callers[id](body);
      setRuns((current) => ({
        ...current,
        [id]: { state: "complete", source: "gateway", latencyMs: Math.round(performance.now() - started), data },
      }));
    } catch {
      await wait(500);
      setRuns((current) => ({
        ...current,
        [id]: { state: "complete", source: "sample", latencyMs: Math.round(performance.now() - started), data: fallback },
      }));
    }
  }, []);

  const resetAll = useCallback(() => setRuns(initialRuns), []);

  const value = useMemo(() => ({ runs, run, resetAll }), [runs, run, resetAll]);

  return <PipelineContext.Provider value={value}>{children}</PipelineContext.Provider>;
}

export function usePipeline() {
  const context = useContext(PipelineContext);
  if (!context) throw new Error("usePipeline must be used inside PipelineProvider");
  return context;
}

export function useModuleRun(id) {
  const { runs, run } = usePipeline();
  return {
    run: runs[id],
    execute: (body, fallback) => run(id, body, fallback),
  };
}
