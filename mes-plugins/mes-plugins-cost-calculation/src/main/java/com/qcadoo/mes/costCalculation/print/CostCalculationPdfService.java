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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.costCalculation.print.utils.CostCalculationMaterial;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.costNormsForOperation.constants.TechnologyOperationComponentFieldsCNFO;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class CostCalculationPdfService extends PdfDocumentService {

    private static final String L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER = "costCalculation.costCalculationDetails.report.columnHeader.number";

    private static final String L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME = "costCalculation.costCalculationDetails.report.columnHeader.name";

    private static final String L_COLUMN_HEADER_QUANTITY = "costCalculation.costCalculationDetails.report.columnHeader.quantity";

    private static final String L_COLUMN_HEADER_UNIT = "costCalculation.costCalculationDetails.report.columnHeader.unit";

    private static final String L_COLUMN_HEADER_COSTS = "costCalculation.costCalculationDetails.report.columnHeader.costs";

    private static final String L_COLUMN_HEADER_MARGIN = "costCalculation.costCalculationDetails.report.columnHeader.margin";

    private static final String L_COLUMN_HEADER_TOTAL_COSTS = "costCalculation.costCalculationDetails.report.columnHeader.totalCosts";

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
    private TimeConverterService timeConverterService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private CostCalculationMaterialsService costCalculationMaterialsService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private ParameterService parameterService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("costCalculation.costCalculationDetails.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        DataDefinition dataDefCostCalculation = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION);
        Entity costCalculation = dataDefCostCalculation.find("where id = " + entity.getId().toString()).uniqueResult();

        PdfPTable topPanelColumn = addTopPanelToReport(costCalculation, locale);
        PdfPTable middlePanelColumn = addMiddlePanelToReport(costCalculation, locale);
        PdfPTable bottomPanelColumn = addBottomPanelToReport(costCalculation, locale);

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
        PdfPTable materialsTable = addMaterialsTable(costCalculation, locale);
        document.add(materialsTable);

        document.add(Chunk.NEWLINE);
        document.add(
                new Paragraph(translationService.translate("costCalculation.costCalculationDetails.report.paragraph2", locale),
                        FontUtils.getDejavuBold11Dark()));

        CalculateOperationCostMode calculateOperationCostMode = CalculateOperationCostMode
                .parseString(costCalculation.getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE));

        if (CalculateOperationCostMode.HOURLY.equals(calculateOperationCostMode)) {
            document.add(addHourlyCostsTable(costCalculation, locale));
        } else if (CalculateOperationCostMode.PIECEWORK.equals(calculateOperationCostMode)) {
            document.add(addTableAboutPieceworkCost(costCalculation, locale));
        } else {
            throw new IllegalStateException("Unsupported CalculateOperationCostMode");
        }

        printMaterialAndOperationNorms(document, costCalculation, locale);
    }

    public PdfPTable addTopPanelToReport(final Entity costCalculation, final Locale locale) {
        PdfPTable panelColumn = pdfHelper.createPanelTable(2);
        panelColumn.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);

        Entity order = costCalculation.getBelongsToField(CostCalculationFields.ORDER);
        String calculateOperationCostsMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE);
        String calculateMaterialCostsMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE);

        int[] proportions = new int[] { 30, 70 };
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.number.label", locale) + ":",
                costCalculation.getStringField(CostCalculationFields.NUMBER), proportions);
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.product.label", locale) + ":",
                costCalculation.getBelongsToField(CostCalculationFields.PRODUCT).getStringField(ProductFields.NAME), proportions);
        addAsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.technology.label", locale) + ":",
                costCalculation.getBelongsToField(CostCalculationFields.TECHNOLOGY).getStringField(TechnologyFields.NAME),
                proportions);
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.quantity.label", locale) + ":",
                numberService.format(costCalculation.getField(CostCalculationFields.QUANTITY)), proportions);
        addAsTable(leftPanelColumn, translationService.translate("costCalculation.costCalculation.order.label", locale) + ":",
                order == null ? "" : order.getStringField(OrderFields.NAME), proportions);
        addAsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.description.label", locale) + ":",
                costCalculation.getStringField(CostCalculationFields.DESCRIPTION), proportions);

        PdfPCell leftCell = new PdfPCell(leftPanelColumn);
        leftCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(leftCell);

        PdfPTable rightPanelColumn = pdfHelper.createPanelTable(1);

        rightPanelColumn
                .addCell(
                        new Phrase(
                                translationService.translate(
                                        "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale) + ":",
                        FontUtils.getDejavuBold10Dark()));

        if (!CalculateOperationCostMode.PIECEWORK.getStringValue().equals(calculateOperationCostsMode)) {
            addAsTable(rightPanelColumn,
                    L_TAB_IN_TEXT
                            + translationService.translate(
                                    "costCalculation.costCalculationDetails.report.columnHeader.includeAdditionalTime", locale)
                    + ":",
                    costCalculation.getBooleanField(CostCalculationFields.INCLUDE_ADDITIONAL_TIME)
                            ? translationService.translate("qcadooView.true", locale)
                            : translationService.translate("qcadooView.false", locale));

            addAsTable(rightPanelColumn,
                    L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.includeTPZ.label", locale)
                            + ":",
                    costCalculation.getBooleanField(CostCalculationFields.INCLUDE_TPZ)
                            ? translationService.translate("qcadooView.true", locale)
                            : translationService.translate("qcadooView.false", locale));
        }

        Object reportData = calculateMaterialCostsMode;
        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.calculateMaterialCostsMode.label",
                        locale),
                reportData == null ? translationService.translate("qcadooView.form.blankComboBoxValue", locale)
                        : translationService.translate(
                                "costCalculation.costCalculation.calculateMaterialCostsMode.value." + reportData.toString(),
                                locale));

        reportData = calculateOperationCostsMode;
        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.calculateOperationCostsMode.label",
                        locale),
                reportData == null ? translationService.translate("qcadooView.form.blankComboBoxValue", locale)
                        : translationService.translate(
                                "costCalculation.costCalculation.calculateOperationCostsMode.value." + reportData.toString(),
                                locale));

        PdfPCell rightCell = new PdfPCell(rightPanelColumn);
        rightCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(rightCell);
        return panelColumn;
    }

    private void addAsTable(final PdfPTable table, final String label, final Object fieldValue) {
        addAsTable(table, label, fieldValue, new int[] {});
    }

    public PdfPTable addMiddlePanelToReport(final Entity costCalculation, final Locale locale) {
        PdfPTable panelColumn = pdfHelper.createPanelTable(2);
        panelColumn.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);

        leftPanelColumn.addCell(new Phrase(translationService
                .translate("costCalculation.costCalculationDetails.window.mainTab.form.technicalProductionCost", locale) + ":",
                FontUtils.getDejavuBold10Dark()));

        Object reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.totalMaterialCosts.label", locale)
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

        leftPanelColumn.addCell(
                new Phrase(translationService.translate("overheadsValue", locale) + ":", FontUtils.getDejavuBold9Dark()));

        reportData = costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN);
        Object reportData2 = costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE);
        addAsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.productionCostMargin.label", locale)
                        + ":",
                (reportData == null ? ""
                        : format(reportData) + (reportData2 == null ? ""
                                : " %\n (" + "= " + format(reportData2) + " " + currencyService.getCurrencyAlphabeticCode()
                                        + ")")));

        reportData = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN);
        reportData2 = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE);
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
        reportData2 = costCalculation.getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD_VALUE);
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

        rightPanelColumn
                .addCell(
                        new Phrase(
                                translationService.translate(
                                        "costCalculation.costCalculationDetails.window.mainTab.form.totalCost", locale) + ":",
                        FontUtils.getDejavuBold9Dark()));

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS);
        addAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService
                        .translate("costCalculation.costCalculation.totalTechnicalProductionCosts.label", locale) + ":",
                (reportData == null ? "" : format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        rightPanelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);

        PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_COSTS);
        cellTable.addCell(new Phrase(
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.totalCosts.label", locale) + ":",
                FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_COST_PER_UNIT);
        cellTable
                .addCell(
                        new Phrase(
                                L_TAB_IN_TEXT + translationService
                                        .translate("costCalculation.costCalculation.totalCostPerUnit.label", locale) + ":",
                        FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);

        reportData = costCalculation.getDecimalField(CostCalculationFields.TECHNICAL_PRODUCTION_COSTS);
        cellTable
                .addCell(new Phrase(
                        L_TAB_IN_TEXT + translationService
                                .translate("costCalculation.costCalculation.technicalProductionCosts.label", locale) + ":",
                FontUtils.getDejavuBold10Dark()));
        addRightAlignedAmountCell(cellTable, reportData);

        rightPanelColumn.addCell(cellTable);
        PdfPCell rightCell = new PdfPCell(rightPanelColumn);
        rightCell.setBorder(PdfPCell.NO_BORDER);
        panelColumn.addCell(rightCell);
        return panelColumn;
    }

    private String format(Object obj) {
        return formatWithScale(obj, 2);
    }

    private String formatWithScale(Object obj, int scale) {
        return numberService.format(numberService.setScale((BigDecimal) obj, scale));
    }

    private void addRightAlignedAmountCell(PdfPTable table, Object reportData) {
        BigDecimal reportValue = numberService.setScale((BigDecimal) reportData, 2);
        PdfPCell cell = new PdfPCell(
                new Phrase((reportData == null ? "" : format(reportValue)) + " " + currencyService.getCurrencyAlphabeticCode(),
                        FontUtils.getDejavuRegular10Dark()));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    public PdfPTable addBottomPanelToReport(final Entity costCalculation, final Locale locale) {
        PdfPTable panelColumn = pdfHelper.createPanelTable(2);
        panelColumn.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        panelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);

        leftPanelColumn.addCell(
                new Phrase(translationService.translate("sellPriceValue", locale) + ":", FontUtils.getDejavuBold10Dark()));

        Object reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS);

        reportData = costCalculation.getDecimalField(CostCalculationFields.PROFIT);
        Object reportData2 = costCalculation.getDecimalField(CostCalculationFields.PROFIT_VALUE);
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

        reportData = costCalculation.getDecimalField(CostCalculationFields.SELL_PRICE_VALUE);
        rightPanelColumn
                .addCell(
                        new Phrase(
                                translationService.translate(
                                        "costCalculation.costCalculationDetails.window.mainTab.form.totalCost", locale) + ":",
                        FontUtils.getDejavuBold9Dark()));

        rightPanelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);

        PdfPTable cellTable = new PdfPTable(2);
        cellTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        reportData = costCalculation.getDecimalField(CostCalculationFields.SELL_PRICE_VALUE);
        cellTable
                .addCell(
                        new Phrase(
                                L_TAB_IN_TEXT + translationService
                                        .translate("costCalculation.costCalculation.sellPriceValue.label", locale) + ":",
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
            } catch (DocumentException e) {
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

    public PdfPTable addMaterialsTable(final Entity costCalculation, final Locale locale) {
        List<String> materialsTableHeader = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        materialsTableHeader.addAll(Arrays
                .asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, L_COLUMN_HEADER_QUANTITY,
                        L_COLUMN_HEADER_UNIT, L_COLUMN_HEADER_COSTS, L_COLUMN_HEADER_MARGIN, L_COLUMN_HEADER_TOTAL_COSTS)
                .stream().map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

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

        Entity technology;
        Entity order = costCalculation.getBelongsToField(CostCalculationFields.ORDER);

        if (order == null) {
            technology = costCalculation.getBelongsToField(CostCalculationFields.TECHNOLOGY);
        } else {
            technology = costCalculation.getBelongsToField(CostCalculationFields.ORDER)
                    .getBelongsToField(CostCalculationFields.TECHNOLOGY);
        }

        BigDecimal quantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);

        Map<Long, BigDecimal> neededProductQuantities = getNeededProductQuantities(costCalculation, technology, quantity,
                MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);

        MathContext mathContext = numberService.getMathContext();
        List<CostCalculationMaterial> sortedMaterials = costCalculationMaterialsService
                .getSortedMaterialsFromProductQuantities(costCalculation, neededProductQuantities, order);
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

        BigDecimal totalMaterialCosts = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS);
        BigDecimal materialCostsMarginValue = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE);

        BigDecimal totalCostOfMaterialValue = totalMaterialCosts.add(materialCostsMarginValue, mathContext);

        String totalMaterialCostsToString = numberService.format(totalMaterialCosts);
        String materialCostMarginValueToString = numberService.format(materialCostsMarginValue);
        String totalCostOfMaterialToString = numberService.format(totalCostOfMaterialValue);

        materialsTable
                .addCell(new Phrase(translationService.translate("costCalculation.costCalculation.report.totalMaterial", locale),
                        FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell("");
        materialsTable.addCell("");
        materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

        materialsTable.addCell(new Phrase(totalMaterialCostsToString, FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell(new Phrase(materialCostMarginValueToString, FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell(new Phrase(totalCostOfMaterialToString, FontUtils.getDejavuRegular7Dark()));

        materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

        return materialsTable;
    }

    private Map<Long, BigDecimal> getNeededProductQuantities(final Entity costCalculationOrProductionBalance,
            final Entity technology, final BigDecimal quantity, final MrpAlgorithm algorithm) {
        return productQuantitiesService.getNeededProductQuantities(technology, quantity, algorithm);
    }

    private PdfPTable addHourlyCostsTable(final Entity costCalculation, final Locale locale) {
        List<String> hourlyCostsTableHeader = Lists.newArrayList();

        hourlyCostsTableHeader.addAll(Arrays
                .asList(L_COLUMN_HEADER_LEVEL, L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COLUMN_HEADER_MACH_DURATION, L_COLUMN_HEADER_MACH_COSTS, L_COLUMN_HEADER_LAB_DURATION,
                        L_COLUMN_HEADER_LAB_COSTS, L_COLUMN_HEADER_MARGIN, L_COLUMN_HEADER_TOTAL_COSTS)
                .stream().map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

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
            Integer totalMachineWorkTimeSummary = 0;
            Integer totalLaborWorkTimeSummary = 0;

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
                        new Phrase(timeConverterService.convertTimeToString(machineWorkTime), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(totalMachineOperationCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(
                        new Phrase(timeConverterService.convertTimeToString(laborWorkTime), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(totalLaborOperationCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(operationMarginCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(format(totalOperationCost), FontUtils.getDejavuRegular7Dark()));

                hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                totalMachineWorkTimeSummary += IntegerUtils.convertNullToZero(machineWorkTime);
                totalLaborWorkTimeSummary += IntegerUtils.convertNullToZero(laborWorkTime);

                totalOperationCostSummary = totalOperationCostSummary.add(operationCost, mathContext);
            }

            BigDecimal productionCostMarginValue = costCalculation
                    .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE);

            BigDecimal totalOperationCost = totalOperationCostSummary.add(productionCostMarginValue, mathContext);

            String totalMachineWorkTimeToString = TimeConverterService.convertTimeToString(totalMachineWorkTimeSummary);
            String totalMachineHourlyCosts = format(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS));
            String totalLaborWorkTimeToString = TimeConverterService.convertTimeToString(totalLaborWorkTimeSummary);
            String totalLaborHourlyCosts = format(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS));
            String totalProductionCostMarginValue = format(productionCostMarginValue);
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
            hourlyCostsTable.addCell(new Phrase(totalProductionCostMarginValue, FontUtils.getDejavuRegular7Dark()));
            hourlyCostsTable.addCell(new Phrase(totalOperationCostToString, FontUtils.getDejavuRegular7Dark()));

            hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }

        return hourlyCostsTable;
    }

    private PdfPTable addTableAboutPieceworkCost(final Entity costCalculation, final Locale locale) {
        List<String> pieceworkCostsTableHeader = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        pieceworkCostsTableHeader.addAll(Arrays
                .asList(L_COLUMN_HEADER_LEVEL, L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        "costCalculation.costCalculationDetails.report.columnHeader.pieces",
                        "costCalculation.costCalculationDetails.report.columnHeader.operationCost", L_COLUMN_HEADER_MARGIN,
                        L_COLUMN_HEADER_TOTAL_COSTS)
                .stream().map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));
        alignments.put(translationService.translate(L_COLUMN_HEADER_LEVEL, locale), HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.pieces", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.operationCost", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_MARGIN, locale), HeaderAlignment.RIGHT);
        alignments.put(translationService.translate(L_COLUMN_HEADER_TOTAL_COSTS, locale), HeaderAlignment.RIGHT);

        List<Entity> calculationOperationComponents = costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        PdfPTable pieceworkCostsTable = pdfHelper.createTableWithHeader(pieceworkCostsTableHeader.size(),
                pieceworkCostsTableHeader, false, alignments);

        try {
            float[] columnWidths = { 1f, 0.75f, 1f, 1f, 1f, 1.25f };
            pieceworkCostsTable.setWidths(columnWidths);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (!calculationOperationComponents.isEmpty()) {
            BigDecimal totalOperationCostSummary = BigDecimal.ZERO;
            BigDecimal totalPieces = BigDecimal.ZERO;

            MathContext mathContext = numberService.getMathContext();

            for (Entity calculationOperationComponent : calculationOperationComponents) {
                BigDecimal pieces = calculationOperationComponent.getDecimalField(CalculationOperationComponentFields.PIECES);
                BigDecimal operationCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.OPERATION_COST);
                BigDecimal operationMarginCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.OPERATION_MARGIN_COST);
                BigDecimal totalOperationCost = calculationOperationComponent
                        .getDecimalField(CalculationOperationComponentFields.TOTAL_OPERATION_COST);

                pieceworkCostsTable.addCell(new Phrase(
                        calculationOperationComponent.getField(CalculationOperationComponentFields.NODE_NUMBER).toString(),
                        FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable
                        .addCell(new Phrase(calculationOperationComponent.getBelongsToField(TechnologiesConstants.MODEL_OPERATION)
                                .getStringField(OperationFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                pieceworkCostsTable.addCell(new Phrase(format(pieces), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(format(operationCost), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(format(operationMarginCost), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(format(totalOperationCost), FontUtils.getDejavuRegular7Dark()));

                pieceworkCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                totalPieces = totalPieces.add(pieces, mathContext);

                totalOperationCostSummary = totalOperationCostSummary.add(operationCost, mathContext);
            }

            BigDecimal productionCostMarginValue = costCalculation
                    .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE);

            BigDecimal totalOperationCost = totalOperationCostSummary.add(productionCostMarginValue, mathContext);

            String totalPiecesToString = format(totalPieces);
            String totalOperationCostSummaryToString = format(totalOperationCostSummary);
            String productionCostMarginValueToString = format(productionCostMarginValue);
            String totalOperationCostToString = format(totalOperationCost);

            pieceworkCostsTable.addCell(
                    new Phrase(translationService.translate("costCalculation.costCalculation.report.totalOperation", locale),
                            FontUtils.getDejavuRegular7Dark()));
            pieceworkCostsTable.addCell(new Phrase("", FontUtils.getDejavuRegular7Dark()));
            pieceworkCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            pieceworkCostsTable.addCell(new Phrase(totalPiecesToString, FontUtils.getDejavuRegular7Dark()));
            pieceworkCostsTable.addCell(new Phrase(totalOperationCostSummaryToString, FontUtils.getDejavuRegular7Dark()));
            pieceworkCostsTable.addCell(new Phrase(productionCostMarginValueToString, FontUtils.getDejavuRegular7Dark()));
            pieceworkCostsTable.addCell(new Phrase(totalOperationCostToString, FontUtils.getDejavuRegular7Dark()));

            pieceworkCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }

        return pieceworkCostsTable;
    }

    public PdfPTable addOptionTablePrintCostNormsOfMaterials(final Entity costCalculation, final Locale locale) {
        List<String> optionTableHeader = Lists.newArrayList();

        Map<String, String> costModeName = getCostMode(costCalculation);

        optionTableHeader.addAll(Arrays
                .asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME, costModeName.get(L_LOCALE_TYPE))
                .stream().map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        Entity technology;
        Entity order = costCalculation.getBelongsToField(CostCalculationFields.ORDER);

        if (order == null) {
            technology = costCalculation.getBelongsToField(CostCalculationFields.TECHNOLOGY);
        } else {
            technology = order.getBelongsToField(CostCalculationFields.TECHNOLOGY);
        }

        BigDecimal givenQty = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);

        Map<Long, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology, givenQty,
                MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);

        PdfPTable printCostNormsOfMaterialTable = pdfHelper.createTableWithHeader(optionTableHeader.size(), optionTableHeader,
                false);

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            printCostNormsOfMaterialTable
                    .addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable
                    .addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
            Entity entityProduct = productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                    costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS));
            BigDecimal toDisplay = entityProduct.getDecimalField(costModeName.get(L_COST_MODE));
            BigDecimal quantity = product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER);
            String unit = product.getStringField(ProductFields.UNIT);

            printCostNormsOfMaterialTable.addCell(
                    new Phrase(numberService.format(toDisplay) + " " + " / " + numberService.format(quantity) + " " + unit,
                            FontUtils.getDejavuRegular7Dark()));
        }

        return printCostNormsOfMaterialTable;
    }

    public void printMaterialAndOperationNorms(final Document document, final Entity costCalculation, final Locale locale)
            throws DocumentException {
        if (costCalculation.getBooleanField(CostCalculationFields.PRINT_COST_NORMS_OF_MATERIALS)) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(
                    translationService.translate("costCalculation.costCalculationDetails.report.paragraph3", locale),
                    FontUtils.getDejavuBold11Dark()));
            PdfPTable optionTable = addOptionTablePrintCostNormsOfMaterials(costCalculation, locale);
            document.add(optionTable);
        }

        if (costCalculation.getBooleanField(CostCalculationFields.PRINT_OPERATION_NORMS)) {
            CalculateOperationCostMode calculateOperationCostMode = CalculateOperationCostMode
                    .parseString(costCalculation.getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE));

            if (CalculateOperationCostMode.PIECEWORK.equals(calculateOperationCostMode)) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph(
                        translationService.translate("costCalculation.costCalculationDetails.report.paragraph4", locale),
                        FontUtils.getDejavuBold11Dark()));
                document.add(addOptionTablePrintOperationNormsPiecework(costCalculation, locale));
            } else if (CalculateOperationCostMode.HOURLY.equals(calculateOperationCostMode)) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph(
                        translationService.translate("costCalculation.costCalculationDetails.report.paragraph5", locale),
                        FontUtils.getDejavuBold11Dark()));
                addOptionTablePrintOperationNormsHourly(document, costCalculation, locale);
            }
        }
    }

    public void addOptionTablePrintOperationNormsHourly(final Document document, final Entity costCalculation,
            final Locale locale) throws DocumentException {
        List<String> optionTableHeader = Lists.newArrayList();

        optionTableHeader.addAll(Arrays
                .asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME)
                .stream().map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

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
                    timeConverterService.convertTimeToString(
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
                    timeConverterService.convertTimeToString(
                            technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TJ))
                            + " (g:m:s)");

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.laborUtilization.label", locale) + ":",
                    format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsTNFO.LABOR_UTILIZATION)));

            addTableCellAsTwoColumnsTable(panelTableContent,
                    translationService
                            .translate("costCalculation.costCalculationDetails.report.columnHeader.additionalTime.label", locale)
                            + ":",
                    timeConverterService.convertTimeToString(technologyOperationComponent
                            .getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION)) + " (g:m:s)");
            BigDecimal machineHourlyCost = BigDecimal.ZERO;
            BigDecimal laborHourlyCost = BigDecimal.ZERO;

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

    public PdfPTable addOptionTablePrintOperationNormsPiecework(final Entity costCalculation, final Locale locale) {
        List<String> optionTableHeader = Lists.newArrayList();

        optionTableHeader.addAll(Arrays
                .asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                        L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME,
                        "costCalculation.costCalculationDetails.report.columnHeader.pieceworkCost",
                        "costCalculation.costCalculationDetails.report.columnHeader.forNumberOfOperations")
                .stream().map(translate -> translationService.translate(translate, locale)).collect(Collectors.toList()));

        List<Entity> calculationOperationComponents = entityTreeUtilsService
                .getSortedEntities(costCalculation.getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS));

        PdfPTable operationNormsTable = pdfHelper.createTableWithHeader(optionTableHeader.size(), optionTableHeader, false);
        operationNormsTable.setSpacingBefore(10);

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            Entity operation = calculationOperationComponent.getBelongsToField(CalculationOperationComponentFields.OPERATION);

            operationNormsTable
                    .addCell(new Phrase(operation.getStringField(OperationFields.NUMBER), FontUtils.getDejavuRegular7Dark()));

            operationNormsTable
                    .addCell(new Phrase(operation.getStringField(OperationFields.NAME), FontUtils.getDejavuRegular7Dark()));

            BigDecimal pieceworkCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.PIECEWORK_COST);

            operationNormsTable
                    .addCell(new Phrase((pieceworkCost == null) ? "" : format(pieceworkCost), FontUtils.getDejavuRegular7Dark()));

            Integer numberOfOperations = calculationOperationComponent
                    .getIntegerField(CalculationOperationComponentFields.NUMBER_OF_OPERATIONS);

            operationNormsTable.addCell(new Phrase((numberOfOperations == null) ? "" : numberOfOperations.toString(),
                    FontUtils.getDejavuRegular7Dark()));
        }

        return operationNormsTable;
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
        Map<String, String> costModeName = new HashMap<String, String>();

        String localeType = "";
        String costMode = "";
        String costCalculationMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE);

        if ("01nominal".equals(costCalculationMode)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.nominalCost";
            costMode = "nominalCost";
        } else if ("02average".equals(costCalculationMode)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.averageCost";
            costMode = "averageCost";
        } else if ("03lastPurchase".equals(costCalculationMode)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.lastPurchaseCost";
            costMode = "lastPurchaseCost";
        } else if ("04costForOrder".equals(costCalculationMode)) {
            localeType = "costCalculation.costCalculationDetails.report.columnHeader.costForOrder";
            costMode = "costForOrder";
        }

        costModeName.put(L_LOCALE_TYPE, localeType);
        costModeName.put(L_COST_MODE, costMode);

        return costModeName;
    }

}