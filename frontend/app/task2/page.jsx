"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import MetricsBar from "@/components/MetricsBar";
import { dummyRosters, dummySchedule, dummyMetrics } from "@/lib/types/dummyData";
import { exportJson } from "@/lib/utils/exportJson";
import { api } from "@/lib/api/index";
import { Play, Download, Users, Loader2, BarChart3 } from "lucide-react";

export default function Task2Page() {
  const [result, setResult] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleRun() {
    setLoading(true);
    try {
      const data = await api.task2.assign(dummySchedule);
      setResult(data.rosters || data);
      setMetrics(data.metrics || dummyMetrics.task2);
    } catch {
      setResult(dummyRosters);
      setMetrics(dummyMetrics.task2);
    } finally {
      setLoading(false);
    }
  }

  const rosterData = result || dummyRosters;

  // Calculate fairness: how evenly are invigilators distributed?
  const invigilatorCounts = {};
  rosterData.forEach((r) => {
    r.assigned_invigilators.forEach((name) => {
      invigilatorCounts[name] = (invigilatorCounts[name] || 0) + 1;
    });
  });
  const counts = Object.values(invigilatorCounts);
  const avgLoad = counts.length ? (counts.reduce((a, b) => a + b, 0) / counts.length).toFixed(1) : 0;

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Invigilator Assignment</h1>
        <p className="text-muted-foreground mt-1">Fairly distribute supervisors across all exam sessions using the Hungarian algorithm.</p>
      </div>

      <MetricsBar metrics={metrics} />
      
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Input Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="w-5 h-5 text-primary" />
              Exam Schedule Input
            </CardTitle>
            <CardDescription>This data comes from the Timetable Generator (Task 5).</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Session</TableHead>
                  <TableHead>Room</TableHead>
                  <TableHead className="text-right">Needed</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dummySchedule.map((s, i) => (
                  <TableRow key={i}>
                    <TableCell className="font-medium">{s.course_code}</TableCell>
                    <TableCell>{s.session}</TableCell>
                    <TableCell><Badge variant="outline">{s.room_id}</Badge></TableCell>
                    <TableCell className="text-right">{s.required_invigilators}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <Button className="w-full" onClick={handleRun} disabled={loading}>
              {loading ? <Loader2 className="w-4 h-4 animate-spin mr-2" /> : <Play className="w-4 h-4 mr-2" />}
              {loading ? "Assigning..." : "Assign Invigilators"}
            </Button>
          </CardContent>
        </Card>
        
        {/* Output Card */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <div className="space-y-1">
              <CardTitle>Proctor Roster</CardTitle>
              <CardDescription>Who supervises which exam.</CardDescription>
            </div>
            <Badge className="bg-primary/15 text-primary hover:bg-primary/20">
              <BarChart3 className="w-3.5 h-3.5 mr-1" />
              Avg Load: {avgLoad}
            </Badge>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Exam</TableHead>
                  <TableHead>Room</TableHead>
                  <TableHead>Assigned Invigilators</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rosterData.map((roster, index) => (
                  <TableRow key={index}>
                    <TableCell className="font-medium">{roster.exam_id}</TableCell>
                    <TableCell><Badge variant="outline">{roster.room_id}</Badge></TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1.5">
                        {roster.assigned_invigilators.map((name, i) => (
                          <Badge key={i} variant="secondary" className="text-xs">{name}</Badge>
                        ))}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
          <CardFooter className="border-t pt-4">
            <Button variant="outline" className="w-full" onClick={() => exportJson(rosterData, "output_proctor_roster.json")}>
              <Download className="w-4 h-4 mr-2" /> Download Roster Data
            </Button>
          </CardFooter>
        </Card>
      </div>

      {/* Fairness Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Workload Distribution</CardTitle>
          <CardDescription>How many exams each invigilator is assigned to.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {Object.entries(invigilatorCounts).map(([name, count]) => (
              <div key={name} className="flex items-center justify-between bg-muted/50 rounded-lg px-3 py-2 border border-border">
                <span className="text-sm font-medium truncate mr-2">{name}</span>
                <Badge variant="outline" className="shrink-0">{count} exam{count > 1 ? "s" : ""}</Badge>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
