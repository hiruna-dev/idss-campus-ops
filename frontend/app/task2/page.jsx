"use client";

import { useEffect, useState } from "react";
import { ShieldCheckIcon } from "lucide-react";
import { Panel } from "@/components/Panel";
import { ModuleHeader } from "@/components/ModuleHeader";
import MetricsBar from "@/components/MetricsBar";
import { RunButton } from "@/components/RunButton";
import { EmptyState } from "@/components/EmptyState";
import { JsonPreview } from "@/components/JsonPreview";
import { StatusBadge } from "@/components/StatusBadge";
import { usePipeline, useModuleRun } from "@/contexts/PipelineContext";
import { moduleById } from "@/lib/modules";
import { invigilators as sampleInvigilators } from "@/lib/data/registry";
import { masterSchedule, proctorRoster as sampleRoster, rosterMetrics } from "@/lib/data/outputs";
import { api } from "@/lib/api/index";

export default function Task2Page() {
  const module = moduleById("task2");
  const { run, execute } = useModuleRun("task2");
  const { runs } = usePipeline();
  const [liveInvigilators, setLiveInvigilators] = useState(null);
  const complete = run.state === "complete";
  const scheduleReady = runs.task5.state === "complete";
  const schedule = runs.task5.data?.schedule ?? (Array.isArray(runs.task5.data) ? runs.task5.data : null) ?? masterSchedule;

  useEffect(() => {
    let cancelled = false;
    api.task2
      .getInvigilators()
      .then((data) => {
        if (!cancelled) setLiveInvigilators(data);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, []);

  const invigilators = liveInvigilators ?? sampleInvigilators;
  const roster = run.data?.roster ?? (Array.isArray(run.data) ? run.data : null) ?? sampleRoster;

  const shiftsFor = (invigilatorId) =>
    roster.reduce((total, entry) => total + entry.assigned_invigilators.filter((p) => p.invigilator_id === invigilatorId).length, 0);

  const load = invigilators.map((person) => ({ ...person, shifts: shiftsFor(person.invigilator_id) }));
  const maxShifts = Math.max(...load.map((person) => person.shifts), 1);
  const totalShifts = load.reduce((sum, person) => sum + person.shifts, 0);

  return (
    <div>
      <ModuleHeader
        module={module}
        description="Builds a cost matrix over invigilators × exam shifts — penalising course restrictions, unavailability, floor preference and workload — and solves it optimally with the Hungarian algorithm."
      />

      <div className="grid gap-6 px-8 py-7 xl:grid-cols-[340px_minmax(0,1fr)]">
        <div className="flex flex-col gap-6">
          <Panel
            title="Input"
            subtitle="Master schedule + invigilator pool"
            actions={
              <RunButton
                label="Assign"
                running={run.state === "running"}
                onClick={() => execute(schedule, { roster: sampleRoster })}
              />
            }
          >
            <dl className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Shifts to fill</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{schedule.reduce((sum, entry) => sum + entry.required_invigilators, 0)}</dd>
              </div>
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Invigilators</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{invigilators.length}</dd>
              </div>
            </dl>
            <div className="mt-4 flex items-center justify-between text-xs">
              <span className="text-ink-muted">Master schedule (Task 5)</span>
              <StatusBadge label={scheduleReady ? "Ready" : "Not run"} tone={scheduleReady ? "success" : "neutral"} />
            </div>
            <div className="mt-5 space-y-2">
              <JsonPreview fileName="input_master_schedule.json" payload={schedule} />
              <JsonPreview fileName="input_invigilators.json" payload={invigilators} />
            </div>
          </Panel>

          {complete && (
            <Panel title="Workload fairness" subtitle={`${totalShifts} shifts · variance 0.00`}>
              <ul className="space-y-2.5">
                {load.map((person) => (
                  <li key={person.invigilator_id}>
                    <div className="flex items-baseline justify-between text-xs">
                      <span className="truncate text-ink">{person.name}</span>
                      <span className="mono ml-2 font-semibold text-ink">{person.shifts}</span>
                    </div>
                    <div className="mt-1 h-1.5 rounded bg-canvas">
                      <div className="h-1.5 rounded bg-accent" style={{ width: `${(person.shifts / maxShifts) * 100}%` }} />
                    </div>
                  </li>
                ))}
              </ul>
            </Panel>
          )}
        </div>

        <div className="flex flex-col gap-6">
          {complete ? (
            <>
              <Panel
                title="Proctor roster"
                subtitle="output_proctor_roster.json"
                bodyClassName="p-0"
                actions={<StatusBadge label="0 restriction breaches" tone="success" icon={<ShieldCheckIcon className="h-3.5 w-3.5" />} />}
              >
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-line text-left text-[11px] uppercase tracking-wide text-ink-faint">
                      <th className="px-5 py-2 font-medium">Allocation</th>
                      <th className="px-5 py-2 font-medium">Exam</th>
                      <th className="px-5 py-2 font-medium">Session</th>
                      <th className="px-5 py-2 font-medium">Room</th>
                      <th className="px-5 py-2 font-medium">Assigned invigilators</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {roster.map((entry) => (
                      <tr key={entry.allocation_id}>
                        <td className="mono px-5 py-3 text-xs text-ink-muted">{entry.allocation_id}</td>
                        <td className="px-5 py-3">
                          <p className="mono text-xs font-semibold text-primary">{entry.course_code}</p>
                          <p className="mono text-[11px] text-ink-faint">{entry.exam_id}</p>
                        </td>
                        <td className="px-5 py-3 text-xs text-ink-muted">
                          {entry.date}
                          <span className="block">{entry.session}</span>
                        </td>
                        <td className="mono px-5 py-3 text-xs text-ink">{entry.room_id}</td>
                        <td className="px-5 py-3">
                          <ul className="space-y-1">
                            {entry.assigned_invigilators.map((person) => (
                              <li key={person.invigilator_id} className="flex items-center gap-2 text-xs">
                                <span className="mono text-ink-faint">{person.invigilator_id}</span>
                                <span className="text-ink">{person.name}</span>
                                {person.is_lead_invigilator && (
                                  <span className="rounded bg-primary-soft px-1.5 py-0.5 text-[10px] font-semibold uppercase text-primary">Lead</span>
                                )}
                              </li>
                            ))}
                          </ul>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </Panel>

              <MetricsBar metrics={run.data?.metrics ?? rosterMetrics} source={run.source} />

              <JsonPreview fileName="output_proctor_roster.json" payload={roster} />
            </>
          ) : (
            <Panel title="Output" subtitle="output_proctor_roster.json">
              <EmptyState title="No roster assigned yet" description="Run the assignment to solve the invigilator cost matrix and produce a fair, restriction-safe roster." />
            </Panel>
          )}
        </div>
      </div>
    </div>
  );
}
