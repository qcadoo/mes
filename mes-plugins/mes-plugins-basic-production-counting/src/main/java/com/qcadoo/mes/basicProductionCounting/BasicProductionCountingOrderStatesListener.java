package com.qcadoo.mes.basicProductionCounting;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class BasicProductionCountingOrderStatesListener extends OrderStateListener {

    @Autowired
    private MaterialRequirementReportDataService materialRequirementReportDataService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<ChangeOrderStateMessage> onAccepted(Entity newEntity) {
        Preconditions.checkArgument(newEntity != null, "Order is null");
        final Entity order = newEntity.getDataDefinition().get(newEntity.getId());

        final Map<Entity, BigDecimal> productsReq = materialRequirementReportDataService
                .getQuantitiesForOrdersTechnologyProducts(Arrays.asList(order), false);

        for (Entry<Entity, BigDecimal> productReq : productsReq.entrySet()) {
            Entity productionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
            productionCounting.setField("order", order);
            productionCounting.setField("product", productReq.getKey());
            productionCounting.setField("plannedQuantity", productReq.getValue());
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
            if (!productionCounting.isValid()) {
                throw new IllegalStateException("Saved order entity is invalid.");
            }
        }

        Entity productionCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
        productionCounting.setField("order", order);
        productionCounting.setField("product", order.getBelongsToField("product"));
        productionCounting.setField("plannedQuantity", order.getField("plannedQuantity"));
        productionCounting = productionCounting.getDataDefinition().save(productionCounting);
        if (!productionCounting.isValid()) {
            throw new IllegalStateException("Saved order entity is invalid.");
        }

        return super.onAccepted(order);
    }

}
