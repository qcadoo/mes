package com.qcadoo.mes.costCalculation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CostCalculationService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ExpressionService expressionService;

    public void showCostCalculateFromOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            String url = "../page/costCalculation/costCalculationFromOrder.html?context={\"orderId.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showCostCalculateFromTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            String url = "../page/costCalculation/costCalculationFromTechnology.html?context={\"technologyId.id\":\""
                    + technologyId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void fillFieldFromOrder(final ViewDefinitionState state) {
        FormComponent formOrderId = (FormComponent) state.getComponentByReference("orderId");

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
            number.setFieldValue(generateNumber(6));
            quantity.setFieldValue(orderEntity.getField("plannedQuantity"));
            order.setFieldValue(orderEntity.getField("name"));

            Entity productEntity = orderEntity.getBelongsToField("product");
            product.setFieldValue(productEntity.getId());

            Entity defaultTechnologyEntity = orderEntity.getBelongsToField("technology");

            technology.setFieldValue(defaultTechnologyEntity.getId());
            defaultTechnology.setFieldValue(defaultTechnologyEntity.getId());

        }
    }

    public void fillFieldFromTechnology(final ViewDefinitionState state) {

        FormComponent formTechnologyId = (FormComponent) state.getComponentByReference("technologyId");

        FieldComponent number = (FieldComponent) state.getComponentByReference("number");
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent quantity = (FieldComponent) state.getComponentByReference("quantity");
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");
        FieldComponent technology = (FieldComponent) state.getComponentByReference("technology");

        Long technologyId = (Long) formTechnologyId.getFieldValue();
        Entity technologyEntity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        if (technologyEntity != null) {
            number.setFieldValue(generateNumber(6));
            technology.setFieldValue(technologyEntity.getId());
            defaultTechnology.setFieldValue(technologyEntity.getId());

            Entity productEntity = technologyEntity.getBelongsToField("product");
            product.setFieldValue(productEntity.getId());
            quantity.setFieldValue(technologyEntity.getField("minimalQuantity"));
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

}
