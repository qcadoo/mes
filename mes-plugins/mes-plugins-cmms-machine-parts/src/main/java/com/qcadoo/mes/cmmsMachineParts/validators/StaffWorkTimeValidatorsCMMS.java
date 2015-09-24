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
package com.qcadoo.mes.cmmsMachineParts.validators;

import com.qcadoo.mes.cmmsMachineParts.constants.StaffWorkTimeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StaffWorkTimeValidatorsCMMS {
    public boolean validatesWith(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {

        if (!checkOperatorWorkTime(staffWorkTimeDD, staffWorkTime)) {
            return false;
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {
        Date dateFrom = staffWorkTime.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START);
        Date dateTo = staffWorkTime.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }
        staffWorkTime.addError(staffWorkTimeDD.getField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END),
                "productionCounting.productionTracking.productionTrackingError.effectiveExecutionTimeEndBeforeEffectiveExecutionTimeStart");
        return false;
    }

}
