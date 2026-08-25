package com.idss.task5.repository;

import com.idss.task5.dto.MasterScheduleEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * MongoDB repository for master schedule entries.
 * Collection: master_schedules
 * Consumed by Task 2 (reads this collection as their input).
 */
@Repository
public interface ScheduleRepository extends MongoRepository<MasterScheduleEntry, String> {

    Optional<MasterScheduleEntry> findByExam_id(String examId);

    List<MasterScheduleEntry> findByDateAndSession(String date, String session);

    void deleteAllByExam_idIn(List<String> examIds);
}