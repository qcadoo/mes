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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionPerShift.constants.PPSReportFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PPSReportHooks {

    @Autowired
    private PPSReportXlsHelper ppsReportXlsHelper;

    public boolean checkIfIsMoreThatFiveDays(final DataDefinition reportDD, final Entity report) {
        int days = ppsReportXlsHelper.getNumberOfDaysBetweenGivenDates(report);

        if (days > 7) {
            report.addError(reportDD.getField(PPSReportFields.DATE_FROM), "productionPerShift.report.onlyFiveDays");
            report.addError(reportDD.getField(PPSReportFields.DATE_TO), "productionPerShift.report.onlyFiveDays");

            return false;
        }

        return true;
    }

    public final boolean validateDates(final DataDefinition reportDD, final Entity report) {
        Date dateFrom = (Date) report.getField(PPSReportFields.DATE_FROM);
        Date dateTo = (Date) report.getField(PPSReportFields.DATE_TO);
        if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
            report.addError(reportDD.getField(PPSReportFields.DATE_TO), "productionPerShift.report.badDatesOrder");
            return false;
        } else {
            return true;
        }
    }

    public void clearGenerated(final DataDefinition reportDD, final Entity report) {
        report.setField(PPSReportFields.GENERATED, false);
        report.setField(PPSReportFields.FILE_NAME, null);
    }

}
