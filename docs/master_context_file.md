# **Master Context File (MCF) v2.2 — Viva-Optimized**
### Intelligent Decision Support System (IDSS) for University Campus & Exam Operations
**BSc (Hons) Computing 26.1 | PDSA | Single Building Campus**
**Tech Stack: Next.js (Frontend) + Spring Cloud Gateway + Spring Boot Microservices (Backend) + MongoDB | Repo: Leader's GitHub (Shared)**

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
6. Integrate all 5 into **one deployable system** with one DB, one API Gateway, and one Next.js frontend (Ch.8)

### 1.3 Scope — In vs Out (So agents don't overbuild)
**IN:** 5 Spring Boot microservices (one per task), Spring Cloud Gateway (centralized routing), Next.js frontend (single app, 5 tabs/pages), MongoDB persistence of final outputs, shared REST contracts, JSON file contracts for algorithm I/O, experimental benchmarking (time/memory/graphs).

**OUT:** No payment, no student registration, no email/SMS, no real-time GPS, no full Figma system. UI is functional, not beautiful — marks are in algorithms. No Docker, no Kubernetes — keep the infrastructure minimal.

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
[Next.js Frontend — Single App, 5 pages/tabs]  (localhost:3000)
         |
         ▼
[API Gateway — Spring Cloud Gateway :8080]
         |
  ┌──────┼──────────────────────────────────┐
  ▼      ▼        ▼         ▼              ▼
[task1  [task2  [task3    [task4          [task5
 :8081]  :8082]  :8083]    :8084]          :8085]
  Route   Invigil Clash     Room            Timetable
  Svc     Svc     Svc       Ranking Svc     Generator Svc]
         |
         ▼
[MongoDB 7 — one DB, accessed by all services]
         |
