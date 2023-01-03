package com.qcadoo.mes.technologies.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.WorkstationStateChangeFields;
import com.qcadoo.mes.technologies.states.constants.WorkstationState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class WorkstationStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_WORKSTATION_STATE_CHANGE);
    }

    @Override
    public StateEnum parseStateEnum(String stringValue) {
        return WorkstationState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return WorkstationStateChangeFields.WORKSTATION;
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION);
    }
}
