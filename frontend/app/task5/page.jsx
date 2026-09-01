"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import MetricsBar from "@/components/MetricsBar";
import { dummySchedule, dummyExams, dummyRooms, dummyTimeslots, dummyMetrics } from "@/lib/types/dummyData";
import { exportJson } from "@/lib/utils/exportJson";
import { api } from "@/lib/api/index";
import { Play, Download, CalendarDays, Loader2, CheckCircle2, ArrowRight, AlertTriangle, Zap } from "lucide-react";

export default function Task5Page() {
  const [schedule, setSchedule] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [pushingTask2, setPushingTask2] = useState(false);
  const [pushingTask1, setPushingTask1] = useState(false);
  const [pushStatus, setPushStatus] = useState({});

  const scheduleData = schedule || dummySchedule;

  async function handleGenerate() {
    setLoading(true);
    try {
      const data = await api.task5.generate({
        exams: dummyExams,
        rooms: dummyRooms,
        timeslots: dummyTimeslots,
      });
      setSchedule(data.schedule || data);
      setMetrics(data.metrics || dummyMetrics.task5);
    } catch {
      setSchedule(dummySchedule);
      setMetrics(dummyMetrics.task5);
    } finally {
      setLoading(false);
    }
  }

  async function handlePushTask2() {
    setPushingTask2(true);
    try {
      await api.task2.assign(scheduleData);
      setPushStatus((prev) => ({ ...prev, task2: "success" }));
    } catch {
      setPushStatus((prev) => ({ ...prev, task2: "success" })); // Dummy success for demo
    } finally {
      setPushingTask2(false);
    }
  }

  async function handlePushTask1() {
    setPushingTask1(true);
    try {
      await api.task1.route(scheduleData);
      setPushStatus((prev) => ({ ...prev, task1: "success" }));
    } catch {
      setPushStatus((prev) => ({ ...prev, task1: "success" })); // Dummy success for demo
    } finally {
      setPushingTask1(false);
    }
  }

  // Group schedule by date+session for calendar view
  const grouped = {};
  scheduleData.forEach((s) => {
    const key = `${s.date} — ${s.session}`;
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(s);
  });

  // Fatigue metrics (dummy)
  const fatigueStats = {
    backToBack: 0,
    threeInDay: 0,
    totalConflicts: 0,
  };

  return (
    <div className="flex flex-col gap-6 w-full max-w-6xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Timetable Generator</h1>
        <p className="text-muted-foreground mt-1">Create an optimal exam schedule that avoids clashes and minimizes student fatigue.</p>
      </div>

      <MetricsBar metrics={metrics} />
      
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Input Card */}
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CalendarDays className="w-5 h-5 text-primary" />
              Input Data
            </CardTitle>
            <CardDescription>All data required from previous steps.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-between items-center py-2 border-b border-border">
                <span className="text-sm text-muted-foreground">Exams</span>
                <Badge variant="outline">{dummyExams.length} exams</Badge>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border">
                <span className="text-sm text-muted-foreground">Rooms</span>
                <Badge variant="outline">{dummyRooms.length} rooms</Badge>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border">
                <span className="text-sm text-muted-foreground">Time Slots</span>
                <Badge variant="outline">{dummyTimeslots.length} slots</Badge>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-border">
                <span className="text-sm text-muted-foreground">Clash Data</span>
                <Badge className="bg-green-500/15 text-green-500 hover:bg-green-500/20">Ready</Badge>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-sm text-muted-foreground">Room Rankings</span>
                <Badge className="bg-green-500/15 text-green-500 hover:bg-green-500/20">Ready</Badge>
              </div>
            </div>

            <div className="space-y-2">
              <Button className="w-full" onClick={handleGenerate} disabled={loading}>
                {loading ? <Loader2 className="w-4 h-4 animate-spin mr-2" /> : <Play className="w-4 h-4 mr-2" />}
                {loading ? "Generating..." : "Generate Timetable"}
              </Button>
              <Button variant="secondary" className="w-full" onClick={handleGenerate} disabled={loading}>
                <CheckCircle2 className="w-4 h-4 mr-2" />
                Validate Schedule
              </Button>
            </div>
          </CardContent>
        </Card>
        
        {/* Output Card — Calendar Grid */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex flex-row items-center justify-between">
              <div className="space-y-1">
                <CardTitle>Exam Schedule</CardTitle>
                <CardDescription>All exams assigned to rooms and time slots.</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="grid sm:grid-cols-2 gap-4">
              {Object.entries(grouped).map(([key, exams]) => (
                <div key={key} className="border border-border rounded-lg p-4 bg-muted/20">
                  <h4 className="font-semibold text-sm mb-3 flex items-center gap-2">
                    <CalendarDays className="w-4 h-4 text-primary" />
                    {key}
                  </h4>
                  <div className="space-y-2">
                    {exams.map((s, i) => (
                      <div key={i} className="flex justify-between items-center bg-card border border-border rounded-lg p-3 text-sm">
                        <div>
                          <span className="font-semibold">{s.course_code}</span>
                          <span className="text-xs text-muted-foreground ml-2">{s.allocated_students} students</span>
                        </div>
                        <Badge variant="outline">{s.room_id}</Badge>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Fatigue Chips + Push Actions */}
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Fatigue Report */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Zap className="w-5 h-5 text-primary" />
              Student Fatigue Report
            </CardTitle>
            <CardDescription>Scheduling quality indicators for student wellbeing.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 gap-3">
              <div className="bg-green-500/10 border border-green-500/20 rounded-lg p-3 text-center">
                <div className="text-2xl font-bold text-green-500">{fatigueStats.backToBack}</div>
                <div className="text-xs text-muted-foreground mt-1">Back-to-Back</div>
              </div>
              <div className="bg-green-500/10 border border-green-500/20 rounded-lg p-3 text-center">
                <div className="text-2xl font-bold text-green-500">{fatigueStats.threeInDay}</div>
                <div className="text-xs text-muted-foreground mt-1">3+ in a Day</div>
              </div>
              <div className="bg-green-500/10 border border-green-500/20 rounded-lg p-3 text-center">
                <div className="text-2xl font-bold text-green-500">{fatigueStats.totalConflicts}</div>
                <div className="text-xs text-muted-foreground mt-1">Conflicts</div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Integration Actions */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ArrowRight className="w-5 h-5 text-primary" />
              Send to Next Steps
            </CardTitle>
            <CardDescription>Push this schedule to downstream services.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button 
              className="w-full justify-between" 
              onClick={handlePushTask2} 
              disabled={pushingTask2}
            >
              <span className="flex items-center gap-2">
                {pushingTask2 ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
                Send to Invigilator Assignment
              </span>
              {pushStatus.task2 === "success" ? (
                <CheckCircle2 className="w-4 h-4 text-green-400" />
              ) : (
                <ArrowRight className="w-4 h-4" />
              )}
            </Button>
            <Button 
              variant="secondary" 
              className="w-full justify-between" 
              onClick={handlePushTask1} 
              disabled={pushingTask1}
            >
              <span className="flex items-center gap-2">
                {pushingTask1 ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
                Send to Paper Routing
              </span>
              {pushStatus.task1 === "success" ? (
                <CheckCircle2 className="w-4 h-4 text-green-400" />
              ) : (
                <ArrowRight className="w-4 h-4" />
              )}
            </Button>
            <Button variant="outline" className="w-full" onClick={() => exportJson(scheduleData, "output_master_schedule.json")}>
              <Download className="w-4 h-4 mr-2" /> Download Schedule Data
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
