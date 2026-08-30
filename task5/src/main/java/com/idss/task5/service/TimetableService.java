package com.idss.task5.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idss.common.model.*;
import com.idss.task5.algorithm.*;
import com.idss.task5.dto.*;
import com.idss.task5.repository.ScheduleRepository;
import com.idss.task5.util.CanonicalMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates the timetable generation pipeline:
 * 1. Load input data (from local JSON files or API request)
 * 2. Build conflict matrix from student enrollments
 * 3. Run selected algorithm (GA, SA, or Greedy)
 * 4. Build output_master_schedule.json
 * 5. Save to MongoDB + write JSON file
 */
@Service
public class TimetableService {

    private final ScheduleRepository scheduleRepository;
    private final ObjectMapper objectMapper;

    @Value("${task5.data.input-dir:../data/input}")
    private String inputDir;

    @Value("${task5.data.output-dir:../data/shared}")
    private String outputDir;

    public TimetableService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate timetable using local JSON files (development mode).
     * This is the primary method per leader's instruction: "use local JSON files for dev".
     */
    public Map<String, Object> generateFromLocalFiles(String algorithmChoice) throws Exception {
        // Load all inputs from local JSON files
        List<Exam> exams = loadJson(inputDir + "/input_exams.json", new TypeReference<List<Exam>>() {});
        List<Student> students = loadJson(inputDir + "/input_student_enrollments.json", new TypeReference<List<Student>>() {});
        List<Timeslot> timeslots = loadJson(inputDir + "/input_timeslots.json", new TypeReference<List<Timeslot>>() {});

        // Load Task 4 outputs (room rankings + room reference)
        List<RankedRoom> roomRankings = loadJson(inputDir + "/input_room_rankings.json", new TypeReference<List<RankedRoom>>() {});
        List<RoomReference> roomReferences = loadJson(inputDir + "/input_room_reference.json", new TypeReference<List<RoomReference>>() {});

        return generateTimetable(exams, students, timeslots, roomRankings, roomReferences, algorithmChoice);
    }

