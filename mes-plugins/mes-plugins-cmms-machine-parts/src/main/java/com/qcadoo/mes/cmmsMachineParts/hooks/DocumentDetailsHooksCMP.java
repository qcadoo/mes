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

import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service public class DocumentDetailsHooksCMP {

    private static final String L_FORM = "form";

    public void toggleEnabledForEventLookup(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        LookupComponent eventLookup = (LookupComponent) view.getComponentByReference(DocumentFieldsCMP.MAINTENANCE_EVENT);
        LookupComponent plannedEventLookup = (LookupComponent) view.getComponentByReference(DocumentFieldsCMP.PLANNED_EVENT);

        String state = document.getStringField(DocumentFields.STATE);
        String type = document.getStringField(DocumentFields.TYPE);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(type)) {
            eventLookup.setEnabled(false);
            plannedEventLookup.setEnabled(false);
        } else {
            if (state.equals(DocumentState.DRAFT.getStringValue()) && type
                    .equals(DocumentType.INTERNAL_OUTBOUND.getStringValue())) {
                eventLookup.setEnabled(true);
                plannedEventLookup.setEnabled(true);
            } else {
                eventLookup.setEnabled(false);
                plannedEventLookup.setEnabled(false);
            }
        }

    }
}
