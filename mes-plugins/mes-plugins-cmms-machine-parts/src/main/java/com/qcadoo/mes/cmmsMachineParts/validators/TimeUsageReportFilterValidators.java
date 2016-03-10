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

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.TimeUsageReportFilterFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TimeUsageReportFilterValidators {

    public boolean validate(final DataDefinition timeUsageReportDD, final Entity timeUsageReport) {
        Date fromDate = timeUsageReport.getDateField(TimeUsageReportFilterFields.FROM_DATE);
        Date toDate = timeUsageReport.getDateField(TimeUsageReportFilterFields.TO_DATE);
        if (toDate.before(fromDate)) {
            timeUsageReport.addError(timeUsageReportDD.getField(TimeUsageReportFilterFields.TO_DATE),
                    "cmmsMachineParts.timeUsageReport.error.wrongDateOrder");
            return false;
        }
        return true;
    }
}