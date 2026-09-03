package com.idss.task3.repository;

import com.idss.task3.controller.ConflictGraphResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@code conflict_graph} collection
 * (master_context_file.md Section 5).
 *
 * <p>{@link ConflictGraphResponse#getGenerationTimestamp()} is the {@code @Id}
 * key, so {@code findById}/{@code save} operate on {@code generation_timestamp}
 * directly.</p>
 */
public interface ConflictGraphRepository extends MongoRepository<ConflictGraphResponse, String> {
}
