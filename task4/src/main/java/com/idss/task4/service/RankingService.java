package com.idss.task4.service;

import com.idss.common.model.Room;
import com.idss.common.util.JsonLoader;
import com.idss.task4.algorithm.FilterEngine;
import com.idss.task4.algorithm.RoomRegistry;
import com.idss.task4.algorithm.TOPSISEngine;
import com.idss.task4.dto.ExamRequest;
import com.idss.task4.dto.RoomRanking;
import com.idss.task4.dto.RoomReference;
import com.idss.task4.dto.RoomScore;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private static final String ROOM_MASTER_PATH = "data/input/input_room_master.json";

    private RoomRegistry roomRegistry;
    private final FilterEngine filterEngine;
    private final TOPSISEngine topsisEngine;

    public RankingService() {
        this.filterEngine = new FilterEngine();
        this.topsisEngine = new TOPSISEngine();
    }

    /**
     * Loads the room master data at startup and builds the HashMap registry.
     */
    @PostConstruct
    public void init() throws IOException {
        List<Room> rooms = JsonLoader.loadList(ROOM_MASTER_PATH, Room.class);
        this.roomRegistry = new RoomRegistry(rooms);
        System.out.println("[Task4] RoomRegistry loaded: " + roomRegistry.size() + " rooms");
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

        return rankings;
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
        List<RoomReference> references = new ArrayList<>();
        for (Room room : roomRegistry.getAllRooms()) {
            references.add(new RoomReference(
                    room.room_id,
                    room.room_name,
                    room.floor,
                    room.is_accessible
            ));
        }
        return references;
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
}
