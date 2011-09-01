package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.FIELDS;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class CostNormsForOperationService {


    /* ****** VIEW EVENT LISTENERS ******* */

    public void copyCostValuesFromSelectedTechnology(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        
    }

    /* ******* MODEL HOOKS ******* */

    public void copyCostNormsToOrderOperationComponent(final DataDefinition dd, final Entity orderOperationComponent) {
        Entity source = orderOperationComponent.getBelongsToField("technologyOperationComponent");
        // IMPORTANT! be sure that entity isn't in detached state
        source = source.getDataDefinition().get(source.getId());
        if (!copyCostValuesFromGivenOperation(orderOperationComponent, source)) {
            fillCostFieldsWithDefaultValues(orderOperationComponent);
        }
    }

    /* ******* AWESOME HELPERS ;) ******* */

    private Boolean copyCostValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(source != null, "given source is null");
        boolean result = false;
        for (String fieldName : FIELDS) {
            if (target.getField(fieldName) != null || source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
            result = true;
        }
        return result;
    }

    private void fillCostFieldsWithDefaultValues(final Entity entity) {
        for (String fieldName : FIELDS) {
            if ("numberOfOperations".equals(fieldName)) {
                entity.setField(fieldName, 1);
                continue;
            }
            entity.setField(fieldName, BigDecimal.ZERO);
        }
    }
    
}
