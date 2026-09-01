package com.idss.task3.controller;

import com.idss.common.model.Exam;
import com.idss.common.util.JsonLoader;
import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.service.ClashAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task3")
public class ClashController {

    private final ClashAnalysisService clashAnalysisService;

    @Value("${task3.input.enrollments-path}")
    private String enrollmentsPath;

    @Value("${task3.input.exams-path}")
    private String examsPath;

    public ClashController(ClashAnalysisService clashAnalysisService) {
        this.clashAnalysisService = clashAnalysisService;
    }

    @PostMapping("/detect")
    public ResponseEntity<?> detectClashes(
            @RequestBody(required = false) List<StudentEnrollment> enrollments,
            @RequestParam(defaultValue = "DSATUR") String algorithm) {
        try {
            if (enrollments == null || enrollments.isEmpty()) {
                enrollments = JsonLoader.loadList(enrollmentsPath, StudentEnrollment.class);
            }

            List<Exam> exams = JsonLoader.loadList(examsPath, Exam.class);

            ConflictGraphBuilder.Builder builder = new ConflictGraphBuilder.Builder().withExams(exams);
            ConflictGraph graph = builder.build(enrollments);

            ClashAnalysisService.AnalysisResult analysis = clashAnalysisService.analyze(graph, algorithm);

            Map<String, Object> result = new HashMap<>();
            result.put("conflict_graph", analysis.getGraphResponse());
            result.put("clash_analysis", analysis.getAnalysisOutput());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Clash detection failed"
            ));
        }
    }
}
