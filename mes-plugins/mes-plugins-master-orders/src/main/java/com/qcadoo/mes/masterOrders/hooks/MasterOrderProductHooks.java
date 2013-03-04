package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MasterOrderProductHooks {

    @Autowired
    private MasterOrderHooks masterOrderHooks;

    public void countCumulatedOrderQuantity(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return;
        }
        BigDecimal cumulatedQuantity = masterOrderHooks.countCumulatedOrderQuantityForMasterOrder(
                masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER),
                masterOrderProduct.getBelongsToField(MasterOrderFields.PRODUCT));
        masterOrderProduct.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, cumulatedQuantity);
    }

}
