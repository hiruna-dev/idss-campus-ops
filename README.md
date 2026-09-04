# IDSS for Campus & Exam Operations

Intelligent Decision Support System (IDSS) for University Campus & Exam Operations.
A multi-module microservices project that solves five operational problems for a
single-building campus: paper logistics routing, invigilator assignment, exam clash
detection, room ranking, and timetable optimization.

## Modules

| Module  | Package           | Port | Responsibility                                      |
|---------|-------------------|------|-----------------------------------------------------|
| common  | com.idss.common   | —    | Shared models, JSON loader, Mongo connection, canonical field mapping |
| gateway | com.idss.gateway  | 8080 | Spring Cloud Gateway — routes requests + CORS       |
| task1   | com.idss.task1    | 8081 | Paper logistics & routing (A*, Dijkstra, Bellman-Ford) |
| task2   | com.idss.task2    | 8082 | Invigilator assignment (Hungarian algorithm)        |
| task3   | com.idss.task3    | 8083 | Clash detection (DSATUR / Welsh-Powell / Backtracking) |
| task4   | com.idss.task4    | 8084 | Room ranking (AHP + TOPSIS / SAW / Fuzzy MCDM)      |
| task5   | com.idss.task5    | 8085 | Timetable optimization (GA + SA + Greedy)           |

## Tech Stack

- **Backend:** Java 17+, Spring Boot 3.2.5, Maven, Spring Cloud Gateway
- **Frontend:** Next.js (App Router), React, Tailwind CSS, shadcn/ui
- **Database:** MongoDB 7 (`idss` database, one collection per output type)
- **Testing:** JUnit 5 (via spring-boot-starter-test)

**Repository:** https://github.com/hiruna-dev/idss-campus-ops

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+** (or use the included `mvnw` / `mvnw.cmd` wrapper)
- **Node.js 18+** and npm (for the frontend)
- **MongoDB 7** — a local instance or a MongoDB Atlas cluster

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/hiruna-dev/idss-campus-ops.git
cd idss-campus-ops
```

### 2. Configure environment variables

Copy the example env file and fill in your MongoDB connection details:

```bash
cp .env.example .env
```

Edit `.env`:

```dotenv
MONGO_URI=mongodb+srv://<username>:<password>@<cluster>.mongodb.net
MONGO_DATABASE=idss

# Optional: override the data root used by the JSON loader / database seeder.
# Defaults to the project root (parent of the module that calls it).
DATA_DIR=data
```

The `.env` file is gitignored — never commit real credentials.

### 3. Build the backend

```bash
mvn clean install
```

All seven modules (`common` + `gateway` + `task1`..`task5`) must compile and their
tests must pass. On Windows you can use `.\mvnw.cmd clean install` instead.

### 4. Start the backend services

Startup order matters — downstream tasks depend on upstream ones being available:

```
MongoDB  →  task3 & task4  →  task5  →  task2  →  task1  →  gateway
```

Run each service in its own terminal:

```bash
java -jar task3/target/task3-1.0.0.jar
java -jar task4/target/task4-1.0.0.jar
java -jar task5/target/task5-1.0.0.jar
java -jar task2/target/task2-1.0.0.jar
java -jar task1/target/task1-1.0.0.jar
java -jar gateway/target/gateway-1.0.0.jar
```

The API Gateway listens on `http://localhost:8080` and routes all frontend traffic
to the individual task services.

### 5. Start the frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000` in your browser. The Next.js app talks only to the
API Gateway at `http://localhost:8080`.

To override the gateway URL (e.g. when the backend runs elsewhere), set:

```bash
NEXT_PUBLIC_API_URL=http://<host>:8080
```

## API Reference

All endpoints are served through the gateway at `http://localhost:8080`.

### Task 1 — Paper Logistics & Routing  (`/api/task1`)

| Method | Path                  | Description                                            |
|--------|-----------------------|--------------------------------------------------------|
| GET    | `/dispatch-orders`    | Seed dispatch order list (read-only)                   |
| GET    | `/building-graph`     | Seed building graph node list (read-only)              |
| POST   | `/route`              | Compute optimal routes for dispatch orders (body optional) |
| GET    | `/routes/{dispatchId}`| Retrieve a previously computed route                   |
| GET    | `/benchmark`          | Latest empirical benchmark metrics                     |
| GET    | `/health`             | Health check                                           |

### Task 2 — Invigilator Assignment  (`/api/task2`)

