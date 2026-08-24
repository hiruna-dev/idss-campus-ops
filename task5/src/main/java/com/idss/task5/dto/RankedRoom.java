package com.idss.task5.dto;

/**
 * Per-exam ranked room entry from Task 4's output_room_rankings.json.
 * One row per eligible room per exam, TOPSIS-ranked.
 * Score = closeness coefficient 0-1 (higher = better).
 * Task 5 uses Map<exam_id, List<RankedRoom>> — room selection is O(1): take list[0].
 */
public class RankedRoom {
    public String exam_id;
    public String room_id;
    public int rank;
    public double score;
    public boolean meets_hard_constraints;
}