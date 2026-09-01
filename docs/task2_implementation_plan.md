# Task 2 — Invigilator Assignment: Implementation Plan

**Module:** `task2/` | **Port:** 8082 | **Package:** `com.idss.task2`
**Algorithm:** Hungarian (Kuhn-Munkres) — `O(n³)`
**Timeline:** 6 days at ~1 hour/day

---

## Day 1 — Foundation: Dataset, Models, Spring Boot Skeleton

**Goal:** Project compiles, service starts, `/health` returns `{"status":"UP"}`.

### 1.1 Create `input_invigilators.json`

Create `data/input/input_invigilators.json` with 10–15 invigilators. Each must match the canonical contract:

```json
[
  {
    "invigilator_id": "INV_01",
    "name": "Dr. Aruni Silva",
    "max_shifts_per_day": 2,
    "max_total_shifts": 8,
    "restricted_courses": ["PDSA201"],
    "preferred_floors": [1, 2],
    "unavailability": [{"date": "2026-08-20", "session": "Afternoon"}]
  }
]
```

**Design the dataset carefully:**
- Include at least 2 invigilators with `restricted_courses` so constraint logic is testable.
- Include at least 2 with `unavailability` entries that overlap with the master schedule dates/sessions.
- Include at least 1 with `preferred_floors` that won't match some exam rooms.
- Ensure total invigilator capacity (`sum of max_total_shifts`) comfortably exceeds total required invigilators across all exams.
- Vary `max_shifts_per_day` (some 1, some 2, some 3) to test overload constraints.

### 1.2 Create Model Classes

Create these in `task2/src/main/java/com/idss/task2/model/`:

**`MasterScheduleEntry.java`** — Input from Task 5:
```java
public class MasterScheduleEntry {
    public String exam_id;
    public String course_code;
    public String course_title;
    public String date;
    public String session;
    public String start_time;
    public String end_time;
    public String room_id;
    public String canonical_room_id;
    public int floor;
    public int allocated_students;
    public int required_invigilators;
    public boolean requires_accessibility;
    public boolean requires_step_free_access;
}
```

**`AssignedInvigilator.java`** — One invigilator within a roster entry:
```java
public class AssignedInvigilator {
    public String invigilator_id;
    public String name;
    public boolean is_lead_invigilator;
}
```

**`RosterEntry.java`** — One exam's assignment in the roster:
```java
public class RosterEntry {
    public String allocation_id;
    public String exam_id;
    public String course_code;
    public String date;
    public String session;
    public String room_id;
    public List<AssignedInvigilator> assigned_invigilators;
}
```

**`ProctorRoster.java`** — Top-level output document:
```java
public class ProctorRoster {
    public String generation_timestamp;
    public String status;
    public int total_shifts_allocated;
    public List<RosterEntry> roster;
}
```

**`RosterMetrics.java`** — Benchmark metrics:
```java
public class RosterMetrics {
    public String generation_timestamp;
    public String algorithm_used;
    public long execution_time_ms;
    public long memory_allocated_kb;
    public String status;
    public int hard_constraint_violations;
    public double fairness_variance;
    public int total_shifts_allocated;
}
```

### 1.3 Create Spring Boot Application

**`task2/src/main/java/com/idss/task2/Task2Application.java`:**
```java
@SpringBootApplication
public class Task2Application {
    public static void main(String[] args) {
        SpringApplication.run(Task2Application.class, args);
    }
}
```

**`task2/src/main/resources/application.yml`:**
```yaml
server:
  port: 8082

spring:
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DATABASE:idss}
```

### 1.4 Create Health Endpoint (Stub)

**`task2/src/main/java/com/idss/task2/controller/HealthController.java`:**
- `GET /api/task2/health` → returns `{"status":"UP"}`

### 1.5 Verification Checklist

- [ ] `input_invigilators.json` exists and is valid JSON (check with a JSON linter)
- [ ] `mvn clean install` compiles all modules without errors
- [ ] `mvn spring-boot:run -pl task2` starts the service on port 8082
- [ ] `GET http://localhost:8082/api/task2/health` returns `{"status":"UP"}`
- [ ] All model field names match `group_data_contracts.md` exactly (no camelCase variants)