[/data/shared/ — JSON files for algorithm I/O benchmarking]
```

**API Gateway** provides a single entry point for the frontend. All requests go to `localhost:8080`, and the gateway routes them to the correct service based on the URL path. This provides centralized routing, CORS handling, and a clean architecture for the Group Report diagram.

**Algorithm cores run inside each Spring Boot service.** The Java algorithm classes (A*, Hungarian, DSATUR, TOPSIS, GA) live in `src/main/java/com/idss/taskN/algorithm/`. The REST controller just calls the algorithm and returns the result. MongoDB stores final outputs only.

### 2.2 Module Dependency (Build & Run Order)

```
Task 3 + Task 4 (parallel, no deps) → Task 5 (calls Task 3 + Task 4 APIs) → Task 2 (calls Task 5 API) → Task 1 (calls Task 5 API + building graph)
```

*Startup order:* MongoDB first → task3 & task4 → task5 → task2 → task1 → gateway → Next.js frontend.

### 2.3 Tech Stack (Locked)

| Layer | Technology | Notes |
|---|---|---|
| **Frontend** | Next.js 14+ (App Router), **plain JavaScript**, Tailwind CSS | One repo, 5 route groups (`/task1`…`/task5`). Calls services via Gateway only. |
| **API Gateway** | Spring Cloud Gateway | Routes `/api/task1/**` → `task1-service:8081`, etc. CORS handled here. |
| **Backend** | Java 17+, Spring Boot 3.x, Maven | One Maven parent, 6 child modules (common, gateway, task1…task5) |
| **Algorithm I/O** | JSON files (`input_*.json` / `output_*.json`) | Benchmarked here — NOT Mongo queries |
| **DB** | MongoDB 7 | One cluster, `idss` database, one collection per output type |
| **Testing/Benchmark** | JUnit 5 + JMH | For `execution_time_ms` — in each service's `src/test/` |
| **Version Control** | GitHub (Leader's repo) | All 10 = Collaborators (Write). Branch: `main` (protected), `dev`, `task/N-name` |

### 2.4 Why this Stack is OK for PDSA (Tell this in viva)

> "The algorithm core uses in-memory Java structures (`int[][]`, `HashMap`, `PriorityQueue`) — that's what we benchmark for Big O. Spring Boot is just the delivery mechanism. MongoDB only persists the final `output_master_schedule.json` etc., so DB latency never pollutes `execution_time_ms`. The API Gateway provides centralized routing and CORS handling — the Next.js frontend calls the gateway, and the gateway routes to the correct microservice."

### 2.5 API Gateway Configuration (`gateway/src/main/resources/application.yml`)

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:3000"
            allowedMethods: "*"
            allowedHeaders: "*"
      routes:
        - id: task1-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/task1/**
        - id: task2-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/task2/**
        - id: task3-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/task3/**
        - id: task4-service
          uri: http://localhost:8084
          predicates:
            - Path=/api/task4/**
        - id: task5-service
          uri: http://localhost:8085
          predicates:
            - Path=/api/task5/**
```

Frontend calls `http://localhost:8080/api/task5/generate`. Gateway forwards to `http://localhost:8085/api/task5/generate`. Single entry point, centralized CORS, clean architecture.

**Fallback Plan:** If the gateway fails during demo, switch to Next.js rewrites (add `next.config.js` with rewrite rules pointing directly to service ports). Keep this as a backup.

---

## 3. App Flow & Navigation (What the Examiner Will Click)

### 3.1 Overall Flow — Next.js Frontend (5 Pages)

```
[/login (Dummy: Admin)] → [/dashboard — Stats: Total Exams, Students, Rooms, Timetable Status]
   ├─ /task1  Route Optimizer       — Input: Building Graph + Dispatch Orders → Path on floor map + Turn-by-turn manifest
   ├─ /task2  Invigilator Assignment — Input: Master Schedule + Invigilators → Roster table + Fairness chart
   ├─ /task3  Clash Detection        — Input: Enrollments → Conflict graph (nodes=exams, edges=clashes) + Min sessions
   ├─ /task4  Room Ranking           — Input: Exam + Rooms → Ranked table (TOPSIS scores) + Weights
   └─ /task5  Timetable Generator    — Input: Exams + Students + Rooms + Slots → Calendar grid + Fatigue report
         └─ Buttons: [Generate] [Validate] [Export JSON] [Push to Task 2 / Task 1]
```

### 3.2 Key User Story (For your report's Use Case Diagram)

Admin loads data via UI → clicks **Task 3** (clash detection) → clicks **Task 4** (room ranking) → clicks **Task 5 → Generate** (produces `output_master_schedule.json`, written to `/data/shared/` and Mongo) → Task 2 auto-assigns invigilators (reads from Mongo) → Task 1 auto-routes papers → Admin exports `IDSS_Report.pdf`.

### 3.3 Integration Wire — How the 5 become 1

1. **Task 5** `POST /api/task5/generate` (via Gateway) → writes `output_master_schedule.json` to `/data/shared/` AND inserts into `master_schedules` Mongo collection → returns schedule JSON to frontend.
2. **Task 2** `POST /api/task2/assign` (via Gateway) → reads from `master_schedules` Mongo collection (or accepts Task 5 JSON directly) → inserts into `proctor_rosters`.
3. **Task 1** `POST /api/task1/route` (via Gateway) → reads `destination_room_id` from `master_schedules` + building graph → inserts into `delivery_routes`.
4. **Frontend** "Push to Task 2 / Task 1" button calls those endpoints in sequence via the Gateway — no manual copy-paste.

### 3.4 REST API Contract Summary (All via Gateway at `localhost:8080`)

| Service | Base Path | Key Endpoints |
|---|---|---|
| Task 1 — Routing | `/api/task1` | `POST /route` — body: dispatch order; `GET /routes/{dispatch_id}` |
| Task 2 — Invigilator | `/api/task2` | `POST /assign` — body: schedule; `GET /roster/{exam_id}` |
| Task 3 — Clash | `/api/task3` | `POST /detect` — body: enrollments; `GET /conflict-graph` |
| Task 4 — Room Rank | `/api/task4` | `POST /rank` — body: exam + rooms; `GET /rankings/{exam_id}` |
| Task 5 — Timetable | `/api/task5` | `POST /generate` — body: exams + students + rooms + slots; `GET /schedule` |
| All | `/api/*/benchmark` | `GET` — returns last benchmark result (time, memory, violations) |

All responses are `application/json`. HTTP 200 = success, HTTP 422 = hard constraint violated.

---

## 4. UI/UX Minimal Kit — Next.js Edition (Copy This Exactly)

**Goal:** Look like ONE system in Group Report screenshots, with 1 hour of work.

### 4.1 Shared Theme (CSS Variables)

Create `app/globals.css` with:

```css
:root {
  --primary:  #0F4C75; /* headers, primary buttons */
  --accent:   #3282B8; /* links, highlights */
  --bg:       #F5F7FA; /* page background */
  --card:     #FFFFFF; /* card background */
  --text:     #1B262C; /* body text */
  --success:  #2E7D32;
  --warning:  #EF6C00;
}
```

### 4.2 Shared Layout (`app/layout.jsx`)

Every page shares this shell:

```
[Top Bar]  IDSS — University Exam Operations
           [Task 1] [Task 2] [Task 3] [Task 4] [Task 5]   ← Next.js <Link> nav tabs, active tab highlighted
