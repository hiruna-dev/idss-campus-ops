package com.idss.task5.algorithm;

import com.idss.common.model.Student;
import java.util.*;

/**
 * 2D Conflict Matrix — core data structure for Task 5 timetable optimization.
 *
 * <p>Built from student enrollment data in O(S * K^2) time where S = students,
 * K = max courses per student. The resulting E x E symmetric matrix stores the
 * number of shared students between every pair of exams.</p>
 *
 * <p>Provides O(1) clash checks, which is critical because the fitness function
 * is called ~50,000 times during GA evolution (500 generations x 100 population).</p>
 *
 * <p>Space complexity: O(E^2) — for 100 exams this is 10,000 ints (~40KB).</p>
 *
 * @see GeneticEngine
 * @see ConstraintValidator
 */
public class ConflictMatrix {

    private final int[][] matrix;
    private final Map<String, Integer> examIndexMap;
    private final Map<Integer, String> indexExamMap;
    private final int[] degree;
    private final int size;

    /**
     * Build conflict matrix from student enrollments.
     * For each student, every pair of their enrolled courses gets +1 in the matrix.
     */
    public ConflictMatrix(List<Student> students, List<String> courseCodes) {
        this.size = courseCodes.size();
        this.matrix = new int[size][size];
        this.examIndexMap = new HashMap<>();
        this.indexExamMap = new HashMap<>();
        this.degree = new int[size];

        // Build index maps
        for (int i = 0; i < size; i++) {
            examIndexMap.put(courseCodes.get(i), i);
            indexExamMap.put(i, courseCodes.get(i));
        }

        // Build matrix from enrollments
        for (Student student : students) {
            List<String> courses = student.enrolled_courses;
            if (courses == null) continue;

            // For every pair of courses this student takes
            for (int i = 0; i < courses.size(); i++) {
                Integer idxA = examIndexMap.get(courses.get(i));
                if (idxA == null) continue;

                for (int j = i + 1; j < courses.size(); j++) {
                    Integer idxB = examIndexMap.get(courses.get(j));
                    if (idxB == null) continue;

                    matrix[idxA][idxB]++;
                    matrix[idxB][idxA]++; // symmetric
                }
            }
        }

        // Calculate degree (total clashing students per exam)
        for (int i = 0; i < size; i++) {
            int sum = 0;
            for (int j = 0; j < size; j++) {
                sum += matrix[i][j];
            }
            degree[i] = sum;
        }
    }

    /**
     * O(1) clash check — do these two exams share any students?
     */
    public boolean hasClash(int examIndexA, int examIndexB) {
        return matrix[examIndexA][examIndexB] > 0;
    }

    /**
     * O(1) clash check by course code.
     */
    public boolean hasClash(String courseCodeA, String courseCodeB) {
        Integer idxA = examIndexMap.get(courseCodeA);
        Integer idxB = examIndexMap.get(courseCodeB);
        if (idxA == null || idxB == null) return false;
        return matrix[idxA][idxB] > 0;
    }

    /**
     * Get number of shared students between two exams.
     */
    public int getSharedStudents(int examIndexA, int examIndexB) {
        return matrix[examIndexA][examIndexB];
    }

    /**
     * Get exam index by course code. Returns -1 if not found.
     */
    public int getIndex(String courseCode) {
        Integer idx = examIndexMap.get(courseCode);
        return idx != null ? idx : -1;
    }

    /**
     * Get course code by matrix index.
     */
    public String getCourseCode(int index) {
        return indexExamMap.get(index);
    }

    /**
     * Get degree (total clashing students) for an exam.
     * Used by Greedy Largest Degree First to order exams.
     */
    public int getDegree(int examIndex) {
        return degree[examIndex];
    }

    /**
     * Get the raw matrix (for benchmarking/testing).
     */
    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * Number of exams in the matrix.
     */
    public int getSize() {
        return size;
    }

    /**
     * Get all course codes in index order.
     */
    public List<String> getCourseCodes() {
        List<String> codes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            codes.add(indexExamMap.get(i));
        }
        return codes;
    }

    /**
     * Count total conflict edges (for reporting).
     */
    public int getTotalConflictEdges() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (matrix[i][j] > 0) count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConflictMatrix (").append(size).append(" exams, ");
        sb.append(getTotalConflictEdges()).append(" conflict edges)\n");

        // Header
        sb.append(String.format("%-10s", ""));
        for (int j = 0; j < size; j++) {
            sb.append(String.format("%-8s", getCourseCode(j)));
        }
        sb.append("\n");

        // Rows
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%-10s", getCourseCode(i)));
            for (int j = 0; j < size; j++) {
                sb.append(String.format("%-8d", matrix[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}