---

## Day 2 — Core Algorithm: Cost Matrix + Hungarian Implementation

**Goal:** Hungarian algorithm correctly solves a test cost matrix. No Spring wiring yet — pure Java, unit-tested.

### 2.1 Create `CostMatrixBuilder.java`

Location: `task2/src/main/java/com/idss/task2/algorithm/CostMatrixBuilder.java`

**Purpose:** Given a list of `MasterScheduleEntry` (exams needing invigilators) and a list of `Invigilator`, build a 2D cost matrix where `cost[i][j]` = the cost of assigning invigilator `i` to exam-shift `j`.

**How it works:**

1. **Expand columns by `required_invigilators`.** If exam EX_101 needs 2 invigilators, it gets 2 columns. This means each column represents one "slot" (one invigilator needed for one exam).

2. **Rows = invigilators.** Each invigilator is one row. If there are more slots than invigilators, pad with dummy rows (high cost). If more invigilators than slots, pad with dummy columns (zero cost).

3. **Cost calculation** for `cost[invigilator][slot]`:
   - Start at base cost 0.
   - **+10000** if invigilator has `restricted_courses` containing this exam's `course_code` (hard violation — should never be assigned).
   - **+10000** if invigilator has `unavailability` matching this exam's `date` + `session` (hard violation).
   - **+5000** if assigning would exceed `max_shifts_per_day` for that date (hard violation).
   - **+5000** if assigning would exceed `max_total_shifts` (hard violation).
   - **+50** if exam's `floor` is not in invigilator's `preferred_floors` (soft penalty).
   - **+10** per existing shift already assigned to this invigilator on the same day (discourages stacking).
   - **+1** per total shift already assigned to this invigilator (encourages fairness/distribution).

4. **Return** the cost matrix plus metadata (which column maps to which exam, which row maps to which invigilator).

**Key design decisions to document for viva:**
- Why 10000 for hard violations: ensures the Hungarian algorithm will never pick them unless there's no alternative (which would mean `status: INFEASIBLE`).
- Why 50 for floor mismatch vs 1 for fairness: floor preference is a stronger soft constraint than load balancing.

### 2.2 Create `Hungarian.java`

Location: `task2/src/main/java/com/idss/task2/algorithm/Hungarian.java`

**Purpose:** Implement the Kuhn-Munkres (Hungarian) algorithm to find the minimum-cost assignment in a square cost matrix.

**Implementation approach (potentials/augmenting path method):**

```
Input: int[][] cost (n x n, square)
Output: int[] assignment where assignment[i] = column assigned to row i

Algorithm:
1. Initialize potentials u[] (rows) and v[] (columns) to 0.
2. Initialize matching arrays: matchColByRow[] = -1, matchRowByCol[] = -1.
3. For each row i:
   a. Reset slack[] and slackRow[] arrays.
   b. Use BFS to find an augmenting path from row i:
      - Maintain a queue of rows to process.
      - For each row in queue, for each column j:
        If reduced cost (cost[row][j] - u[row] - v[j]) == 0 and column j is unmatched:
          → Found augmenting path. Flip matches along path.
        If reduced cost == 0 and column j is matched:
          → Add matched row to queue.
        Else:
          → Track minimum slack for column j.
      - If no augmenting path found in BFS, update potentials:
        delta = min(slack[j]) for all unmatched columns j
        u[row] += delta for all rows in BFS tree
        v[col] -= delta for all columns in BFS tree
        Repeat BFS.
4. Return matchColByRow[].
```

**Handle non-square matrices:**
- If rows > columns: pad columns with zero-cost dummy columns.
- If columns > rows: pad rows with high-cost dummy rows.
- After assignment, filter out dummy assignments.

**Data structures (memorize for viva):**
- `int[][] cost` — the cost matrix
- `int[] u, v` — dual potentials
- `int[] matchColByRow, matchRowByCol` — matching arrays
- `int[] slack, slackRow` — BFS helpers
- `boolean[] visitedRow, visitedCol` — BFS visited tracking

