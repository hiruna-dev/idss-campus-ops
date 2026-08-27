package com.idss.task3.graph;

import com.idss.common.model.Exam;
import com.idss.task3.model.ConflictEdge;
import com.idss.task3.model.StudentEnrollment;
import com.idss.task3.model.VertexResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds the conflict graph for exam scheduling.
 *
 * Complexity Analysis (Section 12):
 * - Conflict graph construction: O(S * K^2) where S is the number of students
 *   and K is the maximum number of courses enrolled by a single student.
 * - Matrix build (conversion to edges): O(E^2) where E is the number of unique exams.
 */
public class ConflictGraphBuilder implements ConflictGraph {
    private static final Logger log = LoggerFactory.getLogger(ConflictGraphBuilder.class);

    private final int[][] conflictMatrix;
    private final List<ConflictEdge> edges;
    private final List<VertexResult> vertices;
    private final List<String> examRegistry;
    private final double graphDensity;

    private ConflictGraphBuilder(Builder builder) {
        this.examRegistry = builder.examRegistry;
        this.conflictMatrix = builder.conflictMatrix;
        this.edges = builder.edges;
        this.vertices = builder.vertices;
        this.graphDensity = builder.graphDensity;
    }

    @Override
    public int[][] getConflictMatrix() {
        return conflictMatrix;
    }

    @Override
    public List<ConflictEdge> getEdges() {
        return edges;
    }

    @Override
    public List<VertexResult> getVertices() {
        return vertices;
    }

    @Override
    public List<String> getExamRegistry() {
        return examRegistry;
    }

    @Override
    public double getGraphDensity() {
        return graphDensity;
    }

    @Override
    public boolean isValid() {
        int n = conflictMatrix.length;
        for (int i = 0; i < n; i++) {
            if (conflictMatrix[i][i] != 0) {
                log.error("Matrix diagonal is not zero at index {}", i);
                return false;
            }
            for (int j = 0; j < n; j++) {
                if (conflictMatrix[i][j] != conflictMatrix[j][i]) {
                    log.error("Matrix is not symmetric at [{}][{}]", i, j);
                    return false;
                }
            }
        }
        return true;
    }

    public static class Builder {
        private List<String> examRegistry = new ArrayList<>();
        private int[][] conflictMatrix;
        private List<ConflictEdge> edges = new ArrayList<>();
        private List<VertexResult> vertices = new ArrayList<>();
        private double graphDensity;

        public Builder withExams(List<Exam> exams) {
            if (exams != null && !exams.isEmpty()) {
                this.examRegistry = exams.stream()
                        .map(e -> e.course_code)
                        .distinct()
                        .collect(Collectors.toList());
            }
            return this;
        }

        public ConflictGraphBuilder build(List<StudentEnrollment> enrollments) {
            // Fallback: If exam registry is empty, infer from enrollments
            if (this.examRegistry.isEmpty()) {
                Set<String> inferredExams = new LinkedHashSet<>();
                for (StudentEnrollment se : enrollments) {
                    if (se.getEnrolledCourses() != null) {
                        inferredExams.addAll(se.getEnrolledCourses());
                    }
                }
                this.examRegistry.addAll(inferredExams);
            }

            int n = this.examRegistry.size();
            this.conflictMatrix = new int[n][n];

            Map<String, Integer> examIndexMap = new HashMap<>();
            for (int i = 0; i < n; i++) {
                examIndexMap.put(this.examRegistry.get(i), i);
                
                VertexResult vr = new VertexResult();
                vr.setExamId(this.examRegistry.get(i));
                vr.setCourseCode(this.examRegistry.get(i));
                this.vertices.add(vr);
            }

            // O(S * K^2) Construction
            for (StudentEnrollment se : enrollments) {
                if (se.getEnrolledCourses() == null) continue;

                // 1. Reject/flag duplicate course codes within one student's list
                List<String> rawCourses = se.getEnrolledCourses();
                Set<String> uniqueCourses = new LinkedHashSet<>(rawCourses);
                if (uniqueCourses.size() < rawCourses.size()) {
                    log.warn("Student {} has duplicate courses in enrollment. Duplicates ignored.", se.getStudentId());
                }

                // 2. Flag enrolled courses that don't exist in the exam registry
                List<String> validCourses = new ArrayList<>();
                for (String course : uniqueCourses) {
                    if (examIndexMap.containsKey(course)) {
                        validCourses.add(course);
                    } else {
                        log.warn("Enrolled course {} for student {} does not exist in exam registry.", course, se.getStudentId());
                    }
                }

                // Pair courses to build conflict matrix
                for (int i = 0; i < validCourses.size(); i++) {
                    for (int j = i + 1; j < validCourses.size(); j++) {
                        int idx1 = examIndexMap.get(validCourses.get(i));
                        int idx2 = examIndexMap.get(validCourses.get(j));
                        
                        conflictMatrix[idx1][idx2]++;
                        conflictMatrix[idx2][idx1]++;
                    }
                }
            }

            // O(E^2) Matrix build
            for (int i = 0; i < n; i++) {
                int degree = 0;
                for (int j = i + 1; j < n; j++) {
                    if (conflictMatrix[i][j] > 0) {
                        this.edges.add(new ConflictEdge(this.examRegistry.get(i), this.examRegistry.get(j), conflictMatrix[i][j]));
                        degree++;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (conflictMatrix[i][j] > 0) {
                        degree++;
                    }
                }
                this.vertices.get(i).setDegree(degree);
            }

            // Graph density calculation
            if (n > 1) {
                int maxEdges = (n * (n - 1)) / 2;
                this.graphDensity = (double) this.edges.size() / maxEdges;
            } else {
                this.graphDensity = 0.0;
            }

            ConflictGraphBuilder graph = new ConflictGraphBuilder(this);
            
            // Wire validation step
            if (!graph.isValid()) {
                throw new IllegalStateException("Constructed conflict matrix is invalid (symmetry or diagonal violation).");
            }

            return graph;
        }
    }
}
