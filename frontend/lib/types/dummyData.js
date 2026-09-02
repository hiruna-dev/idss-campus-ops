// ─── Exams ───
export const dummyExams = [
  { exam_id: "EX_101", course_code: "PDSA201", student_count: 48, requires_accessibility: false },
  { exam_id: "EX_102", course_code: "NET102", student_count: 120, requires_accessibility: true },
  { exam_id: "EX_103", course_code: "CS301", student_count: 35, requires_accessibility: false },
  { exam_id: "EX_104", course_code: "MATH101", student_count: 85, requires_accessibility: true },
];

// ─── Students ───
export const dummyStudents = [
  { student_id: "STU_001", enrolled_courses: ["PDSA201", "NET102"] },
  { student_id: "STU_002", enrolled_courses: ["PDSA201", "CS301"] },
  { student_id: "STU_003", enrolled_courses: ["NET102", "MATH101"] },
  { student_id: "STU_004", enrolled_courses: ["CS301", "MATH101"] },
  { student_id: "STU_005", enrolled_courses: ["PDSA201"] },
  { student_id: "STU_006", enrolled_courses: ["NET102", "CS301"] },
  { student_id: "STU_007", enrolled_courses: ["MATH101"] },
  { student_id: "STU_008", enrolled_courses: ["PDSA201", "MATH101"] },
  { student_id: "STU_009", enrolled_courses: ["NET102"] },
  { student_id: "STU_010", enrolled_courses: ["CS301", "NET102"] },
];

// ─── Rooms ───
export const dummyRooms = [
  { room_id: "R101", capacity: 60, has_ac: true, noise_level: 4, is_accessible: true },
  { room_id: "R102", capacity: 150, has_ac: true, noise_level: 2, is_accessible: false },
  { room_id: "R103", capacity: 40, has_ac: false, noise_level: 5, is_accessible: true },
  { room_id: "HALL_A", capacity: 300, has_ac: true, noise_level: 3, is_accessible: true },
];

// ─── Timeslots ───
export const dummyTimeslots = [
  { slot_id: "SLOT_01", date: "2026-08-20", session: "Morning" },
  { slot_id: "SLOT_02", date: "2026-08-20", session: "Afternoon" },
  { slot_id: "SLOT_03", date: "2026-08-21", session: "Morning" },
  { slot_id: "SLOT_04", date: "2026-08-21", session: "Afternoon" },
];

// ─── Master Schedule (Task 5 output) ───
export const dummySchedule = [
  { exam_id: "EX_101", course_code: "PDSA201", date: "2026-08-20", session: "Morning", room_id: "R101", canonical_room_id: "ROOM_R101", allocated_students: 48, required_invigilators: 2 },
  { exam_id: "EX_102", course_code: "NET102", date: "2026-08-20", session: "Afternoon", room_id: "HALL_A", canonical_room_id: "ROOM_HALL_A", allocated_students: 120, required_invigilators: 4 },
  { exam_id: "EX_103", course_code: "CS301", date: "2026-08-21", session: "Morning", room_id: "R103", canonical_room_id: "ROOM_R103", allocated_students: 35, required_invigilators: 1 },
  { exam_id: "EX_104", course_code: "MATH101", date: "2026-08-21", session: "Afternoon", room_id: "HALL_A", canonical_room_id: "ROOM_HALL_A", allocated_students: 85, required_invigilators: 3 },
];

// ─── Proctor Rosters (Task 2 output) ───
export const dummyRosters = [
  { allocation_id: "ALOC_001", exam_id: "EX_101", room_id: "R101", assigned_invigilators: ["Dr. Smith", "Prof. Jones"] },
  { allocation_id: "ALOC_002", exam_id: "EX_102", room_id: "HALL_A", assigned_invigilators: ["Dr. Adams", "Mr. Clark", "Ms. Davis", "Prof. Evans"] },
  { allocation_id: "ALOC_003", exam_id: "EX_103", room_id: "R103", assigned_invigilators: ["Dr. Smith"] },
  { allocation_id: "ALOC_004", exam_id: "EX_104", room_id: "HALL_A", assigned_invigilators: ["Prof. Jones", "Mr. Clark", "Ms. Davis"] },
];

// ─── Delivery Routes (Task 1 output) ───
export const dummyRoutes = {
  dispatch_id: "DSP_001",
  path_sequence: ["VAULT_G01", "HALLWAY_G_EAST", "ELEVATOR_G", "ELEVATOR_1", "HALLWAY_1_NORTH", "R101"],
  step_free_verified: true,
  total_distance_m: 47.5,
  estimated_time_min: 3.2,
};

