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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_FROM;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.DATE_TO;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.GENERATED;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.NAME;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.NUMBER;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.SHIFT;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public final class AssignmentToShiftReportDetailsHooks {

    private static final String L_FORM = "form";

    private static final List<String> REPORT_FIELDS = Arrays.asList(NUMBER, NAME, SHIFT, DATE_FROM, DATE_TO);

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void generateAssignmentToShiftReportNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_REPORT, L_FORM, NUMBER);
    }

    public void disableFields(final ViewDefinitionState view) {
        FormComponent assignmentToShiftReportForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long assignmentToShiftReportId = assignmentToShiftReportForm.getEntityId();

        if (assignmentToShiftReportId == null) {
            setFieldsState(view, REPORT_FIELDS, true);
        } else {
            Entity assignmentToShiftReport = getAssignmentToShiftReportFromDB(assignmentToShiftReportId);

            if (assignmentToShiftReport != null) {
                boolean generated = assignmentToShiftReport.getBooleanField(GENERATED);

                if (generated) {
                    setFieldsState(view, REPORT_FIELDS, false);
                } else {
                    setFieldsState(view, REPORT_FIELDS, true);
                }
            }
        }
    }

    private void setFieldsState(final ViewDefinitionState view, final List<String> fieldNames, final boolean enabled) {
        for (String fieldName : fieldNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);

            fieldComponent.setEnabled(enabled);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private Entity getAssignmentToShiftReportFromDB(final Long assignmentToShiftReportId) {
        return dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_REPORT).get(assignmentToShiftReportId);
    }       
}
