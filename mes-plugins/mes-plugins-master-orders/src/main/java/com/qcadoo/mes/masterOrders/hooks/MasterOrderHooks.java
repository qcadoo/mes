package com.qcadoo.mes.masterOrders.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MasterOrderHooks {

    public void setExternalSynchronizedField(final DataDefinition dataDefinition, final Entity masterOrder) {
        masterOrder.setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

}
