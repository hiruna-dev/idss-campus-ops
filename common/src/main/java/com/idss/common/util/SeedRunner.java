package com.idss.common.util;

import com.idss.common.util.DatabaseSeeder;

/**
 * Entry point for running {@link DatabaseSeeder} standalone.
 *
 * <p>Run via Maven:</p>
 * <pre>
 * mvn -pl common -am compile exec:java -Dexec.mainClass="com.idss.common.util.SeedRunner"
 * </pre>
 *
 * <p>Requires a {@code .env} file in the project root with {@code MONGO_URI}
 * and {@code MONGO_DATABASE} pointing to a reachable MongoDB instance
 * (e.g. an Atlas cluster). Input JSON files must exist in {@code data/input/}.</p>
 */
public final class SeedRunner {

    private SeedRunner() {
        throw new AssertionError("SeedRunner is an entry point; do not instantiate.");
    }

    public static void main(String[] args) {
        System.out.println("[SeedRunner] Starting database seed...");
        try {
            DatabaseSeeder.seed();
            System.out.println("[SeedRunner] Seed completed successfully.");
        } catch (Exception e) {
            System.err.println("[SeedRunner] Seed failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
