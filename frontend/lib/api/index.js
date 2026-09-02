export const GATEWAY_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

async function post(path, body) {
  const res = await fetch(`${GATEWAY_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new Error(`${path} failed: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

async function get(path) {
  const res = await fetch(`${GATEWAY_URL}${path}`);
  if (!res.ok) {
    throw new Error(`${path} failed: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

export const api = {
  task1: {
    route: (dispatchOrders) => post("/api/task1/route", dispatchOrders),
    getRoute: (dispatchId) => get(`/api/task1/routes/${dispatchId}`),
    getDispatchOrders: () => get("/api/task1/dispatch-orders"),
    getBuildingGraph: () => get("/api/task1/building-graph"),
    benchmark: () => get("/api/task1/benchmark"),
  },
  task2: {
    assign: (schedule) => post("/api/task2/assign", schedule),
    getRoster: (examId) => get(`/api/task2/roster/${examId}`),
    getInvigilators: () => get("/api/task2/invigilators"),
    benchmark: () => get("/api/task2/benchmark"),
  },
  task3: {
    detect: (enrollments) => post("/api/task3/detect", enrollments),
    getConflictGraph: () => get("/api/task3/conflict-graph"),
    getExams: () => get("/api/task3/exams"),
    getEnrollments: () => get("/api/task3/enrollments"),
    benchmark: () => get("/api/task3/benchmark"),
  },
  task4: {
    rank: (examAndRooms) => post("/api/task4/rank", examAndRooms),
    getRankings: (examId) => get(`/api/task4/rankings/${examId}`),
    getRoomReference: () => get("/api/task4/room-reference"),
    benchmark: () => get("/api/task4/benchmark"),
  },
  task5: {
    generate: (input) => post("/api/task5/generate", input),
    getSchedule: () => get("/api/task5/schedule"),
    benchmark: () => get("/api/task5/benchmark"),
  },
};
