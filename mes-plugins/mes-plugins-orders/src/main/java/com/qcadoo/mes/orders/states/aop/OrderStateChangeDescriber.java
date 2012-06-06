package com.qcadoo.mes.orders.states.aop;

import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER_STATE_CHANGE;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.constants.OrderState;
import com.qcadoo.mes.orders.constants.OrderStateChangeFields;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Configurable
public final class OrderStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ORDER_STATE_CHANGE);
    }

    @Override
    public Object parseStateEnum(final String stringValue) {
        return OrderState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return OrderStateChangeFields.ORDER;
    }

}
