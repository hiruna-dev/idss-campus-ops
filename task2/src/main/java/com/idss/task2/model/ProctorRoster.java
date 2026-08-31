package com.idss.task2.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "proctor_rosters")
public class ProctorRoster {
    @Id
    public String generation_timestamp;
    public String status;
    public int total_shifts_allocated;
    public List<RosterEntry> roster;
}
