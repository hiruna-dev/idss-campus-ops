// Shared seed data — same shape as the canonical input_*.json contracts.

export const exams = [
  { exam_id: "EX_101", course_code: "PDSA201", course_title: "Data Structures & Algorithms", duration_hours: 3, student_count: 48, year: 2, department: "School of Computing", requires_accessibility: false },
  { exam_id: "EX_102", course_code: "NET102", course_title: "Network Fundamentals", duration_hours: 2, student_count: 36, year: 1, department: "School of Computing", requires_accessibility: false },
  { exam_id: "EX_103", course_code: "DBS301", course_title: "Database Systems", duration_hours: 3, student_count: 62, year: 3, department: "School of Computing", requires_accessibility: false },
  { exam_id: "EX_104", course_code: "AIM401", course_title: "Artificial Intelligence Methods", duration_hours: 3, student_count: 28, year: 4, department: "School of Computing", requires_accessibility: true },
  { exam_id: "EX_105", course_code: "SEN205", course_title: "Software Engineering Practice", duration_hours: 2, student_count: 54, year: 2, department: "School of Computing", requires_accessibility: false },
  { exam_id: "EX_106", course_code: "WEB102", course_title: "Web Technologies", duration_hours: 2, student_count: 40, year: 1, department: "School of Computing", requires_accessibility: false },
  { exam_id: "EX_107", course_code: "CYB302", course_title: "Cyber Security Principles", duration_hours: 2, student_count: 24, year: 3, department: "School of Computing", requires_accessibility: false },
  { exam_id: "EX_108", course_code: "MAT101", course_title: "Discrete Mathematics", duration_hours: 3, student_count: 70, year: 1, department: "School of Science", requires_accessibility: false },
];

export const totalStudents = 412;

export const studentEnrollments = [
  { student_id: "STU_001", name: "A. Perera", year: 2, enrolled_courses: ["PDSA201", "NET102", "SEN205"] },
  { student_id: "STU_002", name: "N. Fernando", year: 1, enrolled_courses: ["NET102", "WEB102", "MAT101"] },
  { student_id: "STU_003", name: "K. Jayasuriya", year: 3, enrolled_courses: ["DBS301", "CYB302"] },
  { student_id: "STU_004", name: "M. Rathnayake", year: 2, enrolled_courses: ["PDSA201", "SEN205", "MAT101"] },
  { student_id: "STU_005", name: "S. De Silva", year: 4, enrolled_courses: ["AIM401", "DBS301"] },
  { student_id: "STU_006", name: "T. Wickramasinghe", year: 1, enrolled_courses: ["WEB102", "MAT101", "NET102"] },
  { student_id: "STU_007", name: "R. Gunawardena", year: 2, enrolled_courses: ["PDSA201", "NET102"] },
  { student_id: "STU_008", name: "H. Bandara", year: 3, enrolled_courses: ["CYB302", "DBS301", "SEN205"] },
];

export const rooms = [
  { room_id: "R101", room_name: "Lecture Theatre 101", floor: 1, capacity: 60, has_ac: true, noise_level: 4, accessibility_score: 5, is_accessible: true },
  { room_id: "R102", room_name: "Lecture Theatre 102", floor: 1, capacity: 75, has_ac: false, noise_level: 3, accessibility_score: 4, is_accessible: true },
  { room_id: "R205", room_name: "Seminar Hall 205", floor: 2, capacity: 45, has_ac: true, noise_level: 5, accessibility_score: 2, is_accessible: false },
  { room_id: "LAB_2B", room_name: "Computer Lab 2B", floor: 2, capacity: 30, has_ac: true, noise_level: 4, accessibility_score: 2, is_accessible: false },
  { room_id: "LAB_3A", room_name: "Computer Lab 3A", floor: 3, capacity: 40, has_ac: true, noise_level: 3, accessibility_score: 1, is_accessible: false },
  { room_id: "R301", room_name: "Exam Hall 301", floor: 3, capacity: 80, has_ac: true, noise_level: 5, accessibility_score: 4, is_accessible: true },
  { room_id: "R004", room_name: "Ground Auditorium", floor: 0, capacity: 120, has_ac: true, noise_level: 2, accessibility_score: 5, is_accessible: true },
];

export const timeslots = [
  { slot_id: "SLOT_01", date: "2026-08-20", session: "Morning", start_time: "09:00", end_time: "12:00", max_exams_parallel: 3 },
  { slot_id: "SLOT_02", date: "2026-08-20", session: "Afternoon", start_time: "13:30", end_time: "16:30", max_exams_parallel: 3 },
  { slot_id: "SLOT_03", date: "2026-08-21", session: "Morning", start_time: "09:00", end_time: "12:00", max_exams_parallel: 3 },
  { slot_id: "SLOT_04", date: "2026-08-21", session: "Afternoon", start_time: "13:30", end_time: "16:30", max_exams_parallel: 3 },
  { slot_id: "SLOT_05", date: "2026-08-22", session: "Morning", start_time: "09:00", end_time: "12:00", max_exams_parallel: 3 },
  { slot_id: "SLOT_06", date: "2026-08-22", session: "Afternoon", start_time: "13:30", end_time: "16:30", max_exams_parallel: 3 },
];

export const invigilators = [
  { invigilator_id: "INV_01", name: "Dr. Aruni Silva", max_shifts_per_day: 2, max_total_shifts: 8, restricted_courses: ["PDSA201"], preferred_floors: [1, 2], unavailability: [{ date: "2026-08-20", session: "Afternoon" }] },
  { invigilator_id: "INV_02", name: "Mr. Nuwan Perera", max_shifts_per_day: 2, max_total_shifts: 6, restricted_courses: [], preferred_floors: [1], unavailability: [] },
  { invigilator_id: "INV_03", name: "Ms. Dilini Wijesinghe", max_shifts_per_day: 1, max_total_shifts: 4, restricted_courses: ["DBS301"], preferred_floors: [2, 3], unavailability: [{ date: "2026-08-21", session: "Morning" }] },
  { invigilator_id: "INV_04", name: "Dr. Kasun Herath", max_shifts_per_day: 2, max_total_shifts: 8, restricted_courses: [], preferred_floors: [0, 1], unavailability: [] },
  { invigilator_id: "INV_05", name: "Ms. Tharushi Alwis", max_shifts_per_day: 2, max_total_shifts: 6, restricted_courses: ["WEB102"], preferred_floors: [3], unavailability: [] },
  { invigilator_id: "INV_06", name: "Mr. Sahan Kumara", max_shifts_per_day: 2, max_total_shifts: 6, restricted_courses: [], preferred_floors: [2, 3], unavailability: [{ date: "2026-08-22", session: "Morning" }] },
  { invigilator_id: "INV_07", name: "Dr. Iresha Madushani", max_shifts_per_day: 1, max_total_shifts: 4, restricted_courses: ["AIM401"], preferred_floors: [1, 3], unavailability: [] },
  { invigilator_id: "INV_08", name: "Mr. Chamara Ellawala", max_shifts_per_day: 2, max_total_shifts: 8, restricted_courses: [], preferred_floors: [0, 2], unavailability: [] },
];
