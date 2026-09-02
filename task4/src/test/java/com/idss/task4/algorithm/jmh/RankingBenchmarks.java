package com.idss.task4.algorithm.jmh;

import com.idss.common.model.Room;
import com.idss.task4.algorithm.AHPEngine;
import com.idss.task4.algorithm.FilterEngine;
import com.idss.task4.algorithm.FuzzyMCDMEngine;
import com.idss.task4.algorithm.SAWEngine;
import com.idss.task4.algorithm.TOPSISEngine;
import com.idss.task4.dto.ExamRequest;
import com.idss.task4.dto.RoomScore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark state and methods for Chapter 8/9's experimental evaluation
 * (MCF Section 9.1: "JUnit 5 + JMH ... for execution_time_ms").
 *
 * <p>Replaces the hand-rolled {@code System.nanoTime()} loop in
 * {@code BenchmarkTest} — JMH forks a clean JVM per trial, runs an
 * adaptive warm-up until the JIT is fully tiered up, and (via
 * {@code GCProfiler}) reports actual bytes allocated per operation
 * instead of a noisy {@code Runtime} heap-delta snapshot.</p>
 *
 * <p>Dataset generation mirrors {@code BenchmarkTest}'s methodology
 * (seeded synthetic rooms, fixed exam request, capacity 20-150) so
 * results are directly comparable to Chapter 8's earlier JUnit-based run.</p>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class RankingBenchmarks {

    @Param({"10", "50", "500"})
    public int n;

    private List<Room> eligibleRooms;
    private SAWEngine sawEngine;
    private TOPSISEngine topsisEngine;
    private FuzzyMCDMEngine fuzzyMCDMEngine;

    @Setup(Level.Trial)
    public void setUp() {
        List<Room> rooms = generateRooms(n, 42L + n);
        ExamRequest exam = new ExamRequest("BENCH_EXAM", "BENCH101", "Benchmark Exam", 40, false, null);
        this.eligibleRooms = new FilterEngine().filter(rooms, exam);
        this.sawEngine = new SAWEngine();
        this.topsisEngine = new TOPSISEngine();
        this.fuzzyMCDMEngine = new FuzzyMCDMEngine();
    }

    private List<Room> generateRooms(int n, long seed) {
        Random random = new Random(seed);
        List<Room> rooms = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Room room = new Room();
            room.room_id = "BR" + i;
            room.room_name = "Benchmark Room " + i;
            room.floor = random.nextInt(5);
            room.capacity = 20 + random.nextInt(131);
            room.has_ac = random.nextBoolean();
            room.noise_level = 1 + random.nextInt(5);
            room.accessibility_score = 1 + random.nextInt(5);
            room.is_accessible = room.accessibility_score >= 4;
            rooms.add(room);
        }
        return rooms;
    }

    @Benchmark
    public List<RoomScore> sawScore() {
        return sawEngine.score(eligibleRooms);
    }

    @Benchmark
    public List<RoomScore> topsisScore() {
        return topsisEngine.score(eligibleRooms);
    }

    @Benchmark
    public List<RoomScore> fuzzyMcdmScore() {
        return fuzzyMCDMEngine.score(eligibleRooms);
    }

    /**
     * Measures AHP weight derivation (constructor work) in isolation.
     * Independent of {@code eligibleRooms} — included per {@code n} context
     * only so Chapter 8 can show it stays flat while the other three grow.
     */
    @Benchmark
    public double[] ahpWeightDerivation() {
        return new AHPEngine().getWeights();
    }
}