    /**
     * Core timetable generation pipeline.
     */
    public Map<String, Object> generateTimetable(
            List<Exam> exams,
            List<Student> students,
            List<Timeslot> timeslots,
            List<RankedRoom> roomRankings,
            List<RoomReference> roomReferences,
            String algorithmChoice) {

        // Step 1: Extract course codes and build maps
        List<String> courseCodes = exams.stream()
                .map(e -> e.course_code)
                .collect(Collectors.toList());

        Map<String, Exam> examMap = exams.stream()
                .collect(Collectors.toMap(e -> e.course_code, e -> e));

        // Build room ranking map: exam_id -> list of ranked rooms (sorted by rank)
        Map<String, List<RankedRoom>> roomRankingMap = new HashMap<>();
        if (roomRankings != null) {
            for (RankedRoom rr : roomRankings) {
                roomRankingMap.computeIfAbsent(rr.exam_id, k -> new ArrayList<>()).add(rr);
            }
            // Sort each list by rank
            for (List<RankedRoom> list : roomRankingMap.values()) {
                list.sort(Comparator.comparingInt(r -> r.rank));
            }
        }

        // Build room reference map: room_id -> RoomReference
        Map<String, RoomReference> roomRefMap = new HashMap<>();
        if (roomReferences != null) {
            for (RoomReference ref : roomReferences) {
                roomRefMap.put(ref.room_id, ref);
            }
        }

        // Step 2: Build conflict matrix
        ConflictMatrix conflictMatrix = new ConflictMatrix(students, courseCodes);

        // Step 3: Create constraint validator
        ConstraintValidator validator = new ConstraintValidator(conflictMatrix, timeslots, exams);

        int numSlots = timeslots.size();

        // Step 4: Run selected algorithm
        AlgorithmResult result;
        String algoName = algorithmChoice != null ? algorithmChoice.toUpperCase() : "GA";

        switch (algoName) {
            case "GREEDY":
                GreedyScheduler greedy = new GreedyScheduler(conflictMatrix, validator, numSlots);
                result = greedy.run();
                break;
            case "SA":
                SimulatedAnnealing sa = new SimulatedAnnealing(conflictMatrix, validator, numSlots);
                result = sa.run();
                break;
            case "GA":
            default:
                GeneticEngine ga = new GeneticEngine(conflictMatrix, validator, numSlots);
                result = ga.run();
                break;
        }

        // Step 5: Build output_master_schedule.json
        int[] chromosome = result.bestChromosome;
        List<MasterScheduleEntry> schedule = new ArrayList<>();
        Map<Integer, Set<String>> usedRoomsBySlot = new HashMap<>();
        Map<Integer, Integer> examsPerSlot = new HashMap<>();
        int resourceViolations = 0;

        for (int i = 0; i < courseCodes.size(); i++) {
            String courseCode = courseCodes.get(i);
            int slotIndex = chromosome[i];
            Timeslot slot = timeslots.get(slotIndex);
            Exam exam = examMap.get(courseCode);

            // Find best room for this exam from Task 4 rankings
            String roomId = "UNASSIGNED";
            int floor = 0;
            String canonicalRoomId = "ROOM_UNASSIGNED";

            // Select the highest-ranked eligible room not already used in this slot.
            List<RankedRoom> ranked = roomRankingMap.get(exam.exam_id);
            if (ranked != null && !ranked.isEmpty()) {
                Set<String> usedRooms = usedRoomsBySlot.computeIfAbsent(slotIndex, k -> new HashSet<>());
                for (RankedRoom candidate : ranked) {
                    RoomReference ref = roomRefMap.get(candidate.room_id);
                    if (!candidate.meets_hard_constraints || ref == null
                            || (exam.requires_accessibility && !ref.is_accessible)
                            || usedRooms.contains(candidate.room_id)) {
                        continue;
                    }
                    roomId = candidate.room_id;
                    canonicalRoomId = CanonicalMapper.toCanonical(roomId);
                    floor = ref.floor;
                    usedRooms.add(roomId);
                    break;
                }
            }
            if ("UNASSIGNED".equals(roomId)) {
                resourceViolations++;
            }

            int parallelCount = examsPerSlot.merge(slotIndex, 1, Integer::sum);
            if (slot.max_exams_parallel > 0 && parallelCount > slot.max_exams_parallel) {
                resourceViolations++;
            }

            MasterScheduleEntry entry = new MasterScheduleEntry();
            entry.exam_id = exam.exam_id;
            entry.course_code = exam.course_code;
            entry.course_title = exam.course_title;
            entry.date = slot.date;
            entry.session = slot.session;
            entry.start_time = slot.start_time;
            entry.end_time = slot.end_time;
            entry.room_id = roomId;
            entry.canonical_room_id = canonicalRoomId;
            entry.floor = floor;
            entry.allocated_students = exam.student_count;
            entry.required_invigilators = MasterScheduleEntry.calculateInvigilators(exam.student_count);
            entry.requires_accessibility = exam.requires_accessibility;
            entry.requires_step_free_access = exam.requires_accessibility; // same boolean, derived

            schedule.add(entry);
        }

        // Room availability and slot capacity are hard constraints in the final output.
        result.hardViolations += resourceViolations;

        // Step 6: Build metrics response
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("algorithm_used", result.algorithmName);
        metrics.put("execution_time_ms", Math.round(result.executionTimeMs * 100.0) / 100.0);
        metrics.put("memory_allocated_kb", Math.round(result.memoryKb * 100.0) / 100.0);
        metrics.put("hard_constraint_violations", result.hardViolations);
        metrics.put("resource_constraint_violations", resourceViolations);
        metrics.put("clash_free", result.isClashFree());
        metrics.put("total_fatigue_penalty", result.totalFatiguePenalty);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("back_to_back_same_day", result.fatigueBreakdown[0]);
        breakdown.put("two_exams_same_day", result.fatigueBreakdown[1]);
        breakdown.put("consecutive_day", result.fatigueBreakdown[2]);
        metrics.put("fatigue_breakdown", breakdown);

        metrics.put("soft_constraint_satisfaction_percentage",
                validator.calculateSatisfactionPercentage(chromosome));

        if (result.generationsEvaluated > 0) {
            metrics.put("generations_evaluated", result.generationsEvaluated);
            metrics.put("population_size", result.populationSize);
            metrics.put("convergence_generation", result.convergenceGeneration);
        }
        if (result.iterationsEvaluated > 0) {
            metrics.put("iterations_evaluated", result.iterationsEvaluated);
        }

        // Step 7: Build fatigue report
        Map<String, Object> fatigueReport = buildFatigueReport(students, chromosome, courseCodes, timeslots, conflictMatrix);

        // Step 8: Save to MongoDB (NOT benchmarked — done after algorithm)
        try {
            scheduleRepository.deleteAll();
            scheduleRepository.saveAll(schedule);
        } catch (Exception e) {
            // MongoDB might not be available during local dev — that's OK
            System.err.println("Warning: Could not save to MongoDB: " + e.getMessage());
        }

        // Step 9: Write output JSON files
        try {
            writeJson(outputDir + "/output_master_schedule.json", schedule);
            writeJson(outputDir + "/output_timetable_metrics.json", metrics);
            writeJson(outputDir + "/output_fatigue_report.json", fatigueReport);
        } catch (Exception e) {
            System.err.println("Warning: Could not write output files: " + e.getMessage());
        }

        // Build final response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generation_timestamp", Instant.now().toString());
        response.put("status", result.isClashFree() ? "OPTIMAL" : "INFEASIBLE");
        response.put("algorithm_used", result.algorithmName);
        response.put("schedule", schedule);
        response.put("metrics", metrics);
        response.put("fatigue_report", fatigueReport);

        if (!result.isClashFree()) {
            response.put("reason", result.hardViolations + " hard constraint violations detected");
            response.put("suggested_remedy", "Add more timeslots or reduce exam count");
        }

        return response;
    }

