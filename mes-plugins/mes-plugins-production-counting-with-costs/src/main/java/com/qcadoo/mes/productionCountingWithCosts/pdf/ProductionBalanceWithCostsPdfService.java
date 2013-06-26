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
package com.qcadoo.mes.productionCountingWithCosts.pdf;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.print.CostCalculationPdfService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.print.utils.EntityProductionTrackingOperationComparator;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public class ProductionBalanceWithCostsPdfService extends PdfDocumentService {

    private static final String L_COSTS = "Costs";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CostCalculationPdfService costCalculationPdfService;

    private static final String NULL_OBJECT = "-";

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("productionCounting.productionBalance.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor,
                productionBalance.getDateField(ProductionBalanceFields.DATE));

        PdfPTable topTable = pdfHelper.createPanelTable(2);
        topTable.addCell(productionBalancePdfService.createLeftPanel(productionBalance, locale));
        topTable.addCell(productionBalancePdfService.createRightPanel(productionBalance, locale));
        topTable.setSpacingBefore(20);
        document.add(topTable);

        PdfPTable parametersForCostsPanel = createParametersForCostsPanel(productionBalance, locale);
        parametersForCostsPanel.setSpacingBefore(20);
        document.add(parametersForCostsPanel);

        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        String calculationMode = productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        final boolean isTypeHourly = CalculateOperationCostMode.parseString(calculationMode).equals(
                CalculateOperationCostMode.HOURLY);
        final boolean isTypePiecework = CalculateOperationCostMode.parseString(calculationMode).equals(
                CalculateOperationCostMode.PIECEWORK);
        final boolean isTypeOfProductionRecordingCumulated = TypeOfProductionRecording.parseString(typeOfProductionRecording)
                .equals(TypeOfProductionRecording.CUMULATED);

        if (isTypeHourly && isTypeOfProductionRecordingCumulated) {
            PdfPTable assumptionForCumulatedRecordsPanel = createAssumptionsForCumulatedRecordsPanel(productionBalance, locale);
            assumptionForCumulatedRecordsPanel.setSpacingBefore(20);
            document.add(assumptionForCumulatedRecordsPanel);
        }

        PdfPTable bottomTable = pdfHelper.createPanelTable(2);
        bottomTable.addCell(createCostsPanel(productionBalance, locale));
        bottomTable.addCell(createOverheadsAndSummaryPanel(productionBalance, locale));
        bottomTable.setSpacingBefore(20);
        bottomTable.setSpacingAfter(20);
        document.add(bottomTable);

        productionBalancePdfService.addInputProductsBalance(document, productionBalance, locale);
        addMaterialCost(document, productionBalance, locale);
        productionBalancePdfService.addOutputProductsBalance(document, productionBalance, locale);

        final boolean isTypeOfProductionRecordingForEach = TypeOfProductionRecording.parseString(typeOfProductionRecording)
                .equals(TypeOfProductionRecording.FOR_EACH);

        if (isTypeHourly) {
            if (isTypeOfProductionRecordingCumulated) {
                productionBalancePdfService.addTimeBalanceAsPanel(document, productionBalance, locale);
                addProductionCosts(document, productionBalance, locale);
            } else if (isTypeOfProductionRecordingForEach) {
                productionBalancePdfService.addMachineTimeBalance(document, productionBalance, locale);
                addCostsBalance("machine", document, productionBalance, locale);

                productionBalancePdfService.addLaborTimeBalance(document, productionBalance, locale);
                addCostsBalance("labor", document, productionBalance, locale);
            }
        } else if (isTypePiecework) {
            productionBalancePdfService.addPieceworkBalance(document, productionBalance, locale);
            addCostsBalance("cycles", document, productionBalance, locale);
        }

        costCalculationPdfService.printMaterialAndOperationNorms(document, productionBalance, locale);
    }

    private void addProductionCosts(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate(
                "productionCounting.productionBalance.report.panel.productionCosts", locale), FontUtils.getDejavuBold11Dark()));

        PdfPTable costsPanel = pdfHelper.createPanelTable(3);

        addCurrencyNumericWithLabel(costsPanel,
                "productionCounting.productionBalance.report.panel.productionCosts.machinePlanned",
                productionBalance.getField(ProductionBalanceFieldsPCWC.PLANNED_MACHINE_COSTS), locale);
        addCurrencyNumericWithLabel(costsPanel,
                "productionCounting.productionBalance.report.panel.productionCosts.machineRegistered",
                productionBalance.getField(ProductionBalanceFieldsPCWC.MACHINE_COSTS), locale);
        addCurrencyNumericWithLabel(costsPanel,
                "productionCounting.productionBalance.report.panel.productionCosts.machineBalance",
                productionBalance.getField(ProductionBalanceFieldsPCWC.MACHINE_COSTS_BALANCE), locale);
        addCurrencyNumericWithLabel(costsPanel, "productionCounting.productionBalance.report.panel.productionCosts.laborPlanned",
                productionBalance.getField(ProductionBalanceFieldsPCWC.PLANNED_LABOR_COSTS), locale);
        addCurrencyNumericWithLabel(costsPanel,
                "productionCounting.productionBalance.report.panel.productionCosts.laborRegistered",
                productionBalance.getField(ProductionBalanceFieldsPCWC.LABOR_COSTS), locale);
        addCurrencyNumericWithLabel(costsPanel, "productionCounting.productionBalance.report.panel.productionCosts.laborBalance",
                productionBalance.getField(ProductionBalanceFieldsPCWC.LABOR_COSTS_BALANCE), locale);

        costsPanel.setSpacingBefore(10);
        document.add(costsPanel);
    }

    private void addMaterialCost(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {

        List<String> materialCostTableHeader = Lists.newArrayList();

        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.productNumber",
                                locale));
        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.plannedCost",
                                locale));
        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.registeredCost",
                                locale));
        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.balance",
                                locale));

        @SuppressWarnings("unchecked")
        List<Entity> products = (List<Entity>) productionBalance.getField("technologyInstOperProductInComps");

        if (!products.isEmpty()) {
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(translationService.translate(
                    "productionCounting.productionBalance.report.table.materialCost", locale), FontUtils.getDejavuBold11Dark()));

            // FIXME mici, had to generate a new linked list in order to sort it.
            products = Lists.newLinkedList(products);
            Collections.sort(products, new EntityProductInOutComparator());

            PdfPTable productsTable = pdfHelper.createTableWithHeader(4, materialCostTableHeader, false);

            String currency = " " + currencyService.getCurrencyAlphabeticCode();

            for (Entity product : products) {
                productsTable.addCell(new Phrase(product.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT)
                        .getStringField("number"), FontUtils.getDejavuRegular9Dark()));

                String plannedCost = numberService.format(product.getField("plannedCost"));
                productsTable.addCell(new Phrase((plannedCost == null) ? NULL_OBJECT : (plannedCost + currency), FontUtils
                        .getDejavuRegular9Dark()));
                String registeredCost = numberService.format(product.getField("registeredCost"));
                productsTable.addCell(new Phrase((registeredCost == null) ? NULL_OBJECT : (registeredCost + currency), FontUtils
                        .getDejavuRegular9Dark()));
                String balance = numberService.format(product.getField("balance"));
                productsTable.addCell(new Phrase((balance == null) ? NULL_OBJECT : (balance + currency), FontUtils
                        .getDejavuRegular9Dark()));
            }

            productsTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                    locale), FontUtils.getDejavuRegular9Dark()));
            String plannedComponentsCosts = numberService.format(productionBalance
                    .getDecimalField(ProductionBalanceFieldsPCWC.PLANNED_COMPONENTS_COSTS));
            productsTable.addCell(new Phrase(
                    (plannedComponentsCosts == null) ? NULL_OBJECT : (plannedComponentsCosts + currency), FontUtils
                            .getDejavuRegular9Dark()));
            String componentsCosts = numberService.format(productionBalance
                    .getDecimalField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS));
            productsTable.addCell(new Phrase((componentsCosts == null) ? NULL_OBJECT : (componentsCosts + currency), FontUtils
                    .getDejavuRegular9Dark()));
            String componentsCostsBalance = numberService.format(productionBalance
                    .getDecimalField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS_BALANCE));
            productsTable.addCell(new Phrase(
                    (componentsCostsBalance == null) ? NULL_OBJECT : (componentsCostsBalance + currency), FontUtils
                            .getDejavuRegular9Dark()));

            document.add(productsTable);
        }
    }

    private void addCostsBalance(final String type, final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {

        List<String> tableHeader = Lists.newArrayList();

        tableHeader.add(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column.operationLevel", locale));
        tableHeader.add(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column.operationName", locale));
        tableHeader.add(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column.planned"
                        + upperCaseFirstLetter(type, locale) + L_COSTS, locale));
        tableHeader.add(translationService
                .translate("productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column." + type
                        + L_COSTS, locale));
        tableHeader.add(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column." + type + "CostsBalance",
                locale));

        boolean isPiecework = "cycles".equals(type);

        @SuppressWarnings("unchecked")
        List<Entity> operationComponents = (List<Entity>) productionBalance
                .getField(isPiecework ? "operationPieceworkCostComponents"
                        : ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS);

        if (!operationComponents.isEmpty()) {
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(translationService.translate(
                    "productionCounting.productionBalanceDetails.window.workCostsTab." + type + L_COSTS, locale), FontUtils
                    .getDejavuBold11Dark()));

            // FIXME mici, had to generate a new linked list in order to sort it.
            operationComponents = Lists.newLinkedList(operationComponents);
            Collections.sort(operationComponents, new EntityProductionTrackingOperationComparator());

            PdfPTable costsTable = pdfHelper.createTableWithHeader(5, tableHeader, false);

            String currency = " " + currencyService.getCurrencyAlphabeticCode();

            for (Entity operationComponent : operationComponents) {
                costsTable.addCell(new Phrase(operationComponent.getBelongsToField("technologyInstanceOperationComponent")
                        .getStringField("nodeNumber"), FontUtils.getDejavuRegular9Dark()));
                costsTable.addCell(new Phrase(operationComponent.getBelongsToField("technologyInstanceOperationComponent")
                        .getBelongsToField("operation").getStringField("name"), FontUtils.getDejavuRegular9Dark()));

                String plannedCost = numberService.format(operationComponent.getField("planned"
                        + upperCaseFirstLetter(type, locale) + L_COSTS));
                costsTable.addCell(new Phrase((plannedCost == null) ? NULL_OBJECT : (plannedCost + currency), FontUtils
                        .getDejavuRegular9Dark()));
                String registeredCost = numberService.format(operationComponent.getField(type + L_COSTS));
                costsTable.addCell(new Phrase((registeredCost == null) ? NULL_OBJECT : (registeredCost + currency), FontUtils
                        .getDejavuRegular9Dark()));
                String balance = numberService.format(operationComponent.getField(type + "CostsBalance"));
                costsTable.addCell(new Phrase((balance == null) ? NULL_OBJECT : (balance + currency), FontUtils
                        .getDejavuRegular9Dark()));
            }

            costsTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                    locale), FontUtils.getDejavuRegular9Dark()));
            costsTable.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));

            String plannedCosts = numberService.format(productionBalance.getDecimalField("planned"
                    + upperCaseFirstLetter(type, locale) + L_COSTS));
            costsTable.addCell(new Phrase((plannedCosts == null) ? NULL_OBJECT : (plannedCosts + currency), FontUtils
                    .getDejavuRegular9Dark()));
            String registeredCosts = numberService.format(productionBalance.getDecimalField(type + L_COSTS));
            costsTable.addCell(new Phrase((registeredCosts == null) ? NULL_OBJECT : (registeredCosts + currency), FontUtils
                    .getDejavuRegular9Dark()));
            String costsBalance = numberService.format(productionBalance.getDecimalField(type + "CostsBalance"));
            costsTable.addCell(new Phrase((costsBalance == null) ? NULL_OBJECT : (costsBalance + currency), FontUtils
                    .getDejavuRegular9Dark()));

            document.add(costsTable);
        }
    }

    public PdfPTable createParametersForCostsPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable parametersForCostsPanel = pdfHelper.createPanelTable(1);

        parametersForCostsPanel.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalance.report.panel.parametersForCosts", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        PdfPTable content = pdfHelper.createPanelTable(2);
        content.setTableEvent(null);

        String sourceOfMaterialCostsField = productionBalance.getStringField("sourceOfMaterialCosts");
        String sourceOfMaterialCosts = translationService.translate(
                "productionCounting.productionBalance.sourceOfMaterialCosts.value." + sourceOfMaterialCostsField, locale);
        pdfHelper.addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.sourceOfMaterialCosts.label", locale),
                sourceOfMaterialCosts, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular9Dark(), 2);

        String calculateMaterialCostsModeField = productionBalance.getStringField("calculateMaterialCostsMode");
        String calculateMaterialCostsMode = translationService.translate(
                "productionCounting.productionBalance.calculateMaterialCostsMode.value." + calculateMaterialCostsModeField,
                locale);
        pdfHelper.addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.calculateMaterialCostsMode.label", locale),
                calculateMaterialCostsMode, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular9Dark(), 2);

        parametersForCostsPanel.addCell(content);

        return parametersForCostsPanel;
    }

    private PdfPTable createAssumptionsForCumulatedRecordsPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable parametersForCostsPanel = pdfHelper.createPanelTable(1);

        parametersForCostsPanel.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalance.report.panel.assumptionsForCumulatedRecords", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        PdfPTable content = pdfHelper.createPanelTable(2);
        content.setTableEvent(null);

        BigDecimal averageMachineHourlyCost = productionBalance.getDecimalField("averageMachineHourlyCost");
        String averageMachineHourlyCostLabel = translationService.translate(
                "productionCounting.productionBalance.averageMachineHourlyCost.label", locale);
        pdfHelper.addTableCellAsTable(content, averageMachineHourlyCostLabel, numberService.format(averageMachineHourlyCost),
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular9Dark(), 2);

        BigDecimal averageLaborHourlyCost = productionBalance.getDecimalField("averageLaborHourlyCost");
        String averageLaborHourlyCostLabel = translationService.translate(
                "productionCounting.productionBalance.averageLaborHourlyCost.label", locale);
        pdfHelper.addTableCellAsTable(content, averageLaborHourlyCostLabel, numberService.format(averageLaborHourlyCost),
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular9Dark(), 2);

        parametersForCostsPanel.addCell(content);

        return parametersForCostsPanel;
    }

    private PdfPTable createCostsPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable content = pdfHelper.createPanelTable(1);

        addRegisteredTechnicalCosts(content, productionBalance, locale);
        addPlannedTechnicalCosts(content, productionBalance, locale);
        addTechnicalCostsBalance(content, productionBalance, locale);

        return content;
    }

    private void addCurrencyNumericWithLabel(final PdfPTable table, final String labelLocale, final Object value,
            final Locale locale, final Font labelFont, final Font valueFont) {
        String toDisplay = NULL_OBJECT;
        BigDecimal valueBD = (BigDecimal) value;
        if (valueBD != null) {
            String currency = currencyService.getCurrencyAlphabeticCode();
            toDisplay = numberService.format(valueBD) + " " + currency;
        }

        pdfHelper.addTableCellAsTable(table, translationService.translate(labelLocale, locale), toDisplay, labelFont, valueFont,
                2);
    }

    private void addCurrencyNumericWithLabel(final PdfPTable table, final String labelLocale, final Object value,
            final Locale locale) {
        addCurrencyNumericWithLabel(table, labelLocale, value, locale, FontUtils.getDejavuRegular9Dark(),
                FontUtils.getDejavuRegular9Dark());
    }

    private void addRegisteredTechnicalCosts(final PdfPTable content, final Entity productionBalance, final Locale locale) {
        content.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.registeredTechnicalProductionCosts", locale)
                + ":", FontUtils.getDejavuBold10Dark()));
        addCurrencyNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.registeredTotalTechnicalProductionCostsLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICALPRODUCTION_COSTS), locale);
        addCurrencyNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.registeredTotalTechnicalProductionCostPerUnitLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT),
                locale);
    }

    private void addPlannedTechnicalCosts(final PdfPTable content, final Entity productionBalance, final Locale locale) {
        content.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.plannedTechnicalProductionCosts", locale)
                + ":", FontUtils.getDejavuBold10Dark()));
        addCurrencyNumericWithLabel(content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.totalTechnicalProductionCostsLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COSTS), locale);
        addCurrencyNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.totalTechnicalProductionCostPerUnitLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT), locale);

    }

    private void addTechnicalCostsBalance(final PdfPTable content, final Entity productionBalance, final Locale locale) {
        content.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.technicalProductionCostsBalance", locale)
                + ":", FontUtils.getDejavuBold10Dark()));
        addCurrencyNumericWithLabel(content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.balanceTechnicalProductionCostsLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COSTS), locale);
        addCurrencyNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.balanceTechnicalProductionCostPerUnitLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT), locale);

    }

    private PdfPTable createOverheadsAndSummaryPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable content = pdfHelper.createPanelTable(1);

        content.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.overheadsForRegisteredCosts", locale) + ":",
                FontUtils.getDejavuBold10Dark()));

        addCurrencyNumericWithLabel(content, "productionCounting.productionBalance.productionCostMarginValue.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN_VALUE), locale);
        addCurrencyNumericWithLabel(content, "productionCounting.productionBalance.materialCostMarginValue.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN_VALUE), locale);
        addCurrencyNumericWithLabel(content, "productionCounting.productionBalance.additionalOverheadValue.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD_VALUE), locale);
        addCurrencyNumericWithLabel(content, "productionCounting.productionBalance.totalOverhead.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.TOTAL_OVERHEAD), locale,
                FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuBold9Dark());

        content.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.summaryForRegisteredCosts", locale),
                FontUtils.getDejavuBold10Dark()));

        addCurrencyNumericWithLabel(content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.totalCostsLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.TOTAL_COSTS), locale);
        addCurrencyNumericWithLabel(content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.totalCostPerUnitLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.TOTAL_COST_PER_UNIT), locale);

        return content;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

    private String upperCaseFirstLetter(final String givenString, final Locale locale) {
        return givenString.substring(0, 1).toUpperCase(locale) + givenString.substring(1);
    }
}
