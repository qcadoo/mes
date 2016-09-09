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
package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AddActionsForPlannedEventHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        setCriteriaModifiers(view);
    }

    private void setCriteriaModifiers(final ViewDefinitionState view) throws JSONException {
        Long plannedEventId = Long.valueOf(view.getJsonContext().get("window.mainTab.plannedEvent").toString());

        if (plannedEventId != null) {
            Entity event = getPlannedEventDD().get(plannedEventId);
            Entity workstation = event.getBelongsToField(PlannedEventFields.WORKSTATION);
            Entity subassembly = event.getBelongsToField(PlannedEventFields.SUBASSEMBLY);
            if (workstation != null) {

                GridComponent grid = (GridComponent) view.getComponentByReference("grid");

                FilterValueHolder filter = grid.getFilterValue();
                filter.put(PlannedEventFields.WORKSTATION, workstation.getId());

                if (subassembly != null) {
                    Entity workstationType = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION_TYPE);
                    filter.put(PlannedEventFields.SUBASSEMBLY, subassembly.getId());
                    filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
                } else {
                    Entity workstationType = workstation.getBelongsToField(WorkstationFields.WORKSTATION_TYPE);
                    filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
                }
                grid.setFilterValue(filter);
            }
        }
    }

    private DataDefinition getPlannedEventDD() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT);
    }

}