**Big O:** `O(n³)` where n = max(invigilators, total slots)

### 2.3 Write Unit Test

Location: `task2/src/test/java/com/idss/task2/HungarianTest.java`

**Test cases:**

1. **Simple 3x3 known matrix:**
   ```
   cost = {{4, 1, 3}, {2, 0, 5}, {3, 2, 2}}
   expected total = 1 + 2 + 2 = 5 (or whatever the known optimal is)
   ```
   Verify the assignment minimizes total cost.

2. **4x4 matrix with one forbidden assignment** (cost 10000):
   Verify the algorithm avoids that assignment.

3. **Non-square matrix** (2 invigilators, 4 slots):
   Verify padding works and 2 slots get dummy assignments.

4. **Identity matrix** (cost[i][i] = 0, everything else = 100):
   Verify diagonal assignment.

### 2.4 Verification Checklist

- [ ] `CostMatrixBuilder` produces a valid matrix from sample data
- [ ] `Hungarian.solve()` returns correct assignments for all 4 test cases
- [ ] No hard violations (cost 10000 entries) are selected unless unavoidable
- [ ] Non-square matrices are handled (padding works)
- [ ] `mvn test -pl task2` passes all tests

---

## Day 3 — Service Layer, Repository, REST Controller

**Goal:** `POST /api/task2/assign` accepts a master schedule JSON and returns a complete proctor roster. Data persists to MongoDB.

### 3.1 Create `ProctorRosterRepository.java`

Location: `task2/src/main/java/com/idss/task2/repository/ProctorRosterRepository.java`

```java
public interface ProctorRosterRepository extends MongoRepository<ProctorRoster, String> {
    // Spring Data Mongo auto-generates queries from method names
    // We may add custom queries later if needed
}
```

Also create a `RosterMetricsRepository` or just embed metrics in the roster document (simpler — decide during implementation).

### 3.2 Create `AssignmentService.java`

Location: `task2/src/main/java/com/idss/task2/service/AssignmentService.java`

**Core flow of `assignInvigilators(List<MasterScheduleEntry> schedule, List<Invigilator> invigilators)`:**

1. **Validate inputs:**
   - Schedule is not empty.
   - Invigilators list is not empty.
   - Every schedule entry has `required_invigilators >= 1`.

2. **Build cost matrix** via `CostMatrixBuilder`.

3. **Run Hungarian algorithm** — benchmark this step only:
   ```java
   long start = System.nanoTime();
   int[] assignment = hungarian.solve(costMatrix);
   long elapsed = System.nanoTime() - start;
   long executionTimeMs = elapsed / 1_000_000;
   ```

4. **Decode assignment** back to invigilator-exam pairs:
   - Map each column index back to its exam.
   - Map each row index back to its invigilator.
   - Skip dummy assignments.

5. **Group assignments by exam:**
   - Each exam may have multiple assigned invigilators (one per slot/column).
   - The first assigned invigilator for each exam is `is_lead_invigilator = true`.

6. **Check hard violations:**
   - Walk through every assignment.
   - Re-check: restricted courses, unavailability, max_shifts_per_day, max_total_shifts.
   - Count violations. If > 0, set `status = "INFEASIBLE"`. Else `"OPTIMAL"`.

7. **Calculate fairness variance:**
   - Count total shifts per invigilator.
   - Compute variance: `sum((shifts_i - mean_shifts)²) / num_invigilators`.
   - Lower is better.

8. **Build `ProctorRoster` object:**
   - Generate `allocation_id` for each entry: `"ALOC_001"`, `"ALOC_002"`, etc.
   - Set `generation_timestamp` to current ISO-8601 UTC.
   - Set `total_shifts_allocated`.

9. **Build `RosterMetrics` object:**
   - `algorithm_used = "Hungarian (Kuhn-Munkres)"`
   - `memory_allocated_kb` via `Runtime.getRuntime().totalMemory() - freeMemory()` converted to KB.
   - Include `hard_constraint_violations` and `fairness_variance`.

