package com.idss.task1.repository;

import com.idss.task1.model.DeliveryRoute;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@code delivery_routes} collection
 * (Student B Deliverable - Subtask 4.4).
 *
 * <p>{@link DeliveryRoute#getDispatchId()} is the {@code @Id} key, so
 * {@code findById}/{@code save} operate on {@code dispatch_id} directly.</p>
 */
public interface DeliveryRouteRepository extends MongoRepository<DeliveryRoute, String> {
}
