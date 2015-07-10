package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service public class MaintenanceEventHooks {

    @Autowired private MaintenanceEventStateChangeDescriber describer;

    @Autowired private StateChangeEntityBuilder stateChangeEntityBuilder;

    public void onCreate(final DataDefinition eventDD, final Entity event) {
        setInitialState(event);
    }

    public void onSave(final DataDefinition eventDD, final Entity event) {
        if (event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING) != null) {
            String person = Strings.nullToEmpty(
                    event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING).getStringField(StaffFields.NAME)) + " "
                    + Strings.nullToEmpty(
                    event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING).getStringField(StaffFields.SURNAME));
            event.setField(MaintenanceEventFields.PERSON_RECEIVING_NAME, person);
        }
    }

    private void setInitialState(final Entity event) {
        stateChangeEntityBuilder.buildInitial(describer, event, MaintenanceEventState.NEW);
    }

}