10. **Save to MongoDB** (not benchmarked):
    - `proctorRosterRepository.save(roster)`

11. **Return** the `ProctorRoster` to the controller.

### 3.3 Create `AssignmentController.java`

Location: `task2/src/main/java/com/idss/task2/controller/AssignmentController.java`

**Endpoints:**

| Method | Path | Input | Output |
|---|---|---|---|
| POST | `/api/task2/assign` | `List<MasterScheduleEntry>` as JSON body | `ProctorRoster` JSON |
| GET | `/api/task2/roster/{exam_id}` | path param | `RosterEntry` for that exam |
| GET | `/api/task2/benchmark` | none | `RosterMetrics` from last run |
| GET | `/api/task2/health` | none | `{"status":"UP"}` |

**POST /assign behavior:**
- Accept `List<MasterScheduleEntry>` in the request body.
- Load invigilators from `data/input/input_invigilators.json` via `JsonLoader.loadList()`.
- Call `assignmentService.assignInvigilators(schedule, invigilators)`.
- Return the `ProctorRoster` as JSON.
- Also write `output_proctor_roster.json` to `data/shared/` via `JsonLoader.write()`.
- Also write `output_roster_metrics.json` to `data/shared/`.

**GET /roster/{exam_id} behavior:**
- Query MongoDB for the roster, find the entry with matching `exam_id`.
- Return 404 if not found.

**GET /benchmark behavior:**
- Return the last `RosterMetrics` (store in memory or query Mongo).

### 3.4 Create a Sample Master Schedule for Testing

