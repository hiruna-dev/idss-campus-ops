// Static sample payloads — used only when the Gateway is unreachable, so every
// page still renders real-looking output. Never computed client-side.

export const GENERATED_AT = "2026-08-18T14:30:00Z";

/* ------------------------------------------------------------------ Task 3 */

export const conflictGraph = {
  generation_timestamp: GENERATED_AT,
  status: "VALID",
  algorithm_used: "DSATUR (Degree of Saturation) Graph Colouring",
  total_exams: 8,
  graph_density: 0.286,
  vertices: [
    { exam_id: "EX_101", course_code: "PDSA201", degree: 2, color: 0, session_index: 0 },
    { exam_id: "EX_102", course_code: "NET102", degree: 3, color: 1, session_index: 1 },
    { exam_id: "EX_103", course_code: "DBS301", degree: 2, color: 1, session_index: 1 },
    { exam_id: "EX_104", course_code: "AIM401", degree: 1, color: 2, session_index: 2 },
    { exam_id: "EX_105", course_code: "SEN205", degree: 2, color: 2, session_index: 2 },
    { exam_id: "EX_106", course_code: "WEB102", degree: 2, color: 0, session_index: 0 },
    { exam_id: "EX_107", course_code: "CYB302", degree: 1, color: 0, session_index: 0 },
    { exam_id: "EX_108", course_code: "MAT101", degree: 3, color: 3, session_index: 3 },
  ],
  edges: [
    { exam_a: "EX_101", exam_b: "EX_102", shared_students: 8 },
    { exam_a: "EX_101", exam_b: "EX_105", shared_students: 12 },
    { exam_a: "EX_102", exam_b: "EX_106", shared_students: 15 },
    { exam_a: "EX_102", exam_b: "EX_108", shared_students: 11 },
    { exam_a: "EX_103", exam_b: "EX_104", shared_students: 5 },
    { exam_a: "EX_103", exam_b: "EX_107", shared_students: 9 },
    { exam_a: "EX_105", exam_b: "EX_108", shared_students: 6 },
    { exam_a: "EX_106", exam_b: "EX_108", shared_students: 22 },
  ],
};

export const clashAnalysis = {
  minimum_sessions: 4,
  sessions_used: 4,
  lower_bound: 3,
  upper_bound: 4,
  clash_pairs: 8,
  session_groups: [
    { session_index: 0, exams: ["EX_101", "EX_106", "EX_107"] },
    { session_index: 1, exams: ["EX_102", "EX_103"] },
    { session_index: 2, exams: ["EX_104", "EX_105"] },
    { session_index: 3, exams: ["EX_108"] },
  ],
};

export const clashMetrics = {
  algorithm_used: "DSATUR Graph Colouring",
  execution_time_ms: 2.41,
  memory_allocated_kb: 128.4,
  status: "VALID",
  violations: 0,
  extra: [
    { label: "Graph density", value: "0.286" },
    { label: "Chromatic number", value: "4" },
    { label: "Clash pairs", value: "8" },
  ],
};

/* ------------------------------------------------------------------ Task 4 */

export const roomRankings = [
  { exam_id: "EX_101", room_id: "R101", rank: 1, score: 0.95, meets_hard_constraints: true, capacity_utilisation: 80.0 },
  { exam_id: "EX_101", room_id: "R301", rank: 2, score: 0.9, meets_hard_constraints: true, capacity_utilisation: 60.0 },
  { exam_id: "EX_101", room_id: "R004", rank: 3, score: 0.85, meets_hard_constraints: true, capacity_utilisation: 40.0 },
  { exam_id: "EX_101", room_id: "R102", rank: 4, score: 0.55, meets_hard_constraints: true, capacity_utilisation: 64.0 },
];

export const roomRankingMetrics = {
  algorithm_used: "AHP (weights) + TOPSIS (ranking)",
  execution_time_ms: 0.86,
  memory_allocated_kb: 42.1,
  status: "OPTIMAL",
  violations: 0,
  extra: [
    { label: "Criteria", value: "3 soft + 2 hard filters" },
    { label: "AHP consistency ratio", value: "0.00" },
  ],
};

