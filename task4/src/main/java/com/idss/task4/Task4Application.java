package com.idss.task4;

import com.idss.common.config.MongoConnection;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Task 4 — Intelligent Decision Module (Room Ranking Service).
 * Runs on port 8084 (master_context_file.md Section 2.1).
 *
 * <p>Algorithms: AHP (weight derivation) + TOPSIS (ranking).
 * Selected over SAW baseline and Fuzzy MCDM comparator
 * (task_4_plan.md Section 8.6).</p>
 */
@SpringBootApplication
public class Task4Application {

    public static void main(String[] args) {
        loadEnvIntoSystemProperties();
        SpringApplication.run(Task4Application.class, args);
    }

    /**
     * Loads the gitignored {@code .env} (if present) into System properties
     * so application.yml's {@code ${MONGO_URI:...}} / {@code ${MONGO_DATABASE:...}}
     * placeholders can resolve real credentials without committing them to a
     * tracked file. An explicit OS environment variable always wins over
     * {@code .env} — this only fills in what isn't already set.
     */
    private static void loadEnvIntoSystemProperties() {
        Dotenv dotenv = MongoConnection.loadEnv();
        setIfAbsent("MONGO_URI", dotenv.get("MONGO_URI"));
        setIfAbsent("MONGO_DATABASE", dotenv.get("MONGO_DATABASE"));
    }

    private static void setIfAbsent(String key, String value) {
        if (value != null && !value.isBlank()
                && System.getProperty(key) == null
                && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
