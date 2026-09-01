import { dummyExams, dummyStudents, dummyRooms, dummyTimeslots } from "@/lib/types/dummyData";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { BookOpen, Users, Building2, Calendar, CheckCircle2, ArrowRight } from "lucide-react";
import Link from "next/link";

const statCards = [
  { label: "Total Exams", icon: BookOpen, color: "text-primary" },
  { label: "Total Students", icon: Users, color: "text-blue-400" },
  { label: "Available Rooms", icon: Building2, color: "text-emerald-400" },
  { label: "Time Slots", icon: Calendar, color: "text-amber-400" },
];

const quickActions = [
  { href: "/task3", label: "Detect Clashes", description: "Find student scheduling conflicts", step: 1 },
  { href: "/task4", label: "Rank Rooms", description: "Score rooms by suitability", step: 2 },
  { href: "/task5", label: "Generate Timetable", description: "Build the exam schedule", step: 3 },
  { href: "/task2", label: "Assign Invigilators", description: "Distribute supervisor duties", step: 4 },
  { href: "/task1", label: "Plan Paper Routes", description: "Route exam papers to halls", step: 5 },
];

export default function DashboardPage() {
  const totalExams = dummyExams.length;
  const totalStudents = dummyStudents.length;
  const totalRooms = dummyRooms.length;
  const totalSlots = dummyTimeslots.length;
  const counts = [totalExams, totalStudents, totalRooms, totalSlots];

  return (
    <div className="flex flex-col gap-8 w-full max-w-6xl mx-auto">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground mt-1">Overview of your university exam operations.</p>
      </div>
      
      {/* Stat Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {statCards.map((card, i) => {
          const Icon = card.icon;
          return (
            <Card key={card.label}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">{card.label}</CardTitle>
                <Icon className={`w-5 h-5 ${card.color}`} />
              </CardHeader>
              <CardContent>
                <div className="text-4xl font-bold">{counts[i]}</div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Timetable Status */}
      <Card className="border-primary/20 bg-primary/5">
        <CardHeader className="flex flex-row items-center gap-3 pb-2">
          <CheckCircle2 className="w-5 h-5 text-primary" />
          <div>
            <CardTitle>Timetable Status</CardTitle>
            <CardDescription>The exam timetable has not been generated yet. Follow the steps below to create one.</CardDescription>
          </div>
        </CardHeader>
      </Card>

      {/* Recommended Workflow */}
      <div>
        <h2 className="text-lg font-semibold mb-4">Recommended Workflow</h2>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          {quickActions.map((action) => (
            <Link key={action.href} href={action.href}>
              <Card className="h-full hover:border-primary/50 hover:bg-primary/5 transition-all duration-200 cursor-pointer group">
                <CardContent className="pt-5 pb-4 px-4">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-6 h-6 rounded-full bg-primary/15 text-primary text-xs font-bold flex items-center justify-center">
                      {action.step}
                    </span>
                    <span className="font-semibold text-sm group-hover:text-primary transition-colors">{action.label}</span>
                  </div>
                  <p className="text-xs text-muted-foreground">{action.description}</p>
                  <ArrowRight className="w-4 h-4 text-primary opacity-0 group-hover:opacity-100 transition-opacity mt-2" />
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
