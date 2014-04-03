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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import com.google.common.collect.Lists;
=======
import com.google.common.collect.Maps;
>>>>>>> master
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
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
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.FontUtils;
<<<<<<< HEAD
=======
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.HeaderAlignment;
>>>>>>> master
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class CostCalculationPdfService extends PdfDocumentService {

    private static final String L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER = "costCalculation.costCalculationDetails.report.columnHeader.number";

    private static final String L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME = "costCalculation.costCalculationDetails.report.columnHeader.name";

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
    private ProductsCostCalculationService productsCostCalculationService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("costCalculation.costCalculationDetails.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        DataDefinition dataDefCostCalculation = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION);
        Entity costCalculation = dataDefCostCalculation.find("where id = " + entity.getId().toString()).uniqueResult();

        PdfPTable leftPanelColumn = addLeftPanelToReport(costCalculation, locale);

        PdfPTable rightPanelColumn = addRightPanelToReport(costCalculation, locale);

        PdfPTable panelTable = pdfHelper.createPanelTable(2);

        panelTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
        panelTable.addCell(leftPanelColumn);
        panelTable.addCell(rightPanelColumn);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);

        panelTable.setTableEvent(null);
        panelTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        document.add(panelTable);

        document.add(new Paragraph(translationService
                .translate("costCalculation.costCalculationDetails.report.paragraph", locale), FontUtils.getDejavuBold11Dark()));
        PdfPTable materialsTable = addMaterialsTable(costCalculation, locale);
        document.add(materialsTable);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("costCalculation.costCalculationDetails.report.paragraph2",
                locale), FontUtils.getDejavuBold11Dark()));

        CalculateOperationCostMode calculateOperationCostMode = CalculateOperationCostMode.parseString(costCalculation
                .getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE));

        if (CalculateOperationCostMode.HOURLY.equals(calculateOperationCostMode)) {
            document.add(addHourlyCostsTable(costCalculation, locale));
        } else if (CalculateOperationCostMode.PIECEWORK.equals(calculateOperationCostMode)) {
            document.add(addTableAboutPieceworkCost(costCalculation, locale));
        } else {
            throw new IllegalStateException("Unsupported CalculateOperationCostMode");
        }

        printMaterialAndOperationNorms(document, costCalculation, locale);
    }

    public PdfPTable addLeftPanelToReport(final Entity costCalculation, final Locale locale) {
        PdfPTable leftPanelColumn = pdfHelper.createPanelTable(1);
        leftPanelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        Entity order = costCalculation.getBelongsToField(CostCalculationFields.ORDER);
        String calculateOperationCostsMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE);
        String calculateMaterialCostsMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE);

        pdfHelper.addTableCellAsTwoColumnsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.number.label", locale) + ":",
                costCalculation.getStringField(CostCalculationFields.NUMBER));
        pdfHelper.addTableCellAsTwoColumnsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.product.label", locale) + ":", costCalculation
                        .getBelongsToField(CostCalculationFields.PRODUCT).getStringField(ProductFields.NAME));
        pdfHelper.addTableCellAsTwoColumnsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.technology.label", locale) + ":", costCalculation
                        .getBelongsToField(CostCalculationFields.TECHNOLOGY).getStringField(TechnologyFields.NAME));
        pdfHelper.addTableCellAsTwoColumnsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.quantity.label", locale) + ":",
                numberService.format(costCalculation.getField(CostCalculationFields.QUANTITY)));
        pdfHelper.addTableCellAsTwoColumnsTable(leftPanelColumn,
                translationService.translate("costCalculation.costCalculation.order.label", locale) + ":", order == null ? ""
                        : order.getStringField(OrderFields.NAME));

        leftPanelColumn.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        if (!CalculateOperationCostMode.PIECEWORK.getStringValue().equals(calculateOperationCostsMode)) {
            pdfHelper.addTableCellAsTwoColumnsTable(
                    leftPanelColumn,
                    L_TAB_IN_TEXT
                            + translationService.translate(
                                    "costCalculation.costCalculationDetails.report.columnHeader.includeAdditionalTime", locale)
                            + ":",
                    costCalculation.getBooleanField(CostCalculationFields.INCLUDE_ADDITIONAL_TIME) ? translationService
                            .translate("qcadooView.true", locale) : translationService.translate("qcadooView.false", locale));

            pdfHelper.addTableCellAsTwoColumnsTable(
                    leftPanelColumn,
                    L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.includeTPZ.label", locale)
                            + ":",
                    costCalculation.getBooleanField(CostCalculationFields.INCLUDE_TPZ) ? translationService.translate(
                            "qcadooView.true", locale) : translationService.translate("qcadooView.false", locale));
        }

        Object reportData = calculateMaterialCostsMode;
        pdfHelper.addTableCellAsTwoColumnsTable(
                leftPanelColumn,
                L_TAB_IN_TEXT
                        + translationService
                                .translate("costCalculation.costCalculation.calculateMaterialCostsMode.label", locale),
                reportData == null ? translationService.translate("qcadooView.form.blankComboBoxValue", locale)
                        : translationService.translate("costCalculation.costCalculation.calculateMaterialCostsMode.value."
                                + reportData.toString(), locale));

        reportData = calculateOperationCostsMode;
        pdfHelper.addTableCellAsTwoColumnsTable(
                leftPanelColumn,
                L_TAB_IN_TEXT
                        + translationService.translate("costCalculation.costCalculation.calculateOperationCostsMode.label",
                                locale),
                reportData == null ? translationService.translate("qcadooView.form.blankComboBoxValue", locale)
                        : translationService.translate("costCalculation.costCalculation.calculateOperationCostsMode.value."
                                + reportData.toString(), locale));

        reportData = costCalculation.getStringField(CostCalculationFields.DESCRIPTION);
        pdfHelper.addTableCellAsTwoColumnsTable(leftPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.description.label", locale) + ":",
                (reportData == null ? "" : reportData));

        return leftPanelColumn;
    }

    public PdfPTable addRightPanelToReport(final Entity costCalculation, final Locale locale) {
        PdfPTable rightPanelColumn = pdfHelper.createPanelTable(1);
        rightPanelColumn.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        rightPanelColumn.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.technicalProductionCost", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        Object reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS);
        pdfHelper.addTableCellAsTwoColumnsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.totalMaterialCosts.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS);
        pdfHelper.addTableCellAsTwoColumnsTable(
                rightPanelColumn,
                L_TAB_IN_TEXT
                        + translationService.translate("costCalculation.costCalculation.totalMachineHourlyCosts.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS);
        pdfHelper.addTableCellAsTwoColumnsTable(
                rightPanelColumn,
                L_TAB_IN_TEXT
                        + translationService.translate("costCalculation.costCalculation.totalLaborHourlyCosts.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS);
        pdfHelper.addTableCellAsTwoColumnsTable(
                rightPanelColumn,
                L_TAB_IN_TEXT
                        + translationService.translate("costCalculation.costCalculation.totalTechnicalProductionCosts.label",
                                locale) + ":", (reportData == null ? "" : numberService.format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode());

        rightPanelColumn.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.overheads", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        reportData = costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN);
        Object reportData2 = costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE);
        pdfHelper.addTableCellAsTwoColumnsTable(
                rightPanelColumn,
                L_TAB_IN_TEXT
                        + translationService.translate("costCalculation.costCalculation.productionCostMargin.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)
                        + (reportData2 == null ? "" : " %\n (" + "= " + reportData2.toString() + " "
                                + currencyService.getCurrencyAlphabeticCode() + ")")));

        reportData = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN);
        reportData2 = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE);
        pdfHelper.addTableCellAsTwoColumnsTable(
                rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.materialCostMargin.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)
                        + (reportData2 == null ? "" : " %\n (" + "= " + reportData2.toString() + " "
                                + currencyService.getCurrencyAlphabeticCode() + ")")));

        reportData = costCalculation.getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD);
        pdfHelper.addTableCellAsTwoColumnsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.additionalOverhead.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode());

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_COSTS);
        rightPanelColumn.addCell(new Phrase(translationService.translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.totalCost", locale) + ":", FontUtils
                .getDejavuBold10Dark()));

        pdfHelper.addTableCellAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.totalCosts.label", locale) + ":",
                (reportData == null ? "" : numberService.format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode(),
                FontUtils.getDejavuBold10Dark(), FontUtils.getDejavuRegular10Dark(), 2);

        reportData = costCalculation.getDecimalField(CostCalculationFields.TOTAL_COST_PER_UNIT);
        pdfHelper.addTableCellAsTable(rightPanelColumn,
                L_TAB_IN_TEXT + translationService.translate("costCalculation.costCalculation.totalCostPerUnit.label", locale)
                        + ":",
                (reportData == null ? "" : numberService.format(reportData)) + " " + currencyService.getCurrencyAlphabeticCode(),
                FontUtils.getDejavuBold10Dark(), FontUtils.getDejavuRegular10Dark(), 2);

        return rightPanelColumn;
    }

    public PdfPTable addMaterialsTable(final Entity costCalculation, final Locale locale) {
        List<String> materialsTableHeader = Lists.newArrayList();

<<<<<<< HEAD
=======
        List<String> materialsTableHeader = new ArrayList<String>();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

>>>>>>> master
        for (String translate : Arrays.asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                "costCalculation.costCalculationDetails.report.columnHeader.quantity",
                "costCalculation.costCalculationDetails.report.columnHeader.unit",
                "costCalculation.costCalculationDetails.report.columnHeader.costs",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            materialsTableHeader.add(translationService.translate(translate, locale));
        }
<<<<<<< HEAD

        PdfPTable materialsTable = pdfHelper.createTableWithHeader(materialsTableHeader.size(), materialsTableHeader, false);
=======
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.number", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.quantity", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.unit", locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.costs", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.margin", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.totalCosts", locale),
                HeaderAlignment.RIGHT);

        PdfPTable materialsTable = pdfHelper.createTableWithHeader(materialsTableHeader.size(), materialsTableHeader, false,
                alignments);