/* ------------------------------------------------------------------ Task 5 */

export const masterSchedule = [
  { exam_id: "EX_101", course_code: "PDSA201", course_title: "Data Structures & Algorithms", date: "2026-08-20", session: "Morning", start_time: "09:00", end_time: "12:00", room_id: "R101", canonical_room_id: "ROOM_R101", floor: 1, allocated_students: 48, required_invigilators: 2, requires_accessibility: false, requires_step_free_access: false },
  { exam_id: "EX_106", course_code: "WEB102", course_title: "Web Technologies", date: "2026-08-20", session: "Morning", start_time: "09:00", end_time: "12:00", room_id: "R205", canonical_room_id: "ROOM_R205", floor: 2, allocated_students: 40, required_invigilators: 2, requires_accessibility: false, requires_step_free_access: false },
  { exam_id: "EX_107", course_code: "CYB302", course_title: "Cyber Security Principles", date: "2026-08-20", session: "Morning", start_time: "09:00", end_time: "12:00", room_id: "LAB_2B", canonical_room_id: "ROOM_LAB2B", floor: 2, allocated_students: 24, required_invigilators: 1, requires_accessibility: false, requires_step_free_access: false },
  { exam_id: "EX_103", course_code: "DBS301", course_title: "Database Systems", date: "2026-08-20", session: "Afternoon", start_time: "13:30", end_time: "16:30", room_id: "R301", canonical_room_id: "ROOM_R301", floor: 3, allocated_students: 62, required_invigilators: 3, requires_accessibility: false, requires_step_free_access: false },
  { exam_id: "EX_102", course_code: "NET102", course_title: "Network Fundamentals", date: "2026-08-20", session: "Afternoon", start_time: "13:30", end_time: "16:30", room_id: "LAB_3A", canonical_room_id: "ROOM_LAB3A", floor: 3, allocated_students: 36, required_invigilators: 2, requires_accessibility: false, requires_step_free_access: false },
  { exam_id: "EX_105", course_code: "SEN205", course_title: "Software Engineering Practice", date: "2026-08-21", session: "Morning", start_time: "09:00", end_time: "12:00", room_id: "R102", canonical_room_id: "ROOM_R102", floor: 1, allocated_students: 54, required_invigilators: 2, requires_accessibility: false, requires_step_free_access: false },
  { exam_id: "EX_104", course_code: "AIM401", course_title: "Artificial Intelligence Methods", date: "2026-08-21", session: "Morning", start_time: "09:00", end_time: "12:00", room_id: "R101", canonical_room_id: "ROOM_R101", floor: 1, allocated_students: 28, required_invigilators: 1, requires_accessibility: true, requires_step_free_access: true },
  { exam_id: "EX_108", course_code: "MAT101", course_title: "Discrete Mathematics", date: "2026-08-21", session: "Afternoon", start_time: "13:30", end_time: "16:30", room_id: "R004", canonical_room_id: "ROOM_R004", floor: 0, allocated_students: 70, required_invigilators: 3, requires_accessibility: false, requires_step_free_access: false },
];

export const timetableMetrics = {
  algorithm_used: "Genetic Algorithm (Hybrid + Hill Climbing)",
  execution_time_ms: 142.3,
  memory_allocated_kb: 512.7,
  status: "OPTIMAL",
  violations: 0,
  extra: [
    { label: "Generations", value: "180 / 500 (early stop)" },
    { label: "Soft satisfaction", value: "94.2%" },
    { label: "Fatigue penalty", value: "34" },
  ],
};

