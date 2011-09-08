package com.qcadoo.mes.costNormsForOperation;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public interface OperationsCostCalculationService {

    public void calculateOperationsCost(final Entity costCalculation);
}
