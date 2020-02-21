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
package com.qcadoo.mes.productionPerShift.hooks;

import com.qcadoo.mes.productionPerShift.constants.BalanceContextFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BalanceContextModelHooks {

    public boolean onValidate(final DataDefinition dataDefinition, final Entity balanceContext) {
        boolean isValid = validateDatesOrder(dataDefinition, balanceContext);
        isValid = validateThresholdField(dataDefinition, balanceContext) && isValid;
        return isValid;
    }

    private boolean validateThresholdField(final DataDefinition dataDefinition, final Entity balanceContext) {
        boolean deviationRequired = balanceContext.getBooleanField(BalanceContextFields.DEVIATION_REQUIRED);
        boolean deviationDefined = balanceContext.getField(BalanceContextFields.DEVIATION_THRESHOLD) != null;
        if (deviationRequired && !deviationDefined) {
            balanceContext.addError(dataDefinition.getField(BalanceContextFields.DEVIATION_THRESHOLD),
                    "qcadooView.validate.field.error.missing");
            return false;
        }
        return true;
    }

    private boolean validateDatesOrder(final DataDefinition dataDefinition, final Entity balanceContext) {
        Date from = balanceContext.getDateField(BalanceContextFields.FROM_DATE);
        Date to = balanceContext.getDateField(BalanceContextFields.TO_DATE);

        if (from.after(to)) {
            balanceContext.addError(dataDefinition.getField(BalanceContextFields.TO_DATE),
                    "productionPerShift.balanceContext.error.toDateIsBeforeFromDate");
            return false;
        }
        return true;
    }

}
