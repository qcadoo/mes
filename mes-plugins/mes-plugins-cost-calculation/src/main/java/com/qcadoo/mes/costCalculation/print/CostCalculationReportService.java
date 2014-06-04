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
package com.qcadoo.mes.costCalculation.print;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostCalculationReportService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private CostCalculationPdfService costCalculationPdfService;

    public void printCostCalculationReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION });
    }

    public void generateCostCalculationReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        if (state instanceof FormComponent) {
            FieldComponent dateField = (FieldComponent) view.getComponentByReference(CostCalculationFields.DATE);
            CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);

            Entity costCalculation = getCostCalculation((Long) state.getFieldValue());

            if (costCalculation == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);

                return;
            } else if (StringUtils.hasText(costCalculation.getStringField(CostCalculationFields.FILE_NAME))) {
                state.addMessage("qcadooReport.errorMessage.documentsWasNotGenerated", MessageType.FAILURE);

                return;
            }

            if (!generatedField.isChecked()) {
                dateField.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, view.getLocale()).format(new Date()));
                generatedField.setChecked(true);
            }

            state.performEvent(view, "save", new String[0]);

            if (state.isHasError()) {
                return;
            }

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                dateField.setFieldValue(null);
                generatedField.setChecked(false);

                return;
            }

            costCalculation = getCostCalculation((Long) state.getFieldValue());

            try {
                generateCostCalculationDocuments(state, costCalculation);

                state.performEvent(view, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private Entity getCostCalculation(final Long costCalculationId) {
        return dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION).get(costCalculationId);
    }

    private void generateCostCalculationDocuments(final ComponentState state, final Entity costCalculation) throws IOException,
            DocumentException {
        Entity costCalculationWithFileName = fileService.updateReportFileName(costCalculation, CostCalculationFields.DATE,
                "costCalculation.costCalculation.report.fileName");

        costCalculationPdfService.generateDocument(costCalculationWithFileName, state.getLocale());
    }

}
