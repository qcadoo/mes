package com.qcadoo.mes.productionPerShift.services;

import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.model.api.Entity;

public interface AutomaticPpsService {

    public void generateProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift);
}
