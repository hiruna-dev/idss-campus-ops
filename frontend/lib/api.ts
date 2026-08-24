const GATEWAY_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export interface Metrics {
  algorithm_used?: string;
  execution_time_ms?: number;
  memory_allocated_kb?: number;
  hard_constraint_violations?: number;
  soft_score?: number;
  status?: string;
}

export interface ApiResponse<T> {
  data: T;
  metrics: Metrics;
}

async function post<T>(path: string, body: unknown): Promise<ApiResponse<T>> {
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

async function get<T>(path: string): Promise<ApiResponse<T>> {
  const res = await fetch(`${GATEWAY_URL}${path}`);
  if (!res.ok) {
    throw new Error(`${path} failed: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

export const api = {
  task1: {
    route: (dispatchOrders: unknown) =>
      post("/api/task1/route", dispatchOrders),
    getRoute: (dispatchId: string) =>
      get(`/api/task1/routes/${dispatchId}`),
    benchmark: () => get("/api/task1/benchmark"),
  },
  task2: {
    assign: (schedule: unknown) =>
      post("/api/task2/assign", schedule),
    getRoster: (examId: string) =>
      get(`/api/task2/roster/${examId}`),
    benchmark: () => get("/api/task2/benchmark"),
  },
  task3: {
    detect: (enrollments: unknown) =>
      post("/api/task3/detect", enrollments),
    getConflictGraph: () => get("/api/task3/conflict-graph"),
    benchmark: () => get("/api/task3/benchmark"),
  },
  task4: {
    rank: (examAndRooms: unknown) =>
      post("/api/task4/rank", examAndRooms),
    getRankings: (examId: string) =>
      get(`/api/task4/rankings/${examId}`),
    benchmark: () => get("/api/task4/benchmark"),
  },
  task5: {
    generate: (input: unknown) =>
      post("/api/task5/generate", input),
    getSchedule: () => get("/api/task5/schedule"),
    benchmark: () => get("/api/task5/benchmark"),
  },
};
