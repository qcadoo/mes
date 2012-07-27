package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.START_DATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

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

    public boolean checkUniqueEntity(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getId() != null) {
            return true;
        }

        Entity existsEntity = dataDefinition.find().add(SearchRestrictions.belongsTo(SHIFT, entity.getBelongsToField(SHIFT)))
                .add(SearchRestrictions.eq(START_DATE, entity.getField(START_DATE))).uniqueResult();
        if (existsEntity != null) {
            entity.addError(dataDefinition.getField(SHIFT), "assignmentToShift.assignmentToShift.entityAlreadyExists");
            entity.addError(dataDefinition.getField(START_DATE), "assignmentToShift.assignmentToShift.entityAlreadyExists");
            return false;
        }
        return true;
    }
}
