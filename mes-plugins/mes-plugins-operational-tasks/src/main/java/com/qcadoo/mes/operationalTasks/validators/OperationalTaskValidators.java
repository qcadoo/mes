/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.operationalTasks.validators;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationalTaskValidators {

    private static final String NAME_IS_BLANK_MESSAGE = "operationalTasks.operationalTask.error.nameIsBlank";

    private static final String WRONG_DATES_ORDER_MESSAGE = "operationalTasks.operationalTask.error.finishDateIsEarlier";

    public boolean onValidate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        boolean isValid = true;
        isValid = hasName(operationalTaskDD, operationalTask) && isValid;
        isValid = datesAreInCorrectOrder(operationalTaskDD, operationalTask) && isValid;
        return isValid;
    }

    private boolean hasName(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE_TASK);
        if ("01otherCase".equalsIgnoreCase(type) && hasBlankName(operationalTask)) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.NAME), NAME_IS_BLANK_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean hasBlankName(final Entity operationalTask) {
        return StringUtils.isBlank(operationalTask.getStringField(OperationalTaskFields.NAME));
    }

    private boolean datesAreInCorrectOrder(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
        Date finishDate = operationalTask.getDateField(OperationalTaskFields.FINISH_DATE);
        if (finishDate.before(startDate)) {
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.START_DATE), WRONG_DATES_ORDER_MESSAGE);
            operationalTask.addError(operationalTaskDD.getField(OperationalTaskFields.FINISH_DATE), WRONG_DATES_ORDER_MESSAGE);
            return false;
        }
        return true;
    }

}
