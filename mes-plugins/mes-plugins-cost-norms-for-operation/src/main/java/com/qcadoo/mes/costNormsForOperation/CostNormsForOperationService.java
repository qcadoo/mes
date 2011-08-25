package com.qcadoo.mes.costNormsForOperation;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Iterable<String> FIELDS = Arrays.asList("pieceworkCost", "numberOfOperations", "laborHourlyCost",
            "machineHourlyCost");

    private final static Logger LOG = LoggerFactory.getLogger(CostNormsForOperationService.class);

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
            if (copyCostValuesFromGivenOperation(target, source)) {
                return;
            }
            copyCostValuesFromGivenOperation(target, source.getBelongsToField("operation"));
            copyCostValuesFromGivenOperation(source, source.getBelongsToField("operation")); // Fill missing technology costs
        } else {
            return;
        }

        fillCostFormFields(viewDefinitionState, source); // propagate model changes into the view
    }

    /* ******* AWESOME HELPERS ;) ******* */

    private boolean copyCostValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(source != null, "given source is null");
        boolean result = false;
        for (String fieldName : FIELDS) {
            if (target.getField(fieldName) == null && source.getField(fieldName) != null) {
                target.setField(fieldName, source.getField(fieldName));
                result = true;
            }
        }
        target.getDataDefinition().save(target);
        return result;
    }

    private void fillCostFormFields(final ViewDefinitionState viewDefinitionState, final Entity source) {
        for (String componentReference : FIELDS) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (component.getFieldValue() != null && component.getFieldValue().toString().isEmpty()) {
                component.setFieldValue(source.getField(componentReference).toString());
            }
        }
    }

    private void debug(final String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }
}
