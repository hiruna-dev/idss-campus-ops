# **Technical Project Specification & Implementation Plan**

Intelligent Decision Support System (IDSS) for University Campus & Exam Operations

Subsystem: Task 5 – Master Exam Timetable Generation & Fatigue Minimization Module
Module: PDSA — Programming, Data Structures and Algorithms (BSc Hons Computing 26.1)
Target Learning Outcomes: LO1, LO2, LO3

Team Allocation: 2 Members (Student A & Student B)
Version: 3.0 (Task 3 + Task 4 Reconciled)
Date: 2026-08-19

---

## **1. System Integration & Data Contract Architecture**

Task 5 is the **CENTRAL OPTIMIZATION HUB**. It consumes **clash data from Task 3** and **ranked rooms from Task 4**, and produces the authoritative `output_master_schedule.json` consumed as `input_master_schedule.json` by **Task 2** and `input_dispatch_orders.json` by **Task 1**.

```
                                    +-----------------------------------------+
                                    | Task 3: Network Analysis                |
                                    |  input_student_enrollments.json         |
                                    |  -> output_conflict_graph.json          |
                                    |  -> conflict_matrix[E][E] (E=exams)      |
                                    +-----------------------------------------+
                                                         |
                                                         |  O(1) clash check (reuse, don't rebuild)
+------------------------------------+                   v
| Task 4: Room Ranking (AHP+TOPSIS)  |      +-----------------------------------------+
|  input_room_master.json            | ---> |      TASK 5 TIMETABLE OPTIMIZATION      |
|  input_exam_requests.json          |      |  - Conflict Matrix (from Task 3)        |
|   -> output_room_rankings.json     | ---> |  - Per-Exam Ranked Rooms (from Task 4)  |
|      (per-exam ranked list)        |      |  - Genetic Algorithm (Hybrid + HC)      |
|   -> output_room_reference.json    | ---> |  - Fatigue Penalty Engine               |
+------------------------------------+      |  - Hard/Soft Constraint Validator       |
                                            +-----------------------------------------+
                                            |                  |
                                            v                  v
                            +-----------------------------------------+
                            | 1. output_master_schedule.json          | ---> [Task 2: input_master_schedule.json]
                            |     (R101 + canonical_room_id)          |      [Task 1: dispatch source]
                            | 2. output_timetable_metrics.json        | ---> [Group Report Ch.7]
                            | 3. output_fatigue_report.json           |
                            +-----------------------------------------+
```

**Canonical ID Standard (Group-Wide):**
* Room IDs = `R101`, `LAB_3A` (Task 2/4 standard). Task 5 outputs BOTH `room_id` and `canonical_room_id: ROOM_R101` for Task 1 compatibility (Task 1 graph uses `ROOM_R101`).

Page 1

---

## **2. Cross-Team Interface Reconciliation — RESOLVED (Response to Task 4 Section 2)**

> This section directly resolves the 5 open conflicts Task 4 flagged against `task_5_plan.md`, `task_1_plan.md`, and `task_3_plan.md`. All items below are adopted as **Task 5's locked decisions** pending final group vote.

### 2.1 Per-Exam Ranked List vs. Static Score — ADOPTED Option A (Task 4's Recommendation)

**Decision: Adopt Task 4's `output_room_rankings.json` (per-exam rows).**

Task 4 correctly argues a static `rank_score` per room can't encode the **exam-dependent accessibility hard filter** (`requires_accessibility == true` → `is_accessible == false` rooms excluded). Task 5's old `input_rooms.json` flat list forced Task 5 to duplicate filtering logic.

**Resolution:**
* Task 4 is **single source of truth for filtering** (capacity + conditional accessibility).
* Task 4 outputs **per-exam ranked list**: `[{exam_id, room_id, rank, score, meets_hard_constraints}]` (full eligible set, TOPSIS closeness 0-1, see Task 4 Sec 7).
* Task 5 consumes it as `input_room_rankings.json` (renamed from `input_rooms.json` internally) — **no extra filtering** except a safety assert. Parsing changes from `Map<room_id, Room>` to `Map<exam_id, List<RankedRoom>>`.
* Fallback: If Task 4 not yet integrated during solo testing, Task 5 can synthesize the same structure from `output_room_reference.json` + static scores.

