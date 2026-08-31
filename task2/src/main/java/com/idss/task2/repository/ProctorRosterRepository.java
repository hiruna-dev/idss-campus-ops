package com.idss.task2.repository;

import com.idss.task2.model.ProctorRoster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProctorRosterRepository extends MongoRepository<ProctorRoster, String> {
}
