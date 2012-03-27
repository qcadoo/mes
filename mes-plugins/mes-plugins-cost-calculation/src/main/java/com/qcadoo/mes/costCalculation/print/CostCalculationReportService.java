/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostCalculationReportService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CostCalculationPdfService costCalculationPdfService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    public void printCostCalculationReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_COST_CALCULATION });
    }

    public void generateCostCalculationReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            Entity costCalculation = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                    CostCalculationConstants.MODEL_COST_CALCULATION).get((Long) state.getFieldValue());

            if (costCalculation == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(costCalculation.getStringField("fileName"))) {
                state.addMessage("qcadooReport.errorMessage.documentsWasNotGenerated", MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                date.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, viewDefinitionState.getLocale())
                        .format(new Date()));
                generated.setFieldValue("1");
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                date.setFieldValue(null);
                generated.setFieldValue("0");
                return;
            }
            costCalculation = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                    CostCalculationConstants.MODEL_COST_CALCULATION).get((Long) state.getFieldValue());
            try {
                generateCostCalDocuments(state, costCalculation);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void generateCostCalDocuments(final ComponentState state, final Entity costCalculation) throws IOException,
            DocumentException {
        Entity costCalculationWithFileName = fileService.updateReportFileName(costCalculation, "date",
                "costCalculation.costCalculation.report.fileName");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        costCalculationPdfService.generateDocument(costCalculationWithFileName, company, state.getLocale());
    }

}
