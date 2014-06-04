/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionPerShift.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DailyProgressValidators {

    public boolean checkUniqueShift(final DataDefinition dailyProgressDD, final Entity dailyProgress) {
        Entity secondDailyProgress = dailyProgressDD
                .find()
                .add(SearchRestrictions.belongsTo(DailyProgressFields.SHIFT,
                        dailyProgress.getBelongsToField(DailyProgressFields.SHIFT)))
                .add(SearchRestrictions.belongsTo(DailyProgressFields.PROGRESS_FOR_DAY,
                        dailyProgress.getBelongsToField(DailyProgressFields.PROGRESS_FOR_DAY))).setMaxResults(1).uniqueResult();
        if (secondDailyProgress != null && dailyProgress.getId() == null) {
            dailyProgress.addError(dailyProgressDD.getField(DailyProgressFields.SHIFT),
                    "productionPerShift.dailyProgress.shiftAndProgressForDay.mustBeUnique");

            return false;
        }

        return true;
    }

}
