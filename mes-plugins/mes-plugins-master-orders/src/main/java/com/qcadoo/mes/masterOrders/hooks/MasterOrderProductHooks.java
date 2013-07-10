package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderProductHooks {

    @Autowired
    private MasterOrderHooks masterOrderHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void countCumulatedOrderQuantity(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return;
        }
        BigDecimal cumulatedQuantity = masterOrderHooks.countCumulatedOrderQuantityForMasterOrder(
                masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER),
                masterOrderProduct.getBelongsToField(MasterOrderFields.PRODUCT));
        masterOrderProduct.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, cumulatedQuantity);
    }

    public boolean checkAssignedOrder(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        List<Entity> assignedOrders = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find().add(SearchRestrictions.belongsTo(OrderFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(OrderFieldsMO.MASTER_ORDER, masterOrder)).list().getEntities();

        if (!assignedOrders.isEmpty()) {
            masterOrderProduct.addGlobalError("masterOrders.masterOrderProduct.delete.existsAssisgnedOrder");
            return false;
        }
        return true;
    }
}
