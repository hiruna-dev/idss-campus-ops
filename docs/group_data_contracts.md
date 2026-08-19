# **Group Data Contracts — IDSS for University Campus & Exam Operations**
**Intelligent Decision Support System | PDSA 26.1 | Single Source of Truth v1.0**
**Date: 2026-08-19 | For all 5 Task Pairs + Coding Agents**

> Prompt your coding agent with **2 files: `task_[N]_plan.md` + this `group_data_contracts.md`**. You will build solo but remain byte-compatible for integration.

---

## 1. Canonical Standards (Read This First)

| Entity | Canonical Field | Canonical Format | Alias / Note |
|---|---|---|---|
| **Room** | `room_id` | `R101`, `LAB_3A`, `R205` | Task 1 graph alias: `ROOM_R101` = `R101`. Task 5 outputs **both** for auto-click. |
| **Exam** | `exam_id` | `EX_101` | Stable ID. `course_code` is human key (`PDSA201`). |
| **Course** | `course_code` | `PDSA201`, `NET102` | Use exactly this case. Never `courseCode` / `subject`. |
| **Student** | `student_id` | `STU_001` | |
| **Invigilator** | `invigilator_id` | `INV_01` | |
| **Date/Session** | `date` + `session` | `2026-08-20` + `Morning`/`Afternoon` | `Morning=09:00-12:00`, `Afternoon=13:30-16:30` is group-wide. |
| **Timestamp** | `generation_timestamp` | ISO-8601 `2026-08-18T14:30:00Z` | |
| **Status** | `status` | ENUM `OPTIMAL` / `FEASIBLE` / `INFEASIBLE` / `VALID` | Use exactly these. |
| **Accessibility (Exam)** | `requires_accessibility` | `boolean` | Canonical exam flag. Task 1 dispatch field `requires_step_free_access` is **derived** from this (same boolean, same value). |
| **Accessibility (Room)** | `is_accessible` | `boolean` | Room nodes: owned by **Task 4** (`output_room_reference.json`). Waypoint nodes (corridors/stairs/elevators): owned by **Task 1**. |
| **AC** | `has_ac` | `boolean` | |
| **Noise** | `noise_level` | **int 1-5** (5=quietest) | **NOT string** `low/medium`. All modules use 1-5. |
| **Capacity** | `capacity` / `student_count` | `int` | Filter: `capacity >= student_count` |

**Rule:** If your module's old plan says `low`, `ROOM_R101`, or `requires_step_free_access` on the EXAM, **map it to the canonical above** before reading/writing JSON.

---

## 2. System Data Flow (Who Feeds Whom)

```
Registry/DB
  ├─ input_exams.json  ──────────────────────────────────────────────────┐
  ├─ input_student_enrollments.json ──> [Task 3] ──> output_conflict_graph.json ──┐
  ├─ input_room_master.json ─────────> [Task 4] ──> output_room_rankings.json ───┼──> [Task 5] ──> output_master_schedule.json ──> [Task 2] ──> output_proctor_roster.json
  │                                    output_room_reference.json ────────────────┼──> [Task 5] (room lookup)                      │
  ├─ input_timeslots.json ───────────────────────────────────────────────────────┘                                    └─> [Task 1] input_dispatch_orders.json ──> [Task 1] ──> output_delivery_routes.json
  └─ input_building_graph.json ───────────────────────────────────────────────────────────────────────────────────────────────> [Task 1]
  └─ input_invigilators.json ───────────────────────────────────────────────────────────────────────────────────────────────> [Task 2]
```

*Execution order:* `Task 3 + Task 4 (parallel, no dependencies) -> Task 5 (needs 3+4) -> Task 2 (needs 5) -> Task 1 (needs 5 + building graph + room reference)`

---

## 3. Shared Entity Schemas (All Modules Must Use These Field Names)

### Exam (Registry Source - Consumed by 3,4,5)
```json
{
  "exam_id": "EX_101",
  "course_code": "PDSA201",
  "course_title": "Data Structures & Algorithms",
  "duration_hours": 3,
  "student_count": 48,
  "year": 2,
  "department": "School of Computing",
  "requires_accessibility": false
}
```