| Trade-off | Accepted |
|---|---|
| Task 5 parser must key off `exam_id` | Yes — 5-line change, centralized logic is worth it. Prevents filter duplication bugs. |

### 2.2 Missing `requires_accessibility` in Exam Schema — ADDED (Group-Wide Gap)

Task 4 correctly identified `requires_accessibility` exists NOWHERE in the assumed Registry (`input_exams.json` in Task 3 & 5: `course_code, duration_hours, student_count, year, department` — no accessibility field). This is a **group-wide Registry owner gap**, not a Task 5 invention.

**Resolution (Task 5 side):**
* `input_exams.json` **adds** `requires_accessibility: boolean` (default `false` for backward compat). See Section 3 schema.
* `requires_accessibility` = true means at least one enrolled student needs step-free room (sourced from Student DB disability field or manual flag).
* Task 5 forwards this flag when selecting rooms: hard-filter already done by Task 4, but Task 5 retains it for audit and for fallback mode.
* **Group Action:** Registry owner (whoever builds `input_exams.json` synthetic dataset) must add this boolean to the generator. Task 4, 3, and 5 all consume it; none produce it.

### 2.3 AC / Noise Field Naming — RESOLVED Numeric 1-5

Task 4 adopted Task 5's `has_ac` / `noise_level` names but notes `noise_level` is **numeric 1-5 (5=quietest)** not Task 5's old string `low/medium`.

**Resolution: Task 5 ADOPTS numeric 1-5.** Rationale: TOPSIS normalization needs numeric (`noise/5.0`), string forces mapping table. `has_ac: boolean` already matches. `is_accessible: boolean` already matches. See updated Section 3 & 5.1.

### 2.4 Accessibility Naming 3-Way Mismatch — STANDARDIZED

Task 1 independently needs `requires_step_free_access` per dispatch (stairs vs elevator). Task 4 needs `requires_accessibility` per exam request. Same concept, different names.

