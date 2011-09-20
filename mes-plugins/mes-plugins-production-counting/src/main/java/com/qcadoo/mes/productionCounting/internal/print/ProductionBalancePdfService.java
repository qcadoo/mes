package com.qcadoo.mes.productionCounting.internal.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class ProductionBalancePdfService extends PdfDocumentService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    SecurityService securityService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = getTranslationService().translate("productionCounting.productionBalance.report.title", locale)
                + " " + productionBalance.getId().toString();
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionBalance.getField("date"),
                securityService.getCurrentUserName());

        PdfPTable leftPanel = createLeftPanel(productionBalance, locale);
        PdfPTable rightPanel = createRightPanel(productionBalance, locale);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        panelTable.addCell(leftPanel);
        panelTable.addCell(rightPanel);
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);

        if ((Boolean) productionBalance.getBelongsToField("order").getField("registerQuantityInProduct")) {
            addInputProductsBalance(document, productionBalance, locale);
        }
        if ((Boolean) productionBalance.getBelongsToField("order").getField("registerQuantityOutProduct")) {
            addOutputProductsBalance(document, productionBalance, locale);
        }

        if ((Boolean) productionBalance.getBelongsToField("order").getField("registerProductionTime")) {
            if (productionBalance.getBelongsToField("order").getField("typeOfProductionRecording") != null
                    && productionBalance.getBelongsToField("order").getStringField("typeOfProductionRecording")
                            .equals("03forEach")) {
                addMachineTimeBalance(document, productionBalance, locale);
                addLaborTimeBalance(document, productionBalance, locale);
            } else {
                addTimeBalanceAsPanel(document, productionBalance, locale);
            }
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

    private PdfPTable createLeftPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable leftPanel = PdfUtil.createPanelTable(1);

        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.title", locale) + ":",
                productionBalance.getId().toString(), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.order", locale),
                productionBalance.getBelongsToField("order").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.product", locale),
                productionBalance.getBelongsToField("product").getStringField("name"), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.report.panel.numberOfRecords", locale),
                productionBalance.getField("recordsNumber").toString(), null, PdfUtil.getArialBold9Dark(),
                PdfUtil.getArialBold9Dark(), null);
        addTableCellAsTable(leftPanel,
                getTranslationService().translate("productionCounting.productionBalance.description.label", locale) + ":",
                productionBalance.getStringField("description"), null, PdfUtil.getArialBold9Dark(), PdfUtil.getArialBold9Dark(),
                null);

        return leftPanel;
    }

    private PdfPTable createRightPanel(final Entity productionBalance, final Locale locale) {
        PdfPTable rightPanel = PdfUtil.createPanelTable(1);

        rightPanel.addCell(new Phrase(getTranslationService().translate(
                "costCalculation.costCalculationDetails.window.mainTab.form.parameters", locale)
                + ":", PdfUtil.getArialBold10Dark()));
        Entity parameters = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                .uniqueResult();
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate(
                        "productionCounting.productionBalance.report.panel.registerQuantityOutProduct", locale)
                + " "
                + ((Boolean) parameters.getField("registerQuantityInProduct") ? getTranslationService().translate(
                        "qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)), PdfUtil
                .getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate(
                        "productionCounting.productionBalance.report.panel.registerQuantityInProduct", locale)
                + " "
                + ((Boolean) parameters.getField("registerQuantityOutProduct") ? getTranslationService().translate(
                        "qcadooView.true", locale) : getTranslationService().translate("qcadooView.false", locale)), PdfUtil
                .getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.registerProductionTime",
                        locale)
                + " "
                + ((Boolean) parameters.getField("registerProductionTime") ? getTranslationService().translate("qcadooView.true",
                        locale) : getTranslationService().translate("qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.allowedPartial", locale)
                + " "
                + ((Boolean) parameters.getField("allowedPartial") ? getTranslationService().translate("qcadooView.true", locale)
                        : getTranslationService().translate("qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.blockClosing", locale)
                + " "
                + ((Boolean) parameters.getField("blockClosing") ? getTranslationService().translate("qcadooView.true", locale)
                        : getTranslationService().translate("qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));
        rightPanel.addCell(new Phrase("\t \t \t"
                + getTranslationService().translate("productionCounting.productionBalance.report.panel.autoCloseOrder", locale)
                + " "
                + ((Boolean) parameters.getField("autoCloseOrder") ? getTranslationService().translate("qcadooView.true", locale)
                        : getTranslationService().translate("qcadooView.false", locale)), PdfUtil.getArialBold9Dark()));

        return rightPanel;
    }

    private void addInputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph",
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
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable inputProductsTable = PdfUtil.createTableWithHeader(7, inputProductsTableHeader, false);

        if (productionBalance.getHasManyField("recordOperationProductInComponent") != null)
            for (Entity inputProduct : productionBalance.getHasManyField("recordOperationProductInComponent")) {
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("number"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("name"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(this.getTranslationService().translate(
                        "basic.product.typeOfMaterial.value."
                                + inputProduct.getBelongsToField("product").getStringField("typeOfMaterial"), locale), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("plannedQuantity")),
                        PdfUtil.getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("usedQuantity")), PdfUtil
                        .getArialRegular9Dark()));
                inputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("balance")), PdfUtil
                        .getArialRegular9Dark()));
            }

        document.add(inputProductsTable);
    }

    private void addOutputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph2",
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
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable outputProductsTable = PdfUtil.createTableWithHeader(7, outputProductsTableHeader, false);

        if (productionBalance.getHasManyField("recordOperationProductOutComponent") != null)
            for (Entity inputProduct : productionBalance.getHasManyField("recordOperationProductOutComponent")) {
                outputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("number"),
                        PdfUtil.getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("name"), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(this.getTranslationService().translate(
                        "basic.product.typeOfMaterial.value."
                                + inputProduct.getBelongsToField("product").getStringField("typeOfMaterial"), locale), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(inputProduct.getBelongsToField("product").getStringField("unit"), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("plannedQuantity")),
                        PdfUtil.getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("usedQuantity")), PdfUtil
                        .getArialRegular9Dark()));
                outputProductsTable.addCell(new Phrase(getDecimalFormat().format(inputProduct.getField("balance")), PdfUtil
                        .getArialRegular9Dark()));
            }

        document.add(outputProductsTable);
    }

    private void addMachineTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph3",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opNumber", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opName", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable machineTimeTable = PdfUtil.createTableWithHeader(5, operationsTimeTableHeader, false);

        if (productionBalance.getHasManyField("productionRecord") != null)
            for (Entity productionRecord : productionBalance.getHasManyField("productionRecord")) {
                machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent")
                        .getBelongsToField("operation").getStringField("number"), PdfUtil.getArialRegular9Dark()));
                machineTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent")
                        .getBelongsToField("operation").getStringField("name"), PdfUtil.getArialRegular9Dark()));
                // TODO planned time ANKI
                machineTimeTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                machineTimeTable.addCell(new Phrase(getDecimalFormat().format(productionRecord.getField("machineTime")), PdfUtil
                        .getArialRegular9Dark()));
                machineTimeTable.addCell(new Phrase(getDecimalFormat().format(productionRecord.getField("machineTimeBalance")),
                        PdfUtil.getArialRegular9Dark()));
            }

        document.add(machineTimeTable);
    }

    private void addLaborTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph4",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opNumber", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opName", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable laborTimeTable = PdfUtil.createTableWithHeader(5, operationsTimeTableHeader, false);

        if (productionBalance.getHasManyField("productionRecord") != null)
            for (Entity productionRecord : productionBalance.getHasManyField("productionRecord")) {
                laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent")
                        .getBelongsToField("operation").getStringField("number"), PdfUtil.getArialRegular9Dark()));
                laborTimeTable.addCell(new Phrase(productionRecord.getBelongsToField("orderOperationComponent")
                        .getBelongsToField("operation").getStringField("name"), PdfUtil.getArialRegular9Dark()));
                // TODO planned time ANKI
                laborTimeTable.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                laborTimeTable.addCell(new Phrase(getDecimalFormat().format(productionRecord.getField("laborTime")), PdfUtil
                        .getArialRegular9Dark()));
                laborTimeTable.addCell(new Phrase(getDecimalFormat().format(productionRecord.getField("laborTimeBalance")),
                        PdfUtil.getArialRegular9Dark()));
            }

        document.add(laborTimeTable);
    }

    private void addTimeBalanceAsPanel(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate(
                "productionCounting.productionBalanceDetails.window.productionTime.tabLabel", locale), PdfUtil
                .getArialBold11Dark()));

        Entity productionRecord = productionBalance.getHasManyField("productionRecord").find().uniqueResult();

        PdfPTable timePanel = PdfUtil.createPanelTable(3);

        // TODO planned time ANKI
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machinePlannedTime.label", locale)
                        + ":", "", null, PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machineRegisteredTime.label", locale)
                        + ":", productionRecord.getStringField("machineTime"), null, PdfUtil.getArialRegular9Dark(),
                PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.machineTimeBalance.label", locale)
                        + ":", productionRecord.getStringField("machineTimeBalance"), null, PdfUtil.getArialRegular9Dark(),
                PdfUtil.getArialRegular9Dark(), null);
        // TODO planned time ANKI
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborPlannedTime.label", locale)
                        + ":", "", null, PdfUtil.getArialRegular9Dark(), PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborRegisteredTime.label", locale)
                        + ":", productionRecord.getStringField("laborTime"), null, PdfUtil.getArialRegular9Dark(),
                PdfUtil.getArialRegular9Dark(), null);
        addTableCellAsTable(
                timePanel,
                getTranslationService().translate(
                        "productionCounting.productionBalanceDetails.window.productionTime.laborTimeBalance.label", locale)
                        + ":", productionRecord.getStringField("laborTimeBalance"), null, PdfUtil.getArialRegular9Dark(),
                PdfUtil.getArialRegular9Dark(), null);

        document.add(timePanel);
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
