/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.print.CostCalculationPdfService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.print.utils.EntityProductionTrackingOperationComparator;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.TechnologyOperationProductInCompFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public class ProductionBalanceWithCostsPdfService extends PdfDocumentService {

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String L_PLANNED = "planned";

    private static final String L_COSTS = "Costs";

    private static final String L_COSTS_BALANCE = "CostsBalance";

    private static final String L_NULL_OBJECT = "-";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private CostCalculationPdfService costCalculationPdfService;

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
        String calculateOperationCostMode = productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        final boolean isTypeOfProductionRecordingCumulated = productionCountingService
                .isTypeOfProductionRecordingCumulated(typeOfProductionRecording);
        final boolean isTypeOfProductionRecordingForEach = productionCountingService
                .isTypeOfProductionRecordingForEach(typeOfProductionRecording);
        final boolean isCalculateOperationCostModeHourly = productionCountingService
                .isCalculateOperationCostModeHourly(calculateOperationCostMode);
        final boolean isCalculateOperationCostModePiecework = productionCountingService
                .isCalculateOperationCostModePiecework(calculateOperationCostMode);

        if (isCalculateOperationCostModeHourly && isTypeOfProductionRecordingCumulated) {
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

        if (isCalculateOperationCostModeHourly) {
            if (isTypeOfProductionRecordingCumulated) {
                productionBalancePdfService.addTimeBalanceAsPanel(document, productionBalance, locale);
                addProductionCosts(document, productionBalance, locale);
            } else if (isTypeOfProductionRecordingForEach) {
                productionBalancePdfService.addMachineTimeBalance(document, productionBalance, locale);
                addCostsBalance("machine", document, productionBalance, locale);

                productionBalancePdfService.addLaborTimeBalance(document, productionBalance, locale);
                addCostsBalance("labor", document, productionBalance, locale);
            }
        } else if (isCalculateOperationCostModePiecework) {
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
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyOperationProductInComponents.column.productNumber",
                                locale));
        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyOperationProductInComponents.column.plannedCost",
                                locale));
        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyOperationProductInComponents.column.registeredCost",
                                locale));
        materialCostTableHeader
                .add(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyOperationProductInComponents.column.balance",
                                locale));
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        alignments
                .put(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.productNumber",
                                locale), HeaderAlignment.LEFT);
        alignments
                .put(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.plannedCost",
                                locale), HeaderAlignment.RIGHT);

        alignments
                .put(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.registeredCost",
                                locale), HeaderAlignment.RIGHT);

        alignments
                .put(translationService
                        .translate(
                                "productionCounting.productionBalanceDetails.window.materialCostsTab.technologyInstOperProductInComps.column.balance",
                                locale), HeaderAlignment.RIGHT);

        List<Entity> technologyOperationProductInComponents = productionBalance
                .getHasManyField(ProductionBalanceFieldsPCWC.TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS);

        if (!technologyOperationProductInComponents.isEmpty()) {
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(translationService.translate(
                    "productionCounting.productionBalance.report.table.materialCost", locale), FontUtils.getDejavuBold11Dark()));

            technologyOperationProductInComponents = Lists.newLinkedList(technologyOperationProductInComponents);
            Collections.sort(technologyOperationProductInComponents, new EntityProductInOutComparator());

            PdfPTable productsTable = pdfHelper.createTableWithHeader(4, materialCostTableHeader, false, alignments);

            String currency = " " + currencyService.getCurrencyAlphabeticCode();

            for (Entity technologyOperationProductInComponent : technologyOperationProductInComponents) {
                productsTable.addCell(new Phrase(technologyOperationProductInComponent.getBelongsToField(
                        TechnologyOperationProductInCompFields.PRODUCT).getStringField(ProductFields.NUMBER), FontUtils
                        .getDejavuRegular7Dark()));
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

                String plannedCost = numberService.format(technologyOperationProductInComponent
                        .getField(TechnologyOperationProductInCompFields.PLANNED_COST));
                productsTable.addCell(new Phrase((plannedCost == null) ? L_NULL_OBJECT : (plannedCost + currency), FontUtils
                        .getDejavuRegular7Dark()));
                String registeredCost = numberService.format(technologyOperationProductInComponent
                        .getField(TechnologyOperationProductInCompFields.REGISTERED_COST));
                productsTable.addCell(new Phrase((registeredCost == null) ? L_NULL_OBJECT : (registeredCost + currency),
                        FontUtils.getDejavuRegular7Dark()));
                String balance = numberService.format(technologyOperationProductInComponent
                        .getField(TechnologyOperationProductInCompFields.BALANCE));
                productsTable.addCell(new Phrase((balance == null) ? L_NULL_OBJECT : (balance + currency), FontUtils
                        .getDejavuRegular7Dark()));
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

            }

            productsTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                    locale), FontUtils.getDejavuRegular7Dark()));
            String plannedComponentsCosts = numberService.format(productionBalance
                    .getDecimalField(ProductionBalanceFieldsPCWC.PLANNED_COMPONENTS_COSTS));
            productsTable.addCell(new Phrase((plannedComponentsCosts == null) ? L_NULL_OBJECT
                    : (plannedComponentsCosts + currency), FontUtils.getDejavuRegular7Dark()));
            String componentsCosts = numberService.format(productionBalance
                    .getDecimalField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS));
            productsTable.addCell(new Phrase((componentsCosts == null) ? L_NULL_OBJECT : (componentsCosts + currency), FontUtils
                    .getDejavuRegular7Dark()));
            String componentsCostsBalance = numberService.format(productionBalance
                    .getDecimalField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS_BALANCE));
            productsTable.addCell(new Phrase((componentsCostsBalance == null) ? L_NULL_OBJECT
                    : (componentsCostsBalance + currency), FontUtils.getDejavuRegular7Dark()));
            productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

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
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
        alignments.put(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column.operationLevel", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column.operationName", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column.planned"
                        + upperCaseFirstLetter(type, locale) + L_COSTS, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService
                .translate("productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column." + type
                        + L_COSTS, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(
                "productionCounting.productionBalanceDetails.window.workCostsTab.operationsCost.column." + type + "CostsBalance",
                locale), HeaderAlignment.RIGHT);

        boolean isPiecework = "cycles".equals(type);

        List<Entity> operationComponents = productionBalance
                .getHasManyField(isPiecework ? ProductionBalanceFieldsPCWC.OPERATION_PIECEWORK_COST_COMPONENTS
                        : ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS);

        if (!operationComponents.isEmpty()) {
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(translationService.translate(
                    "productionCounting.productionBalanceDetails.window.workCostsTab." + type + L_COSTS, locale), FontUtils
                    .getDejavuBold11Dark()));

            operationComponents = Lists.newLinkedList(operationComponents);
            Collections.sort(operationComponents, new EntityProductionTrackingOperationComparator());

            PdfPTable costsTable = pdfHelper.createTableWithHeader(5, tableHeader, false, alignments);

            String currency = " " + currencyService.getCurrencyAlphabeticCode();

            for (Entity operationComponent : operationComponents) {
                costsTable.addCell(new Phrase(operationComponent.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT)
                        .getStringField(TechnologyOperationComponentFields.NODE_NUMBER), FontUtils.getDejavuRegular7Dark()));
                costsTable.addCell(new Phrase(operationComponent.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT)
                        .getBelongsToField(TechnologyOperationComponentFields.OPERATION).getStringField(OperationFields.NAME),
                        FontUtils.getDejavuRegular7Dark()));

                costsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

                String plannedCost = numberService.format(operationComponent.getField("planned"
                        + upperCaseFirstLetter(type, locale) + L_COSTS));
                costsTable.addCell(new Phrase((plannedCost == null) ? L_NULL_OBJECT : (plannedCost + currency), FontUtils
                        .getDejavuRegular7Dark()));
                String registeredCost = numberService.format(operationComponent.getField(type + L_COSTS));
                costsTable.addCell(new Phrase((registeredCost == null) ? L_NULL_OBJECT : (registeredCost + currency), FontUtils
                        .getDejavuRegular7Dark()));
                String balance = numberService.format(operationComponent.getField(type + L_COSTS_BALANCE));
                costsTable.addCell(new Phrase((balance == null) ? L_NULL_OBJECT : (balance + currency), FontUtils
                        .getDejavuRegular7Dark()));
                costsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

            }

            costsTable.addCell(new Phrase(translationService.translate("productionCounting.productionBalance.report.total",
                    locale), FontUtils.getDejavuRegular7Dark()));
            costsTable.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            costsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            String plannedCosts = numberService.format(productionBalance.getDecimalField(L_PLANNED
                    + upperCaseFirstLetter(type, locale) + L_COSTS));
            costsTable.addCell(new Phrase((plannedCosts == null) ? L_NULL_OBJECT : (plannedCosts + currency), FontUtils
                    .getDejavuRegular7Dark()));
            String registeredCosts = numberService.format(productionBalance.getDecimalField(type + L_COSTS));
            costsTable.addCell(new Phrase((registeredCosts == null) ? L_NULL_OBJECT : (registeredCosts + currency), FontUtils
                    .getDejavuRegular7Dark()));
            String costsBalance = numberService.format(productionBalance.getDecimalField(type + L_COSTS_BALANCE));
            costsTable.addCell(new Phrase((costsBalance == null) ? L_NULL_OBJECT : (costsBalance + currency), FontUtils
                    .getDejavuRegular7Dark()));
            costsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

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

        String sourceOfMaterialCostsField = productionBalance
                .getStringField(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS);
        String sourceOfMaterialCosts = translationService.translate(
                "productionCounting.productionBalance.sourceOfMaterialCosts.value." + sourceOfMaterialCostsField, locale);
        pdfHelper.addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.sourceOfMaterialCosts.label", locale),
                sourceOfMaterialCosts, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular7Dark(), 2);

        String calculateMaterialCostsModeField = productionBalance
                .getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE);
        String calculateMaterialCostsMode = translationService.translate(
                "productionCounting.productionBalance.calculateMaterialCostsMode.value." + calculateMaterialCostsModeField,
                locale);
        pdfHelper.addTableCellAsTable(content,
                translationService.translate("productionCounting.productionBalance.calculateMaterialCostsMode.label", locale),
                calculateMaterialCostsMode, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular7Dark(), 2);

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

        BigDecimal averageMachineHourlyCost = productionBalance
                .getDecimalField(ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST);
        String averageMachineHourlyCostLabel = translationService.translate(
                "productionCounting.productionBalance.averageMachineHourlyCost.label", locale);
        pdfHelper.addTableCellAsTable(content, averageMachineHourlyCostLabel, numberService.format(averageMachineHourlyCost),
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular7Dark(), 2);

        BigDecimal averageLaborHourlyCost = productionBalance
                .getDecimalField(ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST);
        String averageLaborHourlyCostLabel = translationService.translate(
                "productionCounting.productionBalance.averageLaborHourlyCost.label", locale);
        pdfHelper.addTableCellAsTable(content, averageLaborHourlyCostLabel, numberService.format(averageLaborHourlyCost),
                FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular7Dark(), 2);

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
        String toDisplay = L_NULL_OBJECT;
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
        addCurrencyNumericWithLabel(table, labelLocale, value, locale, FontUtils.getDejavuRegular7Dark(),
                FontUtils.getDejavuRegular7Dark());
    }

    private void addRegisteredTechnicalCosts(final PdfPTable content, final Entity productionBalance, final Locale locale) {
        content.addCell(new Phrase(translationService.translate(
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.registeredTechnicalProductionCosts", locale)
                + ":", FontUtils.getDejavuBold10Dark()));
        addCurrencyNumericWithLabel(
                content,
                "productionCounting.productionBalanceDetails.window.costsSummaryTab.registeredTotalTechnicalProductionCostsLabel.label",
                productionBalance.getField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COSTS), locale);
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
                FontUtils.getDejavuRegular7Dark(), FontUtils.getDejavuBold9Dark());

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

    private String upperCaseFirstLetter(final String givenString, final Locale locale) {
        return givenString.substring(0, 1).toUpperCase(locale) + givenString.substring(1);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("productionCounting.productionBalance.report.title", locale);
    }

}