| Method | Path                | Description                                            |
|--------|---------------------|--------------------------------------------------------|
| GET    | `/invigilators`     | Seed invigilator list (read-only)                      |
| POST   | `/assign`           | Assign invigilators to a master schedule (body optional) |
| GET    | `/roster/{exam_id}` | Get the roster entry for a single exam                 |
| GET    | `/benchmark`        | Metrics from the last assignment run                   |

### Task 3 — Clash Detection  (`/api/task3`)

| Method | Path            | Description                                                  |
|--------|-----------------|--------------------------------------------------------------|
| GET    | `/exams`        | Seed exam list (read-only)                                   |
| GET    | `/enrollments`  | Seed student enrollment list (read-only)                     |
| POST   | `/detect`       | Detect clashes; optional `?algorithm=DSATUR\|WELSH_POWELL\|BACKTRACKING` |
| GET    | `/health`       | Health check                                                 |

### Task 4 — Room Ranking  (`/api/task4`)

| Method | Path                  | Description                                            |
|--------|-----------------------|--------------------------------------------------------|
| POST   | `/rank`               | Rank eligible rooms for a single exam request          |
| POST   | `/rank-all`           | Batch-rank rooms for all exams; writes output files    |
| GET    | `/rankings/{examId}`  | Retrieve cached rankings for an exam from MongoDB       |
| GET    | `/room-reference`     | Room lookup table (used by Task 1 & Task 5)            |
| GET    | `/rooms`              | Full static room master (capacity, AC, noise)          |
| GET    | `/health`             | Health check                                           |

### Task 5 — Timetable Optimization  (`/api/task5`)

| Method | Path          | Description                                                  |
|--------|---------------|--------------------------------------------------------------|
| POST   | `/generate`   | Generate a timetable; optional `?algorithm=GA\|SA\|GREEDY` (default: GA) |
| GET    | `/schedule`   | Current master schedule from MongoDB                         |
| GET    | `/timeslots`  | Configured timeslots                                         |
| GET/POST | `/benchmark` | Run the full benchmark suite (E=10,30,50,100) for all 3 algorithms |
| GET    | `/health`     | Health check                                                 |

## Frontend Pages

| Route        | Description                                              |
|--------------|----------------------------------------------------------|
| `/`          | Landing page                                             |
| `/dashboard` | Overview dashboard with live counts and metrics         |
| `/task1`     | Paper logistics & routing UI                            |
| `/task2`     | Invigilator assignment UI                               |
| `/task3`     | Clash detection UI                                      |
| `/task4`     | Room ranking UI                                         |
| `/task5`     | Timetable optimization UI                               |

## Data Layout

- `data/input/`  — committed synthetic input datasets (`input_*.json` and benchmark sets).
- `data/shared/` — runtime-generated outputs (`output_*.json`). This directory is tracked
  but its contents are gitignored; outputs are produced when you call the POST endpoints.

## Canonical Field Mapping

Field names, formats, and aliases are centralized in the shared
`com.idss.common.config.Canonical` helper. Never invent field names — map legacy
aliases (e.g. `ROOM_R101`, `noise_level: "low"`) to the canonical form before
reading or writing JSON.

## Running Tests

Run the full test suite from the project root:

```bash
mvn test
```

To run tests for a single module:

```bash
mvn test -pl task1
```

## Project Structure

```
idss-campus-ops/
├── common/        # Shared models, JSON loader, Mongo connection, canonical mapping
├── gateway/       # Spring Cloud Gateway (routes + CORS)
├── task1/         # Paper logistics & routing
├── task2/         # Invigilator assignment
├── task3/         # Clash detection
├── task4/         # Room ranking
├── task5/         # Timetable optimization
├── frontend/      # Next.js frontend
├── data/
│   ├── input/     # Committed synthetic input datasets
│   └── shared/    # Runtime-generated outputs (gitignored)
├── .env.example   # Template for environment variables
├── mvnw / mvnw.cmd  # Maven wrappers
└── pom.xml        # Parent POM (multi-module)
```

## Notes

- The `.env` file is gitignored. Always copy `.env.example` to `.env` and fill in
  your own MongoDB credentials.
- Route/timetable computation never fails the HTTP response if MongoDB is
  unavailable — results are still returned to the caller; persistence failures
  are only logged.
- HTTP `422 Unprocessable Entity` indicates a hard constraint was violated
  (e.g. an infeasible assignment or routing problem).
