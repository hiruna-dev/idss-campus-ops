package com.idss.task5.algorithm;

import com.idss.common.model.Exam;
import com.idss.common.model.Student;
import com.idss.common.model.Timeslot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Task 5 algorithm engine.
 * Tests conflict matrix construction, constraint validation,
 * and all 3 scheduling algorithms on a known 4-exam scenario.
 */
class AlgorithmTest {

    private List<Exam> exams;
    private List<Student> students;
    private List<Timeslot> timeslots;
    private List<String> courseCodes;
    private ConflictMatrix matrix;
    private ConstraintValidator validator;

    @BeforeEach
    void setUp() {
        // 4 exams: PDSA201, NET102, DB301, SE201
        exams = new ArrayList<>();
        Exam e1 = new Exam(); e1.exam_id = "EX_101"; e1.course_code = "PDSA201"; e1.student_count = 48; exams.add(e1);
        Exam e2 = new Exam(); e2.exam_id = "EX_102"; e2.course_code = "NET102"; e2.student_count = 25; exams.add(e2);
        Exam e3 = new Exam(); e3.exam_id = "EX_103"; e3.course_code = "DB301";  e3.student_count = 35; exams.add(e3);
        Exam e4 = new Exam(); e4.exam_id = "EX_104"; e4.course_code = "SE201";  e4.student_count = 42; exams.add(e4);

        courseCodes = List.of("PDSA201", "NET102", "DB301", "SE201");

        // 4 students with known enrollments
        students = new ArrayList<>();
        Student s1 = new Student(); s1.student_id = "STU_001"; s1.enrolled_courses = List.of("PDSA201", "NET102"); students.add(s1);
        Student s2 = new Student(); s2.student_id = "STU_002"; s2.enrolled_courses = List.of("PDSA201", "DB301", "SE201"); students.add(s2);
        Student s3 = new Student(); s3.student_id = "STU_003"; s3.enrolled_courses = List.of("NET102", "DB301"); students.add(s3);
        Student s4 = new Student(); s4.student_id = "STU_004"; s4.enrolled_courses = List.of("PDSA201", "SE201"); students.add(s4);

        // 5 timeslots
        timeslots = new ArrayList<>();
        String[][] slotData = {
            {"SLOT_01", "2026-08-20", "Morning"},
            {"SLOT_02", "2026-08-20", "Afternoon"},
            {"SLOT_03", "2026-08-21", "Morning"},
            {"SLOT_04", "2026-08-21", "Afternoon"},
            {"SLOT_05", "2026-08-22", "Morning"}
        };
        for (String[] sd : slotData) {
            Timeslot t = new Timeslot();
            t.slot_id = sd[0]; t.date = sd[1]; t.session = sd[2];
            t.start_time = "09:00"; t.end_time = "12:00"; t.max_exams_parallel = 3;
            timeslots.add(t);
        }

        matrix = new ConflictMatrix(students, courseCodes);
        validator = new ConstraintValidator(matrix, timeslots, exams);
    }

    @Test
    void conflictMatrix_shouldBeCorrect() {
        // Known edges from task_5_plan.md Section 3
        int pdsa = matrix.getIndex("PDSA201");
        int net = matrix.getIndex("NET102");
        int db = matrix.getIndex("DB301");
        int se = matrix.getIndex("SE201");

        assertEquals(1, matrix.getSharedStudents(pdsa, net), "PDSA-NET should share 1 student");
        assertEquals(1, matrix.getSharedStudents(pdsa, db), "PDSA-DB should share 1 student");
        assertEquals(2, matrix.getSharedStudents(pdsa, se), "PDSA-SE should share 2 students");
        assertEquals(1, matrix.getSharedStudents(net, db), "NET-DB should share 1 student");
        assertEquals(0, matrix.getSharedStudents(net, se), "NET-SE should share 0 students (no edge)");
        assertEquals(1, matrix.getSharedStudents(db, se), "DB-SE should share 1 student");
    }

