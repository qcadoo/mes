package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderProductHooks {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    public boolean onDelete(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        return checkAssignedOrder(masterOrderProduct);
    }

    public void onView(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        countCumulativeOrderQuantity(masterOrderProduct);
    }

    private void countCumulativeOrderQuantity(final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return;
        }
        BigDecimal cumulativeQuantity = masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(
                masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER),
                masterOrderProduct.getBelongsToField(MasterOrderFields.PRODUCT));
        masterOrderProduct.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, cumulativeQuantity);
    }

    private boolean checkAssignedOrder(final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (MasterOrderType.of(masterOrder) != MasterOrderType.MANY_PRODUCTS) {
            return true;
        }
        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        long numOfBelongingOrdersMatchingProduct = masterOrderOrdersDataProvider.countBelongingOrders(masterOrder,
                SearchRestrictions.belongsTo(OrderFields.PRODUCT, product));

        if (numOfBelongingOrdersMatchingProduct > 0) {
            masterOrderProduct.addGlobalError("masterOrders.masterOrderProduct.delete.existsAssignedOrder");
            return false;
        }
        return true;
    }
}
