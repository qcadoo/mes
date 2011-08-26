package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostCalculationViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CostCalculationService costCalculationService;

    public void showCostCalculateFromOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"orderId.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showCostCalculateFromTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"technologyId.id\":\"" + technologyId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void fillField(final ViewDefinitionState state) {
        FormComponent formOrderId = (FormComponent) state.getComponentByReference("orderId");
        FormComponent formTechnologyId = (FormComponent) state.getComponentByReference("technologyId");

        if (formOrderId != null && formOrderId.getFieldValue() != null) {
            fillFieldFromOrder(state);
        } else if (formTechnologyId != null && formTechnologyId.getFieldValue() != null) {
            fillFieldFromTechnology(state);
        }

    }

    public void fillFieldFromOrder(final ViewDefinitionState state) {

        FormComponent formOrderId = (FormComponent) state.getComponentByReference("orderId");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent number = (FieldComponent) state.getComponentByReference("number");
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent quantity = (FieldComponent) state.getComponentByReference("quantity");
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");
        FieldComponent order = (FieldComponent) state.getComponentByReference("order");
        FieldComponent technology = (FieldComponent) state.getComponentByReference("technology");

        Long orderId = (Long) formOrderId.getFieldValue();
        Entity orderEntity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderId);

        if (orderEntity != null) {
            formOrderId.getEntity().setId(null);
            form.getEntity().setId(null);

            number.setFieldValue(generateNumber(6));
            quantity.setFieldValue(orderEntity.getField("plannedQuantity"));
            order.setFieldValue(orderEntity.getId());

            Entity productEntity = orderEntity.getBelongsToField("product");
            product.setFieldValue(productEntity.getId());

            Entity defaultTechnologyEntity = orderEntity.getBelongsToField("technology");

            technology.setFieldValue(defaultTechnologyEntity.getId());
            defaultTechnology.setFieldValue(defaultTechnologyEntity.getId());

            quantity.setEnabled(false);
            order.setEnabled(false);
            product.setEnabled(false);
            technology.setEnabled(false);
            defaultTechnology.setEnabled(false);

            quantity.requestComponentUpdateState();
            number.requestComponentUpdateState();
            order.requestComponentUpdateState();
            product.requestComponentUpdateState();
            technology.requestComponentUpdateState();
            defaultTechnology.requestComponentUpdateState();
            ((FieldComponent) state.getComponentByReference("costPerUnit")).requestComponentUpdateState();
        }
    }

    public void fillFieldFromTechnology(final ViewDefinitionState state) {

        FormComponent formTechnologyId = (FormComponent) state.getComponentByReference("technologyId");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent order = (FieldComponent) state.getComponentByReference("order");
        FieldComponent number = (FieldComponent) state.getComponentByReference("number");
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent quantity = (FieldComponent) state.getComponentByReference("quantity");
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");
        FieldComponent technology = (FieldComponent) state.getComponentByReference("technology");

        Long technologyId = (Long) formTechnologyId.getFieldValue();
        Entity technologyEntity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        if (technologyEntity != null) {
            form.getEntity().setId(null);
            number.setFieldValue(generateNumber(6));
            technology.setFieldValue(technologyEntity.getId());
            defaultTechnology.setFieldValue(technologyEntity.getId());

            Entity productEntity = technologyEntity.getBelongsToField("product");
            product.setFieldValue(productEntity.getId());
            quantity.setFieldValue(technologyEntity.getField("minimalQuantity"));

            order.setEnabled(false);
            product.setEnabled(false);
            technology.setEnabled(false);
            defaultTechnology.setEnabled(false);

            quantity.requestComponentUpdateState();
            number.requestComponentUpdateState();
            product.requestComponentUpdateState();
            technology.requestComponentUpdateState();
            defaultTechnology.requestComponentUpdateState();
            order.requestComponentUpdateState();
        }
    }

    private String generateNumber(final int digitsNumber) {
        List<Entity> costs = dataDefinitionService
                .get(CostCalculateConstants.PLUGIN_IDENTIFIER, CostCalculateConstants.MODEL_COST_CALCULATION).find().list()
                .getEntities();

        long longValue = 0;
        if (costs.size() == 0) {
            longValue++;
        } else {
            longValue = costs.size() + 1;
        }
        return String.format("%0" + digitsNumber + "d", longValue);
    }

    public void generateDateOfCalculation(final DataDefinition dataDefinition, final Entity entity) {

        entity.setField("dateOfCalculation", new Date());

    }

    public void fillFieldDefaultTechnologyAndDisableOrder(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference("defaultTechnology");
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());

        defaultTechnology.setFieldValue(technology.getId());
        defaultTechnology.requestComponentUpdateState();
    }

    public void fillFieldDefaultTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());

        if (order != null) {
            Entity technologyEntity = order.getBelongsToField("technology");
            technology.setFieldValue(technologyEntity.getId());
            technology.requestComponentUpdateState();
        }
    }

    /* FUNCTIONS FOR FIRE CALCULATION AND HANDLING RESULTS BELOW */
    
    /* Event handler, fire total calculation */
    public void calculateTotalCostView(ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args) {
        Map<String, Object> parameters = getValueFromFields(viewDefinitionState);
        Map<String, Object> results;
        ComponentState technologyComponent = viewDefinitionState.getComponentByReference("technology");
        ComponentState orderComponent = viewDefinitionState.getComponentByReference("order");
        Entity order = null;
        Entity technology;

        // check that technology & operation lookup components are found
        checkArgument(technologyComponent != null && orderComponent != null, "Incompatible view");

        if (orderComponent.getFieldValue() != null) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    (Long) orderComponent.getFieldValue());
        }

        if (technologyComponent.getFieldValue() == null) {
            return;
        }

        technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                .get((Long) technologyComponent.getFieldValue());

        // Fire cost calculation algorithm
        results = costCalculationService.calculateTotalCost(technology, order, parameters);
        fillFields(results);
    }

    private Map<String, Object> getValueFromFields(ViewDefinitionState viewDefinitionState) {
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

        return mapWithValueFields;
    }

    private void fillFields(Map<String, Object> resultMap) {
        // TODO - fill result fields in costCalculationDetails view
    }

}