    /**
     * Build the fatigue report by auditing each student's schedule.
     */
    private Map<String, Object> buildFatigueReport(
            List<Student> students, int[] chromosome, List<String> courseCodes,
            List<Timeslot> timeslots, ConflictMatrix conflictMatrix) {

        int totalStudents = students.size();
        int zeroFatigue = 0;
        int withBackToBack = 0;
        String worstStudentId = null;
        int worstScore = 0;
        List<String> worstSchedule = new ArrayList<>();

        for (Student student : students) {
            int studentFatigue = 0;
            boolean hasBackToBack = false;
            List<int[]> examSlots = new ArrayList<>();

            // Find which slots this student's exams are in
            if (student.enrolled_courses != null) {
                for (String course : student.enrolled_courses) {
                    int examIdx = conflictMatrix.getIndex(course);
                    if (examIdx >= 0) {
                        examSlots.add(new int[]{chromosome[examIdx], examIdx});
                    }
                }
            }

            // Sort by slot index
            examSlots.sort(Comparator.comparingInt(a -> a[0]));

            // Check pairs for fatigue
            List<String> studentSchedule = new ArrayList<>();
            for (int i = 0; i < examSlots.size(); i++) {
                Timeslot ts = timeslots.get(examSlots.get(i)[0]);
                String courseCode = courseCodes.get(examSlots.get(i)[1]);
                studentSchedule.add(courseCode + ": " + ts.date + " " + ts.session);

                for (int j = i + 1; j < examSlots.size(); j++) {
                    Timeslot tsA = ts;
                    Timeslot tsB = timeslots.get(examSlots.get(j)[0]);

                    if (tsA.date.equals(tsB.date)) {
                        studentFatigue += 5; // same day
                        if ((tsA.session.equals("Morning") && tsB.session.equals("Afternoon"))
                         || (tsA.session.equals("Afternoon") && tsB.session.equals("Morning"))) {
                            studentFatigue += 10; // back to back
                            hasBackToBack = true;
                        }
                    }
                }
            }

            if (studentFatigue == 0) zeroFatigue++;
            if (hasBackToBack) withBackToBack++;

            if (studentFatigue > worstScore) {
                worstScore = studentFatigue;
                worstStudentId = student.student_id;
                worstSchedule = studentSchedule;
            }
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("total_students_audited", totalStudents);
        report.put("students_with_zero_fatigue", zeroFatigue);
        report.put("students_with_back_to_back", withBackToBack);

        Map<String, Object> worst = new LinkedHashMap<>();
        worst.put("student_id", worstStudentId);
        worst.put("fatigue_score", worstScore);
        worst.put("schedule", worstSchedule);
        report.put("worst_affected_student", worst);

        return report;
    }

    /**
     * Get current schedule from MongoDB.
     */
    public List<MasterScheduleEntry> getCurrentSchedule() {
        return scheduleRepository.findAll();
    }

    // Helper: load JSON from file
    private <T> T loadJson(String filePath, TypeReference<T> typeRef) throws Exception {
        return objectMapper.readValue(new File(filePath), typeRef);
    }

    // Helper: write JSON to file
    private void writeJson(String filePath, Object data) throws Exception {
        new File(filePath).getParentFile().mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
    }
}