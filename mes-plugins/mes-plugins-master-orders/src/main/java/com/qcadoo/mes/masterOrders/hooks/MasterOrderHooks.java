package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.COMPANY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.DEADLINE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
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
        if (!masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE).equals(MasterOrderType.ONE_PRODUCT.getStringValue())) {
            return;
        }
        masterOrder.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY,
                countCumulatedOrderQuantityForMasterOrder(masterOrder, masterOrder.getBelongsToField(MasterOrderFields.PRODUCT)));
    }

    public BigDecimal countCumulatedOrderQuantityForMasterOrder(final Entity masterOrder, final Entity product) {
        List<Entity> ordersWithProducts = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFieldsMO.MASTER_ORDER, masterOrder))
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCT, product)).list().getEntities();
        BigDecimal cumulatedOrderPlannedQUantity = BigDecimal.ZERO;
        for (Entity order : ordersWithProducts) {
            cumulatedOrderPlannedQUantity = cumulatedOrderPlannedQUantity
                    .add(order.getDecimalField(OrderFields.PLANNED_QUANTITY));
        }
        return cumulatedOrderPlannedQUantity;
    }

    public void changedDeadlineAndInOrder(final DataDefinition masterOrderDD, final Entity masterOrder) {
        if (masterOrder.getId() == null) {
            return;
        }
        Date deadline = masterOrder.getDateField(DEADLINE);
        Entity customer = masterOrder.getBelongsToField(COMPANY);
        if (deadline == null && customer == null) {
            return;
        }
        List<Entity> actualOrders = Lists.newArrayList();
        List<Entity> allOrders = masterOrder.getHasManyField(MasterOrderFields.ORDERS);
        for (Entity order : allOrders) {
            if (!order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())) {
                actualOrders.add(order);
                continue;
            }
            if (deadline != null) {
                order.setField(OrderFields.DEADLINE, deadline);
            }
            if (customer != null) {
                order.setField(OrderFields.COMPANY, customer);
            }
            actualOrders.add(order);
        }
        masterOrder.setField(MasterOrderFields.ORDERS, actualOrders);
    }
}
