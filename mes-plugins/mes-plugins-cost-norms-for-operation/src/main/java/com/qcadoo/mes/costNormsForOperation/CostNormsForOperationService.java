package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.FIELDS;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForOperationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /* ****** VIEW HOOKS ******* */

    

    /* ******* MODEL HOOKS ******* */

    public void copyCostNormsToOrderOperationComponent(final DataDefinition dd, final Entity orderOperationComponent) {
        Entity source = orderOperationComponent.getBelongsToField("technologyOperationComponent");
        // be sure that entity isn't in detached state
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

    private void fillCostFormFields(final ViewDefinitionState viewDefinitionState, final Entity source) {
        checkArgument(source != null, "source is null!");
        for (String componentReference : FIELDS) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (component.getFieldValue() != null && component.getFieldValue().toString().isEmpty()
                    && source.getField(componentReference) != null) {
                component.setFieldValue(source.getField(componentReference).toString());
            }
        }
    }
}
