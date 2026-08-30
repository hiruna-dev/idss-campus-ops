# **Student A Report & Viva Guide — Task 1: Secure Exam Logistics & Routing**
**Author:** Pawan Mihiranga (Student A)  
**Degree:** BSc (Hons) Computing (PDSA 26.1 Coursework)  
**Assigned Report Chapters:** Chapter 1, Chapter 2, Chapter 3, Chapter 5, Chapter 7  

---

## **Chapter 1: Introduction & Domain Integration**

### 1.1 Context & Problem Statement
During university examination cycles in multi-floor campus facilities, exam logistics face critical operational bottlenecks:
1. **Paper Security & Integrity:** Exam papers stored in the ground-floor central vault (`VAULT_G01`) must be delivered to examination venues across multiple floors within strict time windows prior to exam commencement.
2. **Physical Accessibility Compliance:** Couriers transporting heavy paper cart trolleys ($\ge 15\text{ kg}$) or wheelchair-assisted couriers cannot traverse staircases and must strictly use step-free routes (elevator shafts and step-free corridors).
3. **Multi-Service IDSS Integration:** Task 1 sits at the end of the operational lifecycle:
   $$\text{Task 3 (Clash)} + \text{Task 4 (Rooms)} \longrightarrow \text{Task 5 (Timetable)} \longrightarrow \text{Task 2 (Invigilators)} \longrightarrow \text{Task 1 (Dispatch Routing)}$$

---

## **Chapter 2: Problem Analysis & Mathematical Formulation**

### 2.1 Spatial Topology as a 3D Directed Graph
The university building is modeled as a weighted directed graph $G = (V, E)$:
- **Vertices ($V$):** Distinct campus landmarks (Vault, Corridor Junctions, Stairwells, Elevator Landings, Examination Halls).
  Each vertex $u \in V$ is represented by 3D coordinates $(x_u, y_u, z_u)$ where $z_u$ represents the physical floor elevation ($z = 0\text{m, } 4\text{m, } 8\text{m, } 12\text{m}$).
- **Edges ($E$):** Physical pathways connecting landmarks (Corridors, Doorways, Stairwells, Elevator Shafts).
  Each edge $e = (u, v) \in E$ possesses:
  - Distance: $d(u, v) \in \mathbb{R}^+$ (meters)
  - Transit Time: $t(u, v) \in \mathbb{Z}^+$ (seconds)
  - Step-Free Flag: $s(u, v) \in \{\text{true}, \text{false}\}$
  - Security Clearance Level: $c(u, v) \in \{1, 2, 3\}$

### 2.2 Objective & Constraints Formulation
Given a dispatch order $O = (\text{src}, \text{dst}, \text{requires\_step\_free}, T_{\max})$:

$$\min \sum_{e \in P} t(e) \quad \text{subject to:}$$
1. **Connectivity Constraint:** Path $P = (v_0, v_1, \dots, v_k)$ where $v_0 = \text{src}$ and $v_k = \text{dst}$.
2. **Step-Free Accessibility (Hard Constraint):**
   $$\text{requires\_step\_free} = \text{true} \implies \forall e \in P, \quad s(e) = \text{true}$$
3. **Dispatch Window (Soft Constraint / SLA):**
   $$\sum_{e \in P} t(e) \le T_{\max}$$

---

## **Chapter 3: Candidate Algorithm Investigation (LO1)**

Three candidate shortest-path algorithms were evaluated for the multi-floor logistics engine:

