# **Technical Project Specification & Implementation Plan**

### Intelligent Decision Support System (IDSS) for University Campus & Exam Operations
**Subsystem:** Task 1 – Secure Exam Paper Logistics & Accessible Multi-Floor Indoor Routing  
**Module:** PDSA — Programming, Data Structures and Algorithms (BSc Hons Computing 26.1)  
**Target Learning Outcomes:** LO1 (Algorithmic Justification), LO2 (Data Structure Implementation), LO3 (Metaheuristics & Empirical Benchmarking)  
**Team Allocation:** 2 Members (Student A & Student B)  
**Tech Stack:** Java 17+, Spring Boot 3.2.5 (Port :8081), Maven, MongoDB 7, Spring Cloud Gateway (:8080), Next.js 14+ (:3000)

---

## **1. System Integration & Data Contract Architecture**

```
                                    +-----------------------------------------+
                                    | Task 4: Room Selection & Ranking        |
                                    | (Supplies active exam room allocations) |
                                    +-----------------------------------------+
                                                         |
+------------------------------------+      +-----------------------------------------+
| Campus Map & Spatial Database      | ---> |       TASK 1 ROUTE OPTIMIZATION         |
| (input_building_graph.json)        |      |  - 3D Multi-Floor Adjacency List        |
+------------------------------------+      |  - A* Search (3D Euclidean Heuristic)   |
                                            |  - Indexed Binary Min-Heap Priority Q   |
+------------------------------------+      |  - Step-Free Accessibility Filter       |
| Task 5: Master Exam Timetable      | ---> +-----------------------------------------+
| (output_master_schedule.json       |                           |
|  -> input_dispatch_orders.json)    |                           v
+------------------------------------+      +-----------------------------------------+
                                            | 1. output_delivery_routes.json          |
                                            | 2. output_routing_metrics.json          |
                                            | 3. MongoDB (delivery_routes collection) |
                                            | 4. Next.js Frontend SVG Route View      |
                                            +-----------------------------------------+
```

### **1.1 Canonical Compliance & Contract Rules**
1. **Room ID Handling:** Building graph uses `ROOM_R101` as graph node keys; canonical schedules use `R101`. Use `com.idss.common.config.Canonical.toAliasedRoomId()` / `toCanonicalRoomId()` to ensure seamless bidirectional resolution.
2. **Accessibility Flag:** The canonical exam flag `requires_accessibility` derives the dispatch order field `requires_step_free_access` (`true` for wheelchair couriers or paper cart trolleys $\ge 15\text{kg}$).
3. **Execution Chain:** `Task 3 & 4` $\rightarrow$ `Task 5 (Master Schedule)` $\rightarrow$ `Task 2 (Invigilators)` $\rightarrow$ `Task 1 (Dispatch Routing)`.

---

## **2. Input JSON Specifications**

### **2.1 `input_building_graph.json` (Spatial Topology)**
*Location:* `data/input/input_building_graph.json`  
Defines all nodes (security vault, corridors, stairs, elevators, exam rooms) across the campus building with 3D coordinates $(x, y, z)$ and physical access attributes.

