package com.idss.task3.coloring;

import com.idss.task3.benchmark.BenchmarkingHarness;
import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.service.Task3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BacktrackingColorerTest {

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
    void testBacktrackingFindsExactThreeSessionsOnSampleData() {
        BacktrackingColorer colorer = new BacktrackingColorer();
        GraphColorer.ColoringResult result = task3Service.runColoring(sampleGraph, colorer);

        assertNotNull(result);
        assertEquals(3, result.getNumColors(), "Backtracking exact chromatic number must be 3 on sample data");

        int violations = task3Service.countHardConstraintViolations(sampleGraph, result.getColorOf());
        assertEquals(0, violations, "Backtracking coloring must have zero hard constraint violations");
    }

    @Test
    void testBacktrackingCapEnforced() {
        List<StudentEnrollment> enrollments = BenchmarkingHarness.generateSyntheticEnrollments(16, new Random(42));
        ConflictGraph graph = new ConflictGraphBuilder.Builder().build(enrollments);

        BacktrackingColorer colorer = new BacktrackingColorer();
        assertThrows(IllegalArgumentException.class, () -> colorer.color(graph),
            "Backtracking should throw IllegalArgumentException when graph size > 15");
    }
}
