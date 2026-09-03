package com.idss.task4.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for {@link RoomRankingDocument}.
 * Collection: {@code room_rankings}.
 *
 * <p>Since {@code examId} is the {@code @Id}, Spring Data's built-in
 * {@code findById(examId)} handles the primary lookup. Additional
 * query methods are provided for batch operations.</p>
 *
 * <p><b>Query performance:</b> {@code findById} uses the {@code _id} index
 * (always present in MongoDB) — O(log n) B-tree lookup, no extra index needed.</p>
 */
@Repository
public interface RoomRankingRepository extends MongoRepository<RoomRankingDocument, String> {

    /**
     * Returns all stored ranking documents.
     * Used by the batch retrieval endpoint if needed.
     * (Inherited from MongoRepository as findAll(), but explicit for clarity.)
     */
    @Override
    List<RoomRankingDocument> findAll();
}
