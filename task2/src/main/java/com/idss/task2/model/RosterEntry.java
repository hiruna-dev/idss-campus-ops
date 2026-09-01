package com.idss.task2.model;

import java.util.List;

public class RosterEntry {
    public String allocation_id;
    public String exam_id;
    public String course_code;
    public String date;
    public String session;
    public String room_id;
    public List<AssignedInvigilator> assigned_invigilators;
}
