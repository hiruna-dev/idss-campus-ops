package com.idss.task3.controller;

import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.service.ClashAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/task3")
public class ClashController {

    private final ClashAnalysisService clashAnalysisService;

    @Autowired
    public ClashController(ClashAnalysisService clashAnalysisService) {
        this.clashAnalysisService = clashAnalysisService;
    }

    @PostMapping("/detect")
    public ResponseEntity<Map<String, Object>> detectClashes(
            @RequestBody List<StudentEnrollment> enrollments,
            @RequestParam(defaultValue = "DSATUR") String algorithm) {

        ConflictGraphBuilder.Builder builder = new ConflictGraphBuilder.Builder();
        ConflictGraph graph = builder.build(enrollments);

        ClashAnalysisService.AnalysisResult analysis = clashAnalysisService.analyze(graph, algorithm);

        Map<String, Object> result = new HashMap<>();
        result.put("conflict_graph", analysis.getGraphResponse());
        result.put("clash_analysis", analysis.getAnalysisOutput());

        return ResponseEntity.ok(result);
    }
}

