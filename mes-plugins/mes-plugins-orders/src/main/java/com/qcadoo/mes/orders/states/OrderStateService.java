package com.qcadoo.mes.orders.states;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

@Service
public class OrderStateService {

    public boolean isSynchronized(final Entity order) {
        return order.getBooleanField(OrderFields.EXTERNAL_SYNCHRONIZED);
    }
}