```json
[
  {
    "node_id": "VAULT_G01",
    "node_name": "Central Exam Security Vault",
    "floor": 0,
    "coordinates": {"x": 10.0, "y": 12.0, "z": 0.0},
    "node_type": "VAULT",
    "is_accessible": true,
    "adjacent_edges": [
      {
        "target_node": "HALLWAY_G_EAST",
        "distance_meters": 14.5,
        "base_transit_time_seconds": 12,
        "edge_type": "CORRIDOR",
        "is_step_free": true,
        "security_clearance_required": 3
      }
    ]
  },
  {
    "node_id": "HALLWAY_G_EAST",
    "node_name": "Ground Floor East Corridor Junction",
    "floor": 0,
    "coordinates": {"x": 24.5, "y": 12.0, "z": 0.0},
    "node_type": "JUNCTION",
    "is_accessible": true,
    "adjacent_edges": [
      {
        "target_node": "VAULT_G01",
        "distance_meters": 14.5,
        "base_transit_time_seconds": 12,
        "edge_type": "CORRIDOR",
        "is_step_free": true,
        "security_clearance_required": 1
      },
      {
        "target_node": "ELEV_G",
        "distance_meters": 8.0,
        "base_transit_time_seconds": 7,
        "edge_type": "CORRIDOR",
        "is_step_free": true,
        "security_clearance_required": 1
      },
      {
        "target_node": "STAIR_G",
        "distance_meters": 6.5,
        "base_transit_time_seconds": 5,
        "edge_type": "CORRIDOR",
        "is_step_free": true,
        "security_clearance_required": 1
      }
    ]
  },
  {
    "node_id": "ELEV_G",
    "node_name": "Ground Floor Central Elevator",
    "floor": 0,
    "coordinates": {"x": 32.5, "y": 12.0, "z": 0.0},
    "node_type": "ELEVATOR",
    "is_accessible": true,
    "adjacent_edges": [
      {
        "target_node": "ELEV_F1",
        "distance_meters": 4.0,
        "base_transit_time_seconds": 25,
        "edge_type": "ELEVATOR_SHAFT",
        "is_step_free": true,
        "security_clearance_required": 1
      },
      {
        "target_node": "ELEV_F3",
        "distance_meters": 12.0,
        "base_transit_time_seconds": 45,
        "edge_type": "ELEVATOR_SHAFT",
        "is_step_free": true,
        "security_clearance_required": 1
      }
    ]
  },
  {
    "node_id": "STAIR_G",
    "node_name": "Ground Floor Main Stairwell",
    "floor": 0,
    "coordinates": {"x": 30.0, "y": 18.5, "z": 0.0},
    "node_type": "STAIRS",
    "is_accessible": false,
    "adjacent_edges": [
      {
        "target_node": "STAIR_F1",
        "distance_meters": 6.0,
        "base_transit_time_seconds": 18,
        "edge_type": "STAIRCASE",
        "is_step_free": false,
        "security_clearance_required": 1
      }
    ]
  },
  {
    "node_id": "ELEV_F1",
    "node_name": "First Floor Central Elevator",
    "floor": 1,
    "coordinates": {"x": 32.5, "y": 12.0, "z": 4.0},
    "node_type": "ELEVATOR",
    "is_accessible": true,
    "adjacent_edges": [
      {
        "target_node": "HALLWAY_F1_CENTRAL",
        "distance_meters": 5.0,
        "base_transit_time_seconds": 4,
        "edge_type": "CORRIDOR",
        "is_step_free": true,
        "security_clearance_required": 1
      }
    ]
  },
  {
    "node_id": "HALLWAY_F1_CENTRAL",
    "node_name": "First Floor Main Hallway",
    "floor": 1,
    "coordinates": {"x": 37.5, "y": 12.0, "z": 4.0},
    "node_type": "JUNCTION",
    "is_accessible": true,
    "adjacent_edges": [
      {
        "target_node": "ROOM_R101",
        "distance_meters": 12.0,
        "base_transit_time_seconds": 10,
        "edge_type": "DOORWAY",
        "is_step_free": true,
        "security_clearance_required": 2
      }
    ]
  },
  {
    "node_id": "ROOM_R101",
    "node_name": "Examination Hall R101",
    "floor": 1,
    "coordinates": {"x": 49.5, "y": 12.0, "z": 4.0},
    "node_type": "EXAM_ROOM",
    "is_accessible": true,
    "adjacent_edges": []
  },
  {
    "node_id": "STAIR_F1",
    "node_name": "First Floor Main Stairwell",
    "floor": 1,
    "coordinates": {"x": 30.0, "y": 18.5, "z": 4.0},
    "node_type": "STAIRS",
    "is_accessible": false,
    "adjacent_edges": [
      {
        "target_node": "STAIR_F3",
        "distance_meters": 12.0,
        "base_transit_time_seconds": 36,
        "edge_type": "STAIRCASE",
        "is_step_free": false,
        "security_clearance_required": 1
      }
    ]
  },
  {
    "node_id": "STAIR_F3",
    "node_name": "Third Floor Main Stairwell",
    "floor": 3,
    "coordinates": {"x": 30.0, "y": 18.5, "z": 12.0},
    "node_type": "STAIRS",
    "is_accessible": false,
    "adjacent_edges": [
      {
        "target_node": "HALLWAY_F3_NORTH",
        "distance_meters": 7.0,
        "base_transit_time_seconds": 6,
        "edge_type": "CORRIDOR",
        "is_step_free": true,
        "security_clearance_required": 1
      }
    ]
  },
  {
    "node_id": "HALLWAY_F3_NORTH",
    "node_name": "Third Floor North Corridor",
    "floor": 3,
    "coordinates": {"x": 37.0, "y": 18.5, "z": 12.0},
    "node_type": "JUNCTION",
    "is_accessible": true,
    "adjacent_edges": [
      {
        "target_node": "ROOM_LAB3A",
        "distance_meters": 18.5,
        "base_transit_time_seconds": 15,
        "edge_type": "DOORWAY",
        "is_step_free": true,
        "security_clearance_required": 2
      }
    ]
  },
  {
    "node_id": "ROOM_LAB3A",
    "node_name": "Computer Lab 3A (Exam Venue)",
    "floor": 3,
    "coordinates": {"x": 55.5, "y": 18.5, "z": 12.0},
    "node_type": "EXAM_ROOM",
    "is_accessible": true,
    "adjacent_edges": []
  }
]
```

