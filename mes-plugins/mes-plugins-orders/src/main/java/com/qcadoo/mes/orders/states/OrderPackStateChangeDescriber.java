package com.qcadoo.mes.orders.states;

import com.qcadoo.mes.orders.constants.OrderPackSateChangeFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderPackState;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderPackStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK_STATE_CHANGE);
    }

    @Override
    public StateEnum parseStateEnum(String stringValue) {
        return OrderPackState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return OrderPackSateChangeFields.ORDER_PACK;
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK);
    }
}