[Main]     Left card: Input (JSON preview / form fields)
           [Run Algorithm] button  →  Right card: Output (table + metrics bar)
[Metrics]  Algorithm: X | Time: Y ms | Memory: Z KB | Violations: 0 | Status: OPTIMAL
[Footer]   [Export JSON]  button → hits `/api/taskN/export`, downloads output_*.json
```

### 4.3 Module-Specific UI (Keep Simple)

- **Task 1:** SVG floor map (`x,y` from building graph, nodes + path in `var(--primary)`), turn-by-turn manifest table.
- **Task 2:** Roster table (`exam_id | room | assigned_invigilators`), fairness variance badge.
- **Task 3:** D3.js or plain SVG graph canvas (4–6 nodes, edges weighted), `minimum_sessions` big number + `session_groups` list.
- **Task 4:** Ranked table (`rank | room_id | score (0-1) | meets_hard_constraints`).
- **Task 5:** Calendar grid (`date × session` cells with `course_code` pills), fatigue chips (`Back-to-back: 0`).

**Data fetching:** Use `fetch` from Next.js Server Components for initial load; use React state + `fetch` for button-triggered algorithm runs. All API calls go to `http://localhost:8080` (the Gateway).

**Do NOT do:** Custom Figma, animations, dark mode, auth, role-based UI — zero marks.

---

## 5. Database & Schema — Mongo Collections = Your JSON Contracts

**Principle:** Mongo schema IS the JSON contract. One collection per `output_*.json`. Services write their own collection; other services read via API or Mongo directly only within the owning service.

| Collection | Owner Service | Sample Document | Key Indexes |
|---|---|---|---|
| `exams` | Registry (seed) | `{"exam_id":"EX_101","course_code":"PDSA201","student_count":48,"requires_accessibility":false}` | `exam_id` unique |
| `students` | Registry (seed) | `{"student_id":"STU_001","enrolled_courses":["PDSA201","NET102"]}` | `student_id` unique |
| `rooms` | Task 4 (seed + output) | `{"room_id":"R101","capacity":60,"has_ac":true,"noise_level":4,"is_accessible":true}` | `room_id` unique |
| `timeslots` | Registry (seed) | `{"slot_id":"SLOT_01","date":"2026-08-20","session":"Morning"}` | `slot_id` unique |
| `conflict_graph` | Task 3 | `{"generation_timestamp":"...","vertices":[...],"edges":[...],"graph_density":0.83}` | `generation_timestamp` |
| `room_rankings` | Task 4 | `{"exam_id":"EX_101","room_id":"R101","rank":1,"score":0.87}` | compound `exam_id + rank` |
| `master_schedules` | Task 5 | `{"exam_id":"EX_101","course_code":"PDSA201","date":"2026-08-20","session":"Morning","room_id":"R101","canonical_room_id":"ROOM_R101","allocated_students":48,"required_invigilators":2}` | `exam_id` unique; compound `date + session + room_id` unique |
| `proctor_rosters` | Task 2 | `{"allocation_id":"ALOC_001","exam_id":"EX_101","assigned_invigilators":[...]}` | `allocation_id` unique |
| `delivery_routes` | Task 1 | `{"dispatch_id":"DSP_001","path_sequence":["VAULT_G01","HALLWAY_G_EAST",...],"step_free_verified":true}` | `dispatch_id` unique |

**Example Spring Boot + Mongo (Java):**

```java
// Task 5 — ScheduleRepository.java (Spring Data)
public interface ScheduleRepository extends MongoRepository<MasterSchedule, String> {
    Optional<MasterSchedule> findByExamId(String examId);
}

// ScheduleService.java — benchmark block wraps algorithm only, NOT the save
long start = System.nanoTime();
List<MasterSchedule> result = geneticEngine.run(input); // ← benchmark THIS
long elapsed = System.nanoTime() - start;

scheduleRepository.saveAll(result); // ← NOT benchmarked
```

