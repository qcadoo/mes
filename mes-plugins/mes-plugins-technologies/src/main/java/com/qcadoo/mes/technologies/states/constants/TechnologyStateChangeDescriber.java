package com.qcadoo.mes.technologies.states.constants;

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_STATE_CHANGE;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public final class TechnologyStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY_STATE_CHANGE);
    }

    @Override
    public String getOwnerFieldName() {
        return TechnologyStateChangeFields.TECHNOLOGY;
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return TechnologyState.parseString(stringValue);
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY);
    }

}
