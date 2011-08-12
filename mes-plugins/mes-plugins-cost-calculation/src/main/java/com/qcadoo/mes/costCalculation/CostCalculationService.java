package com.qcadoo.mes.costCalculation;

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

@Service
public class CostCalculationService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void showCostCalculateFromOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            updateCostCalculationFromOrder(orderId);
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"order.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }

    }

    public void showCostCalculateFromTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            updateCostCalculationFromTechnology(viewDefinitionState, technologyId);
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"technology.id\":\"" + technologyId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    private void updateCostCalculationFromTechnology(final ViewDefinitionState viewDefinitionState, final Long technologyId) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        DataDefinition costCalculationDD = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                CostCalculateConstants.MODEL_COST_CALCULATION);
        Entity technology = technologyDD.get(technologyId);
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("quantity");
        System.out.println("***ala111" + technologyId + "     " + technology);
        // List<Entity> costCalculationOfTechnology = dataDefinitionService
        // .get(CostCalculateConstants.PLUGIN_IDENTIFIER, CostCalculateConstants.MODEL_COST_CALCULATION).find()
        // .add(SearchRestrictions.belongsTo("technology", technology)).list().getEntities();
        Entity costCalculation = costCalculationDD.create();

        costCalculation.setField("product", "product");
        costCalculation.setField("order", "name");
        costCalculation.setField("quantity", "plannedQuantity");
        costCalculation.setField("technology", "technology");
        costCalculation.setField("defaultTechnology", "technology");
        System.out.println("***ala222" + costCalculation);

        System.out.println("***ala333");
        costCalculation.setField("defaultTechnology", technology.getBelongsToField("name"));
        costCalculationDD.save(costCalculation);
        System.out.println("***ala444" + costCalculation);
    }

    private void updateCostCalculationFromOrder(final Long orderId) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        // List<Entity> costCalculationOfOrder = dataDefinitionService
        // .get(CostCalculateConstants.PLUGIN_IDENTIFIER, CostCalculateConstants.MODEL_COST_CALCULATION).find()
        // .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        Entity costCalculation = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                CostCalculateConstants.MODEL_COST_CALCULATION).create();
        costCalculation.setField("product", "product");
        costCalculation.setField("order", order.getField("name"));
        costCalculation.setField("quantity", order.getField("plannedQuantity"));
        costCalculation.setField("technology", order.getBelongsToField("technology"));
        costCalculation.setField("defaultTechnology", order.getField("technology"));
        dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER, CostCalculateConstants.MODEL_COST_CALCULATION).save(
                costCalculation);

    }

}
