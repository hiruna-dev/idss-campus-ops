package com.idss.task3.coloring;

import com.idss.task3.graph.ConflictGraph;
import com.idss.task3.graph.ConflictGraphBuilder;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.service.Task3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WelshPowellColorerTest {

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
    void testWelshPowellValidColoring() {
        WelshPowellColorer colorer = new WelshPowellColorer();
        GraphColorer.ColoringResult result = task3Service.runColoring(sampleGraph, colorer);

        assertNotNull(result);
        assertTrue(result.getNumColors() > 0);

        int violations = task3Service.countHardConstraintViolations(sampleGraph, result.getColorOf());
        assertEquals(0, violations, "Welsh-Powell coloring must have zero hard constraint violations");
    }

    @Test
    void testWelshPowellBoundsInvariant() {
        WelshPowellColorer colorer = new WelshPowellColorer();
        GraphColorer.ColoringResult result = colorer.color(sampleGraph);

        int lowerBound = task3Service.calculateLowerBound(sampleGraph);
        int upperBound = task3Service.calculateUpperBound(sampleGraph);

        assertTrue(lowerBound <= result.getNumColors(),
            "Lower bound (" + lowerBound + ") must be <= sessions used (" + result.getNumColors() + ")");
        assertTrue(result.getNumColors() <= upperBound,
            "Sessions used (" + result.getNumColors() + ") must be <= upper bound (" + upperBound + ")");
    }
}
