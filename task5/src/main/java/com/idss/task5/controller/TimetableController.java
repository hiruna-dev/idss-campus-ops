package com.idss.task5.controller;

import com.idss.task5.dto.MasterScheduleEntry;
import com.idss.task5.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Generates timetable from local JSON files.
     * Optional query param: ?algorithm=GA|SA|GREEDY (default: GA)
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTimetable(
            @RequestParam(value = "algorithm", defaultValue = "GA") String algorithm) {
        try {
            Map<String, Object> result = timetableService.generateFromLocalFiles(algorithm);

            String status = (String) result.get("status");
            if ("INFEASIBLE".equals(status)) {
                return ResponseEntity.unprocessableEntity().body(result);
            }

            return ResponseEntity.ok(result);
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
     * GET /api/task5/health
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "task5-timetable"));
    }
}