# IDSS for Campus &amp; Exam Operations

Intelligent Decision Support System (IDSS) for University Campus &amp; Exam Operations.
BSc (Hons) Computing 26.1 | PDSA | Single Building Campus.

This repository is a multi-module Maven project based on
`docs/master_context_file.md` (MCF v2.0) and
`docs/group_data_contracts.md` (Canonical Contracts v1.0).

## Modules

| Module  | Package           | Port | Responsibility                                      |
|---------|-------------------|------|-----------------------------------------------------|
| common  | com.idss.common   | —    | Shared models, JSON loader, Mongo connection, canonical mapping |
| gateway | com.idss.gateway  | 8080 | Spring Cloud Gateway — routes + CORS                |
| task1   | com.idss.task1    | 8081 | Paper logistics &amp; routing (A*)                   |
| task2   | com.idss.task2    | 8082 | Invigilator assignment (Hungarian)                  |
| task3   | com.idss.task3    | 8083 | Clash detection (DSATUR)                            |
| task4   | com.idss.task4    | 8084 | Room ranking (AHP + TOPSIS)                         |
| task5   | com.idss.task5    | 8085 | Timetable optimization (GA + SA + Greedy)           |

## Tech Stack (Locked)

- **Backend:** Java 17+, Spring Boot 3.2.5, Maven, Spring Cloud Gateway
- **Frontend:** Next.js 14+ (App Router), TypeScript, Tailwind CSS
- **DB:** MongoDB 7 (`idss` database, one collection per output type)
- **Testing:** JUnit 5 (via spring-boot-starter-test)

## Build

```bash
mvn clean install
```

All eight modules (common + gateway + task1..task5) must compile and tests must pass.

## Run

**Startup order (MCF Section 2.2):**
MongoDB → task3 &amp; task4 → task5 → task2 → task1 → gateway → frontend

```bash
# Ensure MongoDB Atlas cluster is accessible (see .env)
mvn clean install
java -jar gateway/target/gateway.jar
java -jar task3/target/task3.jar
java -jar task4/target/task4.jar
java -jar task5/target/task5.jar
java -jar task2/target/task2.jar
java -jar task1/target/task1.jar

# Frontend (separate terminal)
cd frontend &amp;&amp; npm install &amp;&amp; npm run dev
```

The Next.js frontend calls the API Gateway at `http://localhost:8080` only.

## Configuration

1. Copy `.env.example` to `.env`.
2. Set `MONGO_URI` and `MONGO_DATABASE` for your MongoDB instance.

`MongoConnection` reads these via dotenv-java. The `DatabaseSeeder` loads
`data/input/*.json` and inserts them into the Mongo collections listed in
MCF Section 5.

## Data Layout

- `data/input/`  - committed synthetic input datasets (`input_*.json`).
- `data/shared/` - runtime-generated outputs (`output_*.json`), gitignored.

## Canonical Contracts

Field names, formats, and aliases are defined in
`docs/group_data_contracts.md` (and summarized in `docs/master_context_file.md`
Section 6). Never invent field names; map legacy aliases (`ROOM_R101`,
`noise_level: "low"`, etc.) to the canonical form before reading or writing
JSON. The shared `com.idss.common.config.Canonical` helper centralizes this
mapping.
