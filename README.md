# IDSS for Campus &amp; Exam Operations

Intelligent Decision Support System (IDSS) for University Campus &amp; Exam Operations.
BSc (Hons) Computing 26.1 | PDSA | Single Building Campus.

This repository is a multi-module Maven project initialized from
`guidelines/master_context_file.md` (MCF v1.0) and
`guidelines/group_data_contracts.md` (Canonical Contracts v1.0).

## Modules

| Module  | Package           | Responsibility                                      |
|---------|-------------------|-----------------------------------------------------|
| common  | com.idss.common   | Shared models, JSON loader, Mongo connection, theme |
| task1   | com.idss.task1    | Paper logistics &amp; routing (A*)                   |
| task2   | com.idss.task2    | Invigilator assignment (Hungarian)                  |
| task3   | com.idss.task3    | Clash detection (DSATUR)                            |
| task4   | com.idss.task4    | Room ranking (AHP + TOPSIS)                         |
| task5   | com.idss.task5    | Timetable optimization (GA + SA + Greedy)           |

## Tech Stack (Locked)

- Java 17+, Maven 3.9+
- MongoDB driver `mongodb-driver-sync:4.11.1`
- Jackson `jackson-databind:2.16.1` + `jackson-datatype-jsr310:2.16.1`
- dotenv-java `3.0.0`
- JUnit 5 `junit-jupiter:5.10.1`

## Build

```bash
mvn clean install
```

All six modules (common + task1..task5) must compile and tests must pass.

## Run

```bash
mvn clean install && java -jar task5/target/task5.jar
```

Build &amp; runtime order (MCF Section 2.2): `task3 + task4 (parallel) -> task5 -> task2 -> task1`.
Each task module exposes its own main entry point; the integration shell
launches them in this order. (Task main classes are added by each pair during
implementation; the `java -jar` command above is the target workflow per
MCF Section 8.1.)

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