>>>>>>> master

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
            technology = costCalculation.getBelongsToField(CostCalculationFields.ORDER).getBelongsToField(
                    CostCalculationFields.TECHNOLOGY);
        }

        BigDecimal quantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);

        Map<Long, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology, quantity,
                MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);

<<<<<<< HEAD
        // TODO LUPO fix comparator
        // neededProductQuantities = SortUtil.sortMapUsingComparator(neededProductQuantities, new EntityNumberComparator());

        MathContext mathContext = numberService.getMathContext();

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            Entity productEntity = productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                    costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS));

            BigDecimal productQuantity = neededProductQuantity.getValue();

            materialsTable.addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            materialsTable.addCell(new Phrase(numberService.format(productQuantity), FontUtils.getDejavuRegular7Dark()));
            materialsTable.addCell(new Phrase(product.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
=======
            materialsTable.addCell(new Phrase(productWithNeededQuantity.getKey().getStringField(NUMBER), FontUtils
                    .getDejavuRegular7Dark()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            materialsTable.addCell(new Phrase(numberService.format(productQuantity), FontUtils.getDejavuRegular7Dark()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            materialsTable.addCell(new Phrase(productWithNeededQuantity.getKey().getStringField(L_UNIT), FontUtils
                    .getDejavuRegular7Dark()));
>>>>>>> master
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            BigDecimal costForGivenQuantity = productsCostCalculationService.calculateProductCostForGivenQuantity(productEntity,
                    productQuantity, costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE));

            materialsTable.addCell(new Phrase(numberService.format(costForGivenQuantity), FontUtils.getDejavuRegular7Dark()));

            BigDecimal materialCostMargin = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN);

            if (materialCostMargin == null) {
                materialsTable.addCell(new Phrase(numberService.format(0.0), FontUtils.getDejavuRegular7Dark()));
                materialsTable.addCell(new Phrase(numberService.format(costForGivenQuantity), FontUtils.getDejavuRegular7Dark()));
            } else {
                BigDecimal toAdd = costForGivenQuantity.multiply(materialCostMargin.divide(new BigDecimal(100), mathContext),
                        mathContext);
                BigDecimal totalCosts = costForGivenQuantity.add(toAdd, mathContext);

                materialsTable.addCell(new Phrase(numberService.format(toAdd), FontUtils.getDejavuRegular7Dark()));
                materialsTable.addCell(new Phrase(numberService.format(totalCosts), FontUtils.getDejavuRegular7Dark()));
            }

            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }

        BigDecimal totalMaterialCosts = costCalculation.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS);
        BigDecimal materialCostsMarginValue = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE);

        BigDecimal totalCostOfMaterialValue = totalMaterialCosts.add(materialCostsMarginValue, mathContext);

        String totalMaterialCostsToString = numberService.format(totalMaterialCosts);
        String materialCostMarginValueToString = numberService.format(materialCostsMarginValue);
        String totalCostOfMaterialToString = numberService.format(totalCostOfMaterialValue);

        materialsTable.addCell(new Phrase(translationService.translate("costCalculation.costCalculation.report.totalMaterial",
                locale), FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell("");
        materialsTable.addCell("");
        materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

        materialsTable.addCell(new Phrase(totalMaterialCostsToString, FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell(new Phrase(materialCostMarginValueToString, FontUtils.getDejavuRegular7Dark()));
        materialsTable.addCell(new Phrase(totalCostOfMaterialToString, FontUtils.getDejavuRegular7Dark()));

        materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

        return materialsTable;
    }

    private PdfPTable addHourlyCostsTable(final Entity costCalculation, final Locale locale) {
        List<String> hourlyCostsTableHeader = Lists.newArrayList();

        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.level",
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                "costCalculation.costCalculationDetails.report.columnHeader.machDuration",
                "costCalculation.costCalculationDetails.report.columnHeader.machCosts",
                "costCalculation.costCalculationDetails.report.columnHeader.labDuration",
                "costCalculation.costCalculationDetails.report.columnHeader.labCosts",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            hourlyCostsTableHeader.add(translationService.translate(translate, locale));
        }

        List<Entity> calculationOperationComponents = costCalculation
                .getHasManyField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        PdfPTable hourlyCostsTable = pdfHelper
                .createTableWithHeader(hourlyCostsTableHeader.size(), hourlyCostsTableHeader, false);

        try {
            float[] columnWidths = { 1f, 0.75f, 1f, 1f, 1f, 1f, 1f, 1.25f };
            hourlyCostsTable.setWidths(columnWidths);

        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (calculationOperationComponents != null && !calculationOperationComponents.isEmpty()) {
            Integer totalMachineWorkTimeSummary = Integer.valueOf(0);
            Integer totalLaborWorkTimeSummary = Integer.valueOf(0);

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

                hourlyCostsTable.addCell(new Phrase(calculationOperationComponent.getField(
                        CalculationOperationComponentFields.NODE_NUMBER).toString(), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField(
                        TechnologiesConstants.MODEL_OPERATION).getStringField(OperationFields.NUMBER), FontUtils
                        .getDejavuRegular7Dark()));
                hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                hourlyCostsTable.addCell(new Phrase(timeConverterService.convertTimeToString(machineWorkTime), FontUtils
                        .getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(numberService.format(totalMachineOperationCost), FontUtils
                        .getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(timeConverterService.convertTimeToString(laborWorkTime), FontUtils
                        .getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(numberService.format(totalLaborOperationCost), FontUtils
                        .getDejavuRegular7Dark()));
                hourlyCostsTable
                        .addCell(new Phrase(numberService.format(operationMarginCost), FontUtils.getDejavuRegular7Dark()));
                hourlyCostsTable.addCell(new Phrase(numberService.format(totalOperationCost), FontUtils.getDejavuRegular7Dark()));

                hourlyCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                totalMachineWorkTimeSummary += IntegerUtils.convertNullToZero(machineWorkTime);
                totalLaborWorkTimeSummary += IntegerUtils.convertNullToZero(laborWorkTime);

                // BigDecimal totalMachineOperationCostWithMargin =
                // BigDecimalUtils.convertNullToZero(calculationOperationComponent
                // .getDecimalField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN));
                //
                // BigDecimal totalLaborOperationCostWithMargin = BigDecimalUtils.convertNullToZero(calculationOperationComponent
                // .getDecimalField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST_WITH_MARGIN));

                // BigDecimal totalOperationCostSummary =
                // totalMachineOperationCostWithMargin.add(totalLaborOperationCostWithMargin,
                // mathContext);
                //
                // totalOperationCostWithMarginSummary = totalOperationCostWithMarginSummary.add(totalOperationCostWithMargin,
                // mathContext);

                totalOperationCostSummary = totalOperationCostSummary.add(operationCost, mathContext);
            }

            BigDecimal productionCostMarginValue = costCalculation
                    .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE);

            BigDecimal totalOperationCost = totalOperationCostSummary.add(productionCostMarginValue, mathContext);

            String totalMachineWorkTimeToString = timeConverterService.convertTimeToString(totalMachineWorkTimeSummary);
            String totalMachineHourlyCosts = numberService.format(costCalculation
                    .getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS));
            String totalLaborWorkTimeToString = timeConverterService.convertTimeToString(totalLaborWorkTimeSummary);
            String totalLaborHourlyCosts = numberService.format(costCalculation
                    .getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS));
            String totalProductionCostMarginValue = numberService.format(productionCostMarginValue);
            String totalOperationCostToString = numberService.format(totalOperationCost);

            hourlyCostsTable.addCell(new Phrase(translationService.translate(
                    "costCalculation.costCalculation.report.totalOperation", locale), FontUtils.getDejavuRegular7Dark()));
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

        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.level",
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                "costCalculation.costCalculationDetails.report.columnHeader.pieces",
                "costCalculation.costCalculationDetails.report.columnHeader.operationCost",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            pieceworkCostsTableHeader.add(translationService.translate(translate, locale));
        }

        List<Entity> calculationOperationComponents = costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        PdfPTable pieceworkCostsTable = pdfHelper.createTableWithHeader(pieceworkCostsTableHeader.size(),
                pieceworkCostsTableHeader, false);

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

                pieceworkCostsTable.addCell(new Phrase(calculationOperationComponent.getField(
                        CalculationOperationComponentFields.NODE_NUMBER).toString(), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField(
                        TechnologiesConstants.MODEL_OPERATION).getStringField(OperationFields.NUMBER), FontUtils
                        .getDejavuRegular7Dark()));
                pieceworkCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                pieceworkCostsTable.addCell(new Phrase(numberService.format(pieces), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(numberService.format(operationCost), FontUtils.getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(numberService.format(operationMarginCost), FontUtils
                        .getDejavuRegular7Dark()));
                pieceworkCostsTable.addCell(new Phrase(numberService.format(totalOperationCost), FontUtils
                        .getDejavuRegular7Dark()));

                pieceworkCostsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                totalPieces = totalPieces.add(pieces, mathContext);

                totalOperationCostSummary = totalOperationCostSummary.add(operationCost, mathContext);
            }

            BigDecimal productionCostMarginValue = costCalculation
                    .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE);

            BigDecimal totalOperationCost = totalOperationCostSummary.add(productionCostMarginValue, mathContext);

            String totalPiecesToString = numberService.format(totalPieces);
            String totalOperationCostSummaryToString = numberService.format(totalOperationCostSummary);
            String productionCostMarginValueToString = numberService.format(productionCostMarginValue);
            String totalOperationCostToString = numberService.format(totalOperationCost);

            pieceworkCostsTable.addCell(new Phrase(translationService.translate(
                    "costCalculation.costCalculation.report.totalOperation", locale), FontUtils.getDejavuRegular7Dark()));
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
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (String translate : Arrays.asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME, costModeName.get(L_LOCALE_TYPE))) {
            optionTableHeader.add(translationService.translate(translate, locale));
        }

        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME, locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate(costModeName.get("localeType"), locale), HeaderAlignment.RIGHT);

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

        // TODO LUPO fix comparator
        // neededProductQuantities = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());

        PdfPTable printCostNormsOfMaterialTable = pdfHelper.createTableWithHeader(optionTableHeader.size(), optionTableHeader,
<<<<<<< HEAD
                false);

        for (Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());
=======
                false, alignments);
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            printCostNormsOfMaterialTable.addCell(new Phrase(product.getKey().getStringField(NUMBER), FontUtils
                    .getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable.addCell(new Phrase(product.getKey().getStringField(NAME), FontUtils
                    .getDejavuRegular7Dark()));
            Entity entityProduct = productsCostCalculationService.getAppropriateCostNormForProduct(product.getKey(), order,
                    costCalculation.getStringField("sourceOfMaterialCosts"));
            BigDecimal toDisplay = (BigDecimal) entityProduct.getField(costModeName.get("costMode"));
            BigDecimal quantity = (BigDecimal) product.getKey().getField(L_COST_FOR_NUMBER);
            String unit = (String) product.getKey().getStringField(L_UNIT);
            printCostNormsOfMaterialTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            printCostNormsOfMaterialTable.addCell(new Phrase(toDisplay + " " + " / " + quantity + " " + unit, FontUtils
                    .getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
>>>>>>> master

            printCostNormsOfMaterialTable.addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils
                    .getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils
                    .getDejavuRegular7Dark()));
            Entity entityProduct = productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                    costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS));
            BigDecimal toDisplay = entityProduct.getDecimalField(costModeName.get(L_COST_MODE));
            BigDecimal quantity = product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER);
            String unit = product.getStringField(ProductFields.UNIT);

            printCostNormsOfMaterialTable.addCell(new Phrase(numberService.format(toDisplay) + " " + " / "
                    + numberService.format(quantity) + " " + unit, FontUtils.getDejavuRegular7Dark()));
        }

        return printCostNormsOfMaterialTable;
    }

    public void printMaterialAndOperationNorms(final Document document, final Entity costCalculation, final Locale locale)
            throws DocumentException {
        if (costCalculation.getBooleanField(CostCalculationFields.PRINT_COST_NORMS_OF_MATERIALS)) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(translationService.translate("costCalculation.costCalculationDetails.report.paragraph3",
                    locale), FontUtils.getDejavuBold11Dark()));
            PdfPTable optionTable = addOptionTablePrintCostNormsOfMaterials(costCalculation, locale);
            document.add(optionTable);
        }

        if (costCalculation.getBooleanField(CostCalculationFields.PRINT_OPERATION_NORMS)) {
            CalculateOperationCostMode calculateOperationCostMode = CalculateOperationCostMode.parseString(costCalculation
                    .getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE));

            if (CalculateOperationCostMode.PIECEWORK.equals(calculateOperationCostMode)) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph(translationService.translate(
                        "costCalculation.costCalculationDetails.report.paragraph4", locale), FontUtils.getDejavuBold11Dark()));
                document.add(addOptionTablePrintOperationNormsPiecework(costCalculation, locale));
            } else if (CalculateOperationCostMode.HOURLY.equals(calculateOperationCostMode)) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph(translationService.translate(
                        "costCalculation.costCalculationDetails.report.paragraph5", locale), FontUtils.getDejavuBold11Dark()));
                addOptionTablePrintOperationNormsHourly(document, costCalculation, locale);
            }
        }
    }

    public void addOptionTablePrintOperationNormsHourly(final Document document, final Entity costCalculation, final Locale locale)
            throws DocumentException {
        List<String> optionTableHeader = Lists.newArrayList();

        for (String translate : Arrays.asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME)) {
            optionTableHeader.add(translationService.translate(translate, locale));
        }

        List<Entity> calculationOperationComponents = entityTreeUtilsService.getSortedEntities(costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS));

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            PdfPTable panelTableHeader = pdfHelper.createPanelTable(2);
            PdfPTable panelTableContent = pdfHelper.createPanelTable(2);
            panelTableHeader.setSpacingBefore(10);
            panelTableContent.getDefaultCell().setBackgroundColor(null);
            panelTableContent.setTableEvent(null);

            Entity technologyOperationComponent = calculationOperationComponent
                    .getBelongsToField(CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity operation = calculationOperationComponent.getBelongsToField(CalculationOperationComponentFields.OPERATION);

            panelTableHeader.addCell(new Phrase(translationService.translate(
                    L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale)
                    + ": "
                    + operation.getStringField(OperationFields.NUMBER), FontUtils.getDejavuRegular7Dark()));

            panelTableHeader.addCell(new Phrase(translationService.translate(
                    L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME, locale)
                    + ": "
                    + operation.getStringField(OperationFields.NAME), FontUtils.getDejavuRegular7Dark()));

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.productionSetUpTime.label", locale) + ":",
                    timeConverterService.convertTimeToString(technologyOperationComponent
                            .getIntegerField(TechnologyOperationComponentFieldsTNFO.TPZ)) + " (g:m:s)");

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.machineUtilization.label", locale) + ":",
                    numberService.format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsTNFO.MACHINE_UTILIZATION)));

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.productionTimeForOneCycle.label", locale)
                            + ":",
                    timeConverterService.convertTimeToString(technologyOperationComponent
                            .getIntegerField(TechnologyOperationComponentFieldsTNFO.TJ)) + " (g:m:s)");

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.laborUtilization.label", locale) + ":",
                    numberService.format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsTNFO.LABOR_UTILIZATION)));

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.additionalTime.label", locale) + ":",
                    timeConverterService.convertTimeToString(technologyOperationComponent
                            .getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION)) + " (g:m:s)");

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.machineHourlyCost.label", locale) + ":",
<<<<<<< HEAD
                    numberService.format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsCNFO.MACHINE_HOURLY_COST)));