Create `data/shared/sample_master_schedule.json` — a hand-crafted file matching the Task 5 output contract. Use 5–6 exams across 2 dates and 2 sessions. This lets us test without Task 5 being ready.

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
  }
]
```

Include exams that will trigger:
- A restricted course match (one exam's `course_code` is in an invigilator's `restricted_courses`)
- An unavailability conflict (one exam's date+session matches an invigilator's unavailability)
- A floor mismatch (one exam on floor 3, some invigilators only prefer floors 1-2)

### 3.5 Verification Checklist

- [ ] `POST /api/task2/assign` with sample schedule returns a valid `ProctorRoster` JSON
- [ ] Roster contains one `RosterEntry` per exam in the input
- [ ] Each entry has the correct number of `assigned_invigilators` (matching `required_invigilators`)
- [ ] First invigilator in each entry has `is_lead_invigilator: true`
- [ ] No invigilator is assigned to a restricted course
- [ ] No invigilator is assigned during their unavailability
- [ ] `output_proctor_roster.json` is written to `data/shared/`
- [ ] `output_roster_metrics.json` is written to `data/shared/`
- [ ] Data is persisted in MongoDB `proctor_rosters` collection (check with Mongo Compass or shell)
- [ ] `GET /api/task2/roster/EX_101` returns the correct roster entry
- [ ] `GET /api/task2/benchmark` returns metrics from the last run

---

## Day 4 — Constraint Validation, Fairness, Benchmarking

**Goal:** All hard constraints verified at 0 violations. Benchmarks run across multiple dataset sizes. Metrics are accurate.

### 4.1 Add Constraint Validator

Create `task2/src/main/java/com/idss/task2/algorithm/ConstraintValidator.java`

**Purpose:** After the Hungarian algorithm produces an assignment, independently validate all hard constraints. This is a safety net — the cost matrix penalizes violations heavily, but we must verify the final result.

**Checks:**
1. **Restricted courses:** For every `(invigilator, exam)` pair, verify `exam.course_code` is not in `invigilator.restricted_courses`.
2. **Unavailability:** For every pair, verify no entry in `invigilator.unavailability` matches `exam.date` + `exam.session`.
3. **Max shifts per day:** Group all assignments by invigilator + date. Count shifts. Verify count ≤ `max_shifts_per_day`.
4. **Max total shifts:** Group all assignments by invigilator. Count total. Verify count ≤ `max_total_shifts`.

**Return:** `ConstraintResult` with `violations` count and a list of violation descriptions (for debugging).

### 4.2 Add Fairness Calculator

Create `task2/src/main/java/com/idss/task2/algorithm/FairnessCalculator.java`

**Purpose:** Calculate fairness metrics across the final roster.

**Metrics:**
- `fairness_variance` — variance of shift counts across all invigilators (lower = more fair).
- `min_shifts` — minimum shifts assigned to any invigilator.
- `max_shifts` — maximum shifts assigned to any invigilator.
- `mean_shifts` — average shifts per invigilator.
- `floor_preference_satisfaction_rate` — % of assignments where the invigilator's `preferred_floors` includes the exam's floor.

### 4.3 Wire Validator and Fairness into Service

Update `AssignmentService.assignInvigilators()`:
1. After decoding the Hungarian assignment, run `ConstraintValidator`.
2. If violations > 0, set `status = "INFEASIBLE"` and include violation details in the response.
3. Run `FairnessCalculator` and include results in `RosterMetrics`.

### 4.4 Create Benchmark Test

Location: `task2/src/test/java/com/idss/task2/BenchmarkTest.java`

**What it does:**
- Runs the full assignment pipeline (cost matrix → Hungarian → decode) with different dataset sizes.
- No HTTP calls, no Mongo writes — pure algorithm benchmarking.

**Dataset sizes:**
- Small: 5 exams, 10 invigilators
- Medium: 15 exams, 20 invigilators
- Large: 30 exams, 40 invigilators
- Extra Large: 50 exams, 60 invigilators

**For each size, measure:**
- `execution_time_ms` (System.nanoTime around Hungarian.solve only)
- `memory_allocated_kb` (Runtime before/after)
- `hard_constraint_violations`
- `fairness_variance`
- `status`

**Generate `data/shared/benchmark_task2.json`:**
```json
{
  "algorithm_used": "Hungarian (Kuhn-Munkres)",
  "results": [
    {"size": "small", "exams": 5, "invigilators": 10, "execution_time_ms": 1, "memory_allocated_kb": 42, "hard_constraint_violations": 0, "fairness_variance": 0.5, "status": "OPTIMAL"},
    ...
  ]
}
```

This file is what you'll use to generate charts for the Group Report (Ch.8).

### 4.5 Verification Checklist

- [ ] `ConstraintValidator` catches any violation in the assignment
- [ ] Deliberately create a bad assignment (manual test) → validator reports it
- [ ] `fairness_variance` is a non-negative double, makes sense for the dataset
- [ ] Benchmark test runs all 4 sizes without error
- [ ] `benchmark_task2.json` is written to `data/shared/`
- [ ] `execution_time_ms` grows roughly as expected (O(n³) — 8x time for 2x size)
- [ ] `hard_constraint_violations` is 0 for all benchmark sizes
- [ ] `status` is "OPTIMAL" for all benchmark sizes

---

## Day 5 — Edge Cases, Bug Fixes, End-to-End Test

**Goal:** Module is robust, handles edge cases gracefully, and works end-to-end with the exact Task 5 output contract.

### 5.1 Edge Cases to Handle

1. **Empty schedule** — POST /assign with `[]`:
   - Return `{"status": "FEASIBLE", "total_shifts_allocated": 0, "roster": []}`.
   - Don't crash, don't run Hungarian on a 0x0 matrix.

2. **More exams than invigilators can cover** — total required invigilators > sum of max_total_shifts:
   - Set `status = "INFEASIBLE"`.
   - Include `"reason": "Insufficient invigilator capacity"` and `"suggested_remedy": "Add more invigilators or increase max_total_shifts"`.

3. **All invigilators restricted from one course** — no valid assignment possible for an exam:
   - That exam gets 0 assigned invigilators.
   - `status = "INFEASIBLE"` with reason.

4. **Single exam, single invigilator** — trivial case:
   - Should work, assign that invigilator as lead.

5. **Invigilator with empty `restricted_courses`, `preferred_floors`, `unavailability`** — null or empty lists:
   - Handle gracefully (no NPE).

6. **Duplicate exam_ids in schedule** — shouldn't happen per contract, but handle:
   - Log a warning, process the first occurrence, skip duplicates.

### 5.2 Error Handling in Controller

- Wrap service calls in try-catch.
- Return HTTP 422 for `INFEASIBLE` results (per MCF Section 3.4).
- Return HTTP 500 for unexpected errors with a JSON error body: `{"error": "message", "status": "INFEASIBLE"}`.
- Return HTTP 404 for `GET /roster/{exam_id}` when exam not found.

### 5.3 End-to-End Test

**Manual test sequence:**

1. Start MongoDB.
2. Start task2 service: `mvn spring-boot:run -pl task2`
3. Start gateway: `mvn spring-boot:run -pl gateway`
4. Test through the gateway (port 8080):
   ```
   POST http://localhost:8080/api/task2/assign
   Content-Type: application/json
   Body: [contents of sample_master_schedule.json]
   ```
5. Verify the response:
   - `status` is "OPTIMAL"
   - `roster` has one entry per exam
   - No restricted course violations
   - No unavailability violations
   - `total_shifts_allocated` equals sum of `required_invigilators` across all exams
6. Check MongoDB `proctor_rosters` collection has the document.
7. Check `data/shared/output_proctor_roster.json` exists and matches the API response.
8. Test `GET http://localhost:8080/api/task2/roster/EX_101` — returns the correct entry.
9. Test `GET http://localhost:8080/api/task2/benchmark` — returns metrics.
10. Test `GET http://localhost:8080/api/task2/health` — returns `{"status":"UP"}`.

