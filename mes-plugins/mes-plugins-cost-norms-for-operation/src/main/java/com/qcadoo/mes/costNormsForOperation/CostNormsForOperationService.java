package com.qcadoo.mes.costNormsForOperation;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForOperationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /* ****** VIEW HOOKS ******* */

    public void inheirtOperationCostValuesFromOperation(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        // form.getEntity() may will also return that entity, but in detached state!
        Entity entity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(form.getEntityId());

        copyCostValuesFromGivenOperation(viewDefinitionState, entity.getBelongsToField("operation"));
    }

    public void inheirtOperationCostValuesFromTechnology(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        // form.getEntity() may will also return that entity, but in detached state!
        Entity entity = dataDefinitionService.get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT).get(form.getEntityId());

        copyCostValuesFromGivenOperation(viewDefinitionState, entity.getBelongsToField("technologyOperationComponent"));
    }

    private void copyCostValuesFromGivenOperation(final ViewDefinitionState viewDefinitionState, final Entity source) {
        if (source == null) {
            return;
        }

        for (String componentReference : Arrays.asList("pieceworkCost", "numberOfOperations", "laborHourlyCost",
                "machineHourlyCost")) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (component.getFieldValue() != null && component.getFieldValue().toString().isEmpty()) {
                component.setFieldValue(source.getField(componentReference).toString());
            }

        }
    }
}
