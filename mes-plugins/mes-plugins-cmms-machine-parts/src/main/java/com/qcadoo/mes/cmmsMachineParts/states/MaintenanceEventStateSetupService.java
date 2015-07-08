package com.qcadoo.mes.cmmsMachineParts.states;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.UserFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service public class MaintenanceEventStateSetupService {

    private static final String QCADOO_SECURITY = "qcadooSecurity";

    private static final String USER = "user";

    @Autowired private DataDefinitionService dataDefinitionService;

    @Autowired private SecurityService securityService;

    public void setupOnInProgress(final StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        if (event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING) == null) {
            Entity user = dataDefinitionService.get(QCADOO_SECURITY, USER).get(securityService.getCurrentUserId());
            Entity staff = user.getBelongsToField(UserFields.STAFF);
            if (staff != null) {
                event.setField(MaintenanceEventFields.PERSON_RECEIVING, staff);
                stateChangeContext.setOwner(event);
            }

        }

    }

}
