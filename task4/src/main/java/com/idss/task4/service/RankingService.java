package com.idss.task4.service;

import com.idss.common.model.Room;
import com.idss.task4.algorithm.AHPEngine;
import com.idss.task4.algorithm.FilterEngine;
import com.idss.task4.algorithm.RoomRegistry;
import com.idss.task4.algorithm.TOPSISEngine;
import com.idss.task4.dto.ExamRequest;
import com.idss.task4.dto.RoomRanking;
import com.idss.task4.dto.RoomReference;
import com.idss.task4.dto.RoomScore;
import com.idss.task4.repository.RoomRankingDocument;
import com.idss.task4.repository.RoomRankingRepository;

import com.idss.common.util.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Orchestration service connecting the Room Registry, Filter Engine,
 * and TOPSIS Scoring Engine into the full ranking pipeline.
 *
 * <p><b>Pipeline per exam request:</b>
 * <ol>
 *   <li>RoomRegistry → all rooms (O(1) lookup backing)</li>
 *   <li>FilterEngine → eligible rooms (O(n) filter)</li>
 *   <li>TOPSISEngine → scored rooms (O(n·m) TOPSIS)</li>
 *   <li>PriorityQueue → ranked output (O(k log k) extraction)</li>
 * </ol>
 *
 * <p>The PriorityQueue is a max-heap (via RoomScore's Comparable),
 * so polling yields rooms in descending score order, which maps
 * directly to rank 1, 2, 3, ... (task_4_plan.md Section 9).</p>
 */
@Service
public class RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingService.class);
    private static final String ROOMS_COLLECTION = "rooms";
    private static final String ALGORITHM_LABEL = "AHP (weights) + TOPSIS (ranking)";

    @Value("${task4.input.room-master-path:data/input/input_room_master.json}")
    private String roomMasterPath;

    private RoomRegistry roomRegistry;
    private final FilterEngine filterEngine;
    private final AHPEngine ahpEngine;
    private final TOPSISEngine topsisEngine;
    private final RoomRankingRepository rankingRepository;
    private final MongoTemplate mongoTemplate;

    public RankingService(RoomRankingRepository rankingRepository, MongoTemplate mongoTemplate) {
        this.rankingRepository = rankingRepository;
        this.mongoTemplate = mongoTemplate;
        this.filterEngine = new FilterEngine();
        // Weights are derived via AHP (task_4_plan.md Section 6) rather than
        // asserted — see AHPEngine for the pairwise comparison + CR check.
        this.ahpEngine = new AHPEngine();
        double[] w = ahpEngine.getWeights();
        this.topsisEngine = new TOPSISEngine(w[0], w[1], w[2]);
    }

    /**
     * Loads the room master data at startup and builds the HashMap registry.
     */
    @PostConstruct
    public void init() {
        try {
            List<Room> rooms = JsonLoader.loadList(roomMasterPath, Room.class);
            this.roomRegistry = new RoomRegistry(rooms);
            System.out.println("[Task4] RoomRegistry loaded: " + roomRegistry.size() + " rooms from JSON '" + roomMasterPath + "'");
        } catch (Exception e) {
            System.err.println("[Task4] Failed to load RoomRegistry from JSON: " + e.getMessage());
        }
    }

    /**
     * Ranks all eligible rooms for a single exam request.
     *
     * @param exam the exam request containing student_count and accessibility flag
     * @return ranked list of RoomRanking DTOs (rank 1 = best), may be empty
     */
    public List<RoomRanking> rankForExam(ExamRequest exam) {
        // Step 1: Filter — O(n)
        List<Room> eligible = filterEngine.filter(roomRegistry.getAllRooms(), exam);

        if (eligible.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 2: Score via TOPSIS — O(n·m) where m=3
        List<RoomScore> scores = topsisEngine.score(eligible);

        // Step 3: Insert into PriorityQueue (max-heap) — O(k log k) total
        PriorityQueue<RoomScore> maxHeap = new PriorityQueue<>(scores);

        // Step 4: Poll in descending order to assign ranks 1..k
        List<RoomRanking> rankings = new ArrayList<>(scores.size());
        int rank = 1;
        while (!maxHeap.isEmpty()) {
            RoomScore rs = maxHeap.poll();
            rankings.add(new RoomRanking(
                    exam.getExamId(),
                    rs.getRoomId(),
                    rank++,
                    rs.getScore(),
                    true  // all rooms in the output passed hard constraints
            ));
        }

        // Persist to MongoDB — upsert by examId (which is the @Id). Non-fatal:
        // ranking computation must not be blocked by database availability
        // (master_context_file.md Section 2.4), matching task1/task3/task5.
        try {
            RoomRankingDocument doc = new RoomRankingDocument(
                    exam.getExamId(), ALGORITHM_LABEL, rankings, Instant.now());
            rankingRepository.save(doc);
        } catch (Exception e) {
            log.warn("Failed to persist room rankings to MongoDB (result still returned to caller): {}", e.getMessage());
        }

        return rankings;
    }

    /**
     * Retrieves cached rankings for a specific exam from MongoDB.
     * This backs the {@code GET /rankings/{examId}} endpoint
     * required by MCF Section 3.4 and task_4_plan.md Section 12.
     *
     * @param examId the exam ID to look up (e.g. "EX_101")
     * @return Optional containing the ranking document, or empty if not yet computed
     */
    public Optional<RoomRankingDocument> findRankingsByExamId(String examId) {
        return rankingRepository.findById(examId);
    }

    /**
     * Ranks rooms for all exam requests (batch processing).
     * Used to generate the complete output_room_rankings.json.
     *
     * @param exams list of all exam requests
     * @return combined ranked list across all exams
     */
    public List<RoomRanking> rankForAllExams(List<ExamRequest> exams) {
        List<RoomRanking> allRankings = new ArrayList<>();
        for (ExamRequest exam : exams) {
            allRankings.addAll(rankForExam(exam));
        }
        return allRankings;
    }

    /**
     * Generates the output_room_reference.json lookup table.
     * Contains room_id, room_name, floor, is_accessible for every room
     * in the registry (consumed by Task 1 and Task 5).
     *
     * @return list of RoomReference DTOs
     */
    public List<RoomReference> generateRoomReference() {
        List<RoomReference> refs = new ArrayList<>();
        for (Room r : roomRegistry.getAllRooms()) {
            refs.add(new RoomReference(r.room_id, r.room_name, r.floor, r.is_accessible));
        }
        return refs;
    }

    public java.util.Collection<Room> getAllRooms() {
        return roomRegistry.getAllRooms();
    }

    /**
     * Returns the underlying registry for direct lookups.
     */
    public RoomRegistry getRoomRegistry() {
        return roomRegistry;
    }

    /**
     * Returns the TOPSIS engine (for benchmarking/testing access to weights).
     */
    public TOPSISEngine getTopsisEngine() {
        return topsisEngine;
    }

    /**
     * Returns the AHP engine (for benchmarking/testing access to the
     * derived weights and Consistency Ratio).
     */
    public AHPEngine getAhpEngine() {
        return ahpEngine;
    }
}