=======
                    numberService.format(calculationOperationComponent.getField("machineHourlyCost")));
>>>>>>> master

            addTableCellAsTwoColumnsTable(panelTableContent, "", "");

            addTableCellAsTwoColumnsTable(
                    panelTableContent,
                    translationService.translate(
                            "costCalculation.costCalculationDetails.report.columnHeader.laborHourlyCost.label", locale) + ":",
<<<<<<< HEAD
                    numberService.format(technologyOperationComponent
                            .getDecimalField(TechnologyOperationComponentFieldsCNFO.LABOR_HOURLY_COST)));
=======
                    numberService.format(calculationOperationComponent.getField("laborHourlyCost")));
>>>>>>> master

            document.add(panelTableHeader);
            document.add(panelTableContent);
        }
    }

    public PdfPTable addOptionTablePrintOperationNormsPiecework(final Entity costCalculation, final Locale locale) {
<<<<<<< HEAD
        List<String> optionTableHeader = Lists.newArrayList();
=======
        List<String> optionTableHeader = new ArrayList<String>();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
>>>>>>> master

        for (String translate : Arrays.asList(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME,
                "costCalculation.costCalculationDetails.report.columnHeader.pieceworkCost",
                "costCalculation.costCalculationDetails.report.columnHeader.forNumberOfOperations")) {
            optionTableHeader.add(translationService.translate(translate, locale));
        }
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NAME, locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.pieceworkCost", locale),
                HeaderAlignment.RIGHT);
        alignments
                .put(translationService.translate(
                        "costCalculation.costCalculationDetails.report.columnHeader.forNumberOfOperations", locale),
                        HeaderAlignment.LEFT);

        List<Entity> calculationOperationComponents = entityTreeUtilsService.getSortedEntities(costCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS));