**Indexes to mention in viva (TRD depth):** `master_schedules` compound index on `date_1_session_1_room_id_1` to enforce no double-booking at DB level.

---

## 6. TRD — Canonical Data Contracts (The Only Truth for Agents)

**Full contracts are in `group_data_contracts.md` — this is the 1-page cheat sheet.**

| Entity | Canonical | Alias / Rule |
|---|---|---|
| Room | `room_id: "R101"` | Alias `ROOM_R101` (= R101). Task 5 outputs both. Task 1 accepts both. |
| Noise | `noise_level: int 1–5` (5 = quietest) | Never a string like `"low"`. |
| AC | `has_ac: boolean` | |
| Room Accessible | `is_accessible: boolean` | |
| Exam Accessible Need | `requires_accessibility: boolean` (on EXAM) | Task 1 dispatch uses `requires_step_free_access` (same boolean, derived). |
| Status | `OPTIMAL / FEASIBLE / INFEASIBLE / VALID` | |
| Timestamp | ISO-8601 `2026-08-18T14:30:00Z` | |

**Chain (Byte-Identical):**
`Task 3 output_conflict_graph.json` + `Task 4 output_room_rankings.json` → `Task 5 output_master_schedule.json` **==** `Task 2 input_master_schedule.json` **==** Task 1 dispatch source (`destination_room_id`).

**If your old plan says `low` or `ROOM_R101` alone, map it to canonical before reading/writing.**

**Spring Boot DTOs must match these fields exactly.** Use `@JsonProperty("room_id")` if your Java field name differs.

Full JSON examples: see `group_data_contracts.md` Section 4.

---

## 7. Algorithm & Data Structure Strategy — Viva Gold (LO1/LO2/LO3)

**Each pair must be able to say this in 60 seconds:**

| Task | Selected (Primary) | Comparators | Data Structures (Say These Words) | Key Big O to Memorize |
|---|---|---|---|---|
| **1 Routing** | **A* (3D Euclidean + β=3.5 floor penalty)** | Dijkstra (baseline), Bellman-Ford (contrast) | `AdjacencyList Map<String,List<Edge3D>> O(V+E)`, `Indexed Binary Min-Heap O(log V) decreaseKey`, `HashMap pos[node]` | `O((V+E) log V)` worst, `Θ(b^d)` avg |
| **2 Invigilator** | **Hungarian (Kuhn-Munkres)** | Hopcroft-Karp (max matching), Greedy Priority | `2D Cost Matrix int[][]`, `1D arrays match/slack/parent`, `HashSet O(1) restricted check` | `O(n³)` |
| **3 Clash** | **DSATUR** | Welsh-Powell, Backtracking (exact) | `AdjacencyList O(V+E)`, `Conflict Matrix int[][] O(1) lookup`, `PriorityQueue (saturation,degree)` | Build `O(S·K²)`, DSATUR `O(V²)` practical |
| **4 Room** | **AHP (weights) + TOPSIS (ranking)** | SAW (baseline), Fuzzy MCDM (LO3) | `HashMap roomRegistry O(1)`, `Decision Matrix double[k][3]`, `PriorityQueue<RoomScore>` | `O(n·m)` (n rooms, m = 3 criteria) |
| **5 Timetable** | **Genetic Algorithm (Hybrid + Hill Climbing)** | Simulated Annealing, Greedy Largest Degree | `Conflict Matrix int[][] O(1)`, `Chromosome int[E]`, `HashMap examIndex + fitnessCache`, `PriorityQueue` | `O(G·P·E)` (G gens, P pop) |

**LO3 Heuristic Story (Say this):** "Timetabling is NP-Hard `O(S^E)`. Brute force `10^30` is impossible, so we use metaheuristics (GA/SA) — near-optimal 94% satisfaction in 142ms vs exact that never finishes."

---

## 8. Implementation Plan, Repo & Work Allocation

### 8.1 Repo Structure (Create exactly this)

