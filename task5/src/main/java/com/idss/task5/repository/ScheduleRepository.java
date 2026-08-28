package com.idss.task5.repository;

import com.idss.task5.dto.MasterScheduleEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB repository for master schedule entries.
 * Collection: master_schedules
 * Consumed by Task 2 (reads this collection as their input).
 */
@Repository
public interface ScheduleRepository extends MongoRepository<MasterScheduleEntry, String> {
    // Built-in methods used: deleteAll(), saveAll(), findAll()
    // No custom query methods needed — avoids snake_case parsing issues
}