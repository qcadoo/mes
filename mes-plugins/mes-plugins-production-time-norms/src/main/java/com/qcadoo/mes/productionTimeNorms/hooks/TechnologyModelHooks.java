package com.qcadoo.mes.productionTimeNorms.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyModelHooks {

    public void setDefaultValuesIfEmpty(final DataDefinition dataDefinition, final Entity technology) {
        if (technology.getId() == null) {
            return;
        }
        Entity savedTechnology = dataDefinition.get(technology.getId());
        if ("02accepted".equals(technology.getStringField("state")) && "01draft".equals(savedTechnology.getStringField("state"))) {

            final EntityTree operationComponents = savedTechnology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                setOperationComponentDefaults(operationComponent);
            }
        }
    }

    private void setOperationComponentDefaults(Entity operationComponent) {
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
        if (operationComponent.getField("countMachine") == null) {
            operationComponent.setField("countMachine", BigDecimal.ZERO);
        }

        operationComponent = operationComponent.getDataDefinition().save(operationComponent);

        if (!operationComponent.isValid()) {
            throw new IllegalStateException("Saved Technology operation component entity is invalid!");
        }
    }

}
