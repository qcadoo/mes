package com.qcadoo.mes.productionCounting.internal.print;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class ProductionBalancePdfService extends PdfDocumentService {

    @Autowired
    SecurityService securityService;

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        String documentTitle = getTranslationService().translate("productionCounting.productionBalance.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, (Date) productionBalance.getField("date"),
                securityService.getCurrentUserName());
        addPanel(document, productionBalance, locale);
        addInputProductsBalance(document, productionBalance, locale);
        addOutputProductsBalance(document, productionBalance, locale);
        machineTimeBalance(document, productionBalance, locale);
        laborTimeBalance(document, productionBalance, locale);
    }

    private void addPanel(final Document document, final Entity productionBalance, final Locale locale) throws DocumentException {
        PdfPTable panelTable = PdfUtil.createPanelTable(1);
        // PdfUtil.addTableCellAsTable(
        // panelTable,
        // getTranslationService().translate("productionCounting.productionBalance.report.panel.productionBalance.date",
        // locale), ((Date) productionBalance.getField("date")).toString(), null, PdfUtil.getArialBold10Dark(),
        // PdfUtil.getArialRegular10Dark());
        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);
        document.add(panelTable);
    }

    private void addInputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> inputProductsTableHeader = new ArrayList<String>();
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.matNumber", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.matName", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.matType", locale));
        inputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        inputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(7, inputProductsTableHeader, false);

        document.add(table);
    }

    private void addOutputProductsBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph2",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> outputProductsTableHeader = new ArrayList<String>();
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.matNumber", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.matName", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.matType", locale));
        outputProductsTableHeader.add(getTranslationService().translate("basic.product.unit.label", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedQuantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.usedQuantity", locale));
        outputProductsTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(7, outputProductsTableHeader, false);

        document.add(table);
    }

    private void machineTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph3",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opNumber", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opNamer", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.level", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(6, operationsTimeTableHeader, false);

        document.add(table);
    }

    private void laborTimeBalance(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("productionCounting.productionBalance.report.paragraph4",
                locale), PdfUtil.getArialBold11Dark()));

        List<String> operationsTimeTableHeader = new ArrayList<String>();
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opNumber", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.opNamer", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.level", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.plannedDuration", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.registeredTime", locale));
        operationsTimeTableHeader.add(getTranslationService().translate(
                "productionCounting.productionBalance.report.columnHeader.balance", locale));

        PdfPTable table = PdfUtil.createTableWithHeader(6, operationsTimeTableHeader, false);

        document.add(table);
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
