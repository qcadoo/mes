/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.FaultTypesService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MaintenanceEventValidators {

    @Autowired
    private FaultTypesService faultTypesService;

    @Autowired
    private MaintenanceEventService maintenanceEventService;

    public boolean validate(final DataDefinition eventDD, final Entity event) {
        return validateRequiredFields(eventDD, event) && validateIfExistOpenIssue(eventDD, event);
    }

    public boolean validateIfExistOpenIssue(DataDefinition eventDD, Entity event) {
        if (event.getId() == null || MaintenanceEventState.of(event) == MaintenanceEventState.NEW
                || MaintenanceEventState.of(event) == MaintenanceEventState.IN_PROGRESS) {
            MaintenanceEventType type = MaintenanceEventType.from(event);
            if (type.compareTo(MaintenanceEventType.FAILURE) == 0) {
                if (maintenanceEventService.existOpenFailrueForObjectFromEvent(event)) {
                    event.addGlobalError("cmmsMachineParts.error.existOpenIssueForObject", true);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateRequiredFields(final DataDefinition eventDD, final Entity event) {
        return validateDescription(eventDD, event) && validateFaultType(eventDD, event);
    }

    public boolean validateDescription(final DataDefinition eventDD, final Entity event) {
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

    public boolean validateFaultType(final DataDefinition eventDD, final Entity event) {
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

}
