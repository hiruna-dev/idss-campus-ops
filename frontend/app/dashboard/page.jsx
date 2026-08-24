export default function DashboardPage() {
  const stats = [
    { label: "Total Exams", value: "—" },
    { label: "Total Students", value: "—" },
    { label: "Total Rooms", value: "—" },
    { label: "Timetable Status", value: "Not Generated" },
  ];

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-primary">Dashboard</h2>
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        {stats.map((stat) => (
          <div
            key={stat.label}
            className="rounded-lg bg-card p-4 shadow"
          >
            <p className="text-sm text-gray-500">{stat.label}</p>
            <p className="mt-1 text-2xl font-bold text-primary">
              {stat.value}
            </p>
          </div>
        ))}
      </div>
      <div className="rounded-lg bg-card p-6 shadow">
        <h3 className="font-semibold text-primary">System Overview</h3>
        <p className="mt-2 text-sm text-gray-600">
          Use the navigation tabs above to access each module. Run algorithms
          in order: Task 3 (Clash Detection) &amp; Task 4 (Room Ranking) →
          Task 5 (Timetable) → Task 2 (Invigilator) → Task 1 (Routing).
        </p>
      </div>
    </div>
  );
}
