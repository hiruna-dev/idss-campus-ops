package com.idss.task3.graph;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idss.common.model.Exam;
import com.idss.task3.model.StudentEnrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConflictGraphBuilderTest {

    private List<Exam> sampleExams;
    private List<StudentEnrollment> sampleEnrollments;
    
    private List<Exam> fullExams;
    private List<StudentEnrollment> fullEnrollments;

    @BeforeEach
    void setUp() throws IOException {
        // Setup 4-exam sample as per Section 3.3
        sampleExams = new ArrayList<>();
        sampleExams.add(createExam("PDSA201"));
        sampleExams.add(createExam("SE201"));
        sampleExams.add(createExam("NET102"));
        sampleExams.add(createExam("DB101"));

        sampleEnrollments = new ArrayList<>();
        // To get weight 2 for PDSA201-SE201, 0 for NET102-SE201
        // and 5 edges out of 6 possible (max edges = 4*3/2 = 6, 5 edges -> density = 5/6 = 0.8333)
        // Missing edge is NET102-SE201
        
        sampleEnrollments.add(createEnrollment("S1", "PDSA201", "SE201", "DB101"));
        sampleEnrollments.add(createEnrollment("S2", "PDSA201", "SE201"));
        sampleEnrollments.add(createEnrollment("S3", "NET102", "DB101", "PDSA201"));

        // Setup real JSON files if they exist for fallback testing
        ObjectMapper mapper = new ObjectMapper();
        File examsFile = new File("../../data/input/input_exams.json");
        File enrollmentsFile = new File("../../data/input/input_student_enrollments.json");
        
        if (examsFile.exists()) {
            fullExams = mapper.readValue(examsFile, new TypeReference<List<Exam>>() {});
        }
        if (enrollmentsFile.exists()) {
            fullEnrollments = mapper.readValue(enrollmentsFile, new TypeReference<List<StudentEnrollment>>() {});
        }
    }

    private Exam createExam(String code) {
        Exam e = new Exam();
        e.course_code = code;
        return e;
    }

    private StudentEnrollment createEnrollment(String id, String... courses) {
        StudentEnrollment se = new StudentEnrollment();
        se.setStudentId(id);
        se.setEnrolledCourses(Arrays.asList(courses));
        return se;
    }

    @Test
    void testSampleDataMatrixAndEdges() {
        ConflictGraphBuilder graph = new ConflictGraphBuilder.Builder()
                .withExams(sampleExams)
                .build(sampleEnrollments);

        // verify exactly 5 conflict edges are produced
        assertEquals(5, graph.getEdges().size(), "Should have exactly 5 conflict edges");

        // verify matrix symmetry and zero diagonal
        assertTrue(graph.isValid());

        // verify the conflict matrix matches Section 3.3's worked example exactly
        int pdsaIdx = graph.getExamRegistry().indexOf("PDSA201");
        int seIdx = graph.getExamRegistry().indexOf("SE201");
        int netIdx = graph.getExamRegistry().indexOf("NET102");
        
        int[][] matrix = graph.getConflictMatrix();
        
        assertEquals(2, matrix[pdsaIdx][seIdx]); // PDSA201-SE201 weight 2
        assertEquals(0, matrix[netIdx][seIdx]);  // NET102-SE201 weight 0
        
        assertEquals(0, matrix[pdsaIdx][pdsaIdx]);
        assertEquals(matrix[pdsaIdx][seIdx], matrix[seIdx][pdsaIdx]);

        // verify graph density calculation (expected 0.8333 for the 4-exam example)
        assertEquals(0.8333, graph.getGraphDensity(), 0.0001);
    }

    @Test
    void testFallbackBehaviorWhenExamsAbsent() {
        if (fullEnrollments != null) {
            ConflictGraphBuilder graph = new ConflictGraphBuilder.Builder()
                    .withExams(null)
                    .build(fullEnrollments);

            assertNotNull(graph.getExamRegistry());
            assertFalse(graph.getExamRegistry().isEmpty());
            assertTrue(graph.getEdges().size() > 0);
            assertTrue(graph.isValid());
        }
    }
}
