/**
 * @typedef {Object} Exam
 * @property {string} exam_id
 * @property {string} course_code
 * @property {number} student_count
 * @property {boolean} requires_accessibility
 */

/**
 * @typedef {Object} Room
 * @property {string} room_id
 * @property {number} capacity
 * @property {boolean} has_ac
 * @property {number} noise_level - 1 to 5 (5 is quietest)
 * @property {boolean} is_accessible
 */

/**
 * @typedef {Object} MasterSchedule
 * @property {string} exam_id
 * @property {string} course_code
 * @property {string} date - ISO date e.g. "2026-08-20"
 * @property {string} session - e.g. "Morning", "Afternoon"
 * @property {string} room_id
 * @property {string} canonical_room_id
 * @property {number} allocated_students
 * @property {number} required_invigilators
 */

/**
 * @typedef {Object} Roster
 * @property {string} allocation_id
 * @property {string} exam_id
 * @property {string[]} assigned_invigilators
 */

/**
 * @typedef {Object} Route
 * @property {string} dispatch_id
 * @property {string[]} path_sequence
 * @property {boolean} step_free_verified
 */
