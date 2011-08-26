package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.FIELDS;

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

    public void inheritOperationCostValues(final ViewDefinitionState viewDefinitionState) {
        
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        String formName = form.getName();
        Entity target, source;

        if ("form".equals(formName)) { // technology
            target = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(form.getEntityId());
            source = target.getBelongsToField("operation");
            copyCostValuesFromGivenOperation(target, source);
        } else if ("orderOperationComponent".equals(formName)) { // technology instance (technology inside order)
            target = dataDefinitionService.get(ProductionSchedulingConstants.PLUGIN_IDENTIFIER,
                    ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT).get(form.getEntityId());
            source = target.getBelongsToField("technologyOperationComponent");
            if (copyCostValuesFromGivenOperation(target, source) != null) {
                return;
            }
            copyCostValuesFromGivenOperation(target, source.getBelongsToField("operation"));
            source = copyCostValuesFromGivenOperation(source, source.getBelongsToField("operation")); // Fill missing technology costs
        } else {
            return;
        }

        fillCostFormFields(viewDefinitionState, source); // propagate model changes into the view
    }

    /* ******* AWESOME HELPERS ;) ******* */

    private Entity copyCostValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(source != null, "given source is null");
        boolean result = false;
        for (String fieldName : FIELDS) {
            if (target.getField(fieldName) == null && source.getField(fieldName) != null) {
                target.setField(fieldName, source.getField(fieldName));
                result = true;
            }
        }
        if(!result) {
            return null;
        }
        return target.getDataDefinition().save(target);
    }

    private void fillCostFormFields(final ViewDefinitionState viewDefinitionState, final Entity source) {
        checkArgument(source != null, "source is null!");
        for (String componentReference : FIELDS) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (component.getFieldValue() != null && component.getFieldValue().toString().isEmpty() && source.getField(componentReference) != null) {
                component.setFieldValue(source.getField(componentReference).toString());
            }
        }
    }
}
