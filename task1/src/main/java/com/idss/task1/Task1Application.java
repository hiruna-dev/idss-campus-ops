package com.idss.task1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application Entry Point for Task 1 (Port :8081).
 *
 * <p>Subsystem: Secure Exam Paper Logistics & Accessible Multi-Floor Indoor Routing.
 * Dispatches paper delivery routes using 3D Euclidean A* Search and Indexed Min-Heap.</p>
 */
@SpringBootApplication
public class Task1Application {

    public static void main(String[] args) {
        SpringApplication.run(Task1Application.class, args);
    }
}
