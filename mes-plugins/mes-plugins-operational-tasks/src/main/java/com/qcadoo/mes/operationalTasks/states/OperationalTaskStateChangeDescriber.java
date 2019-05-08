package com.qcadoo.mes.operationalTasks.states;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskStateChangeFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.states.constants.OperationalTaskState;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTaskStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK_STATE_CHANGE);
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
        return dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
    }
}
