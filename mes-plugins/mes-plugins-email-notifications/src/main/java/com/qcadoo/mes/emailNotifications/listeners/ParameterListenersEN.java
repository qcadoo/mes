/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.emailNotifications.listeners;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParameterListenersEN {

    private static final String L_STAFF_NOTIFICATIONS_LOOKUP = "staffNotificationsStaffLookup";
    private static final String L_EMAIL = "email";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSelectedStaffChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent lookup = (FieldComponent) view.getComponentByReference(L_STAFF_NOTIFICATIONS_LOOKUP);

        FieldComponent email = (FieldComponent) view.getComponentByReference(L_EMAIL);

        if (lookup.getFieldValue() == null) {
            email.setFieldValue(null);
            return;
        }

        Long staffId = Long.valueOf(lookup.getFieldValue().toString());
        Entity staff = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF).get(staffId);
        if (staff == null) {
            email.setFieldValue(null);
            return;
        }

        email.setFieldValue(staff.getField(StaffFields.EMAIL));
    }

}
