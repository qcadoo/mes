package com.qcadoo.mes.cmmsMachineParts.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.security.api.SecurityService;

@Service
public class PlannedEventStateValidationService {

    @Autowired
    private ParameterService parameterService;

    private static final String QCADOO_SECURITY = "qcadooSecurity";

    private static final String USER = "user";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private EventFieldsForTypeFactory eventFieldsForTypeFactory;

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

    public void validationOnPlanned(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfRequiredFieldsAreSet(event, stateChangeContext);
    }

    public void validationOnInRealization(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfRequiredFieldsAreSet(event, stateChangeContext);
    }

    public void validationOnRealized(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfRequiredFieldsAreSet(event, stateChangeContext);
    }

    private void checkIfRequiredFieldsAreSet(Entity event, StateChangeContext stateChangeContext) {
        boolean areSet = true;
        if (event.getBelongsToField(PlannedEventFields.OWNER) == null) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.OWNER,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            areSet = false;
        }
        if (event.getIntegerField(PlannedEventFields.DURATION) == null
                || event.getIntegerField(PlannedEventFields.DURATION).equals(0)) {
            if (durationIsNotHidden(event)) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.DURATION,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            areSet = false;
            }
        }
        String basedOn = event.getStringField(PlannedEventFields.BASED_ON);
        if (basedOn.equals(PlannedEventBasedOn.DATE.getStringValue())) {
            if (event.getDateField(PlannedEventFields.DATE) == null) {
                stateChangeContext.addFieldValidationError(PlannedEventFields.DATE,
                        "cmmsMachineParts.plannedEvent.state.fieldRequired");
                areSet = false;
            }
        } else if (basedOn.equals(PlannedEventBasedOn.COUNTER.getStringValue())) {
            if (event.getDecimalField(PlannedEventFields.COUNTER) == null) {
                stateChangeContext.addFieldValidationError(PlannedEventFields.COUNTER,
                        "cmmsMachineParts.plannedEvent.state.fieldRequired");
                areSet = false;
            }
        }
        if (event.getHasManyField(PlannedEventFields.RESPONSIBLE_WORKERS).isEmpty()) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.RESPONSIBLE_WORKERS,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            areSet = false;
        }
        if (!areSet) {
            stateChangeContext.addValidationError("cmmsMachineParts.plannedEvent.state.fillRequiredFields");
        }
    }

    private boolean durationIsNotHidden(Entity plannedEvent) {
        PlannedEventType type = PlannedEventType.from(plannedEvent);
        FieldsForType fieldsForType = eventFieldsForTypeFactory.createFieldsForType(type);
        return !fieldsForType.getHiddenFields().contains(PlannedEventFields.DURATION);
    }

    public void validationOnCanceled(StateChangeContext stateChangeContext) {

    }
}
