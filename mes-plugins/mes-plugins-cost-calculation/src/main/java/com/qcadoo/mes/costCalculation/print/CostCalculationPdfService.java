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
package com.qcadoo.mes.costCalculation.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculationResultFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.costCalculation.print.utils.CostCalculationMaterial;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.costNormsForOperation.constants.TechnologyOperationComponentFieldsCNFO;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.utils.TimeConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CostCalculationPdfService extends PdfDocumentService {

    private static final String L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER = "costCalculation.costCalculationDetails.report.columnHeader.number";

    private static final String L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME = "costCalculation.costCalculationDetails.report.columnHeader.name";

    private static final String L_COLUMN_HEADER_QUANTITY = "costCalculation.costCalculationDetails.report.columnHeader.quantity";

    private static final String L_COLUMN_HEADER_UNIT = "costCalculation.costCalculationDetails.report.columnHeader.unit";

    private static final String L_COLUMN_HEADER_COSTS = "costCalculation.costCalculationDetails.report.columnHeader.costs";

    private static final String L_COLUMN_HEADER_MARGIN = "costCalculation.costCalculationDetails.report.columnHeader.margin";

    private static final String L_COLUMN_HEADER_TOTAL_COSTS = "costCalculation.calculationResult.totalCost.label";

    private static final String L_COLUMN_HEADER_LEVEL = "costCalculation.costCalculationDetails.report.columnHeader.level";

    private static final String L_COLUMN_HEADER_MACH_DURATION = "costCalculation.costCalculationDetails.report.columnHeader.machDuration";

    private static final String L_COLUMN_HEADER_MACH_COSTS = "costCalculation.costCalculationDetails.report.columnHeader.machCosts";

    private static final String L_COLUMN_HEADER_LAB_DURATION = "costCalculation.costCalculationDetails.report.columnHeader.labDuration";

    private static final String L_COLUMN_HEADER_LAB_COSTS = "costCalculation.costCalculationDetails.report.columnHeader.labCosts";

    private static final String L_LOCALE_TYPE = "localeType";

    private static final String L_COST_MODE = "costMode";

    private static final String L_TAB_IN_TEXT = "\t \t \t";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private CostCalculationMaterialsService costCalculationMaterialsService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CostCalculationComponentsService costCalculationComponentsService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("costCalculation.costCalculationDetails.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        DataDefinition dataDefCostCalculation = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION);
        Entity costCalculation = dataDefCostCalculation.get(entity.getId());
        Entity technology = costCalculation.getManyToManyField(CostCalculationFields.TECHNOLOGIES).get(0);
        Entity calculationResult = costCalculation.getHasManyField(CostCalculationFields.CALCULATION_RESULTS).get(0);

        PdfPTable topPanelColumn = addTopPanelToReport(costCalculation, technology, locale);
        PdfPTable middlePanelColumn = addMiddlePanelToReport(costCalculation, calculationResult, locale);
        PdfPTable bottomPanelColumn = addBottomPanelToReport(costCalculation, calculationResult, locale);

        PdfPTable panelTable = pdfHelper.createPanelTable(1);

        panelTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        panelTable.addCell(topPanelColumn);
        panelTable.addCell(middlePanelColumn);
        panelTable.addCell(bottomPanelColumn);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);

        panelTable.setTableEvent(null);
        panelTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        document.add(panelTable);

        document.add(
                new Paragraph(translationService.translate("costCalculation.costCalculationDetails.report.paragraph", locale),
                        FontUtils.getDejavuBold11Dark()));

        PdfPTable materialsTable = addMaterialsTable(costCalculation, technology, calculationResult, locale);
        document.add(materialsTable);

        document.add(Chunk.NEWLINE);
        document.add(
                new Paragraph(translationService.translate("costCalculation.costCalculationDetails.report.paragraph2", locale),
                        FontUtils.getDejavuBold11Dark()));

        document.add(addHourlyCostsTable(costCalculation, calculationResult, locale));

        boolean includeComponents = costCalculation.getBooleanField("includeComponents");

        if (includeComponents) {
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(
                    translationService.translate("costCalculation.costCalculationDetails.report.componentsParagraph", locale),
                    FontUtils.getDejavuBold11Dark()));

            PdfPTable componentsTable = addComponentsTable(costCalculation, technology, locale);
            document.add(componentsTable);
        }
        printMaterialAndOperationNorms(document, costCalculation, technology, locale);
    }

    private PdfPTable addComponentsTable(final Entity costCalculation, final Entity technology, final Locale locale) {
        List<ComponentsCalculationHolder> basicComponents = costCalculationComponentsService
                .getComponentCosts(costCalculation, technology);

        List<String> componentsTableHeader = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        componentsTableHeader.addAll(Stream
                .of("costCalculation.costCalculationDetails.report.columnHeader.components.number",
                        "costCalculation.costCalculationDetails.report.columnHeader.components.name",
                        "costCalculation.costCalculationDetails.report.columnHeader.components.costOfMaterials",
                        "costCalculation.costCalculationDetails.report.columnHeader.components.costOfLabor",
                        "costCalculation.costCalculationDetails.report.columnHeader.components.sumOfCosts",
                        "costCalculation.costCalculationDetails.report.columnHeader.components.costPerUnit")
                .map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        alignments.put(translationService.translate(
                "costCalculation.costCalculationDetails.report.columnHeader.components.number", locale), HeaderAlignment.LEFT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.components.name",
                locale), HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(
                        "costCalculation.costCalculationDetails.report.columnHeader.components.costOfMaterials", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService
                .translate("costCalculation.costCalculationDetails.report.columnHeader.components.costOfLabor", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService
                .translate("costCalculation.costCalculationDetails.report.columnHeader.components.sumOfCosts", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService
                .translate("costCalculation.costCalculationDetails.report.columnHeader.components.costPerUnit", locale),
                HeaderAlignment.RIGHT);

        PdfPTable componentsTable = pdfHelper.createTableWithHeader(componentsTableHeader.size(), componentsTableHeader, false,
                alignments);

        try {
            float[] columnWidths = { 1.5f, 1.5f, 1f, 1f, 1f, 1f };
            componentsTable.setWidths(columnWidths);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        DataDefinition ccDD = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COMPONENT_COST);
        List<Entity> componentsCost = Lists.newArrayList();
        for (ComponentsCalculationHolder component : basicComponents) {
            componentsTable.addCell(
                    new Phrase(component.getProduct().getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            componentsTable.addCell(
                    new Phrase(component.getProduct().getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
            componentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            componentsTable
                    .addCell(new Phrase(numberService.format(BigDecimalUtils.convertNullToZero(component.getMaterialCost())),
                            FontUtils.getDejavuRegular7Dark()));
            componentsTable.addCell(new Phrase(numberService.format(BigDecimalUtils.convertNullToZero(component.getLaborCost())),
                    FontUtils.getDejavuRegular7Dark()));
            componentsTable.addCell(new Phrase(numberService.format(BigDecimalUtils.convertNullToZero(component.getSumOfCost())),
                    FontUtils.getDejavuRegular7Dark()));
            componentsTable
                    .addCell(new Phrase(numberService.format(BigDecimalUtils.convertNullToZero(component.getCostPerUnit())),
                            FontUtils.getDejavuRegular7Dark()));

            componentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            Entity cc = ccDD.create();
            cc.setField("product", component.getProduct());
            cc.setField("pricePerUnit", component.getCostPerUnit());
            componentsCost.add(cc);

        }
        Entity costCalculationDB = costCalculation.getDataDefinition().get(costCalculation.getId());
        costCalculationDB.setField(CostCalculationFields.COMPONENT_COST, componentsCost);
        costCalculationDB.getDataDefinition().save(costCalculationDB);
        return componentsTable;
    }

    public PdfPTable addTopPanelToReport(final Entity costCalculation, final Entity technology, final Locale locale) {
        PdfPTable panelColumn = pdfHelper.createPanelTable(2);
        panelColumn.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);

        String materialCostsUsed = costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED);

        int[] proportions = new int[] { 30, 70 };
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.number.label", locale) + ":",
                costCalculation.getStringField(CostCalculationFields.NUMBER), proportions);
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.report.product", locale) + ":",
                technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NAME), proportions);
        addAsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.report.technology", locale) + ":",
                technology.getStringField(TechnologyFields.NAME), proportions);
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.quantity.label", locale) + ":",
                numberService.format(costCalculation.getField(CostCalculationFields.QUANTITY)), proportions);
        addAsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.description.label", locale) + ":",
                costCalculation.getStringField(CostCalculationFields.DESCRIPTION), proportions);

        PdfPCell leftCell = new PdfPCell(leftPanelColumn);
        leftCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(leftCell);

        PdfPTable rightPanelColumn = pdfHelper.createPanelTable(1);

        rightPanelColumn.addCell(new Phrase(translationService
                .translate("costCalculation.costCalculationDetails.window.inputDataTab.materialCostsParameters", locale) + ":",
                FontUtils.getDejavuBold10Dark()));

        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate(
                        "costCalculation.costCalculationDetails.report.columnHeader.includeAdditionalTime", locale) + ":",
                costCalculation.getBooleanField(CostCalculationFields.INCLUDE_ADDITIONAL_TIME)
                        ? translationService.translate("qcadooView.true", locale)
                        : translationService.translate("qcadooView.false", locale));

        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.includeTPZ.label", locale) + ":",
                costCalculation.getBooleanField(CostCalculationFields.INCLUDE_TPZ)
                        ? translationService.translate("qcadooView.true", locale)
                        : translationService.translate("qcadooView.false", locale));

        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.materialCostsUsed.label", locale),
                materialCostsUsed == null ? translationService.translate("qcadooView.form.blankComboBoxValue", locale)
                        : translationService.translate(
                                "costCalculation.costCalculation.materialCostsUsed.value." + materialCostsUsed, locale));

        PdfPCell rightCell = new PdfPCell(rightPanelColumn);
        rightCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(rightCell);
        return panelColumn;
    }

    private void addAsTable(final PdfPTable table, final String label, final Object fieldValue) {
        addAsTable(table, label, fieldValue, new int[] {});
    }

    private PdfPTable addMiddlePanelToReport(final Entity costCalculation, final Entity calculationResult, final Locale locale) {
        PdfPTable panelColumn = pdfHelper.createPanelTable(2);
        panelColumn.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);

        leftPanelColumn.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.calculationResultsTab.formCalculationResults.sellPriceOverheads",
                locale) + ":", FontUtils.getDejavuBold10Dark()));

        Object reportData = calculationResult.getDecimalField(CalculationResultFields.MATERIAL_COSTS);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.calculationResult.materialCosts.label", locale)
                        + ":",
                (reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS);
        addAsTable(leftPanelColumn, L_TAB_IN_TEXT
                + translationService.translate("costCalculation.costCalculation.totalMachineHourlyCosts.label", locale) + ":",
                (reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS);
        addAsTable(leftPanelColumn, L_TAB_IN_TEXT
                + translationService.translate("costCalculation.costCalculation.totalLaborHourlyCosts.label", locale) + ":",
                (reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        leftPanelColumn.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.calculationResultsTab.formCalculationResults.overheadsValue",
                locale) + ":", FontUtils.getDejavuBold9Dark()));

        reportData = costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN);
        Object reportData2 = calculationResult.getDecimalField(CalculationResultFields.LABOUR_COST_MARGIN_VALUE);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.productionCostMargin.label", locale)
                        + ":",
                (reportData == null ? ""
                        : format(reportData) + (reportData2 == null ? ""
                                : " %\n (" + "= " + format(reportData2) + " " + currencyService.getCurrencyAlphabeticCode()
                                        + ")")));

        reportData = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN);
        reportData2 = calculationResult.getDecimalField(CalculationResultFields.MATERIAL_COST_MARGIN_VALUE);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.materialCostMargin.label", locale)
                        + ":",
                (reportData == null ? ""
                        : format(reportData) + (reportData2 == null ? ""
                                : " %\n (" + "= " + format(reportData2) + " " + currencyService.getCurrencyAlphabeticCode()
                                        + ")")));

        reportData = costCalculation.getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.additionalOverhead.label", locale)
                        + ":",
                (reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD);
        reportData2 = calculationResult.getDecimalField(CalculationResultFields.REGISTRATION_PRICE_OVERHEAD_VALUE);
        addAsTable(leftPanelColumn, L_TAB_IN_TEXT
                + translationService.translate("costCalculation.costCalculation.registrationPriceOverhead.label", locale) + ":",
                (reportData == null ? ""
                        : format(reportData) + (reportData2 == null ? ""
                                : " %\n (" + "= " + format(reportData2) + " " + currencyService.getCurrencyAlphabeticCode()
                                        + ")")));

        PdfPCell leftCell = new PdfPCell(leftPanelColumn);
        leftCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(leftCell);
        PdfPTable rightPanelColumn = pdfHelper.createPanelTable(1);

        rightPanelColumn.addCell(
                new Phrase(translationService.translate("costCalculation.costCalculation.report.totalMaterial", locale) + ":",
                        FontUtils.getDejavuBold9Dark()));

        reportData = calculationResult.getDecimalField(CalculationResultFields.PRODUCTION_COSTS);
        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.calculationResult.productionCosts.label", locale)
                        + ":",
                (reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        rightPanelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);

        PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        reportData = calculationResult.getDecimalField(CalculationResultFields.TOTAL_COST);
        cellTable.addCell(new Phrase(
                L_TAB_IN_TEXT + translationService.translate("costCalculation.calculationResult.totalCost.label", locale) + ":",
                FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);

        reportData = calculationResult.getDecimalField(CalculationResultFields.REGISTRATION_PRICE);
        cellTable.addCell(new Phrase(L_TAB_IN_TEXT
                + translationService.translate("costCalculation.calculationResult.registrationPrice.label", locale) + ":",
                FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);

        reportData = calculationResult.getDecimalField(CalculationResultFields.TECHNICAL_PRODUCTION_COST);
        cellTable
                .addCell(new Phrase(
                        L_TAB_IN_TEXT + translationService
                                .translate("costCalculation.calculationResult.technicalProductionCost.label", locale) + ":",
                        FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);

        rightPanelColumn.addCell(cellTable);
        PdfPCell rightCell = new PdfPCell(rightPanelColumn);
        rightCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(rightCell);
        return panelColumn;
    }

    private String format(Object obj) {
        return numberService.format(numberService.setScaleWithDefaultMathContext((BigDecimal) obj, 2));
    }

    private void addRightAlignedAmountCell(PdfPTable table, Object reportData) {
        PdfPCell cell = new PdfPCell(
                new Phrase((reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode(),
                        FontUtils.getDejavuRegular10Dark()));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private PdfPTable addBottomPanelToReport(final Entity costCalculation, Entity calculationResult, final Locale locale) {
        PdfPTable panelColumn = pdfHelper.createPanelTable(2);
        panelColumn.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);

        leftPanelColumn.addCell(
                new Phrase(translationService.translate("costCalculation.calculationResult.sellingPrice.label", locale) + ":",
                        FontUtils.getDejavuBold10Dark()));

        Object reportData = costCalculation.getDecimalField(CostCalculationFields.PROFIT);
        Object reportData2 = calculationResult.getDecimalField(CalculationResultFields.PROFIT_VALUE);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.profit.label", locale) + ":",
                (reportData == null ? ""
                        : format(reportData) + (reportData2 == null ? ""
                                : " %\n (" + "= " + format(reportData2) + " " + currencyService.getCurrencyAlphabeticCode()
                                        + ")")));

        PdfPCell leftCell = new PdfPCell(leftPanelColumn);
        leftCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(leftCell);
        PdfPTable rightPanelColumn = pdfHelper.createPanelTable(1);

        rightPanelColumn.addCell(
                new Phrase(translationService.translate("costCalculation.costCalculation.report.totalMaterial", locale) + ":",
                        FontUtils.getDejavuBold9Dark()));

        rightPanelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);

        PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        reportData = calculationResult.getDecimalField(CalculationResultFields.SELLING_PRICE);
        cellTable.addCell(new Phrase(L_TAB_IN_TEXT
                + translationService.translate("costCalculation.calculationResult.sellingPrice.label", locale) + ":",
                FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);
        rightPanelColumn.addCell(cellTable);

        PdfPCell rightCell = new PdfPCell(rightPanelColumn);
        rightCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(rightCell);
        return panelColumn;
    }

    private void addAsTable(final PdfPTable table, final String label, final Object fieldValue, final int[] columnWidths) {
        PdfPTable cellTable = new PdfPTable(2);

        if (columnWidths.length > 0) {
            try {
                cellTable.setWidths(columnWidths);
            } catch (DocumentException ignored) {
            }
        }

        cellTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        cellTable.addCell(new Phrase(label, FontUtils.getDejavuBold7Dark()));

        if (fieldValue == null) {
            cellTable.addCell(new Phrase("-", FontUtils.getDejavuRegular7Dark()));
        } else {
            cellTable.addCell(new Phrase(fieldValue.toString(), FontUtils.getDejavuRegular7Dark()));
        }

        table.addCell(cellTable);
    }

    private PdfPTable addMaterialsTable(final Entity costCalculation, final Entity technology, Entity calculationResult,
            final Locale locale) {
        List<String> materialsTableHeader = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        materialsTableHeader.addAll(Stream
                .of(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, L_COLUMN_HEADER_QUANTITY,
                        L_COLUMN_HEADER_UNIT, L_COLUMN_HEADER_COSTS, L_COLUMN_HEADER_MARGIN, L_COLUMN_HEADER_TOTAL_COSTS)
                .map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.number", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_QUANTITY, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_UNIT, locale), HeaderAlignment.LEFT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_COSTS, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_MARGIN, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_TOTAL_COSTS, locale), HeaderAlignment.RIGHT);

        PdfPTable materialsTable = pdfHelper.createTableWithHeader(materialsTableHeader.size(), materialsTableHeader, false,
                alignments);

        try {
            float[] columnWidths = { 1f, 1f, 0.5f, 1f, 1f, 1.5f };
            materialsTable.setWidths(columnWidths);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        MathContext mathContext = numberService.getMathContext();
        List<CostCalculationMaterial> sortedMaterials = costCalculationMaterialsService
                .getSortedMaterialsFromProductQuantities(costCalculation, technology);
        for (CostCalculationMaterial material : sortedMaterials) {
            materialsTable.addCell(new Phrase(material.getProductNumber(), material.getFont()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            materialsTable.addCell(new Phrase(numberService.format(material.getProductQuantity()), material.getFont()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            materialsTable.addCell(new Phrase(material.getUnit(), material.getFont()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            materialsTable.addCell(new Phrase(numberService.format(material.getCostForGivenQuantity()), material.getFont()));

            materialsTable.addCell(new Phrase(numberService.format(material.getToAdd()), material.getFont()));
            materialsTable.addCell(new Phrase(numberService.format(material.getTotalCost()), material.getFont()));

            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }

        BigDecimal materialCosts = calculationResult.getDecimalField(CalculationResultFields.MATERIAL_COSTS);
        BigDecimal materialCostMarginValue = calculationResult
                .getDecimalField(CalculationResultFields.MATERIAL_COST_MARGIN_VALUE);

        BigDecimal totalCostOfMaterialValue = materialCosts.add(materialCostMarginValue, mathContext);

        String materialCostsToString = numberService.format(materialCosts);
        String materialCostMarginValueToString = numberService.format(materialCostMarginValue);
        String totalCostOfMaterialToString = numberService.format(totalCostOfMaterialValue);

        materialsTable
                .addCell(new Phrase(translationService.translate("costCalculation.costCalculation.report.totalMaterial", locale),
                        FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell("");
        materialsTable.addCell("");
        materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

        materialsTable.addCell(new Phrase(materialCostsToString, FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell(new Phrase(materialCostMarginValueToString, FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell(new Phrase(totalCostOfMaterialToString, FontUtils.getDejavuRegular7Dark()));

        materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

        return materialsTable;
    }

    private PdfPTable addHourlyCostsTable(final Entity costCalculation, Entity calculationResult, final Locale locale) {
        List<String> hourlyCostsTableHeader = Lists.newArrayList();

        hourlyCostsTableHeader.addAll(Stream
                .of(L_COLUMN_HEADER_LEVEL, L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COLUMN_HEADER_MACH_DURATION, L_COLUMN_HEADER_MACH_COSTS, L_COLUMN_HEADER_LAB_DURATION,
                        L_COLUMN_HEADER_LAB_COSTS, L_COLUMN_HEADER_MARGIN, L_COLUMN_HEADER_TOTAL_COSTS)
                .map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
        alignments.put(translationService.translate(L_COLUMN_HEADER_LEVEL, locale), HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_MACH_DURATION, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_MACH_COSTS, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_LAB_DURATION, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_LAB_COSTS, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_MARGIN, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_TOTAL_COSTS, locale), HeaderAlignment.RIGHT);

        List<Entity> calculationOperationComponents = costCalculation
                .getHasManyField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        PdfPTable hourlyCostsTable = pdfHelper.createTableWithHeader(hourlyCostsTableHeader.size(), hourlyCostsTableHeader, false,
                alignments);

        try {
            float[] columnWidths = { 1f, 0.75f, 1f, 1f, 1f, 1f, 1f, 1.25f };
            hourlyCostsTable.setWidths(columnWidths);

        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (calculationOperationComponents != null && !calculationOperationComponents.isEmpty()) {
            int totalMachineWorkTimeSummary = 0;
            int totalLaborWorkTimeSummary = 0;

            BigDecimal totalOperationCostSummary = BigDecimal.ZERO;

            MathContext mathContext = numberService.getMathContext();

            for (Entity calculationOperationComponent : calculationOperationComponents) {
                Integer machineWorkTime = calculationOperationComponent
                        .getIntegerField(CalculationOperationComponentFields.MACHINE_WORK_TIME);
                Integer laborWorkTime = calculationOperationComponent
                        .getIntegerField(CalculationOperationComponentFields.LABOR_WORK_TIME);
                BigDecimal totalMachineOperationCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST);
                BigDecimal totalLaborOperationCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST);
                BigDecimal operationCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.OPERATION_COST);
                BigDecimal operationMarginCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.OPERATION_MARGIN_COST);
                BigDecimal totalOperationCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.TOTAL_OPERATION_COST);

                hourlyCostsTable.addCell(new Phrase(
                        calculationOperationComponent.getField(CalculationOperationComponentFields.NODE_NUMBER).toString(),
                        FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable
                        .addCell(new Phrase(calculationOperationComponent.getBelongsToField(TechnologiesConstants.MODEL_OPERATION)
                                .getStringField(OperationFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                hourlyCostsTable.addCell(
                        new Phrase(TimeConverterService.convertTimeToString(machineWorkTime), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(totalMachineOperationCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(
                        new Phrase(TimeConverterService.convertTimeToString(laborWorkTime), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(totalLaborOperationCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(operationMarginCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(totalOperationCost), FontUtils.getDejavuRegular7Dark()));

                hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                totalMachineWorkTimeSummary += IntegerUtils.convertNullToZero(machineWorkTime);
                totalLaborWorkTimeSummary += IntegerUtils.convertNullToZero(laborWorkTime);

                totalOperationCostSummary = totalOperationCostSummary.add(operationCost, mathContext);
            }

            BigDecimal labourCostMarginValue = calculationResult
                    .getDecimalField(CalculationResultFields.LABOUR_COST_MARGIN_VALUE);

            BigDecimal totalOperationCost = totalOperationCostSummary.add(labourCostMarginValue, mathContext);

            String totalMachineWorkTimeToString = TimeConverterService.convertTimeToString(totalMachineWorkTimeSummary);
            String totalMachineHourlyCosts = format(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS));
            String totalLaborWorkTimeToString = TimeConverterService.convertTimeToString(totalLaborWorkTimeSummary);
            String totalLaborHourlyCosts = format(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS));
            String totalLabourCostMarginValue = format(labourCostMarginValue);
            String totalOperationCostToString = format(totalOperationCost);

            hourlyCostsTable.addCell(
                    new Phrase(translationService.translate("costCalculation.costCalculation.report.totalOperation", locale),
                            FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            hourlyCostsTable.addCell(new Phrase(totalMachineWorkTimeToString, FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase(totalMachineHourlyCosts, FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase(totalLaborWorkTimeToString, FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase(totalLaborHourlyCosts, FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase(totalLabourCostMarginValue, FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase(totalOperationCostToString, FontUtils.getDejavuRegular7Dark()));

            hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }

        return hourlyCostsTable;
    }

    public PdfPTable addOptionTablePrintCostNormsOfMaterials(final Entity costCalculation, final Entity technology,
            final Locale locale) {
        List<String> optionTableHeader = Lists.newArrayList();

        Map<String, String> costModeName = getCostMode(costCalculation);

        optionTableHeader.addAll(Stream
                .of(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME, costModeName.get(L_LOCALE_TYPE))
                .map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        BigDecimal givenQty = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);

        Map<Long, BigDecimal> neededProductQuantities = productsCostCalculationService.getNeededProductQuantities(costCalculation, technology, givenQty);

        PdfPTable printCostNormsOfMaterialTable = pdfHelper.createTableWithHeader(optionTableHeader.size(), optionTableHeader,
                false);

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            printCostNormsOfMaterialTable
                    .addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable
                    .addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
            BigDecimal toDisplay = product.getDecimalField(costModeName.get(L_COST_MODE));
            BigDecimal quantity = product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER);
            String unit = product.getStringField(ProductFields.UNIT);

            printCostNormsOfMaterialTable.addCell(
                    new Phrase(numberService.format(toDisplay) + " " + " / " + numberService.format(quantity) + " " + unit,
                            FontUtils.getDejavuRegular7Dark()));
        }

        return printCostNormsOfMaterialTable;
    }

    public void printMaterialAndOperationNorms(final Document document, final Entity costCalculation, final Entity technology,
            final Locale locale) throws DocumentException {
        if (costCalculation.getBooleanField(CostCalculationFields.PRINT_COST_NORMS_OF_MATERIALS)) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(
                    translationService.translate("costCalculation.costCalculationDetails.report.paragraph3", locale),
                    FontUtils.getDejavuBold11Dark()));
            PdfPTable optionTable = addOptionTablePrintCostNormsOfMaterials(costCalculation, technology, locale);
            document.add(optionTable);
        }

        if (costCalculation.getBooleanField(CostCalculationFields.PRINT_OPERATION_NORMS)) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(
                    translationService.translate("costCalculation.costCalculationDetails.report.paragraph5", locale),
                    FontUtils.getDejavuBold11Dark()));
            addOptionTablePrintOperationNormsHourly(document, costCalculation, locale);
        }
    }

    public void addOptionTablePrintOperationNormsHourly(final Document document, final Entity costCalculation,
            final Locale locale) throws DocumentException {
        List<String> optionTableHeader = Lists.newArrayList();

        optionTableHeader.addAll(Stream
                .of(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME)
                .map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        List<Entity> calculationOperationComponents = entityTreeUtilsService
                .getSortedEntities(costCalculation.getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS));

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            PdfPTable panelTableHeader = pdfHelper.createPanelTable(2);
            PdfPTable panelTableContent = pdfHelper.createPanelTable(2);
            panelTableHeader.setSpacingBefore(10);
            panelTableContent.getDefaultCell().setBackgroundColor(null);
            panelTableContent.setTableEvent(null);

            Entity technologyOperationComponent = calculationOperationComponent
                    .getBelongsToField(CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity operation = calculationOperationComponent.getBelongsToField(CalculationOperationComponentFields.OPERATION);

            panelTableHeader
                    .addCell(new Phrase(
                            translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                                    locale) + ": " + operation.getStringField(OperationFields.NUMBER),
                            FontUtils.getDejavuRegular7Dark()));

            panelTableHeader
                    .addCell(new Phrase(
                            translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME,
                                    locale) + ": " + operation.getStringField(OperationFields.NAME),
                            FontUtils.getDejavuRegular7Dark()));

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.productionSetUpTime.label", locale) + ":",
                    TimeConverterService.convertTimeToString(
                            technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TPZ))
                            + " (g:m:s)");

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.machineUtilization.label", locale) + ":",
                    format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsTNFO.MACHINE_UTILIZATION)));

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.productionTimeForOneCycle.label", locale)
                            + ":",
                    TimeConverterService.convertTimeToString(
                            technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TJ))
                            + " (g:m:s)");

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.laborUtilization.label", locale) + ":",
                    format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsTNFO.LABOR_UTILIZATION)));

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.additionalTime.label", locale) + ":",
                    TimeConverterService.convertTimeToString(technologyOperationComponent
                            .getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION)) + " (g:m:s)");
            BigDecimal machineHourlyCost;
            BigDecimal laborHourlyCost;

            if (SourceOfOperationCosts.PARAMETERS.getStringValue()
                    .equals(costCalculation.getStringField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS))) {
                machineHourlyCost = BigDecimalUtils
                        .convertNullToZero(parameterService.getParameter().getDecimalField("averageMachineHourlyCostPB"));
                laborHourlyCost = BigDecimalUtils
                        .convertNullToZero(parameterService.getParameter().getDecimalField("averageLaborHourlyCostPB"));
            } else {
                machineHourlyCost = technologyOperationComponent
                        .getDecimalField(TechnologyOperationComponentFieldsCNFO.MACHINE_HOURLY_COST);
                laborHourlyCost = technologyOperationComponent
                        .getDecimalField(TechnologyOperationComponentFieldsCNFO.LABOR_HOURLY_COST);
            }

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.machineHourlyCost.label", locale) + ":",
                    format(machineHourlyCost));

            addTableCellAsTwoColumnsTable(panelTableContent, "", "");

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.laborHourlyCost.label", locale) + ":",
                    format(laborHourlyCost));

            document.add(panelTableHeader);
            document.add(panelTableContent);
        }
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("costCalculation.costCalculationDetails.report.title", locale);
    }

    private void addTableCellAsTwoColumnsTable(final PdfPTable table, final String label, final Object value) {
        pdfHelper.addTableCellAsTable(table, label, value, FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(),
                2);
    }

    private Map<String, String> getCostMode(final Entity costCalculation) {
        Map<String, String> costModeName = new HashMap<>();

        String localeType = "";
        String costMode = "";
        String materialCostsUsed = costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED);

        if ("01nominal".equals(materialCostsUsed)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.nominalCost";
            costMode = "nominalCost";
        } else if ("02average".equals(materialCostsUsed)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.averageCost";
            costMode = "averageCost";
        } else if ("03lastPurchase".equals(materialCostsUsed)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.lastPurchaseCost";
            costMode = "lastPurchaseCost";
        } else if ("04averageOfferCost".equals(materialCostsUsed)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.averageOfferCost";
            costMode = "averageOfferCost";
        } else if ("05lastOfferCost".equals(materialCostsUsed)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.lastOfferCost";
            costMode = "lastOfferCost";
        }

        costModeName.put(L_LOCALE_TYPE, localeType);
        costModeName.put(L_COST_MODE, costMode);

        return costModeName;
    }

}
