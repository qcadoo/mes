package com.qcadoo.mes.basicProductionCounting.hooks.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basicProductionCounting.constants.ParameterFieldsBPC;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionProgressModifyLockHelper {

    @Autowired
    private ParameterService parameterService;

    public boolean isLocked(final Entity order) {
        if (order == null) {
            return true;
        }

        String state = order.getStringField(OrderFields.STATE);

        return (OrderStateStringValues.ACCEPTED.equals(state) || OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED
                .equals(state)) && isLocked();
    }

    private boolean isLocked() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsBPC.LOCK_PRODUCTION_PROGRESS);
    }

}