### 5.4 Verification Checklist

- [ ] All 6 edge cases handled without crashes
- [ ] HTTP status codes are correct (200, 404, 422, 500)
- [ ] End-to-end test through the gateway passes all 10 steps
- [ ] `output_proctor_roster.json` matches the contract format exactly
- [ ] No field name deviations from `group_data_contracts.md`
- [ ] `mvn clean install` passes for all modules

---

## Day 6 — Frontend Page + Final Polish

**Goal:** Task 2 page in the Next.js frontend works. Module is demo-ready.

### 6.1 Frontend Page (if this is your responsibility)

Create `frontend/app/task2/page.jsx`:

**Layout (per MCF Section 4.3):**
- Left card: Input area
  - Show the master schedule JSON (read-only preview or file upload).
  - "Run Assignment" button → calls `POST /api/task2/assign` via gateway.
- Right card: Output area
  - Roster table: `allocation_id | exam_id | course_code | date | session | room_id | assigned_invigilators`.
  - Expand invigilators as comma-separated names with `(Lead)` marker.
- Metrics bar: `Algorithm: Hungarian | Time: X ms | Memory: Y KB | Violations: 0 | Fairness Variance: Z | Status: OPTIMAL`.
- Export button: downloads `output_proctor_roster.json`.

**API wrapper** in `frontend/lib/api.js`:
```javascript
export async function assignInvigilators(schedule) {
  const res = await fetch('http://localhost:8080/api/task2/assign', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(schedule),
  });
  if (!res.ok) throw new Error(`Assignment failed: ${res.status}`);
  return res.json();
}
```

### 6.2 Final Code Review

Walk through every file and check:
- [ ] All field names match canonical contract (no `examId` instead of `exam_id`, etc.)
- [ ] `@JsonProperty` annotations used if any Java field name differs from JSON field name
- [ ] No hardcoded MongoDB URI (reads from `.env` via `MongoConnection`)
- [ ] No hardcoded port (reads from `application.yml`)
- [ ] Benchmark measures algorithm only, not Mongo writes or HTTP overhead
- [ ] `generation_timestamp` is ISO-8601 format with `Z` suffix
- [ ] `allocation_id` format is `ALOC_001`, `ALOC_002`, etc. (zero-padded to 3 digits)
- [ ] Comments reference the MCF/contracts section numbers (for viva defense)

### 6.3 Standalone Demo Test

