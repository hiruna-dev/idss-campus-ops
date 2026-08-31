package com.idss.task2.controller;

import com.idss.common.model.Invigilator;
import com.idss.common.util.JsonLoader;
import com.idss.task2.model.MasterScheduleEntry;
import com.idss.task2.model.ProctorRoster;
import com.idss.task2.model.RosterEntry;
import com.idss.task2.model.RosterMetrics;
import com.idss.task2.service.AssignmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task2")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Value("${task2.input.invigilators-path}")
    private String invigilatorsPath;

    @Value("${task2.output.roster-path}")
    private String rosterOutputPath;

    @Value("${task2.output.metrics-path}")
    private String metricsOutputPath;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    //assigns invigilators to exams in the given master schedule
    //loads invigilators from json, runs the hungarian algo via service, writes output files
    @PostMapping("/assign")
    public ResponseEntity<ProctorRoster> assign(@RequestBody List<MasterScheduleEntry> schedule) {
        //loading invigilators from the input json file
        List<Invigilator> invigilators;
        try {
            invigilators = JsonLoader.loadList(invigilatorsPath, Invigilator.class);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        //running the assignment via the service (cost matrix -> hungarian -> decode -> save to mongo)
        ProctorRoster roster = assignmentService.assignInvigilators(schedule, invigilators);

        //writing the roster and metrics to shared json files (non-fatal if it fails)
        try {
            JsonLoader.write(rosterOutputPath, roster);
            JsonLoader.write(metricsOutputPath, assignmentService.getLastMetrics());
        } catch (Exception e) {
            //file write failed but the roster is still returned to the caller
        }

        return ResponseEntity.ok(roster);
    }

    //gets the roster entry for a single exam by exam_id
    //queries the latest roster from mongo and finds the matching entry
    @GetMapping("/roster/{exam_id}")
    public ResponseEntity<RosterEntry> getRosterEntry(@PathVariable("exam_id") String examId) {
        RosterEntry entry = assignmentService.getRosterEntry(examId);
        //returning 404 if the exam isnt in any roster
        if (entry == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entry);
    }

    //returns the benchmark metrics from the last assignment run
    //stored in memory so its lost on restart
    @GetMapping("/benchmark")
    public ResponseEntity<RosterMetrics> benchmark() {
        RosterMetrics metrics = assignmentService.getLastMetrics();
        if (metrics == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(metrics);
    }
}
