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
package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.DashboardButtonFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DashboardButtonHooks {

    private static final int L_MAX_NUMBER_OF_BUTTONS = 6;

    public boolean validatesWith(final DataDefinition dashboardButtonDD, final Entity dashboardButton) {
        boolean isValid = checkDashboardButtonsTotalNumberOfEntities(dashboardButtonDD, dashboardButton);

        return isValid;
    }

    private boolean checkDashboardButtonsTotalNumberOfEntities(final DataDefinition dashboardButtonDD,
            final Entity dashboardButton) {
        boolean active = dashboardButton.getBooleanField(DashboardButtonFields.ACTIVE);

        if (active && getDashboardButtonsTotalNumberOfEntities(dashboardButtonDD) >= L_MAX_NUMBER_OF_BUTTONS) {
            dashboardButton.addGlobalError("basic.dashboardButton.error.totalNumberOfEntities",
                    String.valueOf(L_MAX_NUMBER_OF_BUTTONS));

            return false;
        }

        return true;
    }

    private int getDashboardButtonsTotalNumberOfEntities(final DataDefinition dashboardButtonDD) {
        return dashboardButtonDD.find().add(SearchRestrictions.eq(DashboardButtonFields.ACTIVE, true)).list()
                .getTotalNumberOfEntities();
    }

}
