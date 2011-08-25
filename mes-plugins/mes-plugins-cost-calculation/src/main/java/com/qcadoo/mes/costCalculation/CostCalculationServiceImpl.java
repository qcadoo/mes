package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class CostCalculationServiceImpl implements CostCalculationService {

    @Override
    public BigDecimal calculateTotalCostView(ViewDefinitionState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal calculateTotalCost(final Entity technology, final Entity order, final Map<String, Object> parameters,
            final Boolean includeTPZs) {
        checkArgument(technology != null, "technology is null");
        checkArgument(order != null, "order is null");
        checkArgument(parameters.size() != 0, "parameter is empty");

        return new BigDecimal(0);
    }

    @Override
    public HashMap<String, Object> getValueFromFields(ViewDefinitionState viewDefinitionState, ComponentState state, String[] args) {
        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");
        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState.getComponentByReference("includeTPZ");
        FieldComponent includeCostOfMaterial = (FieldComponent) viewDefinitionState
                .getComponentByReference("includeCostOfMaterial");
        FieldComponent includeCostOfOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("includeCostOfOperation");
        FieldComponent productionCostMargin = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionCostMargin");
        FieldComponent materialCostMargin = (FieldComponent) viewDefinitionState.getComponentByReference("materialCostMargin");
        FieldComponent additionalOverhead = (FieldComponent) viewDefinitionState.getComponentByReference("additionalOverhead");

        Map<String, Object> mapWithValueFields = new HashMap<String, Object>();

        mapWithValueFields.put("quantity", quantity.getFieldValue());
        mapWithValueFields.put("includeTPZ", includeTPZ.getFieldValue());
        mapWithValueFields.put("includeCostOfMaterial", includeCostOfMaterial.getFieldValue());
        mapWithValueFields.put("includeCostOfOperation", includeCostOfOperation.getFieldValue());
        mapWithValueFields.put("productionCostMargin", productionCostMargin.getFieldValue());
        mapWithValueFields.put("materialCostMargin", materialCostMargin.getFieldValue());
        mapWithValueFields.put("additionalOverhead", additionalOverhead.getFieldValue());

        return (HashMap<String, Object>) mapWithValueFields;
    }

    @Override
    public void fillFields(HashMap<String, Object> hashMap) {
        System.out.println();
        // TODO Auto-generated method stub

    }

}
