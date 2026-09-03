package com.idss.task4.repository;

import com.idss.task4.dto.RoomRanking;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document for persisted room rankings.
 * Collection: {@code room_rankings} (task_4_plan.md Section 1 architecture).
 *
 * <p>Each document stores the complete ranked room list for one exam,
 * keyed by {@code exam_id}. This enables {@code GET /rankings/{examId}}
 * to return cached results without re-running TOPSIS.</p>
 *
 * <p><b>Design choice:</b> one document per exam (not one per room-ranking row)
 * because the query pattern is always "get all rankings for exam X" — storing
 * them as an embedded list avoids N+1 queries and matches the
 * {@code output_room_rankings.json} shape exactly.</p>
 */
@Document(collection = "room_rankings")
public class RoomRankingDocument {

    /**
     * Exam ID used as the Mongo _id — guarantees uniqueness per exam
     * and makes upserts natural (re-ranking the same exam overwrites).
     */
    @Id
    private String examId;

    /**
     * The algorithm combination used to produce these rankings.
     * Stored for auditability (task_4_plan.md Section 8.6).
     */
    @Field("algorithm_used")
    private String algorithmUsed;

    /**
     * Ranked room list for this exam, ordered by rank ascending.
     * Each entry matches the {@link RoomRanking} DTO schema.
     */
    @Field("rankings")
    private List<RoomRanking> rankings;

    /**
     * Timestamp when these rankings were computed.
     * Allows consumers to check freshness.
     */
    @Field("computed_at")
    private Instant computedAt;

    public RoomRankingDocument() {}

    public RoomRankingDocument(String examId, String algorithmUsed,
                                List<RoomRanking> rankings, Instant computedAt) {
        this.examId = examId;
        this.algorithmUsed = algorithmUsed;
        this.rankings = rankings;
        this.computedAt = computedAt;
    }

    // --- Getters & Setters ---

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getAlgorithmUsed() { return algorithmUsed; }
    public void setAlgorithmUsed(String algorithmUsed) { this.algorithmUsed = algorithmUsed; }

    public List<RoomRanking> getRankings() { return rankings; }
    public void setRankings(List<RoomRanking> rankings) { this.rankings = rankings; }

    public Instant getComputedAt() { return computedAt; }
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }

    @Override
    public String toString() {
        return "RoomRankingDocument{examId='" + examId +
               "', algorithmUsed='" + algorithmUsed +
               "', rankingsCount=" + (rankings != null ? rankings.size() : 0) +
               ", computedAt=" + computedAt + "}";
    }
}