**Simulate the viva demo:**
1. Load `data/input/input_invigilators.json` and `data/shared/sample_master_schedule.json`.
2. Click "Run Assignment" on the frontend (or curl the API).
3. Show the roster table with correct assignments.
4. Show the metrics bar with `Violations: 0` and `Status: OPTIMAL`.
5. Export the JSON and show it matches the contract.
6. Be ready to explain: cost matrix construction, Hungarian algorithm steps, Big O, why this algorithm vs alternatives.

### 6.4 Verification Checklist

- [ ] Frontend page renders without console errors
- [ ] "Run Assignment" button triggers the API and shows results
- [ ] Metrics bar displays all fields correctly
- [ ] Export button downloads valid JSON
- [ ] `npm run build` passes (no errors)
- [ ] Full standalone demo works end-to-end
- [ ] You can explain the algorithm in 60 seconds (practice the viva answer)

---

## Quick Reference: File Map

```
task2/
├── pom.xml                                    (exists)
├── src/
│   ├── main/
│   │   ├── java/com/idss/task2/
│   │   │   ├── Task2Application.java          [Day 1]
│   │   │   ├── algorithm/
│   │   │   │   ├── CostMatrixBuilder.java     [Day 2]
│   │   │   │   ├── Hungarian.java             [Day 2]
│   │   │   │   ├── ConstraintValidator.java   [Day 4]
│   │   │   │   └── FairnessCalculator.java    [Day 4]
│   │   │   ├── controller/
│   │   │   │   ├── AssignmentController.java  [Day 3]
│   │   │   │   └── HealthController.java      [Day 1]
│   │   │   ├── service/
│   │   │   │   └── AssignmentService.java     [Day 3, updated Day 4]
│   │   │   ├── repository/
│   │   │   │   └── ProctorRosterRepository.java [Day 3]
│   │   │   └── model/
│   │   │       ├── MasterScheduleEntry.java   [Day 1]
│   │   │       ├── AssignedInvigilator.java   [Day 1]
│   │   │       ├── RosterEntry.java           [Day 1]
│   │   │       ├── ProctorRoster.java         [Day 1]
│   │   │       └── RosterMetrics.java         [Day 1]
│   │   └── resources/
│   │       └── application.yml                [Day 1]
│   └── test/
│       └── java/com/idss/task2/
│           ├── HungarianTest.java             [Day 2]
│           └── BenchmarkTest.java             [Day 4]

data/
├── input/
│   └── input_invigilators.json                [Day 1]
└── shared/
    ├── sample_master_schedule.json            [Day 3]
    ├── output_proctor_roster.json             [Day 3, generated]
    ├── output_roster_metrics.json             [Day 3, generated]
    └── benchmark_task2.json                   [Day 4, generated]

frontend/ (if your responsibility)
└── app/task2/page.jsx                         [Day 6]
```

---

## Viva Cheat Sheet (Task 2 Specific)

**"Why Hungarian?"**
"We investigated 3: Greedy Priority (baseline — fast O(n log n) but unfair distribution), Hopcroft-Karp (max bipartite matching — O(V·E) but doesn't optimize cost, just feasibility), and Hungarian/Kuhn-Munkres (selected — O(n³) but guarantees optimal minimum-cost assignment). Greedy was fast but had 3x higher fairness variance. Hungarian gives the provably optimal assignment for our cost matrix in polynomial time."

**"What's in your cost matrix?"**
"Rows are invigilators, columns are exam slots (expanded by required_invigilators per exam). Cost encodes hard constraints at weight 10000 (restricted courses, unavailability, shift limits) and soft preferences at weight 1-50 (floor preference, load balancing). The Hungarian algorithm minimizes total cost, so hard violations are never chosen unless the problem is infeasible."

**"Big O?"**
"O(n³) where n = max(invigilators, total slots). The augmenting path BFS runs n times, each taking O(n²) in the worst case. For our scale (50 exams, 60 invigilators, ~100 slots), this is ~1M operations — under 10ms."

