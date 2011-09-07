package com.qcadoo.mes.costCalculation.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculateConstants;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementReportDataServiceImpl;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

public class CostCalculationPdfView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private MaterialRequirementReportDataServiceImpl materialRequirementReportDataService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = getTranslationService().translate("costCalculation.costCalculationDetails.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        DataDefinition dataDefCostCalculation = dataDefinitionService.get(CostCalculateConstants.PLUGIN_IDENTIFIER,
                CostCalculateConstants.MODEL_COST_CALCULATION);
        Entity costCalculation = dataDefCostCalculation.find("where id = " + model.get("id").toString()).uniqueResult();

        // Entity calculationOperationComponent = costCalculation.getBelongsToField("calculationOperationComponent");

        PdfPTable leftPanelColumn = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.number.label", locale) + ":",
                costCalculation.getStringField("number"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.product.label", locale) + ":", costCalculation
                        .getBelongsToField("product").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.defaultTechnology.label", locale) + ":",
                costCalculation.getBelongsToField("defaultTechnology").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.technology.label", locale) + ":",
                costCalculation.getBelongsToField("technology").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.quantity.label", locale) + ":",
                getDecimalFormat().format(costCalculation.getField("quantity")), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        Entity order = costCalculation.getBelongsToField("order");
        addTableCellAsTable(leftPanelColumn,
                getTranslationService().translate("costCalculation.costCalculation.order.label", locale) + ":",
                order != null ? order.getStringField("name") : "", null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        leftPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        addTableCellAsTable(leftPanelColumn,
                "\t \t \t" + getTranslationService().translate("costCalculation.costCalculation.includeTPZ.label", locale) + ":",
                (Boolean) costCalculation.getField("includeTPZ") ? getTranslationService().translate("qcadooView.true", locale)
                        : getTranslationService().translate("qcadooView.false", locale), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        Object reportData;
        reportData = costCalculation.getField("calculateMaterialCostsMode");
        addTableCellAsTable(
                leftPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.calculateMaterialCostsMode.label",
                                locale),
                reportData != null ? getTranslationService().translate(
                        "costCalculation.costCalculation.calculateMaterialCostsMode.value." + reportData.toString(), locale)
                        : getTranslationService().translate("qcadooView.form.blankComboBoxValue", locale), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("calculateOperationCostsMode");
        addTableCellAsTable(
                leftPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.calculateOperationCostsMode.label",
                                locale),
                reportData != null ? getTranslationService().translate(
                        "costCalculation.costCalculation.calculateOperationCostsMode.value." + reportData.toString(), locale)
                        : getTranslationService().translate("qcadooView.form.blankComboBoxValue", locale), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);

        reportData = costCalculation.getField("description");
        addTableCellAsTable(
                leftPanelColumn,
                "\t \t \t" + getTranslationService().translate("costCalculation.costCalculation.description.label", locale) + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : ""), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        PdfPTable rightPanelColumn = PdfUtil.createPanelTable(1);
        rightPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.technicalProductionCost", locale)
                + ":", PdfUtil.getArialBold10Dark()));

        reportData = costCalculation.getField("totalMaterialCosts");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.totalMaterialCosts.label", locale)
                        + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("totalMachineHourlyCosts");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.totalMachineHourlyCosts.label",
                                locale) + ":", (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("totalLaborHourlyCosts");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService()
                                .translate("costCalculation.costCalculation.totalLaborHourlyCosts.label", locale) + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("totalTechnicalProductionCosts");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate(
                                "costCalculation.costCalculation.totalTechnicalProductionCosts.label", locale) + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        rightPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.overheads", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        Object reportData2;
        reportData = costCalculation.getField("productionCostMargin");
        reportData2 = costCalculation.getField("productionCostMarginValue");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.productionCostMargin.label", locale)
                        + ":",
                (reportData != null ? getDecimalFormat().format(reportData)
                        + (reportData2 != null ? " ("
                                + getTranslationService().translate(
                                        "costCalculation.costCalculation.productionCostMarginValue.label", locale) + ": "
                                + reportData2.toString() + ")" : "") : "")
                        + "\t" + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("materialCostMargin");
        reportData2 = costCalculation.getField("materialCostMarginValue");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.materialCostMargin.label", locale)
                        + ":",
                (reportData != null ? getDecimalFormat().format(reportData)
                        + (reportData2 != null ? " ("
                                + getTranslationService().translate(
                                        "costCalculation.costCalculation.materialCostMarginValue.label", locale) + ": "
                                + reportData2.toString() + ")" : "") : "")
                        + "\t" + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        reportData = costCalculation.getField("additionalOverhead");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t"
                        + getTranslationService().translate("costCalculation.costCalculation.additionalOverhead.label", locale)
                        + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        reportData = costCalculation.getField("totalCosts");

        rightPanelColumn.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.totalCost", locale)
                + ":", PdfUtil.getArialBold10Dark()));

        reportData = costCalculation.getField("totalCosts");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t" + getTranslationService().translate("costCalculation.costCalculation.totalCost.label", locale) + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark(), null);

        reportData = costCalculation.getField("costPerUnit");
        addTableCellAsTable(
                rightPanelColumn,
                "\t \t \t" + getTranslationService().translate("costCalculation.costCalculation.costPerUnit.label", locale) + ":",
                (reportData != null ? getDecimalFormat().format(reportData) : "") + "\t"
                        + currencyService.getCurrencyAlphabeticCode(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark(), null);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanelColumn);
        panelTable.addCell(rightPanelColumn);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        document.add(new Paragraph(getTranslationService().translate("costCalculation.costCalculationDetails.report.paragraph",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> materialsTableHeader = new ArrayList<String>();
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.number", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.name", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.unit", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.quantity", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.costsPerUnit", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.costs", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.margin", locale));
        materialsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts", locale));

        PdfPTable materialsTable = PdfUtil.createTableWithHeader(8, materialsTableHeader, false);

        List<Entity> orders = costCalculation.getBelongsToField("technology").getHasManyField("orders");
        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForOrdersTechnologyProducts(orders,
                false);
        products = SortUtil.sortMapUsingComparator(products, new EntityNumberComparator());
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            materialsTable.addCell(new Phrase(product.getKey().getStringField("number"), PdfUtil.getArialRegular9Dark()));
            materialsTable.addCell(new Phrase(product.getKey().getStringField("name"), PdfUtil.getArialRegular9Dark()));
            materialsTable.addCell(new Phrase(product.getKey().getStringField("unit"), PdfUtil.getArialRegular9Dark()));
            materialsTable.addCell(new Phrase(getDecimalFormat().format(product.getValue()), PdfUtil.getArialRegular9Dark()));
            BigDecimal nominalCost = (BigDecimal) product.getKey().getField("nominalCost");
            BigDecimal costForNumber = (BigDecimal) product.getKey().getField("costForNumber");
            BigDecimal costPerUnit = nominalCost.divide(costForNumber, 3, RoundingMode.HALF_UP);
            materialsTable.addCell(new Phrase(getDecimalFormat().format(costPerUnit), PdfUtil.getArialRegular9Dark()));
            BigDecimal costs = product.getValue().multiply(costPerUnit);
            materialsTable.addCell(new Phrase(getDecimalFormat().format(costs), PdfUtil.getArialRegular9Dark()));
            reportData = costCalculation.getField("materialCostMargin");
            if (reportData != null) {
                materialsTable.addCell(new Phrase(getDecimalFormat().format(reportData), PdfUtil.getArialRegular9Dark()));
                BigDecimal totalCosts = costs.add((BigDecimal) reportData);
                materialsTable.addCell(new Phrase(getDecimalFormat().format(totalCosts), PdfUtil.getArialRegular9Dark()));
            } else {
                materialsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                materialsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
        }
        document.add(materialsTable);

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("costCalculation.costCalculationDetails.report.paragraph2",
                locale), PdfUtil.getArialBold11Dark()));

        if ("hourly".equals(costCalculation.getField("calculateOperationCostsMode"))) {
            document.add(addTableAboutHourlyCost(costCalculation, locale));
        } else {
            document.add(addTableAboutPieceworkCost(costCalculation, locale));
        }

        String text = getTranslationService().translate("qcadooReport.commons.endOfPrint.label", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("costCalculation.costCalculationDetails.report.fileName", locale);
    }

    private PdfPTable addTableAboutHourlyCost(final Entity costCalculation, final Locale locale) {
        List<String> operationsTableHeader = new ArrayList<String>();
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.number", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.name", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.level", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.duration", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.machCosts", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.labCosts", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.operationCost", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.margin", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.totalCosts", locale));

        List<Entity> calculationOperationComponents = costCalculation.getTreeField("calculationOperationComponents");

        PdfPTable operationsTable = PdfUtil.createTableWithHeader(9, operationsTableHeader, false);
        if (calculationOperationComponents != null && calculationOperationComponents.size() != 0) {
            for (Entity operationComponent : calculationOperationComponents) {
                operationsTable.addCell(new Phrase(operationComponent.getBelongsToField("operation").getStringField("number"),
                        PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(operationComponent.getBelongsToField("operation").getStringField("name"),
                        PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(operationComponent.getBelongsToField("operation").getId().toString(), PdfUtil
                        .getArialRegular9Dark()));
                operationsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                BigDecimal machineHourlyCost = (BigDecimal) operationComponent.getField("machineHourlyCost");
                BigDecimal laborHourlyCost = (BigDecimal) operationComponent.getField("laborHourlyCost");

                operationsTable.addCell(new Phrase(getDecimalFormat().format(machineHourlyCost), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(getDecimalFormat().format(laborHourlyCost), PdfUtil.getArialRegular9Dark()));
                Object reportData = costCalculation.getField("productionCostMargin");
                if (reportData != null) {
                    operationsTable.addCell(new Phrase(getDecimalFormat().format(reportData), PdfUtil.getArialRegular9Dark()));
                    BigDecimal sum = machineHourlyCost.add(laborHourlyCost).add((BigDecimal) reportData);
                    operationsTable.addCell(new Phrase(getDecimalFormat().format(sum), PdfUtil.getArialRegular9Dark()));
                } else {
                    operationsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                    operationsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                }
            }
        }
        return operationsTable;
    }

    private PdfPTable addTableAboutPieceworkCost(final Entity costCalculation, final Locale locale) {
        List<String> operationsTableHeader = new ArrayList<String>();
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.number", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.name", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.level", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.quantity", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.piecework", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.operationCost", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.margin", locale));
        operationsTableHeader.add(getTranslationService().translate(
                "costCalculation.costCalculationDetails.report.columnHeader.totalCost", locale));

        List<Entity> calculationOperationComponents = costCalculation.getTreeField("calculationOperationComponents");

        // Collections.sort(calculationOperationComponents, new EntityOperationComponentComparator());
        PdfPTable operationsTable = PdfUtil.createTableWithHeader(9, operationsTableHeader, false);
        if (calculationOperationComponents.size() != 0) {
            for (Entity calculationOperationComponent : calculationOperationComponents) {
                operationsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField("operation").getStringField(
                        "number"), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField("operation").getStringField(
                        "name"), PdfUtil.getArialRegular9Dark()));
                operationsTable.addCell(new Phrase(calculationOperationComponent.getBelongsToField("operation").getId()
                        .toString(), PdfUtil.getArialRegular9Dark()));
                BigDecimal pieceworkCost = (BigDecimal) calculationOperationComponent.getField("pieceworkCost");
                operationsTable.addCell(new Phrase(getDecimalFormat().format(pieceworkCost), PdfUtil.getArialRegular9Dark()));
                BigDecimal pieces = (BigDecimal) calculationOperationComponent.getField("pieces");
                operationsTable.addCell(new Phrase(getDecimalFormat().format(pieces), PdfUtil.getArialRegular9Dark()));
                BigDecimal operationCost = pieceworkCost.multiply(pieces);
                operationsTable.addCell(new Phrase(getDecimalFormat().format(operationCost), PdfUtil.getArialRegular9Dark()));
                Object reportData = costCalculation.getField("productionCostMargin");
                if (reportData != null) {
                    operationsTable.addCell(new Phrase(getDecimalFormat().format(reportData), PdfUtil.getArialRegular9Dark()));
                    BigDecimal sum = operationCost.add((BigDecimal) reportData);
                    operationsTable.addCell(new Phrase(getDecimalFormat().format(sum), PdfUtil.getArialRegular9Dark()));
                } else {
                    operationsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                    operationsTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                }
            }
        }
        return operationsTable;
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("costCalculation.costCalculationDetails.report.title", locale));
    }

    @Override
    protected Document newDocument() {
        Document doc = new Document(PageSize.A4.rotate());
        doc.setMargins(40, 40, 60, 60);
        return doc;
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

    private List<Entity> getCalculateOperationComponents(EntityTreeNode operationComponents, List<Entity> listOfOperationComponent) {

        List<EntityTreeNode> operationComponentList = operationComponents.getChildren();
        for (EntityTreeNode operationComponent : operationComponentList) {
            if (operationComponent != null) {
                listOfOperationComponent.add(operationComponent);
                listOfOperationComponent = getCalculateOperationComponents(operationComponent, listOfOperationComponent);
            }
        }
        return listOfOperationComponent;
    }
}
