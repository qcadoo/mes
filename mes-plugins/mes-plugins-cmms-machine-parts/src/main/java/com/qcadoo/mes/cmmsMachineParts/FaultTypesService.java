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
package com.qcadoo.mes.cmmsMachineParts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class FaultTypesService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfFaultTypeAppliesToOthers(final Entity faultType) {

        return FaultTypeAppliesTo.from(faultType).compareTo(FaultTypeAppliesTo.NONE) == 0
                || faultType.getId() == getDefaultFaultType().getId();
    }

    public boolean checkIfFaultTypeAppliesToWorkstation(final Entity faultType, final Entity workstation) {
        return checkIfFaultTypeAppliesToWorkstationOrSubassembly(faultType, workstation, FaultTypeFields.WORKSTATIONS);
    }

    public boolean checkIfFaultTypeAppliesToSubassembly(final Entity faultType, final Entity subassembly) {
        return checkIfFaultTypeAppliesToWorkstationOrSubassembly(faultType, subassembly, FaultTypeFields.SUBASSEMBLIES);
    }

    public boolean checkIfFaultTypeAppliesToWorkstationOrSubassembly(final Entity faultType, final Entity entity,
            final String fieldName) {
        if (FaultTypeAppliesTo.from(faultType).compareTo(FaultTypeAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            return checkIfFaultTypeAppliesToEntity(faultType, entity, fieldName)
                    || checkIfFaultTypeAppliesToEntity(faultType, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                            FaultTypeFields.WORKSTATION_TYPES);
        } else if (FaultTypeAppliesTo.from(faultType).compareTo(FaultTypeAppliesTo.WORKSTATION_TYPE) == 0) {
            return checkIfFaultTypeAppliesToEntity(faultType, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                    FaultTypeFields.WORKSTATION_TYPES);
        }
        Entity defaultType = getDefaultFaultType();
        if (defaultType == null) {
            return false;
        }
        return faultType.getId() == defaultType.getId();
    }

    public boolean checkIfFaultTypeAppliesToEntity(final Entity faultType, final Entity entity, final String fieldToTest) {
        boolean result = faultType.getManyToManyField(fieldToTest).stream().anyMatch(e -> e.getId().equals(entity.getId()));
        return result;
    }

    public Entity getDefaultFaultType() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_FAULT_TYPE)
                .find().add(SearchRestrictions.eq(FaultTypeFields.IS_DEFAULT, true)).setMaxResults(1).uniqueResult();
    }
}