**"Data structures?"**
"2D int[][] cost matrix, 1D int arrays for potentials u[] and v[], matching arrays, and slack arrays for BFS. HashSet for O(1) restricted course and unavailability lookups during cost matrix construction."

---

## Follow-up: Activate the Fatigue Penalty (Make Report §2.3 Accurate)

**Goal:** Make the report's §2.3 "Fatigue Minimization" claim true — the `+10` `SAME_DAY_STACK` penalty must actually influence assignments, not be dead code.

**Root cause:** `Hungarian.solve()` produces a one-to-one matching, so each invigilator is assigned to at most one exam per run. Same-day stacking can never occur, and `AssignmentService` passes empty `priorShiftsByDay` / `priorTotalShifts` maps (<ref_snippet file="C:\Users\Asus\Desktop\IDSS_for_NIBM\idss-campus-ops\task2\src\main\java\com\idss\task2\service\AssignmentService.java" lines="55-56" />), so `shiftsToday` is always 0 and the `+10` never fires (<ref_snippet file="C:\Users\Asus\Desktop\IDSS_for_NIBM\idss-campus-ops\task2\src\main\java\com\idss\task2\algorithm\CostMatrixBuilder.java" lines="139-140" />).

**Approach:** Replicate each invigilator row so one invigilator can match multiple slots, then iteratively refine so the `+10` penalty accumulates across same-day assignments.

### Tasks

- [ ] **Replicate invigilator rows in `CostMatrixBuilder.build()`.** Each invigilator becomes `max_total_shifts` rows instead of 1, so the same invigilator can be matched to multiple exam slots within one Hungarian solve. Keep `rowInvigilatorIds` referencing the same `invigilator_id` across replicas (or add a parallel `rowInvigilatorReplicaIndex` list).
- [ ] **Update `AssignmentService.decodeAssignment()` to merge replicas.** Multiple rows mapping to the same `invigilator_id` must be collapsed back into one invigilator's assignment list. Skip duplicate (invigilator, exam) pairs that arise from over-replication.
- [ ] **Wrap build → solve → decode in an iterative refinement loop.** After each solve, populate `priorShiftsByDay` and `priorTotalShifts` from the decoded assignment, rebuild the cost matrix, re-solve. Stop when the assignment stops changing between iterations or after a max-iterations cap (e.g. 5). This is what makes the `+10` and `+1` penalties actually influence the result.
- [ ] **Benchmark only the final Hungarian solve** (or document that the benchmark now covers the refinement loop). Keep `execution_time_ms` semantics clear in `RosterMetrics`.
- [ ] **Write a regression test** in `HungarianTest` / `CostMatrixBuilderTest`: 2 exams on the same `date` with different `session`s, 1 invigilator with `max_shifts_per_day >= 2` and `max_total_shifts >= 2`. Assert the cost matrix reflects a `+10` stacking penalty on the second same-day assignment and that the final roster assigns both exams to that invigilator with the penalty visible.
- [ ] **Re-check `ConstraintValidator`.** With multi-shift invigilators now possible, the `max_shifts_per_day` and `max_total_shifts` checks (<ref_snippet file="C:\Users\Asus\Desktop\IDSS_for_NIBM\idss-campus-ops\task2\src\main\java\com\idss\task2\algorithm\ConstraintValidator.java" lines="55-72" />) can now actually fire. Confirm they still report violations correctly when limits are exceeded.
- [ ] **Re-run `BenchmarkTest`** across all 4 sizes (5/15/30/50 exams). Confirm `hard_constraint_violations == 0` and `status == OPTIMAL` still hold. Record new `execution_time_ms` figures — they will be higher due to row replication + iteration.
- [ ] **Update report §2.3 Fatigue row wording** if needed: the code penalizes same-day stacking (`back_to_back=10` per MCF §6), not "consecutive daily sessions." Align the report text with the implemented behavior once the penalty is live.
- [ ] **Update the §2.4 assumption** (invigilator count). With row replication, the matching is no longer one-to-one on invigilators, so the weaker "invigilators ≥ max concurrent room requirement" assumption may become valid again — verify and adjust the report text accordingly.
