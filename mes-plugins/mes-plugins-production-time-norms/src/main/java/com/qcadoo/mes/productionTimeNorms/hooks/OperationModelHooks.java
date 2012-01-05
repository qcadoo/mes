package com.qcadoo.mes.productionTimeNorms.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationModelHooks {

    public void setDefaultValuesIfEmpty(final DataDefinition dataDefinition, final Entity operationComponent) {
        if (operationComponent.getField("tpz") == null) {
            operationComponent.setField("tpz", 0);
        }
        if (operationComponent.getField("timeNextOperation") == null) {
            operationComponent.setField("timeNextOperation", 0);
        }
        if (operationComponent.getField("productionInOneCycle") == null) {
            operationComponent.setField("productionInOneCycle", BigDecimal.ONE);
        }
        if (operationComponent.getField("machineUtilization") == null) {
            operationComponent.setField("machineUtilization", BigDecimal.ONE);
        }
        if (operationComponent.getField("laborUtilization") == null) {
            operationComponent.setField("laborUtilization", BigDecimal.valueOf(1L));
        }
        if (operationComponent.getField("countRealized") == null) {
            operationComponent.setField("countRealized", "01all");
        }
    }
}
