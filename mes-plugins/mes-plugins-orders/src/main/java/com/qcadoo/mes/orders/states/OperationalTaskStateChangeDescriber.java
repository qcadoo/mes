package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OperationalTaskStateChangeFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OperationalTaskState;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class OperationalTaskStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK_STATE_CHANGE);
    }

    @Override
    public StateEnum parseStateEnum(String stringValue) {
        return OperationalTaskState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return OperationalTaskStateChangeFields.OPERATIONAL_TASK;
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }
}
