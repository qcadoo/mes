package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.FIELDS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class CostNormsForOperationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /* ****** VIEW EVENT LISTENERS ******* */

    public void copyCostValuesFromSelectedTechnology(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        if (componentState.getFieldValue() == null) {
            return;
        }
        FieldComponent component;
        Long operationId = Long.valueOf(componentState.getFieldValue().toString());
        Entity operation = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION).get(operationId);

        for (String fieldName : FIELDS) {
            component = (FieldComponent) state.getComponentByReference(fieldName);
            component.setFieldValue(operation.getField(fieldName));
        }
    }

    public void inheritOperationNormValues(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        copyCostValuesFromSelectedTechnology(state, componentState, args);
    }

    /* ******* MODEL HOOKS ******* */

    public void copyCostNormsToOrderOperationComponent(final DataDefinition dd, final Entity orderOperationComponent) {
        Entity source = orderOperationComponent.getBelongsToField("technologyOperationComponent");
        copyCostValuesFromGivenOperation(orderOperationComponent, source);
    }

    public void copyCostNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity orderOperationComponent) {
        Entity source = orderOperationComponent.getBelongsToField("operation");
        copyCostValuesFromGivenOperation(orderOperationComponent, source);
    }

    /* ******* CUSTOM HELPER(S) ******* */

    private void copyCostValuesFromGivenOperation(final Entity target, final Entity maybeDetachedSource) {
        checkArgument(target != null, "given target is null");
        checkArgument(maybeDetachedSource != null, "given target is null");

        // IMPORTANT! be sure that entity isn't in detached state
        Entity source = maybeDetachedSource.getDataDefinition().get(maybeDetachedSource.getId());

        for (String fieldName : FIELDS) {
            if (source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
        }
    }
}
