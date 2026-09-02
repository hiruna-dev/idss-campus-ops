"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import MetricsBar from "@/components/MetricsBar";
import { dummyConflictGraph, dummyExams, dummyMetrics } from "@/lib/types/dummyData";
import { exportJson } from "@/lib/utils/exportJson";
import { api } from "@/lib/api/index";
import { Play, Download, GitBranch, Loader2, Users, Layers } from "lucide-react";

// Position nodes in a circle for the conflict graph
function getNodePositions(vertices) {
  const cx = 200, cy = 110, radius = 75;
  return vertices.map((v, i) => {
    const angle = (2 * Math.PI * i) / vertices.length - Math.PI / 2;
    return { id: v, x: cx + radius * Math.cos(angle), y: cy + radius * Math.sin(angle) };
  });
}

export default function Task3Page() {
  const [result, setResult] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleRun() {
    setLoading(true);
    try {
      const data = await api.task3.detect(dummyExams);
      setResult(data);
      setMetrics(data.metrics || dummyMetrics.task3);
    } catch {
      setResult(dummyConflictGraph);
      setMetrics(dummyMetrics.task3);
    } finally {
      setLoading(false);
    }
  }

  const graphData = result || dummyConflictGraph;
  const nodePositions = getNodePositions(graphData.vertices);
  const examLookup = Object.fromEntries(dummyExams.map(e => [e.exam_id, e.course_code]));

  // Color palette for session groups
  const sessionColors = [
    { bg: "bg-blue-500/15", text: "text-blue-400", stroke: "#60a5fa" },
    { bg: "bg-emerald-500/15", text: "text-emerald-400", stroke: "#34d399" },
    { bg: "bg-amber-500/15", text: "text-amber-400", stroke: "#fbbf24" },
    { bg: "bg-purple-500/15", text: "text-purple-400", stroke: "#a78bfa" },
  ];

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Clash Detection</h1>
        <p className="text-muted-foreground mt-1">Identify scheduling conflicts where students are enrolled in multiple exams at the same time.</p>
      </div>

      <MetricsBar metrics={metrics} />
      
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Input Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="w-5 h-5 text-primary" />
              Student Enrollments
            </CardTitle>
            <CardDescription>Exams and how many students are enrolled in each.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              {dummyExams.map((exam) => (
                <div key={exam.exam_id} className="flex items-center justify-between py-2 border-b border-border last:border-0">
                  <div className="flex items-center gap-3">
                    <Badge variant="outline">{exam.exam_id}</Badge>
                    <span className="font-medium text-sm">{exam.course_code}</span>
                  </div>
                  <span className="text-sm text-muted-foreground">{exam.student_count} students</span>
                </div>
              ))}
            </div>
            <Button className="w-full" onClick={handleRun} disabled={loading}>
              {loading ? <Loader2 className="w-4 h-4 animate-spin mr-2" /> : <Play className="w-4 h-4 mr-2" />}
              {loading ? "Detecting Clashes..." : "Detect Clashes"}
            </Button>
          </CardContent>
        </Card>
        
        {/* Output Card — Conflict Graph */}
        <Card>
          <CardHeader>
            <div className="flex flex-row items-center justify-between">
              <div className="space-y-1">
                <CardTitle className="flex items-center gap-2">
                  <GitBranch className="w-5 h-5 text-primary" />
                  Conflict Map
                </CardTitle>
                <CardDescription>Lines connect exams that share students — they cannot run at the same time.</CardDescription>
              </div>
              <div className="flex flex-col items-end">
                <span className="text-xs text-muted-foreground uppercase font-semibold">Min Sessions</span>
                <span className="text-3xl font-bold text-primary">{graphData.minimum_sessions}</span>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="aspect-video bg-card border border-border rounded-lg relative flex items-center justify-center p-4">
              <svg width="100%" height="100%" viewBox="0 0 400 220" className="select-none">
                {/* Edges */}
                {graphData.edges.map(([from, to], i) => {
                  const a = nodePositions.find(n => n.id === from);
                  const b = nodePositions.find(n => n.id === to);
                  if (!a || !b) return null;
                  return (
                    <line key={i} x1={a.x} y1={a.y} x2={b.x} y2={b.y}
                      stroke="var(--primary)" strokeWidth="2" opacity="0.6"
                    />
                  );
                })}

                {/* Nodes */}
                {nodePositions.map((node) => (
                  <g key={node.id}>
                    <circle cx={node.x} cy={node.y} r={22} fill="var(--primary)" />
                    <text x={node.x} y={node.y - 3} textAnchor="middle" className="text-[8px] font-bold fill-primary-foreground">
                      {examLookup[node.id] || node.id}
                    </text>
                    <text x={node.x} y={node.y + 8} textAnchor="middle" className="text-[7px] fill-primary-foreground opacity-70">
                      {node.id}
                    </text>
                  </g>
                ))}
              </svg>
            </div>
            
            <div className="mt-4 grid grid-cols-2 gap-3">
              <div className="bg-muted/50 p-3 rounded-lg border border-border flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Graph Density</span>
                <span className="font-semibold">{graphData.graph_density}</span>
              </div>
              <div className="bg-muted/50 p-3 rounded-lg border border-border flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Conflict Edges</span>
                <span className="font-semibold">{graphData.edges.length}</span>
              </div>
            </div>
          </CardContent>
          <CardFooter className="border-t pt-4">
            <Button variant="outline" className="w-full" onClick={() => exportJson(graphData, "output_conflict_graph.json")}>
              <Download className="w-4 h-4 mr-2" /> Download Conflict Data
            </Button>
          </CardFooter>
        </Card>
      </div>

      {/* Session Groups */}
      {graphData.session_groups && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Layers className="w-5 h-5 text-primary" />
              Session Groups
            </CardTitle>
            <CardDescription>Exams grouped into non-conflicting sessions. Exams in the same group can run simultaneously.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-4">
              {graphData.session_groups.map((group, i) => {
                const color = sessionColors[i % sessionColors.length];
                return (
                  <div key={i} className={`rounded-lg border border-border p-4 ${color.bg}`}>
                    <div className="flex items-center gap-2 mb-3">
                      <span className={`text-sm font-semibold ${color.text}`}>Session {group.session}</span>
                      <Badge variant="outline" className="text-xs">{group.exams.length} exam{group.exams.length !== 1 ? "s" : ""}</Badge>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {group.exams.length > 0 ? group.exams.map((examId) => (
                        <Badge key={examId} variant="secondary" className="text-xs">
                          {examLookup[examId] || examId}
                        </Badge>
                      )) : (
                        <span className="text-xs text-muted-foreground italic">No exams in this session</span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
