package com.qcadoo.mes.productionCounting.internal.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public class ProductionCountingPdfService extends PdfDocumentService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    SecurityService securityService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionCounting, final Locale locale)
            throws DocumentException {
        String documentTitle = getTranslationService().translate("productionCounting.productionCounting.report.title", locale)
                + " " + productionCounting.getId().toString();
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionCounting.getField("date"),
                securityService.getCurrentUserName());

        PdfPTable leftPanel = createLeftPanel(productionCounting, locale);
        PdfPTable rightPanel = createRightPanel(productionCounting, locale);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        for (Entity productionRecord : productionCounting.getHasManyField("productionRecords")) {
            addProductionRecord(document, productionRecord, locale);
        }
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

    private PdfPTable createLeftPanel(final Entity productionCounting, final Locale locale) {
        PdfPTable leftPanel = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionCounting.report.title", locale) + ":",
                productionCounting.getId().toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.order", locale),
                productionCounting.getBelongsToField("order").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.product", locale),
                productionCounting.getBelongsToField("product").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                String.valueOf(productionCounting.getHasManyField("productionRecords").size()), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionCounting.getStringField("description"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionCounting, final Locale locale) {
        PdfPTable rightPanel = PdfUtil.createPanelTable(1);

        rightPanel.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityOutProduct", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField("order").getField("registerQuantityInProduct") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerQuantityInProduct", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField("order").getField("registerQuantityOutProduct") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel
                .addCell(new Phrase(
                        "\t \t \t"
                                + getTranslationService().translate(
                                        "productionCounting.productionBalance.report.panel.registerProductionTime", locale)
                                + " "
                                + ((Boolean) productionCounting.getBelongsToField("order").getField("registerProductionTime") ? getTranslationService()
                                        .translate("qcadooView.true", locale) : getTranslationService().translate(
                                        "qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.allowedPartial", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField("order").getField("allowedPartial") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.blockClosing", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField("order").getField("blockClosing") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + ((Boolean) productionCounting.getBelongsToField("order").getField("autoCloseOrder") ? getTranslationService()
                        .translate("qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)),
                PdfUtil.getArialBold9Dark()));

        return rightPanel;
    }

    private void addProductionRecord(Document document, final Entity productionRecord, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph",
                locale)
                + " " + productionRecord.getId().toString(), PdfUtil.getArialBold19Dark()));

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        addTableCellAsTable(
                panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.recordType", locale),
                (Boolean) productionRecord.getField("isFinal") == false ? getTranslationService().translate(
                        "productionCounting.productionCounting.report.panel.recordType.partial", locale)
                        : getTranslationService().translate(
                                "productionCounting.productionCounting.report.panel.recordType.final", locale), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(
                panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.operationAndLevel", locale),
                productionRecord.getBelongsToField("orderOperationComponent").getBelongsToField("operation")
                        .getStringField("name"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.dateAndTime", locale),
                getDecimalFormat().format(productionRecord.getField("creationTime")), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(
                panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.machineOperationTime",
                        locale), getDecimalFormat().format(productionRecord.getField("machineTime")), null,
                PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(panelTable,
                getTranslationService().translate("productionCounting.productionCounting.report.panel.worker", locale),
                productionRecord.getStringField("worker"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(panelTable,
                getTranslationService()
                        .translate("productionCounting.productionCounting.report.panel.laborOperationTime", locale),
                getDecimalFormat().format(productionRecord.getField("laborTime")), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);

        document.add(panelTable);

        if ((Boolean) productionRecord.getBelongsToField("order").getField("registerQuantityInProduct"))
            addInputProducts(document, productionRecord, locale);
        if ((Boolean) productionRecord.getBelongsToField("order").getField("registerQuantityOutProduct"))
            addOutputProducts(document, productionRecord, locale);
    }

    private void addInputProducts(Document document, final Entity productionRecord, final Locale locale) throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph2",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> inputProductsTableHeader = new ArrayList<String>();
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        inputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.quantity", locale));

        PdfPTable inputProductsTable = PdfUtil.createTableWithHeader(5, inputProductsTableHeader, false);

        if (productionRecord.getHasManyField("recordOperationProductInComponents") != null)
            for (Entity productIn : productionRecord.getHasManyField("recordOperationProductInComponents")) {
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField("product").getStringField("number"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField("product").getStringField("name"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(getTranslationService().translate(
                        "basic.product.typeOfMaterial.value."
                                + productIn.getBelongsToField("product").getStringField("typeOfMaterial"), locale), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(productIn.getBelongsToField("product").getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(getDecimalFormat().format(productIn.getField("usedQuantity")), PdfUtil
                        .getArialRegular9Dark()));
            }

        document.add(inputProductsTable);
    }

    private void addOutputProducts(Document document, final Entity productionRecord, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionCounting.report.paragraph3",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> outputProductsTableHeader = new ArrayList<String>();
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.number", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.productionName", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.type", locale));
        outputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.quantity", locale));

        PdfPTable outputProductsTable = PdfUtil.createTableWithHeader(5, outputProductsTableHeader, false);

        if (productionRecord.getHasManyField("recordOperationProductOutComponents") != null)
            for (Entity productOut : productionRecord.getHasManyField("recordOperationProductOutComponents")) {
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField("product").getStringField("number"), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField("product").getStringField("name"), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(getTranslationService().translate(
                        "basic.product.typeOfMaterial.value."
                                + productOut.getBelongsToField("product").getStringField("typeOfMaterial"), locale), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(productOut.getBelongsToField("product").getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(getDecimalFormat().format(productOut.getField("usedQuantity")), PdfUtil
                        .getArialRegular9Dark()));
            }

        document.add(outputProductsTable);
    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("productionCounting.productionBalance.report.title", locale);
    }

}