<<<<<<< HEAD
        PdfPTable operationNormsTable = pdfHelper.createTableWithHeader(optionTableHeader.size(), optionTableHeader, false);
        operationNormsTable.setSpacingBefore(10);
=======
        PdfPTable printCostNormsOfMaterialTable2 = pdfHelper.createTableWithHeader(optionTableHeader.size(), optionTableHeader,
                false, alignments);
        printCostNormsOfMaterialTable2.setSpacingBefore(10);
>>>>>>> master

        for (Entity calculationOperationComponent : calculationOperationComponents) {
            Entity operation = calculationOperationComponent.getBelongsToField(CalculationOperationComponentFields.OPERATION);

<<<<<<< HEAD
            operationNormsTable.addCell(new Phrase(operation.getStringField(OperationFields.NUMBER), FontUtils
                    .getDejavuRegular7Dark()));

            operationNormsTable.addCell(new Phrase(operation.getStringField(OperationFields.NAME), FontUtils
                    .getDejavuRegular7Dark()));

            BigDecimal pieceworkCost = calculationOperationComponent
                    .getDecimalField(CalculationOperationComponentFields.PIECEWORK_COST);

            operationNormsTable.addCell(new Phrase((pieceworkCost == null) ? "" : numberService.format(pieceworkCost), FontUtils
                    .getDejavuRegular7Dark()));
=======
            printCostNormsOfMaterialTable2.addCell(new Phrase(calculationOperationComponent.getBelongsToField(
                    TechnologiesConstants.MODEL_OPERATION).getStringField(NUMBER), FontUtils.getDejavuRegular7Dark()));

            printCostNormsOfMaterialTable2.addCell(new Phrase(calculationOperationComponent.getBelongsToField(
                    TechnologiesConstants.MODEL_OPERATION).getStringField(NAME), FontUtils.getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            printCostNormsOfMaterialTable2.addCell(new Phrase(
                    (calculationOperationComponent.getField("pieceworkCost") == null) ? "" : calculationOperationComponent
                            .getField("pieceworkCost").toString(), FontUtils.getDejavuRegular7Dark()));
            printCostNormsOfMaterialTable2.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

            printCostNormsOfMaterialTable2.addCell(new Phrase(
                    (calculationOperationComponent.getField("numberOfOperations") == null) ? "" : calculationOperationComponent
                            .getField("numberOfOperations").toString(), FontUtils.getDejavuRegular7Dark()));
        }

        return printCostNormsOfMaterialTable2;

    }

    private PdfPTable addTableAboutHourlyCost(final Entity costCalculation, final Locale locale) {
        List<String> operationsTableHeader = new ArrayList<String>();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.level",
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                "costCalculation.costCalculationDetails.report.columnHeader.machDuration",
                "costCalculation.costCalculationDetails.report.columnHeader.machCosts",
                "costCalculation.costCalculationDetails.report.columnHeader.labDuration",
                "costCalculation.costCalculationDetails.report.columnHeader.labCosts",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            operationsTableHeader.add(translationService.translate(translate, locale));

        }
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.level", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.machDuration", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.machCosts", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.labDuration", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.labCosts", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.margin", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.totalCosts", locale),
                HeaderAlignment.RIGHT);
        List<Entity> calculationOperationComponents = costCalculation.getHasManyField(CALCULATION_OPERATION_COMPONENTS);

        PdfPTable operationsTable = pdfHelper.createTableWithHeader(operationsTableHeader.size(), operationsTableHeader, false,
                alignments);
