package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setExternalSynchronizedField(final DataDefinition dataDefinition, final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

    public void countCumulatedOrderQuantity(final DataDefinition dataDefinition, final Entity masterOrder) {
        if (masterOrder.getId() == null) {
            return;
        }
        List<Entity> ordersWithProducts = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFieldsMO.MASTER_ORDER, masterOrder)).list().getEntities();
        BigDecimal cumulatedOrderPlannedQUantity = BigDecimal.ZERO;
        for (Entity order : ordersWithProducts) {
            cumulatedOrderPlannedQUantity = cumulatedOrderPlannedQUantity
                    .add(order.getDecimalField(OrderFields.PLANNED_QUANTITY));
        }
        masterOrder.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, cumulatedOrderPlannedQUantity);
    }
}