export const fatigueReport = {
  total_fatigue_penalty: 34,
  soft_constraint_satisfaction_percentage: 94.2,
  breakdown: [
    { label: "Back-to-back sessions", count: 0, weight: 10, penalty: 0 },
    { label: "Two exams same day", count: 4, weight: 5, penalty: 20 },
    { label: "Consecutive-day exams", count: 14, weight: 1, penalty: 14 },
  ],
  worst_affected: [
    { student_id: "STU_004", name: "M. Rathnayake", exams: 3, penalty: 6 },
    { student_id: "STU_002", name: "N. Fernando", exams: 3, penalty: 6 },
    { student_id: "STU_008", name: "H. Bandara", exams: 3, penalty: 5 },
    { student_id: "STU_001", name: "A. Perera", exams: 3, penalty: 5 },
  ],
};

export const algorithmComparison = [
  { algorithm: "Genetic Algorithm (Hybrid)", time_ms: 142.3, violations: 0, fatigue: 34, selected: true },
  { algorithm: "Simulated Annealing", time_ms: 96.8, violations: 0, fatigue: 51, selected: false },
  { algorithm: "Greedy Largest Degree", time_ms: 3.2, violations: 2, fatigue: 88, selected: false },
];

/* ------------------------------------------------------------------ Task 2 */

export const proctorRoster = [
  { allocation_id: "ALOC_001", exam_id: "EX_101", course_code: "PDSA201", date: "2026-08-20", session: "Morning", room_id: "R101", assigned_invigilators: [{ invigilator_id: "INV_02", name: "Mr. Nuwan Perera", is_lead_invigilator: true }, { invigilator_id: "INV_04", name: "Dr. Kasun Herath", is_lead_invigilator: false }] },
  { allocation_id: "ALOC_002", exam_id: "EX_106", course_code: "WEB102", date: "2026-08-20", session: "Morning", room_id: "R205", assigned_invigilators: [{ invigilator_id: "INV_03", name: "Ms. Dilini Wijesinghe", is_lead_invigilator: true }, { invigilator_id: "INV_08", name: "Mr. Chamara Ellawala", is_lead_invigilator: false }] },
  { allocation_id: "ALOC_003", exam_id: "EX_107", course_code: "CYB302", date: "2026-08-20", session: "Morning", room_id: "LAB_2B", assigned_invigilators: [{ invigilator_id: "INV_06", name: "Mr. Sahan Kumara", is_lead_invigilator: true }] },
  { allocation_id: "ALOC_004", exam_id: "EX_103", course_code: "DBS301", date: "2026-08-20", session: "Afternoon", room_id: "R301", assigned_invigilators: [{ invigilator_id: "INV_07", name: "Dr. Iresha Madushani", is_lead_invigilator: true }, { invigilator_id: "INV_05", name: "Ms. Tharushi Alwis", is_lead_invigilator: false }, { invigilator_id: "INV_02", name: "Mr. Nuwan Perera", is_lead_invigilator: false }] },
  { allocation_id: "ALOC_005", exam_id: "EX_102", course_code: "NET102", date: "2026-08-20", session: "Afternoon", room_id: "LAB_3A", assigned_invigilators: [{ invigilator_id: "INV_04", name: "Dr. Kasun Herath", is_lead_invigilator: true }, { invigilator_id: "INV_06", name: "Mr. Sahan Kumara", is_lead_invigilator: false }] },
  { allocation_id: "ALOC_006", exam_id: "EX_105", course_code: "SEN205", date: "2026-08-21", session: "Morning", room_id: "R102", assigned_invigilators: [{ invigilator_id: "INV_01", name: "Dr. Aruni Silva", is_lead_invigilator: true }, { invigilator_id: "INV_03", name: "Ms. Dilini Wijesinghe", is_lead_invigilator: false }] },
  { allocation_id: "ALOC_007", exam_id: "EX_104", course_code: "AIM401", date: "2026-08-21", session: "Morning", room_id: "R101", assigned_invigilators: [{ invigilator_id: "INV_05", name: "Ms. Tharushi Alwis", is_lead_invigilator: true }] },
  { allocation_id: "ALOC_008", exam_id: "EX_108", course_code: "MAT101", date: "2026-08-21", session: "Afternoon", room_id: "R004", assigned_invigilators: [{ invigilator_id: "INV_07", name: "Dr. Iresha Madushani", is_lead_invigilator: true }, { invigilator_id: "INV_08", name: "Mr. Chamara Ellawala", is_lead_invigilator: false }, { invigilator_id: "INV_01", name: "Dr. Aruni Silva", is_lead_invigilator: false }] },
];

