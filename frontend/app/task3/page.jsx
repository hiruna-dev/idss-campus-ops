"use client";

import { useEffect, useState } from "react";
import { Panel } from "@/components/Panel";
import { ModuleHeader } from "@/components/ModuleHeader";
import MetricsBar from "@/components/MetricsBar";
import { RunButton } from "@/components/RunButton";
import { EmptyState } from "@/components/EmptyState";
import { JsonPreview } from "@/components/JsonPreview";
import { StatusBadge } from "@/components/StatusBadge";
import { useModuleRun } from "@/contexts/PipelineContext";
import { moduleById } from "@/lib/modules";
import { exams as sampleExams, studentEnrollments as sampleStudentEnrollments, totalStudents } from "@/lib/data/registry";
import { clashAnalysis as sampleClashAnalysis, clashMetrics, conflictGraph as sampleConflictGraph } from "@/lib/data/outputs";
import { api } from "@/lib/api/index";

const sessionColors = ["#0F4C75", "#3282B8", "#EF6C00", "#2E7D32"];

function ConflictGraphCanvas({ graph, selected, onSelect }) {
  const width = 620;
  const height = 380;
  const radius = 138;
  const centre = { x: width / 2, y: height / 2 };

  const positions = new Map(
    graph.vertices.map((vertex, index) => {
      const angle = (index / graph.vertices.length) * Math.PI * 2 - Math.PI / 2;
      return [vertex.exam_id, { x: centre.x + radius * Math.cos(angle), y: centre.y + radius * Math.sin(angle) }];
    })
  );

  const isDimmed = (examId) =>
    selected !== null &&
    selected !== examId &&
    !graph.edges.some((edge) => (edge.exam_a === selected && edge.exam_b === examId) || (edge.exam_b === selected && edge.exam_a === examId));

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="h-auto w-full" role="img" aria-label="Exam conflict graph: vertices are exams, edges are shared students">
      {graph.edges.map((edge) => {
        const a = positions.get(edge.exam_a);
        const b = positions.get(edge.exam_b);
        if (!a || !b) return null;
        const active = selected === null || edge.exam_a === selected || edge.exam_b === selected;
        const mid = { x: (a.x + b.x) / 2, y: (a.y + b.y) / 2 };
        return (
          <g key={`${edge.exam_a}-${edge.exam_b}`} opacity={active ? 1 : 0.15}>
            <line x1={a.x} y1={a.y} x2={b.x} y2={b.y} stroke={selected && active ? "#3282B8" : "#B7C4CE"} strokeWidth={1 + edge.shared_students / 8} />
            {selected && active && (
              <text x={mid.x} y={mid.y - 4} textAnchor="middle" className="mono" fontSize="10" fill="#0F4C75">
                {edge.shared_students}
              </text>
            )}
          </g>
        );
      })}

      {graph.vertices.map((vertex) => {
        const position = positions.get(vertex.exam_id);
        if (!position) return null;
        const dimmed = isDimmed(vertex.exam_id);
        return (
          <g key={vertex.exam_id} opacity={dimmed ? 0.25 : 1} onMouseEnter={() => onSelect(vertex.exam_id)} onMouseLeave={() => onSelect(null)} className="cursor-pointer">
            <circle cx={position.x} cy={position.y} r={22} fill={sessionColors[vertex.session_index % sessionColors.length]} stroke="#FFFFFF" strokeWidth={selected === vertex.exam_id ? 3 : 2} />
            <text x={position.x} y={position.y + 3} textAnchor="middle" fontSize="10" fontWeight="600" fill="#FFFFFF">
              {vertex.degree}
            </text>
            <text x={position.x} y={position.y + 38} textAnchor="middle" className="mono" fontSize="11" fill="#1B262C">
              {vertex.course_code}
            </text>
          </g>
        );
      })}
    </svg>
  );
}

