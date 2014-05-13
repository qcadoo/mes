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
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.masterOrders.util.MasterOrderProductsDataService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MasterOrderHooks {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private MasterOrderProductsDataService masterOrderProductsDataService;

    public void onCreate(final DataDefinition dataDefinition, final Entity masterOrder) {
        setExternalSynchronizedField(masterOrder);
    }

    public void onSave(final DataDefinition dataDefinition, final Entity masterOrder) {
        onTypeTransitionFromOneToOther(masterOrder);
        onTypeTransitionFromManyToOther(masterOrder);
        changedDeadlineAndInOrder(masterOrder);
    }

    public void onView(final DataDefinition dataDefinition, final Entity masterOrder) {
        calculateCumulativeQuantityFromOrders(masterOrder);
    }

    public void onCopy(final DataDefinition dataDefinition, final Entity masterOrder) {
        clearExternalFields(masterOrder);
    }

    protected void setExternalSynchronizedField(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

    protected void calculateCumulativeQuantityFromOrders(final Entity masterOrder) {
        if (masterOrder.getId() == null || MasterOrderType.of(masterOrder) != MasterOrderType.ONE_PRODUCT) {
            return;
        }
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        BigDecimal quantitiesSum = masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(masterOrder, product);
        masterOrder.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, quantitiesSum);
    }

    protected void changedDeadlineAndInOrder(final Entity masterOrder) {
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
            if (OrderState.of(order) != OrderState.PENDING) {
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

    private void onTypeTransitionFromOneToOther(final Entity masterOrder) {
        if (masterOrder.getId() == null || isNotLeavingType(masterOrder, MasterOrderType.ONE_PRODUCT)) {
            return;
        }
        if (isTransitionToManyProducts(masterOrder)) {
            clearAndRegenerateProducts(masterOrder);
        }
        clearFieldsForOneProduct(masterOrder);
    }

    private void clearAndRegenerateProducts(final Entity masterOrder) {
        // to avoid uniqueness validation issues on the old data
        masterOrderProductsDataService.deleteExistingMasterOrderProducts(masterOrder);
        Entity masterOrderProduct = masterOrderProductsDataService.createProductEntryFor(masterOrder);
        masterOrder.setField(MasterOrderFields.MASTER_ORDER_PRODUCTS, Lists.newArrayList(masterOrderProduct));
    }

    private void clearFieldsForOneProduct(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.PRODUCT, null);
        masterOrder.setField(MasterOrderFields.TECHNOLOGY, null);
        masterOrder.setField(MasterOrderFields.MASTER_ORDER_QUANTITY, null);
    }

    private void onTypeTransitionFromManyToOther(final Entity masterOrder) {
        if (masterOrder.getId() == null || isNotLeavingType(masterOrder, MasterOrderType.MANY_PRODUCTS)) {
            return;
        }
        // remove master order's products when leaving 'many products' mode
        masterOrderProductsDataService.deleteExistingMasterOrderProducts(masterOrder);
    }

    private boolean isTransitionToManyProducts(final Entity masterOrder) {
        return MasterOrderType.of(masterOrder) == MasterOrderType.MANY_PRODUCTS;
    }

    private boolean isNotLeavingType(final Entity masterOrder, final MasterOrderType type) {
        Entity masterOrderFromDB = masterOrder.getDataDefinition().get(masterOrder.getId());
        MasterOrderType existingMasterOrderType = MasterOrderType.of(masterOrderFromDB);
        MasterOrderType newMasterOrderType = MasterOrderType.of(masterOrder);
        return existingMasterOrderType != type || newMasterOrderType == type;
    }

    private void clearExternalFields(final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_NUMBER, null);
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

}
