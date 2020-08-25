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
package com.qcadoo.mes.materialFlow.hooks;

import com.qcadoo.mes.materialFlow.constants.ParameterFieldsMF;
import com.qcadoo.mes.materialFlow.constants.WhatToShowOnDashboard;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import org.springframework.stereotype.Component;

@Component
public class ParameterHooksMF {

    public void onSave(final DataDefinition parameterDD, final Entity parameter) {
        clearOperationAndLocations(parameter);
    }

    private void clearOperationAndLocations(final Entity parameter) {
        String whatToShowOnDashboard = parameter.getStringField(ParameterFieldsMF.WHAT_TO_SHOW_ON_DASHBOARD);

        if (!WhatToShowOnDashboard.ORDERS.getStringValue().equals(whatToShowOnDashboard)) {
            parameter.setField(ParameterFieldsMF.DASHBOARD_OPERATION, null);
        }
    }

}
