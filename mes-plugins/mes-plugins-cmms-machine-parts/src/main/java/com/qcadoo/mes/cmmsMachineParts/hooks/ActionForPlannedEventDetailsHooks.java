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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ActionForPlannedEventDetailsHooks {

    public void setCriteriaModifiers(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity actionForPlannedEvent = formComponent.getPersistedEntityWithIncludedFormValues();
        Entity event = actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.PLANNED_EVENT);
        Entity workstation = event.getBelongsToField(PlannedEventFields.WORKSTATION);
        Entity subassembly = event.getBelongsToField(PlannedEventFields.SUBASSEMBLY);
        if (workstation != null) {

            LookupComponent actionLookup = (LookupComponent) view.getComponentByReference(ActionForPlannedEventFields.ACTION);

            FilterValueHolder filter = actionLookup.getFilterValue();
            filter.put(PlannedEventFields.WORKSTATION, workstation.getId());

            if (subassembly != null) {
                Entity workstationType = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION_TYPE);
                filter.put(PlannedEventFields.SUBASSEMBLY, subassembly.getId());
                filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
            } else {
                Entity workstationType = workstation.getBelongsToField(WorkstationFields.WORKSTATION_TYPE);
                filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
            }
            actionLookup.setFilterValue(filter);
        }
    }

}
