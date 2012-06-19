package com.qcadoo.mes.orders.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderStateChangeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class OrderStateChangeModelValidators {

    public boolean checkReasonRequired(final DataDefinition orderStateChangeDD, final FieldDefinition fieldDefinition,
            final Entity orderStateChange, final Object oldValue, final Object newValue) {
        boolean result = true;
        if (orderStateChange.getBooleanField(OrderStateChangeFields.REASON_REQUIRED)) {
            result = newValue != null;
        }
        return result;
    }
}
