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
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
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

        boolean hasChange = false;

        for (Entity order : allOrders) {
            if (!order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())) {
                actualOrders.add(order);
                continue;
            }

            if (deadline != null && !order.getDateField(OrderFields.DEADLINE).equals(deadline)) {
                order.setField(OrderFields.DEADLINE, deadline);
                hasChange = true;
            }

            if (customer != null && !order.getBelongsToField(OrderFields.COMPANY).equals(customer)) {
                order.setField(OrderFields.COMPANY, customer);
                hasChange = true;
            }

            actualOrders.add(order);
        }

        if (!hasChange) {
            return;
        }

        masterOrder.setField(MasterOrderFields.ORDERS, actualOrders);
    }

    public void changedTypeFromOneToMany(final DataDefinition masterOrderDD, final Entity masterOrder) {
        Long masterOrderId = masterOrder.getId();

        if (masterOrderId == null) {
            return;
        }

        Entity masterOrderFromDB = masterOrderDD.get(masterOrderId);

        MasterOrderType existingMasterOrderType = MasterOrderType.of(masterOrderFromDB);
        if (existingMasterOrderType != MasterOrderType.ONE_PRODUCT && existingMasterOrderType != MasterOrderType.MANY_PRODUCTS) {
            return;
        }

        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        Entity technology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);
        Entity masterOrderProduct = dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).create();

        masterOrderProduct.setField(MasterOrderProductFields.PRODUCT, product);
        masterOrderProduct.setField(MasterOrderProductFields.TECHNOLOGY, technology);

        masterOrder.setField(MasterOrderFields.PRODUCT, null);
        masterOrder.setField(MasterOrderFields.TECHNOLOGY, null);
        masterOrder.setField(MasterOrderFields.MASTER_ORDER_QUANTITY, null);
        masterOrder.setField(MasterOrderFields.MASTER_ORDER_PRODUCTS, Lists.newArrayList(masterOrderProduct));
    }

    public void clearExternalFields(final DataDefinition masterOrderDD, final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_NUMBER, null);
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

}