```
idss-campus-ops/
├─ pom.xml                      ← parent Maven (modules: common, gateway, task1…task5)
│
├─ common/                      ← com.idss.common: Canonical.java, DTOs, JsonLoader.java
│
├─ gateway/                     ← Spring Cloud Gateway (routes + CORS config)
│   └─ src/main/resources/application.yml
│
├─ task1/                       ← com.idss.task1 | Spring Boot :8081
│   └─ src/main/java/com/idss/task1/
│       ├─ algorithm/           BuildingGraph.java, AStarEngine.java, IndexedHeap.java
│       ├─ controller/          RouteController.java
│       ├─ service/             RouteService.java
│       └─ repository/          DeliveryRouteRepository.java
│
├─ task2/   ← :8082   algorithm/: Hungarian.java, Roster.java
├─ task3/   ← :8083   algorithm/: ConflictGraph.java, DSATUR.java
├─ task4/   ← :8084   algorithm/: AHPTopsis.java, RoomRegistry.java
├─ task5/   ← :8085   algorithm/: ConflictMatrix.java, GeneticEngine.java, SA.java, Greedy.java
│
├─ frontend/                    ← Next.js 14 App Router (plain JavaScript + Tailwind)
│   ├─ app/
│   │   ├─ layout.jsx           ← shared top bar + nav tabs
│   │   ├─ dashboard/page.jsx
│   │   ├─ task1/page.jsx
│   │   ├─ task2/page.jsx
│   │   ├─ task3/page.jsx
│   │   ├─ task4/page.jsx
│   │   └─ task5/page.jsx
│   ├─ components/              ← MetricsBar.jsx, RankedTable.jsx, ConflictGraph.jsx, etc.
│   └─ lib/api.js               ← typed fetch wrappers for each service endpoint (all go to :8080)
│
├─ data/
│   ├─ input/                   ← input_*.json (commit your synthetic datasets)
│   └─ shared/                  ← output_*.json (gitignored, generated at runtime + inserted to Mongo)
│
└─ docs/                        ← this MCF + 5 task plans + group_data_contracts.md + Group Report
```

### 8.2 Git Workflow

- `main` protected, `dev` integration, `task/N-name` feature branches
- Commit convention: `feat(task5): add GA crossover` — leader can see who did what for Group Report Ch.11
- PR review: at least 1 pair from the downstream task reviews the PR (Task 5 reviews Task 3 PR)
- `frontend/` changes reviewed by the full group — it's the demo face

### 8.3 Work Allocation (Each Pair = 2 Members)

| Task | Member A | Member B |
|---|---|---|
| Task 1 | Graph + Parser + Dijkstra | Heap + A* + Benchmark |
| Task 2 | Cost Matrix + Hungarian core | Constraints + Fairness + Eval |
| Task 3 | Graph + Matrix | DSATUR + Benchmark |
| Task 4 | Filter + TOPSIS | AHP + Fuzzy + Integration |
| Task 5 | Loader + Matrix + Greedy | GA + SA + HillClimbing + Benchmark |
| **Frontend** | Layout + Task 1 & 2 pages + API wrappers | Task 3 & 4 & 5 pages + Dashboard |

*Leader tracks contributions via Ch.11 responsibility matrix.*

### 8.4 5-Week Milestones

| Week | Goal |
|---|---|
| **W1** | Lock MCF + `group_data_contracts.md`. Build synthetic datasets (10/30/50/100 exams/rooms). Maven skeleton compiles. Next.js project bootstrapped (`npx create-next-app`). MongoDB running locally. Gateway configured. |
| **W2** | Core algorithm engines (A*, Hungarian, DSATUR, TOPSIS, Greedy) pass hard constraints. REST controllers return correct JSON. Frontend tabs show JSON input/output (no UI polish yet). |
| **W3** | Metaheuristics (GA, SA) + accessibility filter + step-free routing. All 5 `POST /api/taskN/...` endpoints return `hard_violations: 0`. Task 5 → Task 2 → Task 1 chain works via API calls through Gateway. |
| **W4** | Benchmarking — 30 runs per size, generate `time vs n` + `score vs n` charts for Ch.8. Frontend metrics bar live. Export JSON buttons working. |
| **W5** | Integration end-to-end demo (`Task3 → Task4 → Task5 → Task2 → Task1`) on `dev` branch. Reports (Individual 11 chapters + Group 12 chapters). Slides. Test fallback plan (gateway down → Next.js rewrites). |

---

## 9. Testing, Benchmarking & Viva Defense Kit

### 9.1 Benchmark Suite (Do this or Ch.8 gets 0)

