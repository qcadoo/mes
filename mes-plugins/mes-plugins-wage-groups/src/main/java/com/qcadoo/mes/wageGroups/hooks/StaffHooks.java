/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.DETERMINED_INDIVIDUAL;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.INDIVIDUAL_LABOR_COST;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.WAGE_GROUP;
import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.LABOR_HOURLY_COST;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffHooks {

    public void saveLaborHourlyCost(final DataDefinition dataDefinition, final Entity entity) {
        boolean individual = entity.getBooleanField(DETERMINED_INDIVIDUAL);
        if (individual) {
            entity.setField("laborHourlyCost", entity.getField(INDIVIDUAL_LABOR_COST));
        } else {
            Entity wageGroup = entity.getBelongsToField(WAGE_GROUP);
            if (wageGroup == null) {
                entity.setField("laborHourlyCost", null);
                return;
            }
            entity.setField("laborHourlyCost", wageGroup.getField(LABOR_HOURLY_COST));
        }
    }
}
