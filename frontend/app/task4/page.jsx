"use client";

import { useEffect, useState } from "react";
import { AccessibilityIcon } from "lucide-react";
import { Panel } from "@/components/Panel";
import { ModuleHeader } from "@/components/ModuleHeader";
import MetricsBar from "@/components/MetricsBar";
import { RunButton } from "@/components/RunButton";
import { EmptyState } from "@/components/EmptyState";
import { JsonPreview } from "@/components/JsonPreview";
import { StatusBadge } from "@/components/StatusBadge";
import { useModuleRun } from "@/contexts/PipelineContext";
import { moduleById } from "@/lib/modules";
import { exams, rooms } from "@/lib/data/registry";
import { roomRankings as sampleRankings, roomRankingMetrics } from "@/lib/data/outputs";
import { api } from "@/lib/api/index";

// AHP-derived weights (Section 6 of the Task 4 plan) — capacity is a hard filter, not scored.
const AHP_WEIGHTS = { ac: 0.25, noise: 0.25, accessibility: 0.5 };
const criteria = [
  { label: "Accessibility", weight: AHP_WEIGHTS.accessibility },
  { label: "Noise (1–5, 5 = quietest)", weight: AHP_WEIGHTS.noise },
  { label: "Air conditioning", weight: AHP_WEIGHTS.ac },
];

