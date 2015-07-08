package com.qcadoo.mes.cmmsMachineParts.validators;

import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.FaultTypesService;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service public class MaintenanceEventValidators {

    @Autowired private FaultTypesService faultTypesService;

    @Autowired private MaintenanceEventService maintenanceEventService;

    public boolean validate(final DataDefinition eventDD, final Entity event) {
        return validateRequiredFields(eventDD, event) && validateIfExistOpenIssue(eventDD, event);
    }

    private boolean validateIfExistOpenIssue(DataDefinition eventDD, Entity event) {
        if (event.getId() == null || MaintenanceEventState.of(event) == MaintenanceEventState.NEW
                || MaintenanceEventState.of(event) == MaintenanceEventState.IN_PROGRESS) {
            if (maintenanceEventService.existOpenFailrueForObjectFromEvent(event)){
                event.addGlobalError("cmmsMachineParts.error.existOpenIssueForObject", true);
            }
        }
        return true;
    }

    public boolean validateRequiredFields(final DataDefinition eventDD, final Entity event) {
        return validateDescription(eventDD, event) && validateFaultType(eventDD, event);
    }

    private boolean validateDescription(final DataDefinition eventDD, final Entity event) {
        Entity faultType = event.getBelongsToField(MaintenanceEventFields.FAULT_TYPE);
        if (faultType == null) {
            return true;
        }
        Entity otherType = faultTypesService.getDefaultFaultType();
        if (otherType != null && faultType.getId().compareTo(otherType.getId()) == 0) {
            if (StringUtils.isEmpty(event.getStringField(MaintenanceEventFields.DESCRIPTION))) {
                event.addError(eventDD.getField(MaintenanceEventFields.DESCRIPTION), "cmmsMachineParts.error.desriptionRequired");
                return false;
            }
        }
        return true;
    }

    private boolean validateFaultType(final DataDefinition eventDD, final Entity event) {
        Entity faultType = event.getBelongsToField(MaintenanceEventFields.FAULT_TYPE);
        boolean typeCorrect = true;
        if (faultType != null) {
            Entity subassembly = event.getBelongsToField(MaintenanceEventFields.SUBASSEMBLY);
            Entity workstation = event.getBelongsToField(MaintenanceEventFields.WORKSTATION);
            if (subassembly != null) {
                typeCorrect = faultTypesService.checkIfFaultTypeAppliesToSubassembly(faultType, subassembly);
                if (!typeCorrect) {
                    event.addError(eventDD.getField(MaintenanceEventFields.FAULT_TYPE),
                            "cmmsMachineParts.error.faultTypenRequired");
                }
            } else if (workstation != null) {
                typeCorrect = faultTypesService.checkIfFaultTypeAppliesToWorkstation(faultType, workstation);
                if (!typeCorrect) {
                    event.addError(eventDD.getField(MaintenanceEventFields.FAULT_TYPE),
                            "cmmsMachineParts.error.faultTypenRequired");
                }
            }
        }
        return typeCorrect;
    }

    private boolean validateFactoryAndDivision(final DataDefinition eventDD, final Entity event) {
        if (event.getId() == null) {
            return true;
        }
        boolean isCorrect = true;
        if (event.getBelongsToField(MaintenanceEventFields.DIVISION) == null) {
            event.addError(eventDD.getField(MaintenanceEventFields.DIVISION), "cmmsMachineParts.error.divisionRequired");
            isCorrect = false;
        }
        if (event.getBelongsToField(MaintenanceEventFields.FACTORY) == null) {
            event.addError(eventDD.getField(MaintenanceEventFields.FACTORY), "cmmsMachineParts.error.factoryRequired");
            isCorrect = false;
        }
        return isCorrect;

    }
}
