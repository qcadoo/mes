package com.qcadoo.mes.cmmsMachineParts.states;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service public class MaintenanceEventStateValidationService {

    public void validationOnInProgress(final StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfPersonReceivingIsSet(event, stateChangeContext);

    }

    public void validationOnEdited(final StateChangeContext stateChangeContext) {

    }

    public void validationOnClosed(final StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfSolutionDescriptionIsSet(event, stateChangeContext);
        checkIfWorkerTimeIsFilled(event, stateChangeContext);
    }

    public void validationOnRevoked(final StateChangeContext stateChangeContext) {

    }

    public void validationOnPlanned(final StateChangeContext stateChangeContext) {

    }

    private void checkIfPersonReceivingIsSet(Entity event, StateChangeContext stateChangeContext) {
        if (event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING) == null) {
            stateChangeContext.addFieldValidationError(MaintenanceEventFields.PERSON_RECEIVING,
                    "cmmsMachineParts.maintenanceEvent.state.fieldRequired");
        }
    }

    private void checkIfSolutionDescriptionIsSet(Entity event, StateChangeContext stateChangeContext) {
        if (StringUtils.isEmpty(event.getStringField(MaintenanceEventFields.SOLUTION_DESCRIPTION))) {
            stateChangeContext.addFieldValidationError(MaintenanceEventFields.SOLUTION_DESCRIPTION,
                    "cmmsMachineParts.maintenanceEvent.state.fieldRequired");
        }
    }


    private void checkIfWorkerTimeIsFilled(Entity event, StateChangeContext stateChangeContext) {
        if (event.getHasManyField(MaintenanceEventFields.STAF_WORK_TIEMS).isEmpty()) {
            stateChangeContext.addValidationError("cmmsMachineParts.maintenanceEvent.state.noWorkersTimeEntry");
        }
    }


}