### Student Enrollment (Registry -> Task 3 -> Task 5)
```json
{
  "student_id": "STU_001",
  "name": "A. Perera",
  "year": 2,
  "enrolled_courses": ["PDSA201", "NET102"]
}
```

### Room Master (Task 4 Internal)
```json
{
  "room_id": "R101",
  "room_name": "Lecture Theatre 101",
  "floor": 1,
  "capacity": 60,
  "has_ac": true,
  "noise_level": 4,
  "accessibility_score": 5,
  "is_accessible": true
}
```

### Timeslot (Registry -> Task 5)
```json
{
  "slot_id": "SLOT_01",
  "date": "2026-08-20",
  "session": "Morning",
  "start_time": "09:00",
  "end_time": "12:00",
  "max_exams_parallel": 3
}
```

---

## 4. Module Contracts (What Each Task Consumes & Produces)

### **Task 3 — Network Analysis (Clash Detection)**
*Consumes:* `input_student_enrollments.json`, `input_exams.json`
*Produces:*
- `output_conflict_graph.json` `{generation_timestamp, status, algorithm_used:"DSATUR", total_exams, vertices[{exam_id,course_code,degree,color,session_index}], edges[{exam_a,exam_b,shared_students}], graph_density}`
- `output_clash_analysis.json` `{minimum_sessions, sessions_used, lower_bound, upper_bound, clash_pairs, session_groups}`
- Internal `conflict_matrix[E][E]` — **reusable by Task 5** via `edges`

### **Task 4 — Room Ranking (AHP + TOPSIS)**
*Consumes:* `input_room_master.json`, `input_exam_requests.json` (same as `input_exams.json` with `requires_accessibility`)
*Produces:*
- `output_room_rankings.json` — **Per-exam ranked list (Option A, TOPSIS closeness 0-1):**
```json
[
  {"exam_id":"EX_101","room_id":"R101","rank":1,"score":0.87,"meets_hard_constraints":true},
  {"exam_id":"EX_101","room_id":"R205","rank":2,"score":0.79,"meets_hard_constraints":true}
]
```
*Filtered by capacity + conditional accessibility (`requires_accessibility==true` → exclude `is_accessible==false`). Score = TOPSIS `dist_worst/(dist_best+dist_worst)`.*

- `output_room_reference.json` — Lookup for Task 5 & Task 1:
```json
[{"room_id":"R101","room_name":"Lecture Theatre 101","floor":1,"is_accessible":true}]
```

### **Task 5 — Timetable Optimization (Genetic + SA + Greedy)**
*Consumes:* `input_exams.json`, `input_student_enrollments.json` **OR** `output_conflict_graph.json` (reuse matrix), `output_room_rankings.json`, `output_room_reference.json`, `input_timeslots.json`
*Produces:*
- `output_master_schedule.json` — **BYTE-IDENTICAL to Task 2 `input_master_schedule.json` + Task 1 dispatch source:**
```json
[
  {
    "exam_id":"EX_101",
    "course_code":"PDSA201",
    "course_title":"Data Structures & Algorithms",
    "date":"2026-08-20",
    "session":"Morning",
    "start_time":"09:00",
    "end_time":"12:00",
    "room_id":"R101",
    "canonical_room_id":"ROOM_R101",
    "floor":1,
    "allocated_students":48,
    "required_invigilators":2,
    "requires_accessibility":false,
    "requires_step_free_access":false
  }
]
```
`required_invigilators = max(1, ceil(allocated_students/30))`. If `INFEASIBLE`: `{"status":"INFEASIBLE","reason":"...","suggested_remedy":"Add SLOT_06"}`

- `output_timetable_metrics.json` `{algorithm_used:"Genetic Algorithm (Hybrid + Hill Climbing)", execution_time_ms, hard_constraint_violations, total_fatigue_penalty, fatigue_breakdown{back_to_back,same_day,consecutive_day}, soft_constraint_satisfaction_percentage}`
- `output_fatigue_report.json`