---

### **2.2 `input_dispatch_orders.json` (Dispatch Requests)**
*Location:* `data/input/input_dispatch_orders.json` (or derived from Task 5 `output_master_schedule.json`)

```json
[
  {
    "dispatch_id": "DSP_001",
    "exam_id": "EX_101",
    "course_code": "PDSA201",
    "course_title": "Data Structures & Algorithms",
    "session_date": "2026-08-20",
    "exam_start_time": "09:00",
    "source_vault_id": "VAULT_G01",
    "destination_room_id": "ROOM_R101",
    "destination_floor": 1,
    "package_weight_kg": 18.5,
    "transport_mode": "TROLLEY",
    "requires_step_free_access": true,
    "security_clearance_level": 3,
    "max_allowed_transit_seconds": 300
  },
  {
    "dispatch_id": "DSP_002",
    "exam_id": "EX_102",
    "course_code": "NET102",
    "course_title": "Network Fundamentals",
    "session_date": "2026-08-20",
    "exam_start_time": "09:00",
    "source_vault_id": "VAULT_G01",
    "destination_room_id": "ROOM_LAB3A",
    "destination_floor": 3,
    "package_weight_kg": 4.2,
    "transport_mode": "FOOT_COURIER",
    "requires_step_free_access": false,
    "security_clearance_level": 2,
    "max_allowed_transit_seconds": 420
  }
]
```

---

## **3. Output JSON Specifications**

### **3.1 `output_delivery_routes.json`**
*Location:* `data/shared/output_delivery_routes.json` and saved in MongoDB `delivery_routes` collection.

```json
{
  "generation_timestamp": "2026-08-24T20:00:00Z",
  "status": "OPTIMAL",
  "total_dispatches": 2,
  "successful_routes": 2,
  "failed_routes": 0,
  "routes": [
    {
      "dispatch_id": "DSP_001",
      "exam_id": "EX_101",
      "course_code": "PDSA201",
      "source_vault": "VAULT_G01",
      "destination_room": "ROOM_R101",
      "target_floor": 1,
      "requires_step_free_access": true,
      "step_free_verified": true,
      "total_distance_meters": 44.0,
      "estimated_transit_time_seconds": 58,
      "within_time_limit": true,
      "nodes_in_path_count": 6,
      "path_sequence": [
        "VAULT_G01",
        "HALLWAY_G_EAST",
        "ELEV_G",
        "ELEV_F1",
        "HALLWAY_F1_CENTRAL",
        "ROOM_R101"
      ],
      "turn_by_turn_manifest": [
        {"step": 1, "from": "VAULT_G01", "to": "HALLWAY_G_EAST", "action": "Exit vault via East corridor", "time_sec": 12},
        {"step": 2, "from": "HALLWAY_G_EAST", "to": "ELEV_G", "action": "Proceed to Ground Elevator", "time_sec": 7},
        {"step": 3, "from": "ELEV_G", "to": "ELEV_F1", "action": "Take Elevator to Floor 1", "time_sec": 25},
        {"step": 4, "from": "ELEV_F1", "to": "HALLWAY_F1_CENTRAL", "action": "Exit Elevator into Floor 1 Main Hallway", "time_sec": 4},
        {"step": 5, "from": "HALLWAY_F1_CENTRAL", "to": "ROOM_R101", "action": "Deliver papers to Exam Hall R101", "time_sec": 10}
      ]
    },
    {
      "dispatch_id": "DSP_002",
      "exam_id": "EX_102",
      "course_code": "NET102",
      "source_vault": "VAULT_G01",
      "destination_room": "ROOM_LAB3A",
      "target_floor": 3,
      "requires_step_free_access": false,
      "step_free_verified": false,
      "total_distance_meters": 48.0,
      "estimated_transit_time_seconds": 74,
      "within_time_limit": true,
      "nodes_in_path_count": 7,
      "path_sequence": [
        "VAULT_G01",
        "HALLWAY_G_EAST",
        "STAIR_G",
        "STAIR_F1",
        "STAIR_F3",
        "HALLWAY_F3_NORTH",
        "ROOM_LAB3A"
      ],
      "turn_by_turn_manifest": [
        {"step": 1, "from": "VAULT_G01", "to": "HALLWAY_G_EAST", "action": "Exit vault via East corridor", "time_sec": 12},
        {"step": 2, "from": "HALLWAY_G_EAST", "to": "STAIR_G", "action": "Turn towards Main Stairwell", "time_sec": 5},
        {"step": 3, "from": "STAIR_G", "to": "STAIR_F1", "action": "Ascend stairs to Floor 1", "time_sec": 18},
        {"step": 4, "from": "STAIR_F1", "to": "STAIR_F3", "action": "Ascend stairs to Floor 3", "time_sec": 18},
        {"step": 5, "from": "STAIR_F3", "to": "HALLWAY_F3_NORTH", "action": "Enter Floor 3 North Corridor", "time_sec": 6},
        {"step": 6, "from": "HALLWAY_F3_NORTH", "to": "ROOM_LAB3A", "action": "Deliver papers to Computer Lab 3A", "time_sec": 15}
      ]
    }
  ]
}
```

