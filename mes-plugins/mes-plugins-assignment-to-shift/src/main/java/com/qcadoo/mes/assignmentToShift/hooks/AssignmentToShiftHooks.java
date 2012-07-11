package com.qcadoo.mes.assignmentToShift.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class AssignmentToShiftHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private AssignmentToShiftStateChangeDescriber describer;

    public void setInitialState(final DataDefinition dataDefinition, final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, AssignmentToShiftState.DRAFT);
    }

    public void clearState(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(AssignmentToShiftFields.STATE, AssignmentToShiftState.DRAFT.getStringValue());
    }
}