// ─── Conflict Graph (Task 3 output) ───
export const dummyConflictGraph = {
  generation_timestamp: "2026-08-18T14:30:00Z",
  vertices: ["EX_101", "EX_102", "EX_103", "EX_104"],
  edges: [
    ["EX_101", "EX_102"],
    ["EX_102", "EX_104"],
    ["EX_101", "EX_103"],
    ["EX_103", "EX_104"],
  ],
  graph_density: 0.67,
  minimum_sessions: 3,
  session_groups: [
    { session: 1, exams: ["EX_101", "EX_104"] },
    { session: 2, exams: ["EX_102", "EX_103"] },
    { session: 3, exams: [] },
  ],
};

// ─── Room Rankings (Task 4 output) ───
export const dummyRankings = [
  { exam_id: "EX_101", room_id: "R101", rank: 1, score: 0.87, meets_hard_constraints: true },
  { exam_id: "EX_101", room_id: "HALL_A", rank: 2, score: 0.72, meets_hard_constraints: true },
  { exam_id: "EX_101", room_id: "R103", rank: 3, score: 0.65, meets_hard_constraints: true },
  { exam_id: "EX_101", room_id: "R102", rank: 4, score: 0.41, meets_hard_constraints: false },
];

// ─── Building Graph — Node positions for SVG floor map (Task 1) ───
export const buildingGraphNodes = [
  { id: "VAULT_G01", label: "Paper Vault", x: 50, y: 170, floor: 0 },
  { id: "HALLWAY_G_EAST", label: "Ground Hallway", x: 150, y: 170, floor: 0 },
  { id: "ELEVATOR_G", label: "Elevator (G)", x: 250, y: 170, floor: 0 },
  { id: "STAIRCASE_G", label: "Stairs (G)", x: 250, y: 120, floor: 0 },
  { id: "ELEVATOR_1", label: "Elevator (1F)", x: 250, y: 50, floor: 1 },
  { id: "STAIRCASE_1", label: "Stairs (1F)", x: 250, y: 100, floor: 1 },
  { id: "HALLWAY_1_NORTH", label: "1F Hallway", x: 150, y: 50, floor: 1 },
  { id: "R101", label: "Room 101", x: 50, y: 50, floor: 1 },
  { id: "R102", label: "Room 102", x: 50, y: 100, floor: 1 },
  { id: "R103", label: "Room 103", x: 350, y: 50, floor: 1 },
  { id: "HALL_A", label: "Main Hall A", x: 350, y: 170, floor: 0 },
];

export const buildingGraphEdges = [
  { from: "VAULT_G01", to: "HALLWAY_G_EAST", distance: 12 },
  { from: "HALLWAY_G_EAST", to: "ELEVATOR_G", distance: 10 },
  { from: "HALLWAY_G_EAST", to: "HALL_A", distance: 20 },
  { from: "ELEVATOR_G", to: "ELEVATOR_1", distance: 5, type: "elevator" },
  { from: "STAIRCASE_G", to: "STAIRCASE_1", distance: 8, type: "stairs" },
  { from: "ELEVATOR_G", to: "STAIRCASE_G", distance: 3 },
  { from: "ELEVATOR_1", to: "HALLWAY_1_NORTH", distance: 10 },
  { from: "ELEVATOR_1", to: "STAIRCASE_1", distance: 3 },
  { from: "HALLWAY_1_NORTH", to: "R101", distance: 8 },
  { from: "HALLWAY_1_NORTH", to: "R102", distance: 10 },
  { from: "ELEVATOR_1", to: "R103", distance: 12 },
];

// ─── Dummy Metrics (returned after algorithm runs) ───
export const dummyMetrics = {
  task1: { algorithm_used: "A* Search", execution_time_ms: 1.14, memory_allocated_kb: 24, hard_constraint_violations: 0, status: "OPTIMAL" },
  task2: { algorithm_used: "Hungarian (Kuhn-Munkres)", execution_time_ms: 3.52, memory_allocated_kb: 48, hard_constraint_violations: 0, status: "OPTIMAL" },
  task3: { algorithm_used: "DSATUR", execution_time_ms: 0.87, memory_allocated_kb: 16, hard_constraint_violations: 0, status: "OPTIMAL" },
  task4: { algorithm_used: "AHP + TOPSIS", execution_time_ms: 2.13, memory_allocated_kb: 32, hard_constraint_violations: 0, status: "OPTIMAL" },
  task5: { algorithm_used: "Genetic Algorithm (Hybrid)", execution_time_ms: 142, memory_allocated_kb: 256, hard_constraint_violations: 0, status: "OPTIMAL" },
};