export default function Task4Page() {
  const module = moduleById("task4");
  const { run, execute } = useModuleRun("task4");
  const [examId, setExamId] = useState(exams[0].exam_id);
  const [liveRoomReference, setLiveRoomReference] = useState(null);
  const complete = run.state === "complete";

  useEffect(() => {
    let cancelled = false;
    api.task4
      .getRoomReference()
      .then((data) => {
        if (!cancelled) setLiveRoomReference(data);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, []);

  // The live /room-reference endpoint only exposes room_id/room_name/floor/is_accessible
  // (Task 4's deliberately-minimal lookup table for Task 1) — no capacity/AC/noise, so the
  // detailed room table below still needs the full static room master for those attributes.
  const roomReference = liveRoomReference ?? rooms.map((room) => ({ room_id: room.room_id, room_name: room.room_name, floor: room.floor, is_accessible: room.is_accessible }));
  const roomCount = liveRoomReference?.length ?? rooms.length;

  const selectedExam = exams.find((item) => item.exam_id === examId) ?? exams[0];
  const rankings = run.data?.rankings ?? (Array.isArray(run.data) ? run.data : null) ?? sampleRankings;
  // Pin the displayed exam to whichever one actually produced `rankings`, not the live
  // dropdown selection — otherwise flipping the selector after a run silently mismatches
  // the header/utilisation math against stale results (e.g. a room reading >100% capacity).
  const rankedExamId = complete ? rankings[0]?.exam_id : null;
  const exam = (rankedExamId && exams.find((item) => item.exam_id === rankedExamId)) || selectedExam;

  return (
    <div>
      <ModuleHeader
        module={module}
        description="Filters rooms on hard constraints (capacity, and step-free access when the exam requires it), then ranks the survivors with TOPSIS using weights derived from an AHP pairwise comparison."
      />

      <div className="grid gap-6 px-8 py-7 xl:grid-cols-[340px_minmax(0,1fr)]">
        <div className="flex flex-col gap-6">
          <Panel
            title="Input"
            subtitle="Exam request + room master"
            actions={
              <RunButton
                label="Rank rooms"
                running={run.state === "running"}
                onClick={() =>
                  execute(
                    {
                      exam_id: selectedExam.exam_id,
                      course_code: selectedExam.course_code,
                      course_title: selectedExam.course_title,
                      student_count: selectedExam.student_count,
                      requires_accessibility: selectedExam.requires_accessibility,
                    },
                    sampleRankings
                  )
                }
              />
            }
          >
            <label htmlFor="exam-select" className="text-[11px] uppercase tracking-wide text-ink-faint">
              Exam request
            </label>
            <select
              id="exam-select"
              value={examId}
              onChange={(event) => setExamId(event.target.value)}
              className="mt-1.5 w-full rounded border border-line bg-white px-3 py-2 text-sm text-ink focus:border-accent focus:outline-none"
            >
              {exams.map((item) => (
                <option key={item.exam_id} value={item.exam_id}>
                  {item.course_code} — {item.student_count} students
                </option>
              ))}
            </select>

            <dl className="mt-4 space-y-2 text-xs">
              <div className="flex justify-between">
                <dt className="text-ink-muted">Seats required</dt>
                <dd className="mono font-medium text-ink">{selectedExam.student_count}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-ink-muted">Step-free required</dt>
                <dd className="mono font-medium text-ink">{String(selectedExam.requires_accessibility)}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-ink-muted">Rooms in registry</dt>
                <dd className="mono font-medium text-ink">{roomCount}</dd>
              </div>
            </dl>

            <div className="mt-5 space-y-2">
              <JsonPreview fileName="input_room_master.json" payload={rooms} />
            </div>
          </Panel>

          <Panel title="AHP-derived weights" subtitle="Consistency ratio 0.00">
            <ul className="space-y-3">
              {criteria.map((criterion) => (
                <li key={criterion.label}>
                  <div className="flex items-baseline justify-between text-xs">
                    <span className="text-ink">{criterion.label}</span>
                    <span className="mono font-semibold text-primary">{criterion.weight.toFixed(2)}</span>
                  </div>
                  <div className="mt-1.5 h-1.5 rounded bg-canvas">
                    <div className="h-1.5 rounded bg-accent" style={{ width: `${criterion.weight * 100}%` }} />
                  </div>
                </li>
              ))}
            </ul>
            <p className="mt-4 text-xs leading-relaxed text-ink-muted">
              Capacity is a hard filter, not a scored criterion. Accessibility is scored for every exam and additionally
              enforced as a filter when the exam requires it.
            </p>
          </Panel>
        </div>

        <div className="flex flex-col gap-6">
          {complete ? (
            <>
              <Panel
                title={`Ranked rooms — ${exam.course_code}`}
                subtitle="TOPSIS closeness coefficient (higher is better)"
                bodyClassName="p-0"
                actions={exam.requires_accessibility ? <StatusBadge label="Step-free filter active" tone="warning" icon={<AccessibilityIcon className="h-3.5 w-3.5" />} /> : undefined}
              >
                {selectedExam.exam_id !== exam.exam_id && (
                  <p className="border-b border-line bg-warning-soft px-5 py-2 text-xs text-warning">
                    Showing results for {exam.course_code} — select {selectedExam.course_code} and re-run to rank rooms for it.
                  </p>
                )}
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-line text-left text-[11px] uppercase tracking-wide text-ink-faint">
                      <th className="px-5 py-2 font-medium">Rank</th>
                      <th className="px-5 py-2 font-medium">Room</th>
                      <th className="px-5 py-2 font-medium">Capacity use</th>
                      <th className="px-5 py-2 font-medium">Attributes</th>
                      <th className="px-5 py-2 font-medium">Score</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-line">
                    {rankings.map((ranking) => {
                      const room = rooms.find((item) => item.room_id === ranking.room_id);
                      // Live rankings can include rooms the static sample fixture doesn't know
                      // about (e.g. seeded straight into Mongo) — fall back to the live
                      // room-reference lookup for name/floor; capacity/AC/noise genuinely
                      // aren't available there (see roomReference comment above).
                      const refRoom = !room ? roomReference.find((item) => item.room_id === ranking.room_id) : null;
                      const utilisation = ranking.capacity_utilisation ?? (room ? Number(((exam.student_count / room.capacity) * 100).toFixed(1)) : null);
                      return (
                        <tr key={ranking.room_id} className={ranking.rank === 1 ? "bg-primary-soft/50" : ""}>
                          <td className="mono px-5 py-3 font-semibold text-primary">{ranking.rank}</td>
                          <td className="px-5 py-3">
                            <p className="font-medium text-ink">{room?.room_name ?? refRoom?.room_name ?? ranking.room_id}</p>
                            <p className="mono text-xs text-ink-muted">
                              {ranking.room_id} {room ? `· Floor ${room.floor} · ${room.capacity} seats` : refRoom ? `· Floor ${refRoom.floor}` : ""}
                            </p>
                          </td>
                          <td className="mono px-5 py-3 text-ink-muted">{utilisation != null ? `${utilisation}%` : "—"}</td>
                          <td className="px-5 py-3 text-xs text-ink-muted">
                            {room ? `${room.has_ac ? "AC" : "No AC"} · Noise ${room.noise_level} · Access ${room.accessibility_score}` : "—"}
                          </td>
                          <td className="px-5 py-3">
                            <div className="flex items-center gap-2">
                              <div className="h-1.5 w-24 rounded bg-canvas">
                                <div className="h-1.5 rounded bg-primary" style={{ width: `${ranking.score * 100}%` }} />
                              </div>
                              <span className="mono text-xs font-semibold text-ink">{ranking.score.toFixed(3)}</span>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </Panel>

              <MetricsBar metrics={run.data?.metrics ?? roomRankingMetrics} source={run.source} />

              <div className="grid gap-3 sm:grid-cols-2">
                <JsonPreview fileName="output_room_rankings.json" payload={rankings} />
                <JsonPreview fileName="output_room_reference.json" payload={roomReference} />
              </div>
            </>
          ) : (
            <Panel title="Output" subtitle="output_room_rankings.json">
              <EmptyState title="No ranking produced yet" description="Choose an exam request and run the ranker to score every eligible room with AHP-weighted TOPSIS." />
            </Panel>
          )}
        </div>
      </div>
    </div>
  );
}
