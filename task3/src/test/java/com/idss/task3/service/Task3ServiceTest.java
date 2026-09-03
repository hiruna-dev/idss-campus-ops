package com.idss.task3.service;

import com.idss.task3.coloring.BacktrackingColorer;
import com.idss.task3.coloring.DsaturColorer;
import com.idss.task3.coloring.GraphColorer;
import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Task3ServiceTest {

    private Task3Service task3Service;
    private ConflictGraph sampleGraph;

    @BeforeEach
    void setUp() {
        task3Service = new Task3Service();

        List<StudentEnrollment> sampleEnrollments = new ArrayList<>();
        sampleEnrollments.add(createEnrollment("S1", "PDSA201", "SE201", "DB101"));
        sampleEnrollments.add(createEnrollment("S2", "PDSA201", "SE201"));
        sampleEnrollments.add(createEnrollment("S3", "NET102", "DB101", "PDSA201"));

        ConflictGraphBuilder.Builder builder = new ConflictGraphBuilder.Builder();
        sampleGraph = builder.build(sampleEnrollments);
    }

    private StudentEnrollment createEnrollment(String id, String... courses) {
        StudentEnrollment se = new StudentEnrollment();
        se.setStudentId(id);
        se.setEnrolledCourses(Arrays.asList(courses));
        return se;
    }

    @Test
    void testLowerAndUpperBoundsProvingInvariant() {
        int lowerBound = task3Service.calculateLowerBound(sampleGraph);
        int upperBound = task3Service.calculateUpperBound(sampleGraph);

        // Lower bound on 4-exam sample data (clique PDSA201-SE201-DB101) is 3
        assertEquals(3, lowerBound, "Lower bound should be 3 (max clique size on sample data)");
        
        // Upper bound (max static degree + 1) is 3 + 1 = 4
        assertEquals(4, upperBound, "Upper bound should be max static degree + 1 = 4");

        // Chromatic number \chi(G) = 3
        GraphColorer.ColoringResult result = new BacktrackingColorer().color(sampleGraph);
        int chromaticNumber = result.getNumColors();

        // Invariant proof: lower_bound <= \chi(G) <= upper_bound
        assertTrue(lowerBound <= chromaticNumber, "lower_bound must be <= chromatic number chi(G)");
        assertTrue(chromaticNumber <= upperBound, "chromatic number chi(G) must be <= upper_bound");
    }

    @Test
    void testStatusDeterminationRules() {
        // Zero violations and exact search -> OPTIMAL
        assertEquals("OPTIMAL", task3Service.determineStatus(3, 3, 0, true));

        // Zero violations, heuristic, sessionsUsed == lowerBound -> OPTIMAL
        assertEquals("OPTIMAL", task3Service.determineStatus(3, 3, 0, false));

        // Zero violations, heuristic, sessionsUsed > lowerBound -> BEST_FOUND
        assertEquals("BEST_FOUND", task3Service.determineStatus(4, 3, 0, false));

        // Violations > 0 -> INFEASIBLE
        assertEquals("INFEASIBLE", task3Service.determineStatus(3, 3, 1, false));
    }

    @Test
    void testHardConstraintViolationDetection() {
        java.util.Map<String, Integer> invalidColoring = new java.util.HashMap<>();
        invalidColoring.put("PDSA201", 1);
        invalidColoring.put("SE201", 1); // Conflicting! Both enrolled by S1 & S2
        invalidColoring.put("NET102", 2);
        invalidColoring.put("DB101", 3);

        int violations = task3Service.countHardConstraintViolations(sampleGraph, invalidColoring);
        assertTrue(violations > 0, "Invalid coloring must produce > 0 hard constraint violations");
    }

    @Test
    void testOutputJsonFileGeneration() {
        com.idss.task3.repository.ConflictGraphRepository conflictGraphRepository =
                org.mockito.Mockito.mock(com.idss.task3.repository.ConflictGraphRepository.class);
        com.idss.task3.service.ClashAnalysisService analysisService =
                new com.idss.task3.service.ClashAnalysisService(task3Service, conflictGraphRepository);
        com.idss.task3.service.ClashAnalysisService.AnalysisResult result = analysisService.analyze(sampleGraph, "DSATUR");

        assertNotNull(result);
        assertEquals(3, result.getAnalysisOutput().getMinimumSessions());
        assertEquals(3, result.getAnalysisOutput().getSessionsUsed());
        assertEquals(3, result.getAnalysisOutput().getLowerBound());
        assertEquals(4, result.getAnalysisOutput().getUpperBound());
        assertTrue(result.getAnalysisOutput().isColoringValid());
        assertEquals(0, result.getAnalysisOutput().getHardConstraintViolations());
        assertEquals("OPTIMAL", result.getGraphResponse().getStatus());

        java.io.File dir = new java.io.File("../data/shared");
        if (!dir.exists()) dir = new java.io.File("data/shared");

        assertTrue(new java.io.File(dir, "output_conflict_graph.json").exists(), "output_conflict_graph.json must exist");
        assertTrue(new java.io.File(dir, "output_clash_analysis.json").exists(), "output_clash_analysis.json must exist");
    }
}
