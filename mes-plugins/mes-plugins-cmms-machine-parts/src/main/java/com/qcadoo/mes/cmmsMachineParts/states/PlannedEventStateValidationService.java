package com.qcadoo.mes.cmmsMachineParts.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class PlannedEventStateValidationService {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void validationOnInPlan(final StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfOwnerIsSet(event, stateChangeContext);

    }

    private void checkIfOwnerIsSet(Entity event, StateChangeContext stateChangeContext) {
        if (event.getBelongsToField(PlannedEventFields.OWNER) == null) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.OWNER,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            stateChangeContext.addValidationError("cmmsMachineParts.plannedEvent.state.ownerNotFound");
        }
    }

}
