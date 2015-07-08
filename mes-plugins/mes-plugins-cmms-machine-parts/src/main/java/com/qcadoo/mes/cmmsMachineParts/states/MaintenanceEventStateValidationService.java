package com.qcadoo.mes.cmmsMachineParts.states;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;
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
        checkIfPersonReceivingIsSet(event, stateChangeContext);


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

}