- **Sizes:** `V=10,30,50,100,250` (Task 3), `n=10,50,500` rooms (Task 4), `E=10,30,50,100` exams (Task 5)
- **Metrics:** `execution_time_ms`, `memory_allocated_kb`, `hard_violations` (must be 0), `soft_score`, `status`
- **Charts to generate (copy into Ch.8):** `Execution Time vs n (log scale)`, `Solution Quality vs n`, `Selected vs Comparators` bar chart
- **Tools:** `System.nanoTime()`, `Runtime.getRuntime().totalMemory()`, JUnit 5
- **Where:** Each Spring Boot service `src/test/java/com/idss/taskN/BenchmarkTest.java`

The benchmark runs the algorithm Java class **directly** — no HTTP call, no Mongo write, no Gateway — to keep `execution_time_ms` clean.

### 9.2 Viva — Top 10 Questions + 60-Second Answers (Memorize)

**1. Why this algorithm?**
*"We investigated 3: X (baseline), Y (comparator), Z (selected). Selected gives best trade-off: O(...) time, O(...) space, handles soft constraints. Baseline was fast but high fatigue; exact was optimal but exponential `O(K^V)` and never finishes for V>30."*

**2. Big O worst/avg/best?**
*State table from Section 7. Then: "Worst when graph dense, best when sparse/early convergence — measured graphs show avg `Θ(...)`."*

**3. Data structure choice?**
*"We used `HashMap O(1)` for lookups because fitness is called 50k times — `ArrayList O(n)` would blow up to `O(G·P·E·n)`. Matrix `O(E²)` is small (30×30=900 ints) and gives `O(1)` clash checks."*

**4. How do you handle NP-Hard?**
*"Timetabling is NP-Hard `O(S^E)`. We used GA metaheuristic — near-optimal 94% in 142ms, vs brute force `10^30` impossible."*

**5. Hard vs soft constraints?**
*"Hard = must be 0 (clash, capacity, accessibility) — we weight hard ×1000 in fitness. Soft = minimize (fatigue 10/5/1, fairness variance, distance)."*

**6. Integration how?**
*"Task 5 `POST /api/task5/generate` writes `output_master_schedule.json` to `/data/shared/` AND inserts into Mongo `master_schedules`. Task 2's `POST /api/task2/assign` reads that collection — same JSON, no field rename. Frontend calls both via the Gateway."*

**7. Why microservices + Gateway?**
*"Each task is independently deployable and benchmarkable. The API Gateway provides a single entry point for the frontend, handles CORS centrally, and routes requests to the correct service based on URL path. Mongo is shared only via owned collections — Task 2 never writes to Task 5's collection."*

**8. Compare with alternative?**
*Show Ch.8 graph: "Greedy 58ms but fatigue 12, GA 142ms but fatigue 4 — 70% better soft score for 2.5× time, worth it."*

**9. Scalability?**
*"Tested to 100/500, time grows ~`O(n log n)` / `O(G·P·E)` — fits 200ms for 50 exams, 1.14ms for 100-node routing. Bottleneck is conflict matrix `O(E²)` memory — still KBs for our scale."*

**10. Heuristic/LO3?**
*"We implemented Fuzzy MCDM (Task 4) / SA (Task 5) as LO3 heuristic — models uncertainty via triangular fuzzy numbers / accepts worse solutions with `exp(-delta/T)` to escape local optima."*

**11. Why Next.js?**
*"Next.js App Router lets us fetch initial data in Server Components (no loading flash for the examiner), while user-triggered algorithm runs use client-side fetch. All API calls go through the Gateway at port 8080."*

### 9.3 Individual Report 11 Chapters — Who Writes What

Follow your `task_N_plan.md` allocation. Every report needs Ch.3 comparison table, Ch.7 Big O/Θ/Ω, Ch.8 tables+charts. Cite `group_data_contracts.md` as "Group Canonical Contract v1.0".

---

## 10. Integration & Deployment Checklist (Before You Call It Done)

