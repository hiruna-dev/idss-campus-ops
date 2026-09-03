package com.idss.task5.controller;

import com.idss.task5.dto.MasterScheduleEntry;
import com.idss.task5.dto.TimetableRequest;
import com.idss.task5.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Task 5 — Timetable Generation.
 * 
 * Endpoints (all via Gateway at :8080/api/task5/**):
 * - POST /api/task5/generate   — Run timetable generation
 * - GET  /api/task5/schedule   — Get current master schedule
 * - GET  /api/task5/health     — Health check
 * 
 * Default algorithm is GA. Pass ?algorithm=SA or ?algorithm=GREEDY to override.
 */
@RestController
@RequestMapping("/api/task5")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /**
     * POST /api/task5/generate
     * Generates timetable from the request payload, or local JSON files when the
     * body is omitted or empty.
     * Optional query param: ?algorithm=GA|SA|GREEDY (default: GA)
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTimetable(
            @RequestParam(value = "algorithm", defaultValue = "GA") String algorithm,
            @RequestBody(required = false) TimetableRequest request) {
        try {
            Map<String, Object> result = request == null || !request.hasData()
                    ? timetableService.generateFromLocalFiles(algorithm)
                    : timetableService.generateFromRequest(request, algorithm);

            String status = (String) result.get("status");
            if ("INFEASIBLE".equals(status)) {
                return ResponseEntity.unprocessableEntity().body(result);
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage(), "status", "ERROR")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage(), "status", "ERROR")
            );
        }
    }

    /**
     * GET /api/task5/schedule
     * Returns the current master schedule from MongoDB.
     */
    @GetMapping("/schedule")
    public ResponseEntity<List<MasterScheduleEntry>> getSchedule() {
        List<MasterScheduleEntry> schedule = timetableService.getCurrentSchedule();
        return ResponseEntity.ok(schedule);
    }

    /**
     * GET /api/task5/timeslots
     * Returns the configured timeslots.
     */
    @GetMapping("/timeslots")
    public ResponseEntity<List<com.idss.common.model.Timeslot>> getTimeslots() {
        try {
            return ResponseEntity.ok(timetableService.getTimeslots());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/task5/health
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "task5-timetable"));
    }

        /**
     * GET or POST /api/task5/benchmark
     * Runs the full benchmark suite (E=10,30,50,100) for all 3 algorithms.
     * Returns JSON table for Chapter 8 charts.
     */
    @RequestMapping(value = "/benchmark", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> runBenchmark() {
        try {
            List<Map<String, Object>> results = com.idss.task5.benchmark.BenchmarkRunner.runFullBenchmark();

            File dataRoot = new File("data/input").isDirectory()
                    ? new File("data/shared")
                    : new File("../data/shared");
            com.idss.task5.benchmark.BenchmarkRunner.exportResults(
                    results, new File(dataRoot, "benchmark_results.json").getPath());

            return ResponseEntity.ok(Map.of(
                "status", "COMPLETED",
                "results", results
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage(), "status", "ERROR")
            );
        }
    }
}