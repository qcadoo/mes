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
package com.qcadoo.mes.advancedGenealogy;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.print.AdvancedGenealogyPdfService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
public class AdvancedGenealogyReportService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private AdvancedGenealogyPdfService genealogyPdfService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    public void generateReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(view, "save", new String[0]);

            if (state.isHasError()) {
                return;
            }

            final FieldComponent generatedField = (FieldComponent) view.getComponentByReference("isGenerated");

            final FieldComponent reportedBatch = (FieldComponent) view.getComponentByReference("reportedBatch");
            final FieldComponent name = (FieldComponent) view.getComponentByReference("name");
            final FieldComponent type = (FieldComponent) view.getComponentByReference("type");
            final FieldComponent includeDraft = (FieldComponent) view.getComponentByReference("includeDraft");
            final FieldComponent directOnly = (FieldComponent) view.getComponentByReference("directOnly");

            final Entity genealogyReport = dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                    AdvancedGenealogyConstants.MODEL_GENEALOGY_REPORT).get(((FormComponent) state).getEntityId());

            genealogyReport.setField("date", new Date());
            genealogyReport.setField("worker", securityService.getCurrentUserName());
            genealogyReport.setField("generated", true);
            genealogyReport.setField("batch", reportedBatch.getFieldValue());
            genealogyReport.setField("name", name.getFieldValue());
            genealogyReport.setField("type", type.getFieldValue());
            genealogyReport.setField("includeDraft", includeDraft.getFieldValue());
            genealogyReport.setField("directOnly", directOnly.getFieldValue());

            final Entity genealogyReportWithFileName = fileService.updateReportFileName(genealogyReport, "date",
                    "advancedGenealogy.trackingRecordSimpleDetails.report.fileName");

            try {
                genealogyPdfService.generateDocument(genealogyReportWithFileName, state.getLocale());
                generatedField.setFieldValue("1");
                state.addMessage("advancedGenealogy.message.generated", MessageType.SUCCESS);
                state.performEvent(view, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void printReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                AdvancedGenealogyConstants.MODEL_GENEALOGY_REPORT });
    }

    public void disableForm(final ViewDefinitionState view) {
        final FormComponent genealogyReportForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        final Long genealogyReportId = genealogyReportForm.getEntityId();
        final String[] componentsToDisable = { "reportedBatch", "name", "type", "includeDraft", "directOnly" };
        final FieldComponent isGeneratedField = (FieldComponent) view.getComponentByReference("isGenerated");

        if (genealogyReportId != null) {
            final Entity genealogyReport = dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER,
                    AdvancedGenealogyConstants.MODEL_GENEALOGY_REPORT).get(genealogyReportId);

            boolean generated = genealogyReport.getBooleanField("generated");

            isGeneratedField.setFieldValue(generated);

            for (String componentName : componentsToDisable) {
                final FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
                if (generated) {
                    component.setEnabled(false);
                } else {
                    component.setEnabled(true);
                }
                component.requestComponentUpdateState();
            }
        }
    }

}
