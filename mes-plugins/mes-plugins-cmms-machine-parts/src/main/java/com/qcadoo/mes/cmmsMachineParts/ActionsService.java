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

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfActionAppliesToOthers(final Entity action) {

        return ActionAppliesTo.from(action).compareTo(ActionAppliesTo.NONE) == 0 || action.getId().equals(
                getDefaultAction().getId());
    }

    public boolean checkIfActionAppliesToWorkstation(final Entity action, final Entity workstation) {
        return checkIfActionAppliesToWorkstationOrSubassembly(action, workstation, ActionFields.WORKSTATIONS);
    }

    public boolean checkIfActionAppliesToSubassembly(final Entity action, final Entity subassembly) {
        return checkIfActionAppliesToWorkstationOrSubassembly(action, subassembly, ActionFields.SUBASSEMBLIES);
    }

    public boolean checkIfActionAppliesToWorkstationOrSubassembly(final Entity action, final Entity entity, final String fieldName) {
        if (ActionAppliesTo.from(action).compareTo(ActionAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            return checkIfActionAppliesToEntity(action, entity, fieldName)
                    || checkIfActionAppliesToEntity(action, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                            ActionFields.WORKSTATION_TYPES);
        } else if (ActionAppliesTo.from(action).compareTo(ActionAppliesTo.WORKSTATION_TYPE) == 0) {
            return checkIfActionAppliesToEntity(action, entity.getBelongsToField(WorkstationFields.WORKSTATION_TYPE),
                    ActionFields.WORKSTATION_TYPES);
        }
        Entity defaultAction = getDefaultAction();
        if (defaultAction == null) {
            return false;
        }
        return action.getId().equals(defaultAction.getId());
    }

    public boolean checkIfActionAppliesToEntity(final Entity action, final Entity entity, final String fieldToTest) {
        return action.getManyToManyField(fieldToTest).stream().anyMatch(e -> e.getId().equals(entity.getId()));
    }

    public Entity getDefaultAction() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_ACTION)
                .find().add(SearchRestrictions.eq(ActionFields.IS_DEFAULT, true)).setMaxResults(1).uniqueResult();
    }
}
