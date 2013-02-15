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
package com.qcadoo.mes.assignmentToShift.listeners;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.CREATE_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.FILE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.GENERATED;

import java.io.IOException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.print.xls.AssignmentToShiftXlsService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class AssignmentToShiftReportDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private AssignmentToShiftXlsService assignmentReportXlsService;

    public void printAssignmentToShiftReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_REPORT });
    }

    public void generateAssignmentToShiftReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", new String[0]);
        if (!state.isHasError()) {
            Entity assignmentToShiftReport = getAssignmentToShiftReportFromDB((Long) state.getFieldValue());
            if (assignmentToShiftReport == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else {
                if (StringUtils.hasText(assignmentToShiftReport.getStringField(FILE_NAME))) {
                    state.addMessage("assignmentToShift.assignmentToShiftReport.report.error.documentsWasGenerated",
                            MessageType.FAILURE);
                    return;
                }
            }
            if (!assignmentToShiftReport.getBooleanField(GENERATED)) {
                fillReportValues(assignmentToShiftReport);
            }
            assignmentToShiftReport = getAssignmentToShiftReportFromDB((Long) state.getFieldValue());

            try {
                generateAssignmentToShiftReportDocuments(assignmentToShiftReport, state.getLocale());

                state.performEvent(view, "reset", new String[0]);

                state.addMessage(
                        "assignmentToShift.assignmentToShiftReport.window.mainTab.assignmentToShiftReportDetails.generatedMessage",
                        MessageType.SUCCESS);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void generateAssignmentToShiftReportDocuments(final Entity assignmentToShiftReport, final Locale locale)
            throws IOException, DocumentException {

        String localePrefix = "assignmentToShift.assignmentToShiftReport.report.fileName";

        Entity assignmentToShiftReportWithFileName = fileService.updateReportFileName(assignmentToShiftReport, CREATE_DATE,
                localePrefix);

        try {
            assignmentReportXlsService.generateDocument(assignmentToShiftReportWithFileName, locale, PageSize.A3);

        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving goodFood report");
        }
    }

    private void fillReportValues(final Entity assignmentToShiftReport) {
        assignmentToShiftReport.setField(GENERATED, true);
        assignmentToShiftReport.getDataDefinition().save(assignmentToShiftReport);
    }

    private Entity getAssignmentToShiftReportFromDB(final Long entityId) {
        return dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_REPORT).get(entityId);
    }

}
