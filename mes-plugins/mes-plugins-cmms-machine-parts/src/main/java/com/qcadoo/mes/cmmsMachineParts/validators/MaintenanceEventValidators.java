package com.qcadoo.mes.cmmsMachineParts.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.listeners.EventListeners;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MaintenanceEventValidators {

    @Autowired
    private EventListeners eventListeners;

    public boolean validateDescription(final DataDefinition eventDD, final Entity event) {
        Entity faultType = event.getBelongsToField(MaintenanceEventFields.FAULT_TYPE);
        if (event.getId() == null || faultType == null) {
            return true;
        }
        Entity otherType = eventListeners.getDefaultFaultType();
        if (otherType != null && faultType.getId().compareTo(otherType.getId()) == 0) {
            if (StringUtils.isEmpty(event.getStringField(MaintenanceEventFields.DESCRIPTION))) {
                event.addError(eventDD.getField(MaintenanceEventFields.DESCRIPTION), "cmmsMachineParts.error.desriptionRequired");
                return false;
            }
        }
        return true;
    }
}