export const rosterMetrics = {
  algorithm_used: "Hungarian Algorithm (Kuhn-Munkres)",
  execution_time_ms: 4.72,
  memory_allocated_kb: 96.3,
  status: "OPTIMAL",
  violations: 0,
  extra: [
    { label: "Shifts allocated", value: "16 / 16" },
    { label: "Fairness variance", value: "0.00" },
    { label: "Restriction breaches", value: "0" },
  ],
};

/* ------------------------------------------------------------------ Task 1 */

export const deliveryRoutes = [
  { dispatch_id: "DSP_001", exam_id: "EX_101", course_code: "PDSA201", source_vault: "VAULT_G01", destination_room: "ROOM_R101", target_floor: 1, requires_step_free_access: false, step_free_verified: true, total_distance_meters: 48, estimated_transit_time_seconds: 62, within_time_limit: true, nodes_in_path_count: 5, path_sequence: ["VAULT_G01", "HALLWAY_G_EAST", "ELEV_G", "ELEV_F1", "HALLWAY_F1_CENTRAL", "ROOM_R101"], turn_by_turn_manifest: [{ step: 1, from: "VAULT_G01", to: "HALLWAY_G_EAST", action: "Exit vault via East corridor", time_sec: 12 }, { step: 2, from: "HALLWAY_G_EAST", to: "ELEV_G", action: "Proceed to Ground Elevator", time_sec: 7 }, { step: 3, from: "ELEV_G", to: "ELEV_F1", action: "Take Elevator to Floor 1", time_sec: 25 }, { step: 4, from: "ELEV_F1", to: "HALLWAY_F1_CENTRAL", action: "Exit elevator into Floor 1 central hallway", time_sec: 7 }, { step: 5, from: "HALLWAY_F1_CENTRAL", to: "ROOM_R101", action: "Deliver papers to Lecture Theatre 101", time_sec: 11 }] },
  { dispatch_id: "DSP_002", exam_id: "EX_102", course_code: "NET102", source_vault: "VAULT_G01", destination_room: "ROOM_LAB3A", target_floor: 3, requires_step_free_access: false, step_free_verified: false, total_distance_meters: 59.5, estimated_transit_time_seconds: 88, within_time_limit: true, nodes_in_path_count: 6, path_sequence: ["VAULT_G01", "HALLWAY_G_EAST", "STAIR_G", "STAIR_F1", "STAIR_F2", "STAIR_F3", "HALLWAY_F3_NORTH", "ROOM_LAB3A"], turn_by_turn_manifest: [{ step: 1, from: "VAULT_G01", to: "HALLWAY_G_EAST", action: "Exit vault via East corridor", time_sec: 12 }, { step: 2, from: "HALLWAY_G_EAST", to: "STAIR_G", action: "Turn towards main stairwell", time_sec: 5 }, { step: 3, from: "STAIR_G", to: "STAIR_F1", action: "Ascend stairs to Floor 1", time_sec: 18 }, { step: 4, from: "STAIR_F1", to: "STAIR_F2", action: "Ascend stairs to Floor 2", time_sec: 18 }, { step: 5, from: "STAIR_F2", to: "STAIR_F3", action: "Ascend stairs to Floor 3", time_sec: 18 }, { step: 6, from: "STAIR_F3", to: "HALLWAY_F3_NORTH", action: "Enter Floor 3 North corridor", time_sec: 6 }, { step: 7, from: "HALLWAY_F3_NORTH", to: "ROOM_LAB3A", action: "Deliver papers to Computer Lab 3A", time_sec: 11 }] },
  { dispatch_id: "DSP_003", exam_id: "EX_103", course_code: "DBS301", source_vault: "VAULT_G01", destination_room: "ROOM_R301", target_floor: 3, requires_step_free_access: false, step_free_verified: true, total_distance_meters: 57, estimated_transit_time_seconds: 83, within_time_limit: true, nodes_in_path_count: 5, path_sequence: ["VAULT_G01", "HALLWAY_G_EAST", "ELEV_G", "ELEV_F3", "HALLWAY_F3_NORTH", "ROOM_R301"], turn_by_turn_manifest: [{ step: 1, from: "VAULT_G01", to: "HALLWAY_G_EAST", action: "Exit vault via East corridor", time_sec: 12 }, { step: 2, from: "HALLWAY_G_EAST", to: "ELEV_G", action: "Proceed to Ground Elevator", time_sec: 7 }, { step: 3, from: "ELEV_G", to: "ELEV_F3", action: "Take express elevator to Floor 3", time_sec: 45 }, { step: 4, from: "ELEV_F3", to: "HALLWAY_F3_NORTH", action: "Exit elevator into Floor 3 North corridor", time_sec: 7 }, { step: 5, from: "HALLWAY_F3_NORTH", to: "ROOM_R301", action: "Deliver papers to Exam Hall 301", time_sec: 12 }] },
  { dispatch_id: "DSP_004", exam_id: "EX_104", course_code: "AIM401", source_vault: "VAULT_G01", destination_room: "ROOM_R101", target_floor: 1, requires_step_free_access: true, step_free_verified: true, total_distance_meters: 48, estimated_transit_time_seconds: 62, within_time_limit: true, nodes_in_path_count: 5, path_sequence: ["VAULT_G01", "HALLWAY_G_EAST", "ELEV_G", "ELEV_F1", "HALLWAY_F1_CENTRAL", "ROOM_R101"], turn_by_turn_manifest: [{ step: 1, from: "VAULT_G01", to: "HALLWAY_G_EAST", action: "Exit vault via East corridor", time_sec: 12 }, { step: 2, from: "HALLWAY_G_EAST", to: "ELEV_G", action: "Proceed to Ground Elevator (step-free enforced)", time_sec: 7 }, { step: 3, from: "ELEV_G", to: "ELEV_F1", action: "Take Elevator to Floor 1", time_sec: 25 }, { step: 4, from: "ELEV_F1", to: "HALLWAY_F1_CENTRAL", action: "Exit elevator into Floor 1 central hallway", time_sec: 7 }, { step: 5, from: "HALLWAY_F1_CENTRAL", to: "ROOM_R101", action: "Deliver papers to Lecture Theatre 101", time_sec: 11 }] },
  { dispatch_id: "DSP_005", exam_id: "EX_108", course_code: "MAT101", source_vault: "VAULT_G01", destination_room: "ROOM_R004", target_floor: 0, requires_step_free_access: false, step_free_verified: true, total_distance_meters: 26.5, estimated_transit_time_seconds: 22, within_time_limit: true, nodes_in_path_count: 2, path_sequence: ["VAULT_G01", "HALLWAY_G_EAST", "ROOM_R004"], turn_by_turn_manifest: [{ step: 1, from: "VAULT_G01", to: "HALLWAY_G_EAST", action: "Exit vault via East corridor", time_sec: 12 }, { step: 2, from: "HALLWAY_G_EAST", to: "ROOM_R004", action: "Deliver papers to Ground Auditorium", time_sec: 10 }] },
];

export const routingMetrics = {
  algorithm_used: "A* Search (3D Euclidean + floor penalty β=3.5)",
  execution_time_ms: 1.14,
  memory_allocated_kb: 76.8,
  status: "OPTIMAL",
  violations: 0,
  extra: [
    { label: "Nodes explored", value: "14.2% of graph" },
    { label: "Step-free satisfaction", value: "100%" },
    { label: "Optimality ratio vs Dijkstra", value: "1.00" },
  ],
};
