package com.idss.task4.algorithm.jmh;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.File;

/**
 * Drives {@link RankingBenchmarks} via the real JMH runner (not a hand-rolled
 * timing loop) and writes raw results as JMH's own JSON format to
 * {@code data/shared/benchmark_task4_jmh.json} for Chapter 8/9.
 *
 * <p>Run with {@code mvn -pl task4 test -Dtest=JmhBenchmarkRunnerTest}.
 * Takes roughly 1-2 minutes (12 trials: 4 benchmark methods x 3 room-count
 * sizes, each in its own forked JVM).</p>
 */
class JmhBenchmarkRunnerTest {

    private static final String OUTPUT_PATH = "data/shared/benchmark_task4_jmh.json";

    @Test
    void runJmhBenchmarks() throws RunnerException {
        File outputFile = resolveOutputFile();
        outputFile.getParentFile().mkdirs();

        Options options = new OptionsBuilder()
                .include(RankingBenchmarks.class.getSimpleName())
                .forks(1)
                .warmupIterations(3)
                .warmupTime(TimeValue.milliseconds(500))
                .measurementIterations(5)
                .measurementTime(TimeValue.milliseconds(500))
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result(outputFile.getAbsolutePath())
                .build();

        new Runner(options).run();
        System.out.println("[Task4 JMH Benchmark] Results written to " + outputFile.getAbsolutePath());
    }

    /**
     * Resolves data/shared/benchmark_task4_jmh.json against the repo root regardless of
     * whether the JVM's cwd is the repo root or task4/ (mvn -pl task4 test sets cwd
     * to task4/ — see CLAUDE.md's Maven cwd gotcha).
     */
    private static File resolveOutputFile() {
        File cwd = new File(System.getProperty("user.dir"));
        File repoRoot = "task4".equals(cwd.getName()) ? cwd.getParentFile() : cwd;
        return new File(repoRoot, OUTPUT_PATH);
    }
}
