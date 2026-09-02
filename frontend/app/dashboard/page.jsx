"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowRightIcon, CheckIcon, DatabaseIcon, Loader2Icon } from "lucide-react";
import { Panel } from "@/components/Panel";
import { StatusBadge } from "@/components/StatusBadge";
import { usePipeline } from "@/contexts/PipelineContext";
import { modules } from "@/lib/modules";
import { exams, invigilators, rooms, timeslots, totalStudents } from "@/lib/data/registry";
import { buildingGraph, dispatchOrders } from "@/lib/data/buildingGraph";
import { clashAnalysis, masterSchedule } from "@/lib/data/outputs";
import { api } from "@/lib/api/index";

// Fetched from the gateway on load. `timeslots` has no owning service/endpoint
// anywhere in the system, so it stays on the static seed sample.
const liveSources = [
  { key: "exams", fetch: api.task3.getExams },
  { key: "enrollments", fetch: api.task3.getEnrollments },
  { key: "rooms", fetch: api.task4.getRoomReference },
  { key: "invigilators", fetch: api.task2.getInvigilators },
  { key: "buildingGraph", fetch: api.task1.getBuildingGraph },
  { key: "dispatchOrders", fetch: api.task1.getDispatchOrders },
];

export default function DashboardPage() {
  const { runs, run, resetAll } = usePipeline();
  const [pipelineRunning, setPipelineRunning] = useState(false);
  const [live, setLive] = useState(null); // null = still loading; {} shaped like liveSources keys once settled
  const [liveOk, setLiveOk] = useState(false);

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled(liveSources.map((source) => source.fetch())).then((results) => {
      if (cancelled) return;
      const next = {};
      let anyOk = false;
      results.forEach((result, i) => {
        if (result.status === "fulfilled") {
          next[liveSources[i].key] = result.value;
          anyOk = true;
        }
      });
      setLive(next);
      setLiveOk(anyOk);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const completed = modules.filter((m) => runs[m.id].state === "complete").length;
  const timetableReady = runs.task5.state === "complete";
  const schedule = runs.task5.data?.schedule ?? runs.task5.data ?? masterSchedule;

  // task1/task2/task3 expect a raw JSON array body (or none) — an empty object like `{}`
  // fails Jackson deserialization into a List and 400s, silently falling back to sample
  // data. task4 wants an ExamRequest-shaped object; task5 genuinely accepts `{}` (its
  // documented "local-file dev mode").
  const runBodyFor = (moduleId) => {
    if (moduleId === "task1" || moduleId === "task2" || moduleId === "task3") return [];
    if (moduleId === "task4") {
      const exam = live?.exams?.[0] ?? exams[0];
      return {
        exam_id: exam.exam_id,
        course_code: exam.course_code,
        course_title: exam.course_title,
        student_count: exam.student_count,
        requires_accessibility: exam.requires_accessibility,
      };
    }
    return {};
  };

  const runPipeline = async () => {
    setPipelineRunning(true);
    for (const module of modules) {
      await run(module.id, runBodyFor(module.id));
    }
    setPipelineRunning(false);
  };

  const examCount = live?.exams?.length ?? exams.length;
  const studentCount = live?.enrollments?.length ?? totalStudents;
  const roomCount = live?.rooms?.length ?? rooms.length;
  const invigilatorCount = live?.invigilators?.length ?? invigilators.length;
  const buildingGraphCount = live?.buildingGraph?.length ?? buildingGraph.length;
  const dispatchOrderCount = live?.dispatchOrders?.length ?? dispatchOrders.length;

  const stats = [
    { label: "Exams scheduled", value: String(examCount), detail: live?.exams ? "live from Task 3" : "sample dataset" },
    { label: "Students", value: String(studentCount), detail: live?.enrollments ? "live enrolment records" : "sample dataset" },
    { label: "Rooms in registry", value: String(roomCount), detail: live?.rooms ? "live from Task 4" : "sample dataset" },
    { label: "Sessions required", value: `${clashAnalysis.minimum_sessions} / ${timeslots.length}`, detail: "DSATUR lower bound — run Task 3 for live" },
  ];

  const datasets = [
    { file: "input_exams.json", endpoint: "GET /api/task3/exams", records: examCount, live: Boolean(live?.exams) },
    { file: "input_student_enrollments.json", endpoint: "GET /api/task3/enrollments", records: studentCount, live: Boolean(live?.enrollments) },
    { file: "input_room_master.json", endpoint: "GET /api/task4/room-reference", records: roomCount, live: Boolean(live?.rooms) },
    { file: "input_timeslots.json", endpoint: null, records: timeslots.length, live: false },
    { file: "input_invigilators.json", endpoint: "GET /api/task2/invigilators", records: invigilatorCount, live: Boolean(live?.invigilators) },
    { file: "input_building_graph.json", endpoint: "GET /api/task1/building-graph", records: buildingGraphCount, live: Boolean(live?.buildingGraph) },
    { file: "input_dispatch_orders.json", endpoint: "GET /api/task1/dispatch-orders", records: dispatchOrderCount, live: Boolean(live?.dispatchOrders) },
  ];

  return (
    <div className="px-8 py-8">
      <div className="flex flex-wrap items-end justify-between gap-6">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-ink">Exam operations control</h1>
          <p className="mt-1.5 max-w-2xl text-sm text-ink-muted">
            Five algorithmic modules run in dependency order over one shared set of JSON contracts, producing the master exam
            timetable, invigilator roster and paper delivery routes.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={resetAll}
            className="rounded border border-line bg-white px-3.5 py-2 text-sm font-medium text-ink-muted transition-colors duration-150 ease-out hover:text-primary"
          >
            Reset
          </button>
          <button
            type="button"
            onClick={runPipeline}
            disabled={pipelineRunning}
            className="inline-flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors duration-150 ease-out hover:bg-primary-dark disabled:opacity-60"
          >
            {pipelineRunning && <Loader2Icon className="h-4 w-4 animate-spin" aria-hidden="true" />}
            Run full pipeline
          </button>
        </div>
      </div>

      <div className="mt-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((stat) => (
          <div key={stat.label} className="rounded-lg border border-line bg-white p-5">
            <p className="text-[11px] uppercase tracking-wide text-ink-faint">{stat.label}</p>
            <p className="mono mt-2 text-3xl font-semibold text-ink">{stat.value}</p>
            <p className="mt-1 text-xs text-ink-muted">{stat.detail}</p>
          </div>
        ))}
      </div>

      <div className="mt-6 grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <Panel title="Execution pipeline" subtitle={`${completed} of ${modules.length} modules produced output in this session`} bodyClassName="p-0">
          <ol className="divide-y divide-line">
            {modules.map((module, index) => {
              const state = runs[module.id].state;
              return (
                <li key={module.id} className="flex flex-wrap items-center gap-4 px-5 py-4">
                  <span
                    className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-semibold ${
                      state === "complete" ? "bg-success-soft text-success" : "bg-canvas text-ink-faint"
                    }`}
                    aria-hidden="true"
                  >
                    {state === "complete" ? <CheckIcon className="h-4 w-4" /> : index + 1}
                  </span>
                  <div className="min-w-[190px] flex-1">
                    <p className="text-sm font-semibold text-ink">
                      Task {module.taskNumber} · {module.name}
                    </p>
                    <p className="mt-0.5 text-xs text-ink-muted">{module.algorithm}</p>
                  </div>
                  <div className="mono hidden text-[11px] text-ink-faint md:block">{module.produces[0]}</div>
                  {state === "complete" ? (
                    <StatusBadge
                      label={runs[module.id].source === "gateway" ? "Live" : "Sample"}
                      tone={runs[module.id].source === "gateway" ? "accent" : "success"}
                    />
                  ) : state === "running" ? (
                    <StatusBadge label="Running" tone="warning" />
                  ) : (
                    <StatusBadge label="Not run" tone="neutral" />
                  )}
                  <Link href={module.path} className="inline-flex items-center gap-1 text-sm font-semibold text-primary hover:text-accent">
                    Open
                    <ArrowRightIcon className="h-3.5 w-3.5" aria-hidden="true" />
                  </Link>
                </li>
              );
            })}
          </ol>
        </Panel>

        <div className="flex flex-col gap-6">
          <Panel title="Master timetable" subtitle="output_master_schedule.json">
            {timetableReady ? (
              <div>
                <div className="flex items-baseline gap-2">
                  <span className="mono text-3xl font-semibold text-ink">{schedule.length}</span>
                  <span className="text-sm text-ink-muted">exams placed</span>
                </div>
                <ul className="mt-4 space-y-2">
                  {schedule.slice(0, 4).map((entry) => (
                    <li key={entry.exam_id} className="flex items-center justify-between gap-3 text-xs">
                      <span className="mono font-medium text-primary">{entry.course_code}</span>
                      <span className="text-ink-muted">
                        {entry.date} · {entry.session} · {entry.room_id}
                      </span>
                    </li>
                  ))}
                </ul>
                <Link href="/task5" className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-primary hover:text-accent">
                  View full calendar
                  <ArrowRightIcon className="h-3.5 w-3.5" aria-hidden="true" />
                </Link>
              </div>
            ) : (
              <p className="text-sm text-ink-muted">No timetable generated yet. Run Task 3 and Task 4, then generate in Task 5.</p>
            )}
          </Panel>

          <Panel
            title="Seed datasets"
            subtitle="Live gateway calls, falling back to shared JSON contracts"
            bodyClassName="p-0"
            actions={
              live === null ? (
                <StatusBadge label="Loading…" tone="neutral" />
              ) : liveOk ? (
                <StatusBadge label="Live gateway" tone="accent" />
              ) : (
                <StatusBadge label="Sample (gateway unreachable)" tone="warning" />
              )
            }
          >
            <ul className="divide-y divide-line">
              {datasets.map((dataset) => (
                <li key={dataset.file} className="flex items-center gap-3 px-5 py-2.5 text-xs">
                  <DatabaseIcon className="h-3.5 w-3.5 shrink-0 text-ink-faint" aria-hidden="true" />
                  <span className="mono flex-1 truncate text-ink" title={dataset.file}>
                    {dataset.live ? dataset.endpoint : dataset.file}
                  </span>
                  <span className="mono text-ink-muted">{dataset.records}</span>
                  <span className={`text-[10px] uppercase tracking-wide ${dataset.live ? "text-accent" : "text-ink-faint"}`}>
                    {dataset.live ? "live" : "sample"}
                  </span>
                </li>
              ))}
            </ul>
          </Panel>
        </div>
      </div>
    </div>
  );
}