export default function Task3Page() {
  const module = moduleById("task3");
  const { run, execute } = useModuleRun("task3");
  const [selected, setSelected] = useState(null);
  const [liveExams, setLiveExams] = useState(null);
  const [liveEnrollments, setLiveEnrollments] = useState(null);
  const complete = run.state === "complete";

  useEffect(() => {
    let cancelled = false;
    Promise.allSettled([api.task3.getExams(), api.task3.getEnrollments()]).then(([examsResult, enrollmentsResult]) => {
      if (cancelled) return;
      if (examsResult.status === "fulfilled") setLiveExams(examsResult.value);
      if (enrollmentsResult.status === "fulfilled") setLiveEnrollments(enrollmentsResult.value);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const exams = liveExams ?? sampleExams;
  const studentEnrollments = liveEnrollments ?? sampleStudentEnrollments;
  const enrollmentCount = liveEnrollments?.length ?? totalStudents;

  const graph = run.data?.conflict_graph ?? run.data ?? sampleConflictGraph;
  const analysis = run.data?.clash_analysis ?? sampleClashAnalysis;
  // The live API returns session_groups as string[][] (course codes) and clash_pairs as
  // an array of edge objects; the sample fixture uses {session_index, exams} objects and
  // a plain count. Normalize both shapes here rather than assuming one.
  const sessionGroups = (analysis.session_groups ?? []).map((group, index) =>
    Array.isArray(group) ? { session_index: index, exams: group } : group
  );
  const clashPairsCount = Array.isArray(analysis.clash_pairs) ? analysis.clash_pairs.length : analysis.clash_pairs;

  return (
    <div>
      <ModuleHeader
        module={module}
        description="Builds an exam conflict graph from student enrolments — an edge exists whenever two exams share at least one student — then colours it with DSATUR to find the minimum number of clash-free sessions."
      />

      <div className="grid gap-6 px-8 py-7 xl:grid-cols-[340px_minmax(0,1fr)]">
        <div className="flex flex-col gap-6">
          <Panel
            title="Input"
            subtitle="Enrolments and exam registry"
            actions={
              <RunButton
                label="Detect clashes"
                running={run.state === "running"}
                onClick={() => execute(studentEnrollments, sampleConflictGraph)}
              />
            }
          >
            <dl className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Exams</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{exams.length}</dd>
              </div>
              <div>
                <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Enrolments</dt>
                <dd className="mono mt-1 text-xl font-semibold text-ink">{enrollmentCount}</dd>
              </div>
            </dl>
            <div className="mt-5 space-y-2">
              <JsonPreview fileName="input_student_enrollments.json" payload={studentEnrollments} />
              <JsonPreview fileName="input_exams.json" payload={exams} />
            </div>
          </Panel>

          {complete && (
            <Panel title="Clash-free session groups" bodyClassName="p-0">
              <ul className="divide-y divide-line">
                {sessionGroups.map((group) => (
                  <li key={group.session_index} className="flex gap-3 px-5 py-3">
                    <span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full" style={{ background: sessionColors[group.session_index % sessionColors.length] }} aria-hidden="true" />
                    <div>
                      <p className="text-xs font-semibold text-ink">Session {group.session_index + 1}</p>
                      <p className="mono mt-1 text-xs text-ink-muted">{group.exams.join(" · ")}</p>
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
                title="Conflict graph"
                subtitle="Node label = degree · colour = assigned session · edge weight = shared students"
                actions={<StatusBadge label={`Density ${graph.graph_density}`} tone="neutral" />}
              >
                <ConflictGraphCanvas graph={graph} selected={selected} onSelect={setSelected} />
                <p className="mt-2 text-center text-xs text-ink-muted">Hover a vertex to isolate its clash edges.</p>
              </Panel>

              <div className="grid gap-6 lg:grid-cols-[240px_minmax(0,1fr)]">
                <Panel title="Minimum sessions">
                  <p className="mono text-5xl font-semibold text-primary">{analysis.minimum_sessions}</p>
                  <p className="mt-2 text-xs text-ink-muted">
                    Bounds: lower {analysis.lower_bound} · upper {analysis.upper_bound}
                  </p>
                  <p className="mt-3 text-xs text-ink-muted">{clashPairsCount} clashing exam pairs must never share a date and session.</p>
                </Panel>

                <Panel title="Clash pairs" bodyClassName="p-0">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-line text-left text-[11px] uppercase tracking-wide text-ink-faint">
                        <th className="px-5 py-2 font-medium">Exam A</th>
                        <th className="px-5 py-2 font-medium">Exam B</th>
                        <th className="px-5 py-2 text-right font-medium">Shared students</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-line">
                      {graph.edges.map((edge) => (
                        <tr key={`${edge.exam_a}-${edge.exam_b}`}>
                          <td className="mono px-5 py-2 text-ink">{edge.exam_a}</td>
                          <td className="mono px-5 py-2 text-ink">{edge.exam_b}</td>
                          <td className="mono px-5 py-2 text-right font-medium text-ink">{edge.shared_students}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </Panel>
              </div>

              <MetricsBar metrics={run.data?.metrics ?? clashMetrics} source={run.source} />

              <div className="grid gap-3 sm:grid-cols-2">
                <JsonPreview fileName="output_conflict_graph.json" payload={graph} />
                <JsonPreview fileName="output_clash_analysis.json" payload={analysis} />
              </div>
            </>
          ) : (
            <Panel title="Output" subtitle="output_conflict_graph.json">
              <EmptyState title="No conflict graph yet" description="Run clash detection to build the conflict graph and colour it into clash-free sessions." />
            </Panel>
          )}
        </div>
      </div>
    </div>
  );
}
