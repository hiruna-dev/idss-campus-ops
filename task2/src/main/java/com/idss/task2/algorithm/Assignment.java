package com.idss.task2.algorithm;

import com.idss.common.model.Invigilator;
import com.idss.task2.model.MasterScheduleEntry;

//one invigi-exam assignment pair from decoding the hungarian output
//shared between the service, validator and fairness calc
public final class Assignment {
    public final Invigilator inv;
    public final MasterScheduleEntry exam;

    public Assignment(Invigilator inv, MasterScheduleEntry exam) {
        this.inv = inv;
        this.exam = exam;
    }
}
