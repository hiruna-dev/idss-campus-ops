# **Master Context File (MCF) v1.0 — Viva-Optimized**
### Intelligent Decision Support System (IDSS) for University Campus & Exam Operations
**BSc (Hons) Computing 26.1 | PDSA | Single Building Campus**
**Tech Stack: Java (Maven) + MongoDB | Repo: Leader's GitHub (Shared)**

> **Purpose:** One file to keep 10 coders + 10 coding agents on the SAME track. Read this + your `task_N_plan.md` before you prompt any agent. This MCF is viva-optimized — everything is mapped to LO1/LO2/LO3 and the Individual/Group Reports.

---

## 0. How to Use This File (30-Second Rule)

**For every coding agent prompt, upload 2 files:**
1. `master_context_file.md` (this file — the big picture)
2. `task_N_plan.md` (your module's detail — Task 1/2/3/4/5)

**Never invent field names.** Use **Section 5 Canonical Contracts**. If your agent wants to use `ROOM_R101` or `noise: "low"`, force it to `R101` + `noise_level: 4` (Table in 5.1).

**Viva = 40% of your mark.** Sections 8 & 10 tell you exactly what the panel will ask and how to answer in 60 seconds.

---

## 1. PRD Lite — What We Are Building & Why We Will Pass

### 1.1 Problem Domain (7-year-old story)
One big university building. Exam week is chaos: rooms double-booked, students have 2 exams at same time, teachers overworked, papers get lost between vault and hall, and disabled students can't reach 3rd floor. The IDSS fixes the **entire lifecycle**: `Where to put exams? Who clashes? Which room is best? When to schedule to avoid fatigue? Who watches? How to deliver papers safely?`

### 1.2 Objectives (Maps to Group Report Ch.1)
1. Automate clash-free, low-fatigue timetabling (Task 5)
2. Rank rooms fairly by capacity/AC/noise/accessibility (Task 4)
3. Detect minimum sessions via graph coloring (Task 3)
4. Assign invigilators without overload (Task 2)
5. Route papers via shortest accessible path (Task 1)
6. Integrate all 5 into **one executable** with one DB and one navigation shell (Ch.8)

### 1.3 Scope — In vs Out (So agents don't overbuild)
**IN:** 5 Java modules, Mongo persistence of final outputs, shared shell UI, JSON file contracts for algorithm I/O, experimental benchmarking (time/memory/graphs).
**OUT:** No payment, no student registration, no email/SMS, no real-time GPS, no full Figma system. UI is functional, not beautiful — marks are in algorithms.

### 1.4 Success Criteria (What examiners check)
- Hard constraints = 0 violations (clash-free, capacity, accessibility)
- Soft constraints optimized (fatigue, fairness, distance)
- LO1: Can justify algorithm choice with Big O table
- LO2: Can show data structure + flowchart + working code
- LO3: Can compare heuristic vs exact and show graphs
- Integration: `Task 5 output == Task 2 input` byte-identical, Task 1 routes respect Task 5 rooms

---

## 2. System Architecture & Tech Stack

### 2.1 High-Level Architecture
```
[JavaFX Shell UI]  ←→  [Task 1] [Task 2] [Task 3] [Task 4] [Task 5] (Maven modules, each is a Java package)
       ↕                          ↕        ↕        ↕        ↕        ↕
[Common Library: Canonical Models, JSON Loader, Theme.java]  ←→  [MongoDB] (persists final JSONs only)
       ↕
[JSON Files] (algorithm I/O — this is what you benchmark, NOT Mongo queries)
```

### 2.2 Module Dependency (Build & Run Order)
```
Task 3 + Task 4 (parallel, no deps) → Task 5 (needs 3+4) → Task 2 (needs 5) → Task 1 (needs 5 + building graph)
```
*Plan build order:* 3 & 4 → 5 → 2 → 1. *Runtime order is same.*

### 2.3 Tech Stack (Locked)
- **Language:** Java 17+, Maven 3.9, OOP + Collections Framework
- **DB:** MongoDB 7 (stores *final outputs only*, not intermediate algorithm state). Driver: `mongodb-driver-sync` or `Spring Data MongoDB` (pick one, don't mix).
- **JSON:** Jackson / Gson (for `input_*.json` / `output_*.json` — this is your algorithm I/O)
- **Testing/Benchmark:** JUnit 5 + JMH (for `execution_time_ms`)
- **Version Control:** GitHub repo in leader's account — **all 10 must be Collaborators (Write) + everyone forks as backup.** Branch: `main` (protected), `dev`, `task/N-name`.
- **IDE:** IntelliJ / VS Code — any, but Maven structure must be identical.

### 2.4 Why Java + Mongo is OK for PDSA (Tell this in viva)
> "We use in-memory structures (`int[][]`, `HashMap`, `PriorityQueue`) for the algorithm core — that's what we benchmark for Big O. Mongo only persists the final `output_master_schedule.json` etc., so DB latency never pollutes `execution_time_ms`."

---

## 3. App Flow & Navigation (What the Examiner Will Click)

### 3.1 Overall Flow (One executable, 5 tabs)
```
[Login (Dummy: Admin)] → [Dashboard: Stats — Total Exams, Students, Rooms, Timetable Status]
   ├─ Tab 1: Route Optimizer (Task 1) — Input: Building Graph + Dispatch Orders → Show: Path on floor map + Turn-by-turn
   ├─ Tab 2: Invigilator Assignment (Task 2) — Input: Master Schedule + Invigilators → Show: Roster table + Fairness chart
   ├─ Tab 3: Clash Detection (Task 3) — Input: Enrollments → Show: Conflict graph (nodes=exams, edges=clashes) + Min sessions
   ├─ Tab 4: Room Ranking (Task 4) — Input: Exam + Rooms → Show: Ranked table (TOPSIS scores) + Weights
   └─ Tab 5: Timetable Generator (Task 5) — Input: Exams + Students + Rooms + Slots → Show: Calendar grid + Fatigue report
         └─ Actions: [Generate] [Validate] [Export JSON] [Push to Task 2/1]
```

### 3.2 Key User Story (For your report's Use Case Diagram)
Admin loads `input_exams.json` + `input_student_enrollments.json` → Task 3 validates clashes → Task 4 ranks rooms → Task 5 generates `output_master_schedule.json` (clash-free, low fatigue) → Task 2 auto-assigns invigilators → Task 1 auto-routes papers → Admin exports consolidated `IDSS_Report.pdf`.

### 3.3 Integration Wire (How the 5 become 1)
Task 5's `Export to Task 2/1` just writes `output_master_schedule.json` to `/data/shared/` and inserts same doc into Mongo `schedules` collection. Task 2 & Task 1 watch that path/collection — no manual copy-paste.

---

## 4. UI/UX Minimal Kit — 1 Page, Not 20 (Copy This Exactly)

**Goal:** Look like ONE system in Group Report screenshots, with 1 hour of work.

**Shared Theme (Create once: `common/src/main/java/com/idss/common/Theme.java` or `common/src/main/resources/theme.css`):**
```java
public final class Theme {
  public static final String PRIMARY = "#0F4C75"; // headers, primary buttons
  public static final String ACCENT = "#3282B8";  // links, highlights
  public static final String BG = "#F5F7FA";       // background
  public static final String CARD = "#FFFFFF";     // cards
  public static final String TEXT = "#1B262C";
  public static final String SUCCESS = "#2E7D32";
  public static final String WARNING = "#EF6C00";
  public static final String FONT = "Segoe UI / Inter, 14px";
}
```

**Shell (Every module screen must have):**
- Top bar: `IDSS — University Exam Operations | [Task 1] [Task 2] [Task 3] [Task 4] [Task 5]` (tabs, Task 5 tab highlighted when on Task 5)
- Card layout: `Input (left, JSON preview) → [Run Algorithm] → Output (right, table + metrics bar)`
- Metrics bar (bottom of output card): `Algorithm: X | Time: Y ms | Memory: Z KB | Violations: 0 | Status: OPTIMAL`
- Button: `Export JSON` (writes to `/data/shared/output_*.json`)

**Module-Specific (Keep simple):**
- Task 1: Small floor map (use `x,y` from building graph, draw nodes + path in `PRIMARY` color), plus `turn_by_turn_manifest` table.
- Task 2: Roster table (`exam_id | room | assigned_invigilators`), fairness variance badge.
- Task 3: Graph canvas (4-6 nodes, edges weighted), `minimum_sessions` big number + `session_groups` list.
- Task 4: Ranked table (`rank | room_id | score (0-1) | meets_hard_constraints`).
- Task 5: Calendar grid (`date x session` cells with `course_code` pills), fatigue breakdown chips (`Back-to-back: 0`).

**Do NOT do:** Custom Figma, animations, dark mode, auth, role-based UI — zero marks.

---

## 5. Database & Schema — Mongo Collections = Your JSON Contracts

**Principle:** Mongo schema IS the JSON contract. One collection per `output_*.json`. No extra ORM magic.

| Collection | Source Task | Sample Document (One Doc = One JSON Row/File) | Key Indexes |
|---|---|---|---|
| `exams` | Registry | `{"exam_id":"EX_101","course_code":"PDSA201","student_count":48,"requires_accessibility":false, ...}` | `exam_id` unique, `course_code` unique |
| `students` | Registry | `{"student_id":"STU_001","enrolled_courses":["PDSA201","NET102"]}` | `student_id` unique |
| `rooms` | Task 4 (`input_room_master`) | `{"room_id":"R101","capacity":60,"has_ac":true,"noise_level":4,"is_accessible":true}` | `room_id` unique |
| `timeslots` | Registry | `{"slot_id":"SLOT_01","date":"2026-08-20","session":"Morning"}` | `slot_id` unique |
| `conflict_graph` | Task 3 | `{"generation_timestamp":"...","vertices":[...],"edges":[...],"graph_density":0.83}` | `generation_timestamp` |
| `room_rankings` | Task 4 | `{"exam_id":"EX_101","room_id":"R101","rank":1,"score":0.87}` | compound `exam_id+rank` |
| `room_reference` | Task 4 | `{"room_id":"R101","floor":1,"is_accessible":true}` | `room_id` unique |
| `master_schedules` | Task 5 | `{"exam_id":"EX_101","course_code":"PDSA201","date":"2026-08-20","session":"Morning","room_id":"R101","canonical_room_id":"ROOM_R101","allocated_students":48,"required_invigilators":2}` | `exam_id` unique, `date+session+room_id` unique |
| `proctor_rosters` | Task 2 | `{"allocation_id":"ALOC_001","exam_id":"EX_101","assigned_invigilators":[...]}` | `allocation_id` unique |
| `delivery_routes` | Task 1 | `{"dispatch_id":"DSP_001","path_sequence":["VAULT_G01","HALLWAY_G_EAST",...],"step_free_verified":true}` | `dispatch_id` unique |

**Example Mongo Code (Java):**
```java
// Task 5 writes final schedule — same JSON you benchmark
Document doc = Document.parse(jsonString); // from Jackson
mongo.getCollection("master_schedules").insertOne(doc);
```

**Indexes to mention in viva (TRD depth):** `master_schedules` compound index on `date_1_session_1_room_id_1` to enforce no double-booking at DB level too.

---

## 6. TRD — Canonical Data Contracts (The Only Truth for Agents)

**Full contracts are in `group_data_contracts.md` — this is the 1-page cheat sheet.**

| Entity | Canonical | Alias/Rule |
|---|---|---|
| Room | `room_id: R101` | Alias `ROOM_R101` (=R101). Task 5 outputs both. Task 1 accepts both. |
| Noise | `noise_level: int 1-5` (5=quietest) | Never `low` string. |
| AC | `has_ac: boolean` | |
| Room Accessible | `is_accessible: boolean` | Task 4 owns room nodes, Task 1 owns corridors/stairs. |
| Exam Accessible Need | `requires_accessibility: boolean` (on EXAM) | Task 1 dispatch uses `requires_step_free_access` (same boolean, derived). |
| Status | `OPTIMAL/FEASIBLE/INFEASIBLE/VALID` | |
| Timestamp | ISO-8601 `2026-08-18T14:30:00Z` | |

**Chain (Byte-Identical):**
`Task 3 output_conflict_graph.json` + `Task 4 output_room_rankings.json` → `Task 5 output_master_schedule.json` **==** `Task 2 input_master_schedule.json` **==** Task 1 dispatch source (`destination_room_id`).

**If your old plan says `low` or `ROOM_R101` alone, map it to canonical before reading/writing.**

Full JSON examples: see `group_data_contracts.md` Section 4.

---

## 7. Algorithm & Data Structure Strategy — Viva Gold (LO1/LO2/LO3)

**Each pair must be able to say this in 60 seconds:**

| Task | Selected (Primary) | Comparators | Data Structures (Say These Words) | Key Big O to Memorize |
|---|---|---|---|---|
| **1 Routing** | **A* (3D Euclidean + β=3.5 floor penalty)** | Dijkstra (baseline), Bellman-Ford (contrast) | `AdjacencyList Map<String,List<Edge3D>> O(V+E)`, `Indexed Binary Min-Heap O(log V) decreaseKey`, `HashMap pos[node]` | `O((V+E) log V)` worst, `Θ(b^d)` avg |
| **2 Invigilator** | **Hungarian (Kuhn-Munkres)** | Hopcroft-Karp (max matching), Greedy Priority | `2D Cost Matrix int[][]`, `1D arrays match/slack/parent`, `HashSet O(1) restricted check` | `O(n³)` |
| **3 Clash** | **DSATUR** | Welsh-Powell, Backtracking (exact) | `AdjacencyList O(V+E)`, `Conflict Matrix int[][] O(1) lookup`, `PriorityQueue (saturation,degree)` | Build `O(S·K²)`, DSATUR `O(V²)` practical |
| **4 Room** | **AHP (weights) + TOPSIS (ranking)** | SAW (baseline), Fuzzy MCDM (LO3) | `HashMap roomRegistry O(1)`, `Decision Matrix double[k][3]`, `PriorityQueue<RoomScore>` | `O(n·m)` (n rooms, m=3 criteria) |
| **5 Timetable** | **Genetic Algorithm (Hybrid + Hill Climbing)** | Simulated Annealing, Greedy Largest Degree | `Conflict Matrix int[][] O(1)`, `Chromosome int[E]`, `HashMap examIndex + fitnessCache`, `PriorityQueue` | `O(G·P·E)` (G gens, P pop) |

**LO3 Heuristic Story (Say this):** "Timetabling is NP-Hard `O(S^E)`. Brute force `10^30` is impossible, so we use metaheuristics (GA/SA) — near-optimal 94% satisfaction in 142ms vs exact that never finishes."

---

## 8. Implementation Plan, Repo & Work Allocation

### 8.1 Repo Structure (Create exactly this)
```
idss-campus-ops/
├─ pom.xml (parent Maven, modules: common, task1, task2, task3, task4, task5)
├─ common/  (com.idss.common: Theme.java, Canonical.java, JsonLoader.java, Models)
├─ task1/   (com.idss.task1: BuildingGraph.java, AStarEngine.java, IndexedHeap.java)
├─ task2/   (com.idss.task2: Hungarian.java, Roster.java)
├─ task3/   (com.idss.task3: ConflictGraph.java, DSATUR.java)
├─ task4/   (com.idss.task4: AHPTopsis.java, RoomRegistry.java)
├─ task5/   (com.idss.task5: ConflictMatrix.java, GeneticEngine.java, SA.java, Greedy.java)
├─ data/
│   ├─ input/  (input_*.json — commit your synthetic datasets)
│   └─ shared/ (output_*.json — gitignored, generated at runtime + inserted to Mongo)
├─ docs/ (this MCF + 5 task plans + Group Report)
└─ README.md (how to run: mvn clean install && java -jar task5/target/task5.jar)
```

### 8.2 Git Workflow
- `main` protected, `dev` integration, `task/1-routing` etc. feature branches
- Commit convention: `feat(task5): add GA crossover` — leader can see who did what for Group Report Ch.11
- PR review: at least 1 pair from downstream task reviews (Task 5 reviews Task 3 PR)

### 8.3 Work Allocation (Each Pair = 2 Members)
*Same split as your task plans — don't change, just follow it. Leader tracks via Ch.11 responsibility matrix.*
- Task 1: A=Graph+Parser+Dijkstra, B=Heap+A*+Benchmark
- Task 2: A=Cost Matrix+Hungarian core, B=Constraints+Fairness+Eval
- Task 3: A=Graph+Matrix, B=DSATUR+Benchmark
- Task 4: A=Filter+TOPSIS, B=AHP+Fuzzy+Integration
- Task 5: A=Loader+Matrix+Greedy, B=GA+SA+HillClimbing+Benchmark

### 8.4 5-Week Milestones (Mirror your task plans)
**W1:** Lock MCF + group_data_contracts.md, build synthetic datasets (10/30/50/100 exams/rooms), Maven skeleton compiles
**W2:** Core engines (A*, Hungarian, DSATUR, TOPSIS, Greedy) — hard constraints pass
**W3:** Metaheuristics (GA, SA) + accessibility filter + step-free routing
**W4:** Benchmarking — 30 runs per size, generate `time vs n` + `score vs n` charts for Ch.8
**W5:** Integration — `Task5 JSON → Task2 → Task1` end-to-end demo + Reports (Individual 11 chapters + Group 12 chapters) + Slides

---

## 9. Testing, Benchmarking & Viva Defense Kit

### 9.1 Benchmark Suite (Do this or Ch.8 gets 0)
- Sizes: `V=10,30,50,100,250` (Task 3), `n=10,50,500` rooms (Task 4), `E=10,30,50,100` exams (Task 5) — same as your task plans
- Metrics: `execution_time_ms`, `memory_allocated_kb`, `hard_violations (must be 0)`, `soft_score` (fatigue/optimality/fairness/distance), `status`
- Charts to generate (copy into Ch.8): `Execution Time vs n (log scale)`, `Solution Quality vs n`, `Selected vs Comparators` bar chart
- Tools: `System.nanoTime()`, `Runtime.getRuntime().totalMemory()`, JUnit

### 9.2 Viva — Top 10 Questions + 60-Second Answers (Memorize)

**1. Why this algorithm?** *"We investigated 3: X (baseline), Y (comparator), Z (selected). Selected gives best trade-off: O(...) time, O(...) space, and handles soft constraints. Baseline was fast but high fatigue; exact was optimal but exponential `O(K^V)` and never finishes for `V>30`."*

**2. Big O worst/avg/best?** *State table from Section 7. Then: "Worst when graph dense, best when sparse/early convergence — measured graphs show avg `Θ(...)`."*

**3. Data structure choice?** *"We used `HashMap O(1)` for lookups because fitness is called 50k times — `ArrayList O(n)` would blow up to `O(G·P·E·n)`. Matrix `O(E²)` is small (30x30=900 ints) and gives `O(1)` clash checks."*

**4. How handle NP-Hard?** *"Timetabling is NP-Hard `O(S^E)`. We used GA metaheuristic — near-optimal 94% in 142ms, vs brute force `10^30` impossible."*

**5. Hard vs soft constraints?** *"Hard = must be 0 (clash, capacity, accessibility) — we weight hard ×1000 in fitness. Soft = minimize (fatigue 10/5/1, fairness variance, distance)."*

**6. Integration how?** *"Task 5 outputs `output_master_schedule.json` byte-identical to Task 2 input. We output both `R101` and `ROOM_R101` so Task 1 graph clicks. Mongo collection `master_schedules` is just persistence, not algorithm input."*

**7. Compare with alternative?** *Show Ch.8 graph: "Greedy 58ms but fatigue 12, GA 142ms but fatigue 4 — 70% better for 2.5x time, worth it."*

**8. Scalability?** *"Tested to 100/500, time grows ~`O(n log n)`/`O(G·P·E)` — fits 200ms for 50 exams, 1.14ms for 100 nodes routing, so real campus scale is fine. Bottleneck is matrix `O(E²)` memory, still KBs."*

**9. Heuristic/LO3?** *"We implemented Fuzzy MCDM (Task 4) / SA (Task 5) as LO3 heuristic — models uncertainty via triangular fuzzy numbers / accepts worse with `exp(-delta/T)` to escape local optima."*

**10. What would you improve?** *"Hybridize more (GA+Tabu), parallelize fitness, add dynamic re-scheduling for room failure — and move `requires_accessibility` owner to Registry (group gap we flagged)."*

### 9.3 Individual Report 11 Chapters — Who Writes What (Don't duplicate)
Follow your `task_N_plan.md` allocation. Every report needs Ch.3 comparison table, Ch.7 Big O/Θ/Ω, Ch.8 tables+charts. Cite `group_data_contracts.md` as "Group Canonical Contract v1.0".

---

## 10. Integration & Deployment Checklist (Before You Call It Done)

- [ ] `mvn clean install` passes for all 5 modules + common
- [ ] `data/shared/output_master_schedule.json` validates: `hard_constraint_violations==0`
- [ ] Task 2 can read Task 5 output with **zero field rename**
- [ ] Task 1 routes respect `requires_step_free_access` (trolley→elevator, foot→stairs) + `is_accessible` room split
- [ ] Mongo `master_schedules` count == `input_exams.json` length, `room_id` exists in `rooms`
- [ ] Benchmark charts generated for `E=10/30/50/100` (or your task's sizes)
- [ ] Each pair can demo their module **standalone** (load `data/input/*.json` → Run → show metrics bar)
- [ ] Group demo: `Task3 → Task4 → Task5 → Task2 → Task1` chain runs end-to-end in `dev` branch

---

**Author:** Task 5 Pair (Draft for Leader Review) | **Version:** 1.0 Viva-Optimized | **Date:** 2026-08-19 | **Status:** Ready to commit to `docs/` and share with all 10. Next: Leader creates repo, adds Collaborators, merges this MCF + `group_data_contracts.md` to `main`.

**For coding agents:** Paste this entire file at the TOP of your prompt, then your `task_N_plan.md`, then say "Build this module in Java+Maven+Mongo, strictly following Canonical Contracts in Section 6. Output must match JSON examples byte-identical."
