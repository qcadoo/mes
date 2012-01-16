/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

import static com.google.common.collect.Lists.newLinkedList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.technologies.print.ReportDataService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class CostCalculationPdfService extends PdfDocumentService {

    @Autowired
    SecurityService securityService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    CurrencyService currencyService;

    @Autowired
    TreeNumberingService treeNumberingService;

    @Autowired
    ReportDataService reportDataService;

    @Autowired
    MaterialRequirementReportDataService materialRequirementReportDataService;

    @Autowired
    TimeConverterService timeConverterService;

    private String tabInText = "\t \t \t";

    private static final String NAME = "name";

    @Override
    protected void buildPdfContent(Document document, Entity entity, Locale locale) throws DocumentException {
        String documentTitle = getTranslationService().translate("costCalculation.costCalculationDetails.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        DataDefinition dataDefCostCalculation = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                CostCalculateConstants.MODEL_COST_CALCULATION);
        Entity costCalculation = dataDefCostCalculation.find("where id = " + entity.getId().toString()).uniqueResult();

        PdfPTable leftPanelColumn = addLeftPanelToReport(costCalculation, locale);
        PdfPTable rightPanelColumn = addRightPanelToReport(costCalculation, locale);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanelColumn);
        panelTable.addCell(rightPanelColumn);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        document.add(new Paragraph(getTranslationService().translate("costCalculation.costCalculationDetails.report.paragraph",
                locale), PdfUtil.getArialBold11Dark()));
        PdfPTable materialsTable = addMaterialsTable(costCalculation, locale);
        document.add(materialsTable);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("costCalculation.costCalculationDetails.report.paragraph2",
                locale), PdfUtil.getArialBold11Dark()));

        if ("hourly".equals(costCalculation.getField("calculateOperationCostsMode"))) {
            document.add(addTableAboutHourlyCost(costCalculation, locale));
        } else {
            document.add(addTableAboutPieceworkCost(costCalculation, locale));
        }

    }

    @Override
    protected String getReportTitle(Locale locale) {
        return getTranslationService().translate("costCalculation.costCalculationDetails.report.fileName", locale);
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

    public PdfPTable addLeftPanelToReport(final Entity costCalculation, final Locale locale) {
        PdfPTable leftPanelColumn = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.number.label", locale) + ":",
                costCalculation.getStringField("number"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.product.label", locale) + ":", costCalculation
                        .getBelongsToField("product").getStringField(NAME), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.defaultTechnology.label", locale) + ":",
                costCalculation.getBelongsToField("defaultTechnology").getStringField(NAME), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.technology.label", locale) + ":",
                costCalculation.getBelongsToField("technology").getStringField(NAME), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.quantity.label", locale) + ":",
                getDecimalFormat().format(costCalculation.getField("quantity")), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        Entity order = costCalculation.getBelongsToField("order");
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.order.label", locale) + ":",
                order == null ? "" : order.getStringField(NAME), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        leftPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        addTableCellAsTable(leftPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.includeTPZ.label", locale) + ":",
                (Boolean) costCalculation.getField("includeTPZ") ? getTranslationService().translate("qcadooView.true", locale)
                        : getTranslationService().translate("qcadooView.false", locale), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        Object reportData = costCalculation.getField("calculateMaterialCostsMode");
        addTableCellAsTable(
                leftPanelColumn,
                tabInText
                        + getTranslationService().translate("costCalculation.costCalculation.calculateMaterialCostsMode.label",
                                locale),
                reportData == null ? getTranslationService().translate("qcadooView.form.blankComboBoxValue", locale)
                        : getTranslationService().translate(
                                "costCalculation.costCalculation.calculateMaterialCostsMode.value." + reportData.toString(),
                                locale), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("calculateOperationCostsMode");
        addTableCellAsTable(
                leftPanelColumn,
                tabInText
                        + getTranslationService().translate("costCalculation.costCalculation.calculateOperationCostsMode.label",
                                locale),
                reportData == null ? getTranslationService().translate("qcadooView.form.blankComboBoxValue", locale)
                        : getTranslationService().translate(
                                "costCalculation.costCalculation.calculateOperationCostsMode.value." + reportData.toString(),
                                locale)

                , null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);

        reportData = costCalculation.getField("description");
        addTableCellAsTable(leftPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.description.label", locale) + ":",
                (reportData == null ? "" : reportData), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);

        return leftPanelColumn;
    }

    public PdfPTable addRightPanelToReport(Entity costCalculation, Locale locale) {
        PdfPTable rightPanelColumn = PdfUtil.createPanelTable(1);
        rightPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.technicalProductionCost", locale)
                + ":", PdfUtil.getArialBold10Dark()));

        Object reportData = costCalculation.getField("totalMaterialCosts");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.totalMaterialCosts.label", locale)
                        + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("totalMachineHourlyCosts");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText
                        + getTranslationService().translate("costCalculation.costCalculation.totalMachineHourlyCosts.label",
                                locale) + ":", (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("totalLaborHourlyCosts");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText
                        + getTranslationService()
                                .translate("costCalculation.costCalculation.totalLaborHourlyCosts.label", locale) + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("totalTechnicalProductionCosts");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText
                        + getTranslationService().translate(
                                "costCalculation.costCalculation.totalTechnicalProductionCosts.label", locale) + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        rightPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.overheads", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        reportData = costCalculation.getField("productionCostMargin");
        Object reportData2 = costCalculation.getField("productionCostMarginValue");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText
                        + getTranslationService().translate("costCalculation.costCalculation.productionCostMargin.label", locale)
                        + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)
                        + (reportData2 == null ? "" : " ("
                                + getTranslationService().translate(
                                        "costCalculation.costCalculation.productionCostMarginValue.label", locale) + ": "
                                + reportData2.toString() + " " + currencyService.getCurrencyAlphabeticCode() + ")")), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("materialCostMargin");
        reportData2 = costCalculation.getField("materialCostMarginValue");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.materialCostMargin.label", locale)
                        + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)
                        + (reportData2 == null ? "" : " ("
                                + getTranslationService().translate(
                                        "costCalculation.costCalculation.materialCostMarginValue.label", locale) + ": "
                                + reportData2.toString() + " " + currencyService.getCurrencyAlphabeticCode() + ")")), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("additionalOverhead");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.additionalOverhead.label", locale)
                        + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        reportData = costCalculation.getField("totalCosts");

        rightPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.totalCost", locale)
                + ":", PdfUtil.getArialBold10Dark()));

        reportData = costCalculation.getField("totalCosts");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.totalCosts.label", locale) + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark(), null);

        reportData = costCalculation.getField("costPerUnit");
        addTableCellAsTable(
                rightPanelColumn,
                tabInText + getTranslationService().translate("costCalculation.costCalculation.costPerUnit.label", locale) + ":",
                (reportData == null ? "" : getDecimalFormat().format(reportData)) + " "
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark(), null);
        return rightPanelColumn;
    }

    public PdfPTable addMaterialsTable(Entity costCalculation, Locale locale) {

        List<String> materialsTableHeader = new ArrayList<String>();
        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.number",
                "costCalculation.costCalculationDetails.report.columnHeader.name",
                "costCalculation.costCalculationDetails.report.columnHeader.unit",
                "costCalculation.costCalculationDetails.report.columnHeader.quantity",
                "costCalculation.costCalculationDetails.report.columnHeader.costsPerUnit",
                "costCalculation.costCalculationDetails.report.columnHeader.costs",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {

            materialsTableHeader.add(getTranslationService().translate(translate, locale));
        }
        PdfPTable materialsTable = PdfUtil.createTableWithHeader(materialsTableHeader.size(), materialsTableHeader, false);
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        Entity technology;
        if (costCalculation.getBelongsToField("order") == null) {
            technology = costCalculation.getBelongsToField("technology");
        } else {
            technology = costCalculation.getBelongsToField("order").getBelongsToField("technology");
        }
        reportDataService.countQuantityForProductsIn(products, technology, (BigDecimal) costCalculation.getField("quantity"),
                true);
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            materialsTable.addCell(new Phrase(product.getKey().getStringField("number"), PdfUtil.getArialRegular9Dark()));
            materialsTable.addCell(new Phrase(product.getKey().getStringField(NAME), PdfUtil.getArialRegular9Dark()));
            materialsTable.addCell(new Phrase(product.getKey().getStringField("unit"), PdfUtil.getArialRegular9Dark()));
            materialsTable.addCell(new Phrase(getDecimalFormat().format(product.getValue()), PdfUtil.getArialRegular9Dark()));
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal nominalCost = (BigDecimal) product.getKey().getField("nominalCost");
            BigDecimal costForNumber = (BigDecimal) product.getKey().getField("costForNumber");
            BigDecimal costPerUnit = nominalCost.divide(costForNumber, 3, RoundingMode.HALF_UP);
            materialsTable.addCell(new Phrase(getDecimalFormat().format(costPerUnit), PdfUtil.getArialRegular9Dark()));
            BigDecimal costs = product.getValue().multiply(costPerUnit);
            materialsTable.addCell(new Phrase(getDecimalFormat().format(costs), PdfUtil.getArialRegular9Dark()));
            Object reportData = costCalculation.getField("materialCostMargin");
            if (reportData == null) {
                materialsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                materialsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            } else {
                materialsTable.addCell(new Phrase(getDecimalFormat().format(reportData), PdfUtil.getArialRegular9Dark()));
                BigDecimal totalCosts = costs.add((BigDecimal) reportData);
                materialsTable.addCell(new Phrase(getDecimalFormat().format(totalCosts), PdfUtil.getArialRegular9Dark()));
            }
            materialsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        return materialsTable;
    }

    private PdfPTable addTableAboutHourlyCost(final Entity costCalculation, final Locale locale) {
        List<String> operationsTableHeader = new ArrayList<String>();

        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.level",
                "costCalculation.costCalculationDetails.report.columnHeader.name",
                "costCalculation.costCalculationDetails.report.columnHeader.duration",
                "costCalculation.costCalculationDetails.report.columnHeader.machCosts",
                "costCalculation.costCalculationDetails.report.columnHeader.labCosts",
                "costCalculation.costCalculationDetails.report.columnHeader.operationCost",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            operationsTableHeader.add(getTranslationService().translate(translate, locale));
        }

        int[] columnWitdh = { 20, 20, 20, 20, 20, 20, 20, 20, 20 };
        List<Entity> calculationOperationComponents = newLinkedList(costCalculation
                .getTreeField("calculationOperationComponents"));

        Collections.sort(calculationOperationComponents, treeNumberingService.getTreeNodesNumberComparator());

        PdfPTable operationsTable = PdfUtil.createTableWithHeader(operationsTableHeader.size(), operationsTableHeader, false,
                columnWitdh);

        if (calculationOperationComponents != null && calculationOperationComponents.size() != 0) {
            for (Entity calculationOperationComponent : calculationOperationComponents) {
                operationsTable.addCell(new Phrase(calculationOperationComponent.getField("nodeNumber").toString(), PdfUtil
                        .getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField("operation").getStringField(
                        NAME), PdfUtil.getArialRegular9Dark()));

                String duration = timeConverterService.convertTimeToString((Integer) calculationOperationComponent
                        .getField("duration"));

                operationsTable.addCell(new Phrase(duration, PdfUtil.getArialRegular9Dark()));
                operationsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("machineHourlyCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("laborHourlyCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("operationCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("operationMarginCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("totalOperationCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            }
        }
        return operationsTable;
    }

    private PdfPTable addTableAboutPieceworkCost(final Entity costCalculation, final Locale locale) {
        List<String> operationsTableHeader = new ArrayList<String>();

        for (String translate : Arrays.asList("costCalculation.costCalculationDetails.report.columnHeader.level",
                "costCalculation.costCalculationDetails.report.columnHeader.name",
                "costCalculation.costCalculationDetails.report.columnHeader.pieces",
                "costCalculation.costCalculationDetails.report.columnHeader.piecework",
                "costCalculation.costCalculationDetails.report.columnHeader.operationCost",
                "costCalculation.costCalculationDetails.report.columnHeader.margin",
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts")) {
            operationsTableHeader.add(getTranslationService().translate(translate, locale));
        }
        List<Entity> calculationOperationComponents = costCalculation.getTreeField("calculationOperationComponents");

        PdfPTable operationsTable = PdfUtil.createTableWithHeader(operationsTableHeader.size(), operationsTableHeader, false);

        if (calculationOperationComponents.size() != 0) {
            for (Entity calculationOperationComponent : calculationOperationComponents) {
                operationsTable.addCell(new Phrase(calculationOperationComponent.getField("nodeNumber").toString(), PdfUtil
                        .getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField("operation").getStringField(
                        NAME), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(calculationOperationComponent.getField("pieces")),
                        PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("pieceworkCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("operationCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("operationMarginCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(
                        calculationOperationComponent.getField("totalOperationCost")), PdfUtil.getArialRegular9Dark()));
                operationsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            }
        }
        return operationsTable;
    }
}
