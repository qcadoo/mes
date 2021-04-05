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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationOrderInfoOperationalTask {

    private TranslationService translationService;

    private PdfHelper pdfHelper;

    @Autowired
    public OperationOrderInfoOperationalTask(final TranslationService translationService, final PdfHelper pdfHelper) {
        this.translationService = translationService;
        this.pdfHelper = pdfHelper;
    }

    public void print(final Entity order, final Entity operationComponent, final PdfPTable operationTable, final Locale locale) {
        Entity operationalTask = extractOperationalTask(order, operationComponent);

        if (Objects.nonNull(operationalTask)) {
            Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
            String workstationLabel = "";
            String workstationName = "";

            if (Objects.nonNull(workstation)) {
                workstationLabel = translationService.translate("workPlans.workPlan.report.operation.workstation", locale);
                workstationName = workstation.getStringField(WorkstationFields.NAME);
            }

            pdfHelper.addTableCellAsOneColumnTable(operationTable, workstationLabel, workstationName);

            Entity staff = operationalTask.getBelongsToField(OperationalTaskFields.STAFF);
            String staffLabel = "";
            String staffNameAndSurname = "";

            if (Objects.nonNull(staff)) {
                staffLabel = translationService.translate("workPlans.workPlan.report.operation.staff", locale);
                String staffName = staff.getStringField(StaffFields.NAME);
                String staffSurname = staff.getStringField(StaffFields.SURNAME);
                staffNameAndSurname = new StringBuilder(staffName).append(" ").append(staffSurname).toString();
            }

            pdfHelper.addTableCellAsOneColumnTable(operationTable, staffLabel, staffNameAndSurname);

            operationTable.completeRow();
        }
    }

    private Entity extractOperationalTask(final Entity order, final Entity operationComponent) {
        return order.getHasManyField(OrderFields.OPERATIONAL_TASKS).find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, operationComponent))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskStateStringValues.REJECTED))
                .setMaxResults(1).uniqueResult();
    }

}
