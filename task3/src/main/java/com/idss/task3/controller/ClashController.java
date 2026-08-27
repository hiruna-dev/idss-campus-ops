package com.idss.task3.controller;

import com.idss.task3.algorithm.DSATUR;
import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.model.VertexResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/task3")
public class ClashController {

    @PostMapping("/detect")
    public ResponseEntity<Map<String, Object>> detectClashes(@RequestBody List<StudentEnrollment> enrollments) {
        
        ConflictGraphBuilder.Builder builder = new ConflictGraphBuilder.Builder();
        ConflictGraph graph = builder.build(enrollments);
        
        int maxColor = DSATUR.run(graph);
        int minimumSessions = maxColor > 0 ? maxColor : 0;
        
        ConflictGraphResponse graphResponse = new ConflictGraphResponse();
        graphResponse.setGenerationTimestamp(Instant.now().toString());
        graphResponse.setStatus(graph.isValid() ? "OPTIMAL" : "INFEASIBLE");
        graphResponse.setAlgorithmUsed("DSATUR");
        graphResponse.setTotalExams(graph.getVertices().size());
        graphResponse.setVertices(graph.getVertices());
        graphResponse.setEdges(graph.getEdges());
        graphResponse.setGraphDensity(graph.getGraphDensity());
        
        ClashAnalysisResponse analysisResponse = new ClashAnalysisResponse();
        analysisResponse.setMinimumSessions(minimumSessions);
        analysisResponse.setSessionsUsed(minimumSessions);
        
        int maxDegree = 0;
        for (VertexResult vr : graph.getVertices()) {
            if (vr.getDegree() > maxDegree) {
                maxDegree = vr.getDegree();
            }
        }
        
        analysisResponse.setLowerBound(maxColor);
        analysisResponse.setUpperBound(maxDegree + 1);
        analysisResponse.setClashPairs(graph.getEdges().size());
        
        List<List<String>> sessionGroups = new ArrayList<>();
        for (int i = 0; i < minimumSessions; i++) {
            sessionGroups.add(new ArrayList<>());
        }
        for (VertexResult vr : graph.getVertices()) {
            if (vr.getSessionIndex() > 0 && vr.getSessionIndex() <= minimumSessions) {
                sessionGroups.get(vr.getSessionIndex() - 1).add(vr.getExamId());
            }
        }
        analysisResponse.setSessionGroups(sessionGroups);
        
        Map<String, Object> result = new HashMap<>();
        result.put("conflict_graph", graphResponse);
        result.put("clash_analysis", analysisResponse);
        
        try {
            java.io.File dir = new java.io.File("../data/shared");
            if (!dir.exists()) dir = new java.io.File("data/shared");
            if (!dir.exists()) dir.mkdirs();
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new java.io.File(dir, "output_conflict_graph.json"), graphResponse);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new java.io.File(dir, "output_clash_analysis.json"), analysisResponse);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
    }
}
