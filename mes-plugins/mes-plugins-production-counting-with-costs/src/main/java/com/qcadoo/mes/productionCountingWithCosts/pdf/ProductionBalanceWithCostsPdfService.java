/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.productionCountingWithCosts.pdf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;

@Service
public final class ProductionBalanceWithCostsPdfService extends PdfDocumentService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private NumberService numberService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("productionCounting.productionBalance.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionBalance.getField("date"),
                securityService.getCurrentUserName());

        PdfPTable topTable = pdfHelper.createPanelTable(2);
        topTable.addCell(productionBalancePdfService.createLeftPanel(productionBalance, locale));
        topTable.addCell(productionBalancePdfService.createRightPanel(productionBalance, locale));
        topTable.setSpacingBefore(20);
        document.add(topTable);

        PdfPTable parametersForCostsPanel = createParametersForCostsPanel(productionBalance, locale);
        parametersForCostsPanel.setSpacingBefore(20);
        document.add(parametersForCostsPanel);

        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        String typeOfProductionRecording = order.getStringField(OrderFields.TYPE_OF_PRODUCTION_RECORDING);
        String calculationMode = productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        boolean isTypeHourly = CalculateOperationCostMode.parseString(calculationMode).equals(CalculateOperationCostMode.HOURLY);
        boolean isTypeOfProductionRecordingCumulated = TypeOfProductionRecording.parseString(typeOfProductionRecording).equals(
                TypeOfProductionRecording.CUMULATED);

        if (isTypeHourly && isTypeOfProductionRecordingCumulated) {
            PdfPTable assumptionForCumulatedRecordsPanel = createAssumptionsForCumulatedRecordsPanel(productionBalance, locale);
            assumptionForCumulatedRecordsPanel.setSpacingBefore(20);
            document.add(assumptionForCumulatedRecordsPanel);
        }

        PdfPTable bottomTable = pdfHelper.createPanelTable(2);
        bottomTable.addCell(createCostsPanel(productionBalance, locale));
        bottomTable.addCell(createOverheadsAndSummaryPanel(productionBalance, locale));
        bottomTable.setSpacingBefore(20);
        document.add(bottomTable);
    }

    private void addTableCellAsTable(final PdfPTable table, final String label, final Object fieldValue, final String nullValue,
            final Font headerFont, final Font valueFont, final DecimalFormat df) {
        PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        cellTable.addCell(new Phrase(label, headerFont));
        Object value = fieldValue;
        if (value == null) {
            cellTable.addCell(new Phrase(nullValue, valueFont));
        } else {
            if (value instanceof BigDecimal && df != null) {
                cellTable.addCell(new Phrase(df.format(value), valueFont));
            } else {
                cellTable.addCell(new Phrase(value.toString(), valueFont));
            }
        }
        table.addCell(cellTable);
    }

    public PdfPTable createParametersForCostsPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable parametersForCostsPanel = pdfHelper.createPanelTable(1);

        parametersForCostsPanel.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalance.report.panel.parametersForCosts", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        PdfPTable content = pdfHelper.createPanelTable(2);

        String sourceOfMaterialCostsField = productionBalance.getStringField("sourceOfMaterialCosts");
        String sourceOfMaterialCosts = translationService.translate(
                "productionCounting.productionBalance.sourceOfMaterialCosts.value." + sourceOfMaterialCostsField, locale);
        addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.sourceOfMaterialCosts.label", locale),
                sourceOfMaterialCosts, null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

        String calculateMaterialCostsModeField = productionBalance.getStringField("calculateMaterialCostsMode");
        String calculateMaterialCostsMode = translationService.translate(
                "productionCounting.productionBalance.calculateMaterialCostsMode.value." + calculateMaterialCostsModeField,
                locale);
        addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.calculateMaterialCostsMode.label", locale),
                calculateMaterialCostsMode, null, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

        parametersForCostsPanel.addCell(content);

        return parametersForCostsPanel;
    }

    private PdfPTable createAssumptionsForCumulatedRecordsPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable parametersForCostsPanel = pdfHelper.createPanelTable(1);

        parametersForCostsPanel.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalance.report.panel.assumptionsForCumulatedRecords", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        PdfPTable content = pdfHelper.createPanelTable(2);

        BigDecimal averageMachineHourlyCost = (BigDecimal) productionBalance.getField("averageMachineHourlyCost");
        String averageMachineHourlyCostLabel = translationService.translate(
                "productionCounting.productionBalance.averageMachineHourlyCost.label", locale);
        addTableCellAsTable(content, averageMachineHourlyCostLabel, numberService.format(averageMachineHourlyCost), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

        BigDecimal averageLaborHourlyCost = (BigDecimal) productionBalance.getField("averageLaborHourlyCost");
        String averageLaborHourlyCostLabel = translationService.translate(
                "productionCounting.productionBalance.averageLaborHourlyCost.label", locale);
        addTableCellAsTable(content, averageLaborHourlyCostLabel, numberService.format(averageLaborHourlyCost), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);

        parametersForCostsPanel.addCell(content);

        return parametersForCostsPanel;
    }

    private PdfPTable createCostsPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable content = pdfHelper.createPanelTable(1);

        addRegisteredTechnicalCosts(content, productionBalance, locale);
        addPlannedTechnicalCosts(content, productionBalance, locale);

        return content;
    }

    private void addNumericWithLabel(final PdfPTable table, final String labelLocale, final Object value, final Locale locale) {
        BigDecimal valueBD = (BigDecimal) value;
        addTableCellAsTable(table, translationService.translate(labelLocale, locale), numberService.format(valueBD), null,
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold9Dark(), null);
    }

    private void addRegisteredTechnicalCosts(PdfPTable content, Entity productionBalance, Locale locale) {
        content.addCell(new Phrase(
                translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.registeredTechnicalProductionCosts",
                                locale)
                        + ":", FontUtils.getDejavuBold10Dark()));
        addNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.registeredTotalCostsForQuantityLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_COSTS_FOR_QUANTITY), locale);
        addNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.registeredTotalCostPerUnitLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_COST_PER_UNIT), locale);
    }

    private void addPlannedTechnicalCosts(PdfPTable content, Entity productionBalance, Locale locale) {
        content.addCell(new Phrase(
                translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.plannedTechnicalProductionCosts",
                                locale)
                        + ":", FontUtils.getDejavuBold10Dark()));
        addNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.plannedTotalCostsForQuantityLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_COSTS_FOR_QUANTITY), locale);
        addNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.plannedTotalCostPerUnitLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_COSTS_FOR_QUANTITY), locale);

    }

    private PdfPTable createOverheadsAndSummaryPanel(Entity productionBalance, Locale locale) {
        PdfPTable content = pdfHelper.createPanelTable(1);

        content.addCell(new Phrase(
                translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.costsSummaryTab.costsSummaryForm.overheadsForRegisteredCosts",
                                locale)
                        + ":", FontUtils.getDejavuBold10Dark()));

        BigDecimal productionCostMarginValue = (BigDecimal) productionBalance
                .getField(ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN_VALUE);

        addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.productionCostMarginValue.label", locale),
                numberService.format(productionCostMarginValue), null, FontUtils.getDejavuBold9Dark(),
                FontUtils.getDejavuBold9Dark(), null);

        return content;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

}