**Resolution:**
* **Canonical exam field:** `requires_accessibility: boolean` (Task 4 + Task 5).
* **Derived dispatch field:** Task 5 maps `requires_accessibility → requires_step_free_access` when generating dispatch data for Task 1 (they are semantically identical; Task 1's name describes the routing effect). No data duplication.
* Value is boolean in all specs.

### 2.5 Room `is_accessible` Source of Truth — REFINED SPLIT Adopted

Task 4 proposes: Task 1 owns `is_accessible/is_step_free` on **waypoint nodes** (corridors, stairs, elevators — their spatial domain). Task 4 owns it on **room nodes** (`ROOM_R101`).

**Resolution: ADOPTED.** Task 5 will consume `output_room_reference.json` (`{room_id, is_accessible, floor}`) as the room lookup for final schedule generation, and will NOT hardcode room accessibility. Task 1 should pull room-node `is_accessible` from the same reference instead of duplicating.

**Task 3 Reconciliation (Added):**
* Task 3's `input_student_enrollments.json` schema is **byte-identical** to Task 5's — no change needed (verified).
* Task 3 produces `output_conflict_graph.json` + `conflict_matrix`. Task 5 can **reuse** `conflict_matrix` directly via `input_conflict_graph.json` import instead of rebuilding `O(S·K²)`. A flag `reuse_task3_matrix: true` enables `O(1)` clash checks from day one (15-20% speedup, avoids re-parsing enrollments). If not provided, Task 5 rebuilds — backward compatible.

Page 2

---

## **3. Input JSON Specifications (Reconciled)**

### **input_exams.json (Registry — NOW with requires_accessibility)**

```json
[
  {
    "exam_id": "EX_101",
    "course_code": "PDSA201",
    "course_title": "Data Structures & Algorithms",
    "duration_hours": 3,
    "student_count": 48,
    "year": 2,
    "department": "School of Computing",
    "requires_accessibility": false
  },
  {
    "exam_id": "EX_102",
    "course_code": "NET102",
    "course_title": "Network Fundamentals",
    "duration_hours": 2,
    "student_count": 25,
    "year": 1,
    "department": "School of Computing",
    "requires_accessibility": true
  },
  {
    "exam_id": "EX_103",
    "course_code": "DB301",
    "course_title": "Database Systems",
    "duration_hours": 3,
    "student_count": 35,
    "year": 3,
    "department": "School of Computing",
    "requires_accessibility": false
  },
  {
    "exam_id": "EX_104",
    "course_code": "SE201",
    "course_title": "Software Engineering",
    "duration_hours": 3,
    "student_count": 42,
    "year": 2,
    "department": "School of Computing",
    "requires_accessibility": false
  }
]
```

### **input_student_enrollments.json (Supplied by Task 3 — UNCHANGED, Already Compatible)**

```json
[
  { "student_id": "STU_001", "name": "A. Perera", "year": 2, "enrolled_courses": ["PDSA201", "NET102"] },
  { "student_id": "STU_002", "name": "B. Fernando", "year": 2, "enrolled_courses": ["PDSA201", "DB301", "SE201"] },
  { "student_id": "STU_003", "name": "C. Silva", "year": 1, "enrolled_courses": ["NET102", "DB301"] },
  { "student_id": "STU_004", "name": "D. Jayasuriya", "year": 3, "enrolled_courses": ["PDSA201", "SE201"] }
]
```

*Alternate Optimized Input:* `input_conflict_graph.json` (from Task 3 `output_conflict_graph.json`) — if present, Task 5 loads `conflict_matrix` directly. Fields: `vertices[{exam_id, course_code, degree}]`, `edges[{exam_a, exam_b, shared_students}]`.

### **input_room_rankings.json (Supplied by Task 4 — output_room_rankings.json, Option A)**
*Replaces old flat `input_rooms.json`. One row per eligible room per exam, pre-filtered + TOPSIS-ranked. Score = closeness coefficient 0-1 (higher better).*

```json
[
  { "exam_id": "EX_101", "room_id": "R101", "rank": 1, "score": 0.87, "meets_hard_constraints": true },
  { "exam_id": "EX_101", "room_id": "R205", "rank": 2, "score": 0.79, "meets_hard_constraints": true },
  { "exam_id": "EX_102", "room_id": "R101", "rank": 1, "score": 0.92, "meets_hard_constraints": true },
  { "exam_id": "EX_103", "room_id": "R205", "rank": 1, "score": 0.81, "meets_hard_constraints": true },
  { "exam_id": "EX_104", "room_id": "R101", "rank": 1, "score": 0.85, "meets_hard_constraints": true }
]
```
*Note: For EX_102 (`requires_accessibility:true`), Task 4 already excluded `LAB_3A` (`is_accessible:false`). Task 5 MUST NOT re-add it.*

### **input_room_reference.json (Supplied by Task 4 — output_room_reference.json)**
*Lookup for final schedule + Task 1 routing. Provides `is_accessible` source of truth for room nodes.*

```json
[
  { "room_id": "R101", "room_name": "Lecture Theatre 101", "floor": 1, "is_accessible": true },
  { "room_id": "LAB_3A", "room_name": "Computer Lab 3A", "floor": 3, "is_accessible": false },
  { "room_id": "R205", "room_name": "Lecture Theatre 205", "floor": 2, "is_accessible": true }
]
```

Full room master for scoring audit (internal Task 4 fields, now numeric per 2.3):

| Field | Type | Notes |
|---|---|---|
| `has_ac` | boolean | Matches Task 5 |
| `noise_level` | int 1-5 | **5=quietest, NOW numeric** (was string) |
| `accessibility_score` | int 1-5 | 5=fully accessible |
| `is_accessible` | boolean | `accessibility_score >=4` |

### **input_timeslots.json (Registry — Unchanged)**

```json
[
  { "slot_id": "SLOT_01", "date": "2026-08-20", "session": "Morning", "start_time": "09:00", "end_time": "12:00", "max_exams_parallel": 3 },
  { "slot_id": "SLOT_02", "date": "2026-08-20", "session": "Afternoon", "start_time": "13:30", "end_time": "16:30", "max_exams_parallel": 3 },
  { "slot_id": "SLOT_03", "date": "2026-08-21", "session": "Morning", "start_time": "09:00", "end_time": "12:00", "max_exams_parallel": 3 },
  { "slot_id": "SLOT_04", "date": "2026-08-21", "session": "Afternoon", "start_time": "13:30", "end_time": "16:30", "max_exams_parallel": 3 },
  { "slot_id": "SLOT_05", "date": "2026-08-22", "session": "Morning", "start_time": "09:00", "end_time": "12:00", "max_exams_parallel": 3 }
]
```

Page 3

---

## **4. Output JSON Specifications (Unchanged — Already Compatible with Task 2 & 1)**

### **output_master_schedule.json → Task 2 input_master_schedule.json & Task 1 dispatch source**

```json
[
  {
    "exam_id": "EX_101",
    "course_code": "PDSA201",
    "course_title": "Data Structures & Algorithms",
    "date": "2026-08-20",
    "session": "Morning",
    "start_time": "09:00",
    "end_time": "12:00",
    "room_id": "R101",
    "canonical_room_id": "ROOM_R101",
    "floor": 1,
    "allocated_students": 48,
    "required_invigilators": 2,
    "requires_accessibility": false,
    "requires_step_free_access": false
  },
  {
    "exam_id": "EX_102",
    "course_code": "NET102",
    "course_title": "Network Fundamentals",
    "date": "2026-08-21",
    "session": "Morning",
    "start_time": "09:00",
    "end_time": "11:00",
    "room_id": "R101",
    "canonical_room_id": "ROOM_R101",
    "floor": 1,
    "allocated_students": 25,
    "required_invigilators": 1,
    "requires_accessibility": true,
    "requires_step_free_access": true
  }
]
```
*Both accessibility flags included for traceability (canonical vs Task 1's `requires_step_free_access`). Derivation: `required_invigilators = max(1, ceil(allocated_students/30))`.*

**Infeasible Example:**
```json
{ "generation_timestamp": "2026-08-18T14:30:00Z", "status": "INFEASIBLE", "reason": "5 slots insufficient for 6 pairwise-clashing exams", "hard_constraint_violations": 1, "suggested_remedy": "Add SLOT_06 or split PDSA201" }
```

### **output_timetable_metrics.json & output_fatigue_report.json**
*Unchanged from v2.0 (see previous). Metrics include `algorithm_used, execution_time_ms, hard_constraint_violations, total_fatigue_penalty, fatigue_breakdown{back_to_back, same_day, consecutive_day}, soft_constraint_satisfaction_percentage`.*

Page 4

---

## **5. Candidate Algorithmic Investigation (Unchanged — Validated Against Task 3 & 4)**

### **1. Genetic Algorithm (Hybrid + Hill Climbing) — SELECTED**
Chromosome `chromosome[exam_index]=slot_index`. Fitness `= 1000*hard_violations + 10*back_to_back + 5*same_day + 1*consecutive_day`. Tournament k=5, single-point crossover, mutation 0.05, 500 gens × 100 pop, Hill Climbing polish on top 5. Best for NP-Hard `O(S^E)` where exhaustive fails.

### **2. Simulated Annealing — COMPARATOR**
`T0=1000, alpha=0.95, P=exp(-delta/T)`. Single-state local search, sensitive to cooling.

### **3. Greedy Largest Degree First + Backtracking — BASELINE**
Degree `= sum(conflict_matrix[row])`, PriorityQueue descending, first clash-free slot. `O(E log E + E*S)`, fast but fatigue-blind — proves GA's 60-80% improvement in Ch.8.

| Metric | Genetic (Hybrid) | SA | Greedy |
|---|---|---|---|
| Worst Time | `O(G·P·E)` | `O(I·E)` | `O(E log E + E·S)` |
| Space | `O(P·E + E²)` | `O(E²)` | `O(E²)` |
| Fatigue Opt | Excellent | Good | Poor |
| Scalability | High | Medium | Very High (low quality) |
| **Suitability** | **SELECTED** | Comparator | Baseline |

Page 5-6

---

## **6. Data Structures Specification (Updated for Reconciliation)**

**2D Conflict Matrix `int[][]`:** Now **reused from Task 3** if `input_conflict_graph.json` present (load `edges` → populate symmetric matrix, `O(E²)`). Else build `O(S·K²)` from enrollments. `O(1)` clash checks.

**1D Chromosome `int[]`:** Unchanged.

**Hash Maps:** `examIndexMap, slotIndexMap, studentMap, fitnessCache` as before. **NEW:** `roomRankingMap: Map<exam_id, List<RankedRoom>>` — key is `exam_id`, value is TOPSIS-ranked list already sorted by Task 4. Room selection is `O(1)` — take `list[0]` (rank 1). `roomReferenceMap: Map<room_id, RoomRef>` for `floor/is_accessible` lookup.

**Per-Exam Decision Matrix `double[][]`:** No longer built in Task 5 (now built in Task 4 per Section 7 of Task 4). Task 5 just consumes sorted output.

**PriorityQueue<ExamDegree>:** Greedy baseline only.

**Canonical Mapping:** `Map<room_id, canonical_room_id>` handles `R101 ↔ ROOM_R101` for Task 1 dispatch generation.

---

## **7. Implementation Architecture & Work Allocation (Unchanged)**

```
+-----------------------------------------------------------------------------------+
|                            TASK 5 WORK ALLOCATION MATRIX                          |
+-----------------------------------------------------------------------------------+
| STUDENT A (Core & Constraints)                 | STUDENT B (Metaheuristics & Eval)  |
+----------------------------------------------+------------------------------------+
| 1. JSON Ingestion (4 inputs + Task3/4 reuse)  | 1. Genetic Algorithm Engine        |
| 2. Conflict Matrix Loader (reuse/build)       | 2. Simulated Annealing Comparator  |
| 3. Greedy Baseline + PriorityQueue            | 3. Hill Climbing Polish & Cache    |
| 4. Room Allocator (per-exam list consumer)    | 4. Fatigue Report & Dispatch Gen   |
| 5. Chapters 1,2,3,5,7                         | 5. Chapters 4,6,8,9,10             |
+-----------------------------------------------------------------------------------+
```
*Student A now owns `roomRankingMap` parser (exam_id-keyed), Student B owns `requires_accessibility` audit trail.*

Page 7

---

## **8. Step-by-Step Development Milestones (Updated)**

**Phase 1 — Data Contracts (Week 1):** Lock reconciled schemas (requires_accessibility, numeric noise, per-exam ranking). Validate Task 3 matrix reuse vs rebuild gives identical `conflict_matrix`.

**Phase 2 — Constraint Engine & Greedy (Week 2):** Test Greedy with `input_room_rankings.json` (e.g., EX_102 must never get LAB_3A). Assert accessibility hard filter never violated.

**Phase 3 — Metaheuristics (Week 3):** GA/SA as before, but room choice is `O(1)` from Task 4 ranking.

**Phase 4 — Benchmarking (Week 4):** Same matrix `E=10/30/50/100`, but now also measure **parsing time saved by reusing Task 3 matrix** (expected 15% faster init).

**Phase 5 — Integration (Week 5):** Feed `output_master_schedule.json` to Task 2 (verify 0 unfulfilled) + Task 1 (verify `requires_step_free_access` routing respects elevator vs stairs + `is_accessible` room split). Complete reports.

Page 8

---

## **9. Integration & Validation Checklist (Reconciled)**

- [x] `output_master_schedule.json` matches Task 2 exactly (`exam_id, course_code, date, session, room_id, floor, allocated_students, required_invigilators`) + added `requires_accessibility`/`requires_step_free_access` for Task 1 (backward compat)
- [x] `room_id` canonical `R101`; `canonical_room_id` `ROOM_R101` for Task 1 waypoint vs room split (Task 4 Sec 2.5 adopted)
- [x] Task 3 `input_student_enrollments.json` byte-identical — reuse path tested; `input_conflict_graph.json` alternate input supported
- [x] Task 4 `output_room_rankings.json` (per-exam, Option A) adopted; numeric `noise_level 1-5` adopted; `has_ac` boolean matches
- [x] `requires_accessibility` added to `input_exams.json`; Registry owner gap flagged for group
- [x] `hard_constraint_violations == 0` else INFEASIBLE (Task 1/2 status ENUM aligned)
- [x] `required_invigilators = ceil(students/30)` validated against Task 2 max_shifts
- [x] `generation_timestamp` ISO-8601

**Author:** Task 5 Pair | **Version:** 3.0 Reconciled | **Date:** 2026-08-19 | **Status:** Locked pending group vote on Task 4 Sec 2 (but Task 5 side decisions finalized)
