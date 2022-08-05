package com.qcadoo.mes.productionPerShift.services;

import java.util.Date;

import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.model.api.Entity;

public interface AutomaticPpsService {

    void generateProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift);

    void generatePlanProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity planProductionPerShift, Date startDate);
}
