package com.idss.task3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.idss.task3.coloring.BacktrackingColorer;
import com.idss.task3.coloring.DsaturColorer;
import com.idss.task3.coloring.GraphColorer;
import com.idss.task3.coloring.WelshPowellColorer;
import com.idss.task3.controller.ConflictGraphResponse;
import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.model.ClashAnalysisOutput;
import com.idss.task3.model.ConflictEdge;
import com.idss.task3.model.VertexResult;
import com.idss.task3.repository.ConflictGraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Service orchestrating complete Clash Analysis generation and output file persistence.
 */
@Service
public class ClashAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(ClashAnalysisService.class);

    private final Task3Service task3Service;
    private final ObjectMapper objectMapper;
    private final ConflictGraphRepository conflictGraphRepository;

    public ClashAnalysisService(Task3Service task3Service, ConflictGraphRepository conflictGraphRepository) {
        this.task3Service = task3Service;
        this.conflictGraphRepository = conflictGraphRepository;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public AnalysisResult analyze(ConflictGraph graph, String algorithmName) {
        GraphColorer colorer;
        boolean isExact = false;

        if ("Welsh-Powell".equalsIgnoreCase(algorithmName) || "WelshPowell".equalsIgnoreCase(algorithmName)) {
            colorer = new WelshPowellColorer();
        } else if ("Backtracking".equalsIgnoreCase(algorithmName)) {
            colorer = new BacktrackingColorer();
            isExact = true;
        } else {
            colorer = new DsaturColorer();
            algorithmName = "DSATUR";
        }

        GraphColorer.ColoringResult result = colorer.color(graph);
        Map<String, Integer> colorMap = result.getColorOf();
        int sessionsUsed = result.getNumColors();

        int violations = task3Service.countHardConstraintViolations(graph, colorMap);
        int lowerBound = task3Service.calculateLowerBound(graph);
        int upperBound = task3Service.calculateUpperBound(graph);

        int minimumSessions = isExact ? sessionsUsed : Math.min(sessionsUsed, lowerBound > 0 ? lowerBound : sessionsUsed);
        // If lower bound equals sessions used, we know it's optimal
        if (sessionsUsed == lowerBound) {
            minimumSessions = sessionsUsed;
        }

        String status = task3Service.determineStatus(sessionsUsed, lowerBound, violations, isExact);

        // Update VertexResult objects in graph. colorMap is keyed by course_code
        // (DsaturColorer keys off ConflictGraph.getExamRegistry(), which
        // ConflictGraphBuilder populates from exam.course_code, not exam_id) —
        // looking it up by exam_id always missed, leaving every vertex's
        // color/sessionIndex at their default 0 and every session group empty.
        for (VertexResult vr : graph.getVertices()) {
            Integer c = colorMap.get(vr.getCourseCode());
            if (c != null) {
                vr.setColor(c);
                vr.setSessionIndex(c);
            }
        }

        // Build ConflictGraphResponse
        ConflictGraphResponse graphResponse = new ConflictGraphResponse();
        graphResponse.setGenerationTimestamp(Instant.now().toString());
        graphResponse.setStatus(status);
        graphResponse.setAlgorithmUsed(algorithmName);
        graphResponse.setTotalExams(graph.getVertices().size());
        graphResponse.setVertices(graph.getVertices());
        graphResponse.setEdges(graph.getEdges());
        graphResponse.setGraphDensity(graph.getGraphDensity());

        // Build ClashAnalysisOutput
        ClashAnalysisOutput analysisOutput = new ClashAnalysisOutput();
        analysisOutput.setMinimumSessions(minimumSessions);
        analysisOutput.setSessionsUsed(sessionsUsed);
        analysisOutput.setLowerBound(lowerBound);
        analysisOutput.setUpperBound(upperBound);
        analysisOutput.setColoringValid(violations == 0);
        analysisOutput.setHardConstraintViolations(violations);
        analysisOutput.setClashPairs(graph.getEdges());

        // Group exams by session
        List<List<String>> sessionGroups = new ArrayList<>();
        for (int i = 0; i < sessionsUsed; i++) {
            sessionGroups.add(new ArrayList<>());
        }
        for (VertexResult vr : graph.getVertices()) {
            int session = vr.getSessionIndex();
            if (session > 0 && session <= sessionsUsed) {
                sessionGroups.get(session - 1).add(vr.getExamId());
            }
        }
        analysisOutput.setSessionGroups(sessionGroups);

        // Persist JSON files
        writeOutputFiles(graphResponse, analysisOutput);

        // Persist to MongoDB 'conflict_graph' collection (non-fatal — route
        // computation must not be blocked by database availability, matching
        // the pattern used by task1's RouteController.persistRoutes).
        try {
            conflictGraphRepository.save(graphResponse);
        } catch (Exception e) {
            log.warn("Failed to persist conflict graph to MongoDB (result still returned to caller): {}", e.getMessage());
        }

        return new AnalysisResult(graphResponse, analysisOutput);
    }

    private void writeOutputFiles(ConflictGraphResponse graphResponse, ClashAnalysisOutput analysisOutput) {
        try {
            File dir = new File("../data/shared");
            if (!dir.exists()) dir = new File("data/shared");
            if (!dir.exists()) dir.mkdirs();

            objectMapper.writeValue(new File(dir, "output_conflict_graph.json"), graphResponse);
            objectMapper.writeValue(new File(dir, "output_clash_analysis.json"), analysisOutput);
            log.info("Successfully persisted output_conflict_graph.json and output_clash_analysis.json to {}", dir.getCanonicalPath());
        } catch (IOException e) {
            log.error("Failed to write output JSON files", e);
        }
    }

    public static class AnalysisResult {
        private final ConflictGraphResponse graphResponse;
        private final ClashAnalysisOutput analysisOutput;

        public AnalysisResult(ConflictGraphResponse graphResponse, ClashAnalysisOutput analysisOutput) {
            this.graphResponse = graphResponse;
            this.analysisOutput = analysisOutput;
        }

        public ConflictGraphResponse getGraphResponse() { return graphResponse; }
        public ClashAnalysisOutput getAnalysisOutput() { return analysisOutput; }
    }
}
