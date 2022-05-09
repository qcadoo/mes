package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleStateChangeFields;
import com.qcadoo.mes.orders.states.constants.ScheduleState;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class ProductionLineScheduleStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE_STATE_CHANGE);
    }

    @Override
    public StateEnum parseStateEnum(String stringValue) {
        return ScheduleState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return ProductionLineScheduleStateChangeFields.PRODUCTION_LINE_SCHEDULE;
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE);
    }
}
