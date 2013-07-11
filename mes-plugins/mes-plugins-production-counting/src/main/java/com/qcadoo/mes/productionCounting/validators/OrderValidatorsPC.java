package com.qcadoo.mes.productionCounting.validators;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderValidatorsPC {

    public boolean checkAutoCloseFlagForSynchornizedOrder(final DataDefinition orderDD, final Entity order) {
        if (order.getBooleanField(OrderFieldsPC.AUTO_CLOSE_ORDER)
                && StringUtils.hasText(order.getStringField(OrderFields.EXTERNAL_NUMBER))) {
            order.addError(orderDD.getField(OrderFieldsPC.AUTO_CLOSE_ORDER),
                    "productionCounting.order.externalNumber.cannotBeAutoClose");
            return false;
        }
        return true;
    }
}
