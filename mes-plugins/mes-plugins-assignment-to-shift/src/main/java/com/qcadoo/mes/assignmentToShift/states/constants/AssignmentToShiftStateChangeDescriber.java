package com.qcadoo.mes.assignmentToShift.states.constants;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public final class AssignmentToShiftStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_STATE_CHANGE);
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return AssignmentToShiftState.parseString(stringValue);
    }

    @Override
    public String getOwnerFieldName() {
        return AssignmentToShiftStateChangeFields.ASSIGNMENT_TO_SHIFT;
    }

    @Override
    public DataDefinition getOwnerDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ASSIGNMENT_TO_SHIFT);
    }

    @Override
    public String getOwnerStateChangesFieldName() {
        return AssignmentToShiftFields.STATE_CHANGES;
    };

}