---

### **3.2 `output_routing_metrics.json` (Benchmarking Metrics)**
*Location:* `data/shared/output_routing_metrics.json`

```json
{
  "generation_timestamp": "2026-08-24T20:00:00Z",
  "algorithm_used": "A* Search Algorithm (3D Euclidean & Floor Penalty Heuristic)",
  "benchmark_suite_version": "PDSA_26.1_v1.0",
  "execution_time_ms": 1.14,
  "memory_allocated_kb": 76.8,
  "nodes_explored_percentage": 14.2,
  "step_free_constraint_satisfaction_percentage": 100.0,
  "time_window_violations": 0,
  "hard_constraint_violations": 0,
  "optimality_ratio": 1.00,
  "status": "OPTIMAL"
}
```

---

## **4. Candidate Algorithmic Investigation & Formal Justification (LO1)**

Three candidate algorithms were evaluated for the multi-floor route optimization module:

### **1. A* Search Algorithm (3D Euclidean Heuristic) — SELECTED**
- **Evaluation Function:** $f(n) = g(n) + h(n)$
  - $g(n)$: Exact accumulated travel time from the source vault.
  - $h(n)$: Admissible 3D Euclidean distance heuristic with vertical transition penalty:
    $$h(u, \text{target}) = \frac{\sqrt{(x_u - x_t)^2 + (y_u - y_t)^2 + (\beta \cdot (z_u - z_t))^2}}{\text{MaxWalkingSpeed}}$$
    *where $\beta = 3.5$ scales vertical floor transitions (accounting for elevator/stair traversal overhead).*
- **Justification:** Guarantees absolute path optimality when $h(n)$ is consistent and admissible ($h(n) \le h^*(n)$), while pruning over 75% of unneeded corridor branches compared to blind search.

### **2. Dijkstra’s Algorithm (with Indexed Binary Min-Heap) — BASELINE**
- **Mechanism:** Explores nodes outward in strict order of cumulative distance $g(n)$ using a priority queue.
- **Role:** Serves as the ground-truth comparator during empirical benchmarking.

### **3. Bellman-Ford Algorithm — THEORETICAL CONTRAST**
- **Mechanism:** Relaxes all graph edges $|V| - 1$ times iteratively.
- **Evaluation:** Disqualified for runtime routing due to $O(V \cdot E)$ execution overhead, but documented in the report for formal complexity contrast.

### **Comparative Complexity & Feature Matrix (Viva Cheatsheet)**