    @Test
    void conflictMatrix_shouldBeSymmetric() {
        int size = matrix.getSize();
        int[][] raw = matrix.getMatrix();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assertEquals(raw[i][j], raw[j][i], "Matrix must be symmetric at [" + i + "][" + j + "]");
            }
            assertEquals(0, raw[i][i], "Diagonal must be 0 at [" + i + "][" + i + "]");
        }
    }

    @Test
    void conflictMatrix_shouldHave5Edges() {
        assertEquals(5, matrix.getTotalConflictEdges(), "Should have exactly 5 conflict edges");
    }

    @Test
    void greedy_shouldProduceClashFreeSchedule() {
        GreedyScheduler greedy = new GreedyScheduler(matrix, validator, timeslots.size());
        AlgorithmResult result = greedy.run();

        assertEquals(0, result.hardViolations, "Greedy should be clash-free with 5 slots and 4 exams");
        assertNotNull(result.bestChromosome);
        assertEquals(4, result.bestChromosome.length);
    }

    @Test
    void geneticAlgorithm_shouldProduceClashFreeSchedule() {
        GeneticEngine ga = new GeneticEngine(matrix, validator, timeslots.size());
        AlgorithmResult result = ga.run();

        assertEquals(0, result.hardViolations, "GA should be clash-free with 5 slots and 4 exams");
        assertTrue(result.isClashFree());
        assertTrue(result.generationsEvaluated > 0);
    }

    @Test
    void simulatedAnnealing_shouldReturnValidResult() {
        SimulatedAnnealing sa = new SimulatedAnnealing(matrix, validator, timeslots.size());
        AlgorithmResult result = sa.run();

        assertNotNull(result.bestChromosome);
        assertEquals(4, result.bestChromosome.length);
        assertTrue(result.iterationsEvaluated > 0);
    }

    @Test
    void gaShouldBeatGreedy_onFatigue() {
        GreedyScheduler greedy = new GreedyScheduler(matrix, validator, timeslots.size());
        GeneticEngine ga = new GeneticEngine(matrix, validator, timeslots.size());

        AlgorithmResult greedyResult = greedy.run();
        AlgorithmResult gaResult = ga.run();

        assertTrue(gaResult.totalFatiguePenalty <= greedyResult.totalFatiguePenalty,
            "GA fatigue (" + gaResult.totalFatiguePenalty + ") should be <= Greedy (" + greedyResult.totalFatiguePenalty + ")");
    }

    @Test
    void validator_shouldDetectHardViolations() {
        // Put all exams in slot 0 — should cause violations for all clashing pairs
        int[] badSchedule = {0, 0, 0, 0};
        int violations = validator.countHardViolations(badSchedule);
        assertTrue(violations > 0, "All-in-one-slot should have hard violations");
        assertEquals(5, violations, "Should have 5 violations (= 5 conflict edges)");
    }

    @Test
    void validator_shouldReturnZeroForPerfectSchedule() {
        // Assign each exam to a different slot — no clashes possible
        int[] perfectSchedule = {0, 1, 2, 3};
        int violations = validator.countHardViolations(perfectSchedule);
        assertEquals(0, violations, "All-different-slots should have 0 hard violations");
    }

    @Test
    void satisfaction_shouldRemainWithinZeroToOneHundredPercent() {
        Exam first = new Exam();
        first.course_code = "PDSA201";
        Exam second = new Exam();
        second.course_code = "NET102";

        Student student = new Student();
        student.enrolled_courses = List.of("PDSA201", "NET102");

        ConflictMatrix pairMatrix = new ConflictMatrix(
                List.of(student), List.of("PDSA201", "NET102"));
        ConstraintValidator pairValidator = new ConstraintValidator(
                pairMatrix, timeslots, List.of(first, second));

        // Same-day back-to-back includes both soft penalties, so it is the
        // worst possible pair and must report exactly 0%, not a negative value.
        assertEquals(0.0, pairValidator.calculateSatisfactionPercentage(new int[]{0, 1}));
    }
}