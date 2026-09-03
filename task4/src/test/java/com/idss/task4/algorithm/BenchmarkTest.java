package com.idss.task4.algorithm;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idss.common.model.Room;
import com.idss.common.util.JsonLoader;
import com.idss.task4.dto.RoomScore;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Experimental performance evaluation for Chapter 8/9 of the Individual
 * Report — runs SAW, TOPSIS, AHP, and Fuzzy MCDM directly (no HTTP, no
 * Mongo, no Gateway) per MCF Section 9.1, so {@code execution_time_ms}
 * stays clean. Sizes and run count follow MCF Section 9.1: n = 10/50/500
 * rooms for Task 4, 30 runs per size.
 *
 * <p>Writes raw results to {@code data/shared/benchmark_task4.json}
 * (gitignored runtime output, master_context_file.md Section 8.1) so
 * Chapter 8 tables/charts are built from real measured data, not
 * projections.</p>
 */
class BenchmarkTest {

    private static final int[] SIZES = {10, 50, 500};
    private static final int RUNS_PER_SIZE = 30;
    private static final int WARMUP_RUNS = 5;
    private static final int FIXED_STUDENT_COUNT = 40;
    private static final String OUTPUT_PATH = "data/shared/benchmark_task4.json";

    @Test
    void runBenchmarkAndWriteResults() throws IOException {
        ObjectNode root = JsonLoader.mapper().createObjectNode();
        root.put("generated_at", Instant.now().toString());
        root.put("runs_per_size", RUNS_PER_SIZE);
        root.put("warmup_runs", WARMUP_RUNS);
        root.put("fixed_student_count", FIXED_STUDENT_COUNT);

        ArrayNode results = root.putArray("results");
        ArrayNode ahpResults = root.putArray("ahp_weight_derivation");

        for (int n : SIZES) {
            List<Room> rooms = generateRooms(n, /*seed*/ 42L + n);
            List<Room> eligible = new FilterEngine().filter(rooms, examFor(FIXED_STUDENT_COUNT));

            // Sanity check per MCF checklist: every eligible room must satisfy the hard constraints.
            long violations = eligible.stream()
                    .filter(r -> r.capacity < FIXED_STUDENT_COUNT)
                    .count();
            assertEquals(0, violations, "FilterEngine must never leak a capacity violation");

            results.add(benchmarkEngine("SAW", n, eligible, new SAWEngine()::score));
            results.add(benchmarkEngine("TOPSIS", n, eligible, new TOPSISEngine()::score));
            results.add(benchmarkEngine("FuzzyMCDM", n, eligible, new FuzzyMCDMEngine()::score));

            ahpResults.add(benchmarkAhp(n));
        }

        File outputFile = resolveOutputFile();
        outputFile.getParentFile().mkdirs();
        JsonLoader.mapper().writerWithDefaultPrettyPrinter().writeValue(outputFile, root);
        System.out.println("[Task4 Benchmark] Results written to " + outputFile.getAbsolutePath());
    }

    /**
     * Resolves data/shared/benchmark_task4.json against the repo root regardless of
     * whether the JVM's cwd is the repo root or task4/ (mvn -pl task4 test sets cwd
     * to task4/ — see CLAUDE.md's Maven cwd gotcha).
     */
    private static File resolveOutputFile() {
        File cwd = new File(System.getProperty("user.dir"));
        File repoRoot = "task4".equals(cwd.getName()) ? cwd.getParentFile() : cwd;
        return new File(repoRoot, OUTPUT_PATH);
    }

    /** Generates n synthetic rooms with a fixed seed per size, for reproducibility. */
    private List<Room> generateRooms(int n, long seed) {
        Random random = new Random(seed);
        List<Room> rooms = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Room room = new Room();
            room.room_id = "BR" + i;
            room.room_name = "Benchmark Room " + i;
            room.floor = random.nextInt(5);
            room.capacity = 20 + random.nextInt(131); // 20..150, spans both sides of FIXED_STUDENT_COUNT
            room.has_ac = random.nextBoolean();
            room.noise_level = 1 + random.nextInt(5); // 1..5
            room.accessibility_score = 1 + random.nextInt(5); // 1..5
            room.is_accessible = room.accessibility_score >= 4;
            rooms.add(room);
        }
        return rooms;
    }

    private com.idss.task4.dto.ExamRequest examFor(int studentCount) {
        return new com.idss.task4.dto.ExamRequest(
                "BENCH_EXAM", "BENCH101", "Benchmark Exam", studentCount, false, null);
    }

    /** Times and memory-samples one engine's score() over RUNS_PER_SIZE measured runs, after warmup. */
    private ObjectNode benchmarkEngine(String label, int n, List<Room> eligible,
                                        Function<List<Room>, List<RoomScore>> scorer) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            scorer.apply(eligible);
        }

        long totalNanos = 0L;
        long minNanos = Long.MAX_VALUE;
        long maxNanos = Long.MIN_VALUE;
        long totalMemoryBytes = 0L;
        Runtime runtime = Runtime.getRuntime();

        for (int i = 0; i < RUNS_PER_SIZE; i++) {
            System.gc();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();

            long start = System.nanoTime();
            List<RoomScore> scored = scorer.apply(eligible);
            long elapsed = System.nanoTime() - start;

            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            // Guard against a GC landing between the two reads and producing a negative delta.
            long memDelta = Math.max(0L, memAfter - memBefore);

            totalNanos += elapsed;
            minNanos = Math.min(minNanos, elapsed);
            maxNanos = Math.max(maxNanos, elapsed);
            totalMemoryBytes += memDelta;

            if (scored.isEmpty() && !eligible.isEmpty()) {
                throw new IllegalStateException(label + " returned no scores for a non-empty eligible set");
            }
        }

        ObjectNode row = JsonLoader.mapper().createObjectNode();
        row.put("algorithm", label);
        row.put("room_count_n", n);
        row.put("eligible_count_k", eligible.size());
        row.put("avg_time_ms", nanosToMs(totalNanos / (double) RUNS_PER_SIZE));
        row.put("min_time_ms", nanosToMs(minNanos));
        row.put("max_time_ms", nanosToMs(maxNanos));
        row.put("avg_memory_kb", (totalMemoryBytes / (double) RUNS_PER_SIZE) / 1024.0);
        return row;
    }

    /** Times AHP weight derivation (constructor work) in isolation — expected ~constant regardless of n. */
    private ObjectNode benchmarkAhp(int n) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            new AHPEngine();
        }

        long totalNanos = 0L;
        long totalMemoryBytes = 0L;
        Runtime runtime = Runtime.getRuntime();

        for (int i = 0; i < RUNS_PER_SIZE; i++) {
            System.gc();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();

            long start = System.nanoTime();
            AHPEngine engine = new AHPEngine();
            engine.getWeights(); // touch the result so JIT can't eliminate the call
            totalNanos += System.nanoTime() - start;

            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            totalMemoryBytes += Math.max(0L, memAfter - memBefore);
        }

        ObjectNode row = JsonLoader.mapper().createObjectNode();
        row.put("size_context_n", n);
        row.put("avg_time_ms", nanosToMs(totalNanos / (double) RUNS_PER_SIZE));
        row.put("avg_memory_kb", (totalMemoryBytes / (double) RUNS_PER_SIZE) / 1024.0);
        return row;
    }

    private double nanosToMs(double nanos) {
        return Math.round((nanos / 1_000_000.0) * 10000.0) / 10000.0;
    }
}
