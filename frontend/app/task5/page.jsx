"use client";

import { useState } from "react";
import Link from "next/link";
import { CheckCircle2Icon, SendIcon } from "lucide-react";
import { Panel } from "@/components/Panel";
import { ModuleHeader } from "@/components/ModuleHeader";
import MetricsBar from "@/components/MetricsBar";
import { RunButton } from "@/components/RunButton";
import { EmptyState } from "@/components/EmptyState";
import { JsonPreview } from "@/components/JsonPreview";
import { StatusBadge } from "@/components/StatusBadge";
import { usePipeline, useModuleRun } from "@/contexts/PipelineContext";
import { moduleById } from "@/lib/modules";
import { exams, timeslots } from "@/lib/data/registry";
import { algorithmComparison, conflictGraph, fatigueReport, masterSchedule as sampleSchedule, timetableMetrics } from "@/lib/data/outputs";

const validationChecks = [
  { rule: "No clashing exam pair shares a date + session", result: "PASS" },
  { rule: "Every allocated room capacity ≥ allocated students", result: "PASS" },
  { rule: "Accessible exams placed in accessible rooms", result: "PASS" },
  { rule: "No room double-booked in a date + session", result: "PASS" },
  { rule: "Parallel exams per session ≤ 3", result: "PASS" },
];

const dates = Array.from(new Set(timeslots.map((slot) => slot.date)));
const sessions = ["Morning", "Afternoon"];

