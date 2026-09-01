package com.idss.task2.algorithm;

import com.idss.common.model.Invigilator;
import com.idss.task2.model.MasterScheduleEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//validates all hard constraints on a decoded assignment
public class ConstraintValidator {

    public static final class ConstraintResult {
        public final int violations;
        public final List<String> violationDescriptions;

        ConstraintResult(int violations, List<String> violationDescriptions) {
            this.violations = violations;
            this.violationDescriptions = violationDescriptions;
        }
    }

    //true if this invigilator cannot validly cover this exam (restricted course or unavailable)
    public boolean hasHardViolation(Invigilator inv, MasterScheduleEntry exam) {
        if (inv.restricted_courses != null && inv.restricted_courses.contains(exam.course_code)) {
            return true;
        }
        if (inv.unavailability != null) {
            for (Invigilator.Unavailability u : inv.unavailability) {
                if (u.date.equals(exam.date) && u.session.equals(exam.session)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ConstraintResult validate(List<Assignment> assignments) {
        List<String> descriptions = new ArrayList<>();

        //tallies for the shift limit checks
        Map<String, Integer> shiftsPerDay = new HashMap<>();
        Map<String, Integer> totalShifts = new HashMap<>();

        for (Assignment a : assignments) {
            Invigilator inv = a.inv;
            MasterScheduleEntry exam = a.exam;

            //checking restricted courses
            if (inv.restricted_courses != null
                    && inv.restricted_courses.contains(exam.course_code)) {
                descriptions.add(String.format(
                        "%s assigned to restricted course %s (exam %s)",
                        inv.invigilator_id, exam.course_code, exam.exam_id));
            }

            //checking invigi unavailability
            if (inv.unavailability != null) {
                for (Invigilator.Unavailability u : inv.unavailability) {
                    if (u.date.equals(exam.date) && u.session.equals(exam.session)) {
                        descriptions.add(String.format(
                                "%s assigned to %s %s but is unavailable (exam %s)",
                                inv.invigilator_id, exam.date, exam.session, exam.exam_id));
                        break;
                    }
                }
            }

            //checking max shifts per day
            String dayKey = inv.invigilator_id + "|" + exam.date;
            int today = shiftsPerDay.getOrDefault(dayKey, 0) + 1;
            shiftsPerDay.put(dayKey, today);
            if (today > inv.max_shifts_per_day) {
                descriptions.add(String.format(
                        "%s exceeds max_shifts_per_day (%d) on %s (exam %s)",
                        inv.invigilator_id, inv.max_shifts_per_day, exam.date, exam.exam_id));
            }

            //checking max total shifts
            int total = totalShifts.getOrDefault(inv.invigilator_id, 0) + 1;
            totalShifts.put(inv.invigilator_id, total);
            if (total > inv.max_total_shifts) {
                descriptions.add(String.format(
                        "%s exceeds max_total_shifts (%d) (exam %s)",
                        inv.invigilator_id, inv.max_total_shifts, exam.exam_id));
            }
        }

        return new ConstraintResult(descriptions.size(), descriptions);
    }
}