>>>>>>> master

            Integer numberOfOperations = calculationOperationComponent
                    .getIntegerField(CalculationOperationComponentFields.NUMBER_OF_OPERATIONS);

            operationNormsTable.addCell(new Phrase((numberOfOperations == null) ? "" : numberOfOperations.toString(), FontUtils
                    .getDejavuRegular7Dark()));
        }

        return operationNormsTable;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("costCalculation.costCalculationDetails.report.title", locale);
    }

<<<<<<< HEAD
    private void addTableCellAsTwoColumnsTable(final PdfPTable table, final String label, final Object value) {
        pdfHelper.addTableCellAsTable(table, label, value, FontUtils.getDejavuRegular9Dark(), FontUtils.getDejavuRegular9Dark(),
                2);
    }

    private Map<String, String> getCostMode(final Entity costCalculation) {
        Map<String, String> costModeName = new HashMap<String, String>();

        String localeType = "";
        String costMode = "";
        String costCalculationMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE);
=======
    private PdfPTable addTableAboutPieceworkCost(final Entity costCalculation, final Locale locale) {
        List<String> operationsTableHeader = new ArrayList<String>();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.level",
                L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER,
                "costCalculation.costCalculationDetails.report.columnHeader.pieces",
                "costCalculation.costCalculationDetails.report.columnHeader.operationCost",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            operationsTableHeader.add(translationService.translate(translate, locale));
        }

        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.level", locale),
                HeaderAlignment.LEFT);
        alignments.put(
                translationService.translate(L_COST_CALCULATION_COST_CALCULATION_DETAILS_REPORT_COLUMN_HEADER_NUMBER, locale),
                HeaderAlignment.LEFT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.pieces", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.operationCost", locale),
                HeaderAlignment.RIGHT);
        alignments.put(translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.margin", locale),
                HeaderAlignment.RIGHT);
        alignments.put(
                translationService.translate("costCalculation.costCalculationDetails.report.columnHeader.totalCosts", locale),
                HeaderAlignment.RIGHT);

        List<Entity> calculationOperationComponents = costCalculation.getTreeField(CALCULATION_OPERATION_COMPONENTS);

        PdfPTable operationsTable = pdfHelper.createTableWithHeader(operationsTableHeader.size(), operationsTableHeader, false,
                alignments);
>>>>>>> master

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
