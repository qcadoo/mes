/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service public class PlannedEventRealizationDetailsHooks {

    public static final String L_EVENT = "plannedEvent";

    public void onBeforeRender(final ViewDefinitionState view) {
        setFilterValues(view);
    }

    private void setFilterValues(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity plannedEventRealization = form.getPersistedEntityWithIncludedFormValues();
        if (plannedEventRealization != null) {
            Long plannedEventId = plannedEventRealization.getBelongsToField(PlannedEventRealizationFields.PLANNED_EVENT).getId();
            LookupComponent actionsLookup = (LookupComponent) view.getComponentByReference("action");

            FilterValueHolder actionsFVH = actionsLookup.getFilterValue();

            actionsFVH.put(L_EVENT, plannedEventId);
            actionsLookup.setFilterValue(actionsFVH);
            actionsLookup.requestComponentUpdateState();
        }
    }
}