### **Task 2 — Invigilator Assignment (Hungarian)**
*Consumes:* `input_master_schedule.json` (from Task 5), `input_invigilators.json`
```json
// input_invigilators.json
[{"invigilator_id":"INV_01","name":"Dr. Aruni Silva","max_shifts_per_day":2,"max_total_shifts":8,"restricted_courses":["PDSA201"],"preferred_floors":[1,2],"unavailability":[{"date":"2026-08-20","session":"Afternoon"}]}]
```
*Produces:* `output_proctor_roster.json` `{generation_timestamp, status, total_shifts_allocated, roster[{allocation_id, exam_id, course_code, date, session, room_id, assigned_invigilators[{invigilator_id, name, is_lead_invigilator}]}]}` + `output_roster_metrics.json`

### **Task 1 — Paper Logistics & Routing (A*)**
*Consumes:* `input_building_graph.json` (nodes with `coordinates{x,y,z}`, `node_type`, `is_accessible`, `adjacent_edges[{target_node, distance_meters, base_transit_time_seconds, edge_type, is_step_free}]`), `input_dispatch_orders.json` (derived from Task 5: `[{dispatch_id, exam_id, course_code, source_vault_id:"VAULT_G01", destination_room_id:"ROOM_R101" (or R101), destination_floor, package_weight_kg, transport_mode, requires_step_free_access, max_allowed_transit_seconds}]`), `output_room_reference.json` (for room `is_accessible`)
*Produces:* `output_delivery_routes.json` `{generation_timestamp, status, routes[{dispatch_id, path_sequence["VAULT_G01",...,"ROOM_R101"], total_distance_meters, estimated_transit_time_seconds, step_free_verified, turn_by_turn_manifest}]}` + `output_routing_metrics.json`

---

## 5. Field Alias & Compatibility Table (For Code)

| If you see this (old) | Write/Read this (canonical) | Action in code |
|---|---|---|
| `ROOM_R101` | `R101` | Task 5 outputs both; Task 1 accepts both via `if id.startsWith("ROOM_") strip`. |
| `noise_level: "low"` | `noise_level: 4` | Map: low=2, medium=3, high=4, very_high=5 (or use 1-5 numeric directly). |
| `requires_step_free_access` on EXAM | `requires_accessibility` | Rename field on EXAM objects. Keep `requires_step_free_access` only on DISPATCH objects (Task 1). |
| `rank_score`, `rankScore`, `score` | `score` (0-1 TOPSIS) | Unify to `score`. |
| `seating_capacity`, `num_students` | `capacity`, `student_count` | Unify. |

---

## 6. Validation Rules (All Modules)

- `hard_constraint_violations == 0` before `status: OPTIMAL/FEASIBLE`. Else `INFEASIBLE`.
- Room hard filter: `capacity >= student_count` AND if `requires_accessibility==true` → `is_accessible==true`.
- Clash hard filter: `conflict_matrix[i][j] > 0` → exams i,j **never** same `date+session`.
- Fatigue penalties (Task 5): `back_to_back(same day consecutive sessions)=10`, `same_day=5`, `consecutive_day=1` (lower is better).

---

## 7. File Index

**Read-Only Shared Inputs (from Registry):** `input_exams.json`, `input_student_enrollments.json`, `input_room_master.json`, `input_timeslots.json`, `input_building_graph.json`, `input_invigilators.json`
**Intermediate:** `output_conflict_graph.json`, `output_clash_analysis.json`, `output_room_rankings.json`, `output_room_reference.json`
**Final Chain:** `output_master_schedule.json` (=`input_master_schedule.json` = Task 1 dispatch source), `output_proctor_roster.json`, `output_delivery_routes.json`
**Metrics (all `generation_timestamp, algorithm_used, execution_time_ms, memory_allocated_kb, status`):** `output_routing_metrics.json`, `output_roster_metrics.json`, `output_timetable_metrics.json`

**Version:** 1.0 | **Maintainer:** Task 5 Pair (Reconciled across Task 1,2,3,4) | **Next Review:** After group vote on Task 4 Sec 2.
