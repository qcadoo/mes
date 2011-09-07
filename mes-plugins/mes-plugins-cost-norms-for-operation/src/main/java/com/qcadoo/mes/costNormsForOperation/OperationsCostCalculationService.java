package com.qcadoo.mes.costNormsForOperation;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public interface OperationsCostCalculationService {

    public void calculateOperationsCost(final Entity costCalculation);
    
    // required model hook
    public void copyTechnologyTree(final DataDefinition dd, final Entity costCalculation);
}
