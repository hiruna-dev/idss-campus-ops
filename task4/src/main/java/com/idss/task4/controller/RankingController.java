package com.idss.task4.controller;

import com.idss.common.util.JsonLoader;
import com.idss.task4.dto.ExamRequest;
import com.idss.task4.dto.RoomRanking;
import com.idss.task4.dto.RoomReference;
import com.idss.task4.repository.RoomRankingDocument;
import com.idss.task4.service.RankingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Task 4 — Room Ranking.
 * Base path: {@code /api/task4} (master_context_file.md Section 3.4).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /rank} — rank rooms for a single exam request</li>
 *   <li>{@code POST /rank-all} — rank rooms for all exams (batch)</li>
 *   <li>{@code GET /rankings/{examId}} — retrieve cached rankings for an exam</li>
 *   <li>{@code GET /room-reference} — room lookup table for Task 1 & Task 5</li>
 *   <li>{@code GET /health} — health check</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/task4")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Ranks eligible rooms for a single exam request.
     * Body: ExamRequest JSON. Response: List of RoomRanking.
     */
    @PostMapping("/rank")
    public ResponseEntity<List<RoomRanking>> rankForExam(@RequestBody ExamRequest exam) {
        long start = System.nanoTime();
        List<RoomRanking> rankings = rankingService.rankForExam(exam);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("[Task4] Ranked " + rankings.size() +
                " rooms for exam " + exam.getExamId() + " in " + elapsedMs + "ms");

        if (rankings.isEmpty()) {
            return ResponseEntity.unprocessableEntity().body(rankings);
        }
        return ResponseEntity.ok(rankings);
    }

    /**
     * Batch: ranks rooms for all exams loaded from input_exam_requests.json,
     * writes output_room_rankings.json to data/shared/, and returns results.
     */
    @PostMapping("/rank-all")
    public ResponseEntity<Map<String, Object>> rankAllExams() {
        try {
            List<ExamRequest> exams = JsonLoader.loadList(
                    "data/input/input_exam_requests.json", ExamRequest.class);

            long start = System.nanoTime();
            List<RoomRanking> allRankings = rankingService.rankForAllExams(exams);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            // Write output files
            JsonLoader.write("data/shared/output_room_rankings.json", allRankings);

            List<RoomReference> references = rankingService.generateRoomReference();
            JsonLoader.write("data/shared/output_room_reference.json", references);

            // Build response with metrics
            Map<String, Object> response = new HashMap<>();
            response.put("status", allRankings.isEmpty() ? "INFEASIBLE" : "OPTIMAL");
            response.put("algorithm_used", "AHP (weights) + TOPSIS (ranking)");
            response.put("execution_time_ms", elapsedMs);
            response.put("total_exams_processed", exams.size());
            response.put("total_rankings_generated", allRankings.size());
            response.put("hard_constraint_violations", 0);
            response.put("rankings", allRankings);
            response.put("room_reference", references);

            System.out.println("[Task4] Batch: " + allRankings.size() +
                    " rankings for " + exams.size() + " exams in " + elapsedMs + "ms");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("reason", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Retrieves cached rankings for a specific exam from MongoDB.
     * Required by MCF Section 3.4 and task_4_plan.md Section 12.
     *
     * <p>Task 5 calls this endpoint to pull one exam's ranked rooms
     * without triggering a re-computation. Returns 404 if rankings
     * have not yet been computed for the given exam.</p>
     *
     * @param examId the exam ID (e.g. "EX_101")
     * @return the rankings list with metadata, or 404 if not found
     */
    @GetMapping("/rankings/{examId}")
    public ResponseEntity<Map<String, Object>> getRankingsForExam(@PathVariable String examId) {
        Optional<RoomRankingDocument> result = rankingService.findRankingsByExamId(examId);

        if (result.isEmpty()) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("status", "NOT_FOUND");
            notFound.put("exam_id", examId);
            notFound.put("message", "No cached rankings found for exam " + examId +
                    ". Call POST /rank or POST /rank-all first.");
            return ResponseEntity.status(404).body(notFound);
        }

        RoomRankingDocument doc = result.get();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("exam_id", doc.getExamId());
        response.put("algorithm_used", doc.getAlgorithmUsed());
        response.put("computed_at", doc.getComputedAt().toString());
        response.put("total_rankings", doc.getRankings().size());
        response.put("rankings", doc.getRankings());
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the room reference lookup table for Task 1 & Task 5.
     */
    @GetMapping("/room-reference")
    public ResponseEntity<List<RoomReference>> getRoomReference() {
        return ResponseEntity.ok(rankingService.generateRoomReference());
    }

    /**
     * Returns the full static room master for frontend details (capacity, AC, noise).
     */
    @GetMapping("/rooms")
    public ResponseEntity<java.util.Collection<com.idss.common.model.Room>> getRooms() {
        return ResponseEntity.ok(rankingService.getAllRooms());
    }

    /**
     * Health check (master_context_file.md Section 10).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "task4-room-ranking");
        return ResponseEntity.ok(status);
    }
}
