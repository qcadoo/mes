/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_FROM;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_TO;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.FILE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.GENERATED;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.print.xls.AssignmentToShiftXlsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class AssignmentToShiftReportHooks {

    @Autowired
    private AssignmentToShiftXlsHelper assignmentToShiftXlsHelper;

    public boolean checkIfIsMoreThatFiveDays(final DataDefinition assignmentToShiftReportDD, final Entity assignmentToShiftReport) {
        int days = assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport);

        if (days > 5) {
            assignmentToShiftReport.addError(assignmentToShiftReportDD.getField(DATE_FROM),
                    "assignmentToShift.assignmentToShift.report.onlyFiveDays");
            assignmentToShiftReport.addError(assignmentToShiftReportDD.getField(DATE_TO),
                    "assignmentToShift.assignmentToShift.report.onlyFiveDays");

            return false;
        }

        return true;
    }

    public void clearGenerated(final DataDefinition assignmentToShiftReportDD, final Entity assignmentToShiftReport) {
        assignmentToShiftReport.setField(GENERATED, false);
        assignmentToShiftReport.setField(FILE_NAME, null);
    }
    
    public final boolean validateDates(final DataDefinition dataDefinition, final Entity assignmentToShiftReport) {
        Date dateFrom = (Date) assignmentToShiftReport.getField(DATE_FROM);
        Date dateTo = (Date) assignmentToShiftReport.getField(DATE_TO);
        if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
        	assignmentToShiftReport.addError(dataDefinition.getField(DATE_TO), "assignmentToShift.assignmentToShift.report.badDatesOrder");
            return false;
        } else {
			return true;
		}
    }    
}