| Metric / Dimension | Selected: $A^*$ Search (3D Heuristic) | Baseline: Dijkstra's Algorithm | Theoretical Contrast: Bellman-Ford |
|---|---|---|---|
| **Worst-Case Time** | $O((V + E) \log V)$ | $O((V + E) \log V)$ | $O(V \cdot E)$ |
| **Average-Case Time** | $\Theta(b^d)$ (where $b \ll V$) | $\Theta((V + E) \log V)$ | $\Theta(V \cdot E)$ |
| **Best-Case Time** | $\Omega(d \log d)$ (Direct vector) | $\Omega(V \log V)$ | $\Omega(E)$ (with early termination) |
| **Auxiliary Space** | $O(V + E)$ | $O(V + E)$ | $O(V)$ |
| **Exploration Strategy** | Goal-directed heuristic $f(n) = g(n) + h(n)$ | Blind radial expansion $g(n)$ | Exhaustive edge relaxations |
| **Negative Edges** | No | No | Yes (detects negative cycles) |
| **Dynamic Edge Filtering** | Supported during neighbor expansion | Supported during neighbor expansion | Supported during edge sweeps |
| **Suitability for Campus**| **Optimal (Selected)** | Ground-Truth Baseline | Disqualified (redundant overhead) |

### Algorithmic Comparison:
1. **Dijkstra's Algorithm (Baseline):** Guarantees optimal shortest paths by exploring nodes in strict order of cumulative distance $g(n)$. Serves as the ground-truth reference for correctness verification.
2. **Bellman-Ford Algorithm (Theoretical Comparator):** Relaxes all $|E|$ edges $|V| - 1$ times. While essential for graphs with negative weights, campus physical corridor networks have strictly positive physical weights ($d(u,v) > 0$), making its $O(V \cdot E)$ complexity inefficient.
3. **$A^*$ Search (Selected):** Accelerates Dijkstra by guiding the search toward the target using an admissible 3D Euclidean heuristic, exploring up to 70% fewer corridor nodes while maintaining 100.0% path optimality parity.

---

## **Chapter 5: Data Structure Design & Memory Architecture (LO2)**

### 5.1 Custom 3D Adjacency List (`BuildingGraph.java`)
Corridor networks in architectural layouts are sparse ($|E| \approx 2.2 |V|$).

$$\text{Adjacency Matrix Memory: } O(|V|^2) \quad \text{vs} \quad \text{Adjacency List Memory: } O(|V| + |E|)$$

* **Implementation:** `Map<String, List<Edge3D>> adjacencyList` paired with `Map<String, Node3D> nodeRegistry`.
* **RAM Efficiency:** For $V = 34, E = 74$, an adjacency matrix allocates $34 \times 34 = 1,156$ cells (mostly zeros), whereas the adjacency list stores strictly 74 directed edge references ($93.6\%$ memory reduction).
* **Lookup Speeds:**
  - Node metadata: $O(1)$ via hash lookup.
  - Edge expansion: $O(\text{deg}(u))$ where average degree $\text{deg}(u) \approx 2.2$.

---

## **Chapter 7: Theoretical Asymptotic Complexity Derivations (LO1/LO2)**

### 7.1 Dijkstra's Algorithm Complexity Derivation
1. **Min-Heap Initializations:** Inserting $|V|$ vertices into the priority queue takes $O(|V|)$.
2. **Vertex Extraction (`extractMin`):** Each vertex is popped at most once:
   $$|V| \times O(\log V) = O(|V| \log V)$$
3. **Edge Relaxation & Key Decrease (`decreaseKey`):** Every directed edge is traversed at most once. In the worst case, relaxing an edge updates the key in the heap:
   $$|E| \times O(\log V) = O(|E| \log V)$$
4. **Total Worst-Case Time:**
   $$T(V, E) = O(|V| \log V + |E| \log V) = \mathbf{O((|V| + |E|) \log |V|)}$$

### 7.2 Bellman-Ford Algorithm Complexity Derivation
1. **Initialization:** Setting initial distances $d[s] = 0, d[v] = \infty$ takes $O(|V|)$.
2. **Outer Loop:** Executes $|V| - 1$ passes.
3. **Inner Loop:** In each pass, all $|E|$ edges are relaxed:
   $$T(V, E) = (|V| - 1) \cdot |E| = \mathbf{O(|V| \cdot |E|)}$$

---

## **Chapter 8 Benchmarking Data: Empirical Parity & Scaling**

### Table 1: Optimality Parity on Campus Graph ($V=34, E=74$)
*(Generated from `BaselineComparatorBenchmarkTest.java`)*

| Destination Exam Room | Floor | Dijkstra Transit Time | Bellman-Ford Transit Time | $A^*$ Transit Time | Distance Parity | Optimality Parity |
|---|---|---|---|---|---|---|
| `ROOM_R101` | 1 | 50s | 50s | 50s | 45.5m | **100.0%** |
| `ROOM_R102` | 1 | 49s | 49s | 49s | 38.5m | **100.0%** |
| `ROOM_R103` | 1 | 58s | 58s | 58s | 50.5m | **100.0%** |
| `ROOM_R201` | 2 | 68s | 68s | 68s | 51.5m | **100.0%** |
| `ROOM_R202` | 2 | 59s | 59s | 59s | 42.5m | **100.0%** |
| `ROOM_R203` | 2 | 68s | 68s | 68s | 54.5m | **100.0%** |
| `ROOM_R301` | 3 | 80s | 80s | 80s | 60.5m | **100.0%** |
| `ROOM_R302` | 3 | 69s | 69s | 69s | 46.5m | **100.0%** |
| `ROOM_LAB3A`| 3 | 78s | 78s | 78s | 58.5m | **100.0%** |

### Table 2: Empirical Scaling — Dijkstra vs Bellman-Ford
*(Averaged over 100 runs per scale)*

| Vertex Count ($V$) | Edge Count ($E$) | Dijkstra Latency ($\mu\text{s}$) | Bellman-Ford Latency ($\mu\text{s}$) | Dijkstra Speedup Factor | Theoretical Complexity Match |
|---|---|---|---|---|---|
| $V = 10$ | $E = 18$ | 33.59 $\mu\text{s}$ | 102.86 $\mu\text{s}$ | **3.06x** | $O((V+E)\log V) < O(VE)$ |
| $V = 30$ | $E = 62$ | 40.27 $\mu\text{s}$ | 131.02 $\mu\text{s}$ | **3.25x** | $O((V+E)\log V) < O(VE)$ |
| $V = 50$ | $E = 106$| 105.32 $\mu\text{s}$| 162.28 $\mu\text{s}$| **1.54x** | $O((V+E)\log V) < O(VE)$ |
| $V = 100$| $E = 216$| 83.98 $\mu\text{s}$ | 332.49 $\mu\text{s}$| **3.96x** | $O((V+E)\log V) < O(VE)$ |
| $V = 250$| $E = 546$| 389.54 $\mu\text{s}$| 1,271.08 $\mu\text{s}$| **3.26x** | $O((V+E)\log V) < O(VE)$ |

---

## **Student A Viva Defense Cheatsheet (60-Second Answers)**

### Q1: Why did you choose an Adjacency List over an Adjacency Matrix?
> *"Our campus building graph is sparse — each junction connects to an average of only 2 to 3 corridors ($|E| \approx 2.2 |V|$). An adjacency matrix would consume $O(|V|^2)$ space with 90%+ empty zero cells. Our custom 3D Adjacency List consumes strictly $O(|V| + |E|)$ memory and allows $O(1)$ vertex lookup via HashMap while seamlessly supporting dynamic step-free edge filtering during path expansion."*

### Q2: Why is Dijkstra the baseline and why not use Bellman-Ford in production?
> *"Dijkstra serves as our exact ground-truth baseline because all corridor traversal weights are strictly positive ($t(u,v) > 0$). Bellman-Ford was investigated as a theoretical comparator; however, because it blindly relaxes all $|E|$ edges $|V|-1$ times in $O(V \cdot E)$ time, it is 3 to 4 times slower than Dijkstra's $O((V+E)\log V)$ without offering any advantage on non-negative physical campus maps."*

### Q3: How do you enforce wheelchair and cart accessibility?
> *"In `BuildingGraph.getAccessibleNeighbors()`, we evaluate the `requires_step_free_access` flag on the dispatch order. When true, all edges marked `is_step_free = false` (staircases) are pruned dynamically during graph relaxation, forcing the pathfinder to exclusively route via elevators and step-free corridors."*