| Metric / Dimension | A* Search (3D Heuristic) | Dijkstra's Algorithm | Bellman-Ford Algorithm |
|---|---|---|---|
| **Worst-Case Time Complexity** | $O((V + E) \log V)$ | $O((V + E) \log V)$ | $O(V \cdot E)$ |
| **Average-Case Time Complexity** | $\Theta(b^d)$ where $b \ll V$ | $\Theta((V + E) \log V)$ | $\Theta(V \cdot E)$ |
| **Best-Case Time Complexity** | $\Omega(d \log d)$ (Direct line) | $\Omega(V \log V)$ | $\Omega(E)$ (with early break) |
| **Auxiliary Space Complexity** | $O(V + E)$ | $O(V + E)$ | $O(V)$ |
| **Directional Goal Acceleration** | **Yes** (Guided by $h(n)$) | No (Radial expansion) | No (Global iteration) |
| **Step-Free Edge Filtering** | Supported during relaxation | Supported during relaxation | Supported during relaxation |
| **Suitability for Campus Routing**| **Optimal (Selected)** | High (Baseline) | Unusable for real-time |

---

## **5. Data Structures Specification (LO2)**

1. **Custom 3D Adjacency List (`Map<String, List<Edge3D>>`):**  
   Stored in RAM as a hash map pairing each `NodeID` to an array of outgoing directed edges. Consumes strictly $O(V + E)$ memory, avoiding the prohibitive $O(V^2)$ cost of adjacency matrices on sparse corridor networks.
2. **Indexed Binary Min-Heap (`IndexedHeap`):**  
   Backed by an array with an auxiliary lookup map (`pos[node_id]`) tracking vertex positions inside the heap. Enables priority extraction (`extractMin`) in $O(\log V)$ and in-place distance relaxation (`decreaseKey`) in $O(\log V)$ without memory leaks or duplicate node entries.
3. **Predecessor & Node Coordinate Registry (`Map<String, String>`, `Map<String, Node3D>`):**  
   Stores 3D coordinate metadata for constant-time $O(1)$ heuristic calculations and allows backtracking from the destination room to the source vault in $O(L)$ time, where $L$ is path length.

---

## **6. Implementation Architecture & Work Allocation (2 Members)**

```
task1/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/idss/task1/
    │   │   ├── Task1Application.java             # Spring Boot Application (:8081)
    │   │   ├── algorithm/
    │   │   │   ├── BuildingGraph.java            # 3D Adjacency list & Spatial Index
    │   │   │   ├── IndexedHeap.java              # Indexed binary min-heap with decreaseKey O(log V)
    │   │   │   ├── AStarEngine.java              # 3D A* Search + Heuristic + Filter
    │   │   │   ├── DijkstraEngine.java           # Baseline comparator
    │   │   │   └── BellmanFordEngine.java        # Theoretical comparator
    │   │   ├── model/
    │   │   │   ├── Node3D.java / Edge3D.java     # Spatial entities
    │   │   │   ├── Coordinates3D.java            # x, y, z spatial vector
    │   │   │   ├── DispatchOrder.java            # Inbound dispatch DTO
    │   │   │   ├── DeliveryRoute.java            # Outbound route result
    │   │   │   ├── TurnByTurnStep.java           # Manifest step
    │   │   │   └── RoutingMetrics.java           # Performance & Memory metrics DTO
    │   │   ├── service/
    │   │   │   ├── RouteService.java             # Orchestrates algorithm execution & Mongo save
    │   │   │   └── ManifestGenerator.java        # Turn-by-turn instruction generator
    │   │   ├── controller/
    │   │   │   └── RouteController.java          # REST API endpoints (:8081)
    │   │   └── repository/
    │   │       └── DeliveryRouteRepository.java  # Spring Data Mongo repository
    │   └── resources/
    │       └── application.yml                   # server.port: 8081
    └── test/
        └── java/com/idss/task1/
            ├── AStarEngineTest.java              # Correctness & Step-Free constraint test
            └── BenchmarkTest.java                # Pure in-memory timing & memory benchmarks (V=10..100)
```

### **6.1 Detailed Responsibility Matrix**

```
+-----------------------------------------------------------------------------------+
|                            TASK 1 WORK ALLOCATION MATRIX                          |
+-----------------------------------------------------------------------------------+
| STUDENT A (Graph Core & Baseline Systems) | STUDENT B (Heuristics & Benchmarking) |
+-------------------------------------------+---------------------------------------+
| 1. JSON Data Ingestion Parser             | 1. Custom Indexed Binary Min-Heap     |
| 2. Custom 3D Adjacency List Architecture  | 2. A* Search Engine with 3D Heuristic |
| 3. Dijkstra's Algorithm Implementation    | 3. Turn-by-Turn Manifest Generator    |
| 4. Step-Free Edge Filtering System        | 4. Empirical Benchmarking Framework   |
| 5. Chapters 1, 2, 3, 5, 7 in Report       | 5. Chapters 4, 6, 8, 9, 10 in Report  |
+-----------------------------------------------------------------------------------+
```

