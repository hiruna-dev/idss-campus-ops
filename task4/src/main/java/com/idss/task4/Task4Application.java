package com.idss.task4;

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
        SpringApplication.run(Task4Application.class, args);
    }
}