### Backend
- [ ] `mvn clean install` passes for all modules (common, gateway, task1–task5)
- [ ] All 5 services start: `mvn spring-boot:run` in each task folder (ports 8081–8085)
- [ ] Gateway starts: `mvn spring-boot:run` in gateway folder (port 8080)
- [ ] MongoDB running locally on port 27017
- [ ] `POST http://localhost:8080/api/task5/generate` returns `hard_constraint_violations: 0`
- [ ] Task 2 reads Task 5 output with **zero field rename** (test with `curl` via Gateway)
- [ ] Task 1 routes respect `requires_step_free_access` (trolley→elevator, foot→stairs) + `is_accessible` room split
- [ ] Mongo `master_schedules` count == `input_exams.json` length; `room_id` exists in `rooms` collection
- [ ] Benchmark output written to `/data/shared/benchmark_taskN.json` for `E=10/30/50/100` (or your task's sizes)
- [ ] Each service has a `GET /api/taskN/health` that returns `{"status":"UP"}`

### Frontend
- [ ] `npm run build` passes (no errors)
- [ ] Each of the 5 task pages renders without console errors
- [ ] [Run Algorithm] button triggers the correct microservice via Gateway and shows result in the output card
- [ ] Metrics bar shows `execution_time_ms` and `hard_violations` from the API response
- [ ] [Export JSON] button downloads the correct `output_*.json`
- [ ] "Push to Task 2 / Task 1" button on Task 5 page chains API calls and shows confirmation

### Integration
- [ ] `Task3 → Task4 → Task5 → Task2 → Task1` chain runs end-to-end via the frontend on `dev` branch
- [ ] Each pair can demo their module **standalone** (load `data/input/*.json` → Run → show metrics bar)
- [ ] Gateway CORS config allows `http://localhost:3000` (Next.js dev)

### Fallback Plan (Test This Once)
- [ ] If Gateway is down, Next.js rewrites (`next.config.js`) can route directly to services
- [ ] Demo still works without Gateway (just add rewrite rules and restart frontend)

---

## 11. How to Run Everything

### Step 1: Start MongoDB
```bash
# Windows: run mongod.exe or start MongoDB service
# Mac: brew services start mongodb-community
# Linux: sudo systemctl start mongod
```

### Step 2: Start all 5 backend services (5 terminal tabs)
```bash
cd task1 && mvn spring-boot:run    # :8081
cd task2 && mvn spring-boot:run    # :8082
cd task3 && mvn spring-boot:run    # :8083
cd task4 && mvn spring-boot:run    # :8084
cd task5 && mvn spring-boot:run    # :8085
```

### Step 3: Start the Gateway (1 more terminal tab)
```bash
cd gateway && mvn spring-boot:run  # :8080
```

### Step 4: Start the frontend (1 more terminal tab)
```bash
cd frontend
npm install
npm run dev                         # :3000
```

### Step 5: Open browser → `http://localhost:3000`

**That's it. 7 terminals, no Docker, no containers to debug.**

### Fallback: If Gateway Fails
If the Gateway crashes during demo, add this to `frontend/next.config.js` and restart the frontend:

```js
/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      { source: '/api/task1/:path*', destination: 'http://localhost:8081/api/task1/:path*' },
      { source: '/api/task2/:path*', destination: 'http://localhost:8082/api/task2/:path*' },
      { source: '/api/task3/:path*', destination: 'http://localhost:8083/api/task3/:path*' },
      { source: '/api/task4/:path*', destination: 'http://localhost:8084/api/task4/:path*' },
      { source: '/api/task5/:path*', destination: 'http://localhost:8085/api/task5/:path*' },
    ];
  },
};
module.exports = nextConfig;
```

Then change frontend API calls from `http://localhost:8080` to `http://localhost:3000`. Demo continues without Gateway.

---

**Author:** Task 5 Pair (Draft for Leader Review) | **Version:** 2.2 Viva-Optimized | **Date:** 2026-08-24 | **Status:** Ready to commit to `docs/` and share with all 10.

**Changes from v2.1:**
- Re-added Spring Cloud Gateway (single entry point, centralized routing + CORS)
- Added Gateway configuration (`application.yml` with route predicates)
- Updated architecture diagram to show Gateway layer
- Added Gateway to repo structure and startup instructions
- Updated viva answers to explain Gateway benefits
- Added fallback plan (Next.js rewrites if Gateway fails)
- All algorithm plans, data structures, Big O tables, viva answers, and canonical contracts remain identical

**For coding agents:** Paste this entire file at the TOP of your prompt, then your `task_N_plan.md`, then say "Build this Spring Boot microservice in Java+Maven+Mongo, exposing REST endpoints as defined in Section 3.4, strictly following Canonical Contracts in Section 6. Output JSON must match examples byte-identical."