#### **Student A Responsibilities:**
- **Graph Model & Parser:** Implement `BuildingGraph.java` to parse `input_building_graph.json` and build the 3D Adjacency List.
- **Baseline Algorithms:** Implement `DijkstraEngine.java` and `BellmanFordEngine.java` comparator.
- **Accessibility Engine:** Implement the step-free edge filter (ignoring stairs when `requires_step_free_access == true`).
- **Report Chapters:** Lead author on Chapter 2 (Problem Analysis), Chapter 3 (Candidate Algorithm Investigation), Chapter 5 (Data Structure Design), and Chapter 7 (Theoretical Complexity Derivation - $O, \Theta, \Omega$).

#### **Student B Responsibilities:**
- **Priority Queue Design:** Implement the array-backed `IndexedHeap.java` with position-tracking lookup array for $O(\log V)$ `decreaseKey` operations.
- **A* Search Engine:** Implement `AStarEngine.java` with 3D Euclidean distance calculation and vertical floor-transition penalty ($\beta = 3.5$).
- **Route Manifest & Formatting:** Generate the final `output_delivery_routes.json` and `output_routing_metrics.json`.
- **Empirical Benchmarking:** Build `BenchmarkTest.java` to measure latency (ms) and memory (KB) across graph sizes ($V = 10, 30, 50, 100$ up to $V = 10,000$ synthetic).
- **Report Chapters:** Lead author on Chapter 4 (Algorithm Justification), Chapter 6 (Algorithm Design & Pseudocode), Chapter 8 (Experimental Performance Evaluation with Charts), and Chapter 9 (Critical Evaluation).

---

## **7. REST API Endpoints (Gateway Route: `/api/task1/**` $\rightarrow$ `:8081`)**

| Endpoint | Method | Request Body | Response Body | Description |
|---|---|---|---|---|
| `/api/task1/route` | `POST` | `List<DispatchOrder>` | `output_delivery_routes.json` | Computes optimal routes for dispatches and saves to MongoDB |
| `/api/task1/routes/{dispatchId}` | `GET` | — | `DeliveryRoute` | Retrieves calculated route by dispatch ID |
| `/api/task1/benchmark` | `GET` | — | `output_routing_metrics.json` | Returns latest benchmark metrics |
| `/api/task1/health` | `GET` | — | `{"status": "UP"}` | Service health check |

---

## **8. Step-by-Step Development Milestones**

### **Phase 1: Data Contracts & Graph Construction (Student A)**
- Create `data/input/input_building_graph.json` with multi-floor topology.
- Implement `Node3D`, `Edge3D`, `Coordinates3D`, `DispatchOrder` domain models.
- Verify `BuildingGraph` correctly represents corridors, stairwells, and elevator shafts across Floors 0 to 3.

### **Phase 2: Core Algorithm & Heap Implementation (Student B & Student A)**
- Code `IndexedHeap.java` and verify min-heap invariants (`insert`, `extractMin`, `decreaseKey` in $O(\log V)$).
- Implement `DijkstraEngine.java` as the baseline pathfinder.

### **Phase 3: Informed A* Search & Constraint Engine (Student B & Student A)**
- Integrate 3D Euclidean heuristic with $\beta=3.5$ vertical penalty into `AStarEngine.java`.
- Apply step-free constraint filtering (prune stairs when `requires_step_free_access == true`).
- Implement `ManifestGenerator.java` for turn-by-turn courier directions.

### **Phase 4: Service, Mongo Persistence & Gateway Integration (Joint)**
- Implement `RouteService.java`, `RouteController.java`, and `DeliveryRouteRepository.java`.
- Test routing via Gateway (`http://localhost:8080/api/task1/route`).
- Wire up Next.js frontend (`frontend/app/task1/page.jsx`).

### **Phase 5: Benchmarking & Viva Preparation (Student B & Student A)**
- Execute in-memory benchmark test suite (`BenchmarkTest.java`).
- Generate $V=100$ to $V=10,000$ performance comparison charts for Chapter 8.
- Complete individual reports following the NIBM 11-chapter specification.