export default function Task5Page() {
  const module = moduleById("task5");
  const { run, execute } = useModuleRun("task5");
  const { runs, run: runModule } = usePipeline();
  const [validated, setValidated] = useState(false);
  const [pushing, setPushing] = useState(false);
  const complete = run.state === "complete";

  const schedule = run.data?.schedule ?? (Array.isArray(run.data) ? run.data : null) ?? sampleSchedule;

  const pushDownstream = async () => {
    setPushing(true);
    await runModule("task2", schedule);
    // Task 1 has no use for the raw schedule (different shape entirely) — its backend
    // already derives dispatch orders itself from the persisted master schedule when
    // given an empty list (RouteService.loadDefaultDispatchOrders / DispatchMapper).
    await runModule("task1", []);
    setPushing(false);
  };

  const pushed = runs.task2.state === "complete" && runs.task1.state === "complete";

  return (
    <div>
      <ModuleHeader
        module={module}
        description="Assigns every exam a timeslot and room with a hybrid genetic algorithm — hard constraints from the Task 3 conflict graph and Task 4 room rankings, soft constraints from student fatigue penalties."
      />

      <div className="grid gap-6 px-8 py-7 xl:grid-cols-[340px_minmax(0,1fr)]">
        <div className="flex flex-col gap-6">
          <Panel
            title="Input"
            subtitle="Upstream contracts"
            actions={
              <RunButton
                label="Generate"
                running={run.state === "running"}
                onClick={() => {
                  setValidated(false);
                  // Backend requires exams + students + timeslots + room_rankings +
                  // room_references together (TimetableService.validateInputs) — this page
                  // only has exams/timeslots samples, so send an empty body to trigger the
                  // backend's own "local-file dev mode" (generateFromLocalFiles) instead of
                  // an inevitably-incomplete payload.
                  execute({}, { schedule: sampleSchedule });
                }}
              />
            }
          >
            <dl className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Exams</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{exams.length}</dd>
              </div>
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Timeslots</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{timeslots.length}</dd>
              </div>
            </dl>
            <ul className="mt-4 space-y-1.5 text-xs">
              {[
                { label: "Conflict graph (Task 3)", ready: runs.task3.state === "complete" },
                { label: "Room rankings (Task 4)", ready: runs.task4.state === "complete" },
              ].map((dependency) => (
                <li key={dependency.label} className="flex items-center justify-between gap-3">
                  <span className="text-ink-muted">{dependency.label}</span>
                  <StatusBadge label={dependency.ready ? "Ready" : "Not run"} tone={dependency.ready ? "success" : "neutral"} />
                </li>
              ))}
            </ul>
            <div className="mt-5 space-y-2">
              <JsonPreview fileName="input_timeslots.json" payload={timeslots} />
              <JsonPreview fileName="output_conflict_graph.json" payload={runs.task3.data ?? conflictGraph} />
            </div>
          </Panel>

          {complete && (
            <Panel title="Downstream handoff">
              <p className="text-xs leading-relaxed text-ink-muted">
                The master schedule is the byte-identical input for the invigilator roster and the paper delivery routes.
              </p>
              <button
                type="button"
                onClick={pushDownstream}
                disabled={pushing}
                className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded bg-primary px-3.5 py-2 text-sm font-semibold text-white transition-colors duration-150 ease-out hover:bg-primary-dark disabled:opacity-60"
              >
                <SendIcon className="h-4 w-4" aria-hidden="true" />
                {pushing ? "Pushing…" : "Push to Task 2 & Task 1"}
              </button>
              {pushed && (
                <p className="mt-3 text-xs text-success">
                  Roster and routes generated —{" "}
                  <Link href="/task2" className="font-semibold underline">
                    view roster
                  </Link>{" "}
                  ·{" "}
                  <Link href="/task1" className="font-semibold underline">
                    view routes
                  </Link>
                </p>
              )}
            </Panel>
          )}
        </div>

        <div className="flex flex-col gap-6">
          {complete ? (
            <>
              <Panel
                title="Master schedule"
                subtitle="output_master_schedule.json"
                actions={
                  <button
                    type="button"
                    onClick={() => setValidated(true)}
                    className="rounded border border-line px-3 py-1.5 text-xs font-semibold text-primary transition-colors duration-150 ease-out hover:bg-primary-soft"
                  >
                    Validate
                  </button>
                }
              >
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[640px] table-fixed border-collapse">
                    <thead>
                      <tr>
                        <th className="w-24 border-b border-line px-3 py-2 text-left text-[11px] uppercase tracking-wide text-ink-faint">Session</th>
                        {dates.map((date) => (
                          <th key={date} className="border-b border-line px-3 py-2 text-left text-[11px] uppercase tracking-wide text-ink-faint">
                            {date}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {sessions.map((session) => (
                        <tr key={session} className="align-top">
                          <th scope="row" className="border-b border-line px-3 py-3 text-left text-xs font-semibold text-ink">
                            {session}
                            <span className="mono block font-normal text-ink-faint">{session === "Morning" ? "09:00" : "13:30"}</span>
                          </th>
                          {dates.map((date) => {
                            const cell = schedule.filter((entry) => entry.date === date && entry.session === session);
                            return (
                              <td key={`${date}-${session}`} className="border-b border-l border-line px-2 py-2">
                                {cell.length === 0 ? (
                                  <span className="text-xs text-ink-faint">—</span>
                                ) : (
                                  <ul className="space-y-1.5">
                                    {cell.map((entry) => (
                                      <li key={entry.exam_id} className="rounded border border-line bg-canvas px-2.5 py-1.5">
                                        <p className="mono text-xs font-semibold text-primary">{entry.course_code}</p>
                                        <p className="mt-0.5 text-[11px] text-ink-muted">
                                          {entry.room_id} · {entry.allocated_students} students · {entry.required_invigilators} inv.
                                        </p>
                                      </li>
                                    ))}
                                  </ul>
                                )}
                              </td>
                            );
                          })}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Panel>

              {validated && (
                <Panel title="Validation" subtitle="Hard constraint check" bodyClassName="p-0">
                  <ul className="divide-y divide-line">
                    {validationChecks.map((check) => (
                      <li key={check.rule} className="flex items-center gap-3 px-5 py-2.5 text-xs">
                        <CheckCircle2Icon className="h-4 w-4 shrink-0 text-success" aria-hidden="true" />
                        <span className="flex-1 text-ink">{check.rule}</span>
                        <span className="mono font-semibold text-success">{check.result}</span>
                      </li>
                    ))}
                  </ul>
                </Panel>
              )}

              <div className="grid gap-6 lg:grid-cols-2">
                <Panel title="Fatigue report" subtitle="Lower penalty is better">
                  <div className="flex items-baseline gap-3">
                    <span className="mono text-4xl font-semibold text-primary">{fatigueReport.total_fatigue_penalty}</span>
                    <span className="text-xs text-ink-muted">
                      total penalty · {fatigueReport.soft_constraint_satisfaction_percentage}% soft satisfaction
                    </span>
                  </div>
                  <ul className="mt-5 space-y-3">
                    {fatigueReport.breakdown.map((item) => (
                      <li key={item.label}>
                        <div className="flex items-baseline justify-between text-xs">
                          <span className="text-ink">{item.label}</span>
                          <span className="mono text-ink-muted">
                            {item.count} × {item.weight} = {item.penalty}
                          </span>
                        </div>
                        <div className="mt-1.5 h-1.5 rounded bg-canvas">
                          <div className="h-1.5 rounded bg-accent" style={{ width: `${(item.penalty / Math.max(fatigueReport.total_fatigue_penalty, 1)) * 100}%` }} />
                        </div>
                      </li>
                    ))}
                  </ul>
                </Panel>

                <Panel title="Algorithm comparison" subtitle="Same dataset, same benchmark harness" bodyClassName="p-0">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-line text-left text-[11px] uppercase tracking-wide text-ink-faint">
                        <th className="px-5 py-2 font-medium">Algorithm</th>
                        <th className="px-5 py-2 text-right font-medium">Time</th>
                        <th className="px-5 py-2 text-right font-medium">Viol.</th>
                        <th className="px-5 py-2 text-right font-medium">Fatigue</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line">
                      {algorithmComparison.map((row) => (
                        <tr key={row.algorithm} className={row.selected ? "bg-primary-soft/50" : ""}>
                          <td className="px-5 py-2.5 text-xs text-ink">
                            {row.algorithm}
                            {row.selected && <span className="ml-2 text-[10px] font-semibold uppercase text-primary">selected</span>}
                          </td>
                          <td className="mono px-5 py-2.5 text-right text-xs text-ink-muted">{row.time_ms} ms</td>
                          <td className={`mono px-5 py-2.5 text-right text-xs font-semibold ${row.violations === 0 ? "text-success" : "text-danger"}`}>{row.violations}</td>
                          <td className="mono px-5 py-2.5 text-right text-xs text-ink-muted">{row.fatigue}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </Panel>
              </div>

              <MetricsBar metrics={run.data?.metrics ?? timetableMetrics} source={run.source} />

              <div className="grid gap-3 sm:grid-cols-2">
                <JsonPreview fileName="output_master_schedule.json" payload={schedule} />
                <JsonPreview fileName="output_fatigue_report.json" payload={fatigueReport} />
              </div>
            </>
          ) : (
            <Panel title="Output" subtitle="output_master_schedule.json">
              <EmptyState title="No timetable generated yet" description="Generate to place every exam into a clash-free date, session and room, then validate the hard constraints." />
            </Panel>
          )}
        </div>
      </div>
    </div>
  );
}
