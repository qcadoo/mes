package com.qcadoo.mes.productionPerShift.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.model.api.Entity;

@Service
public class AutomaticPpsTechNormAndWorkersService implements AutomaticPpsService {

    @Autowired
    private PpsTechNormAndWorkersAlgorithmService ppsTechNormAlgorithmService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift) {
        ppsTechNormAlgorithmService.generateProgressForDays(progressForDaysContainer, productionPerShift);

    }
}
