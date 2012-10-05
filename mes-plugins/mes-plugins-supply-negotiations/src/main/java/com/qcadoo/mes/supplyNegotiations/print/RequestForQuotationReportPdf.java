package com.qcadoo.mes.supplyNegotiations.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.REQUEST_FOR_QUOTATION_PRODUCTS;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.PRODUCT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields;
import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "requestsForQuotationReportPdf")
public class RequestForQuotationReportPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    FileService fileService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private SecurityService securityService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        String documentTitle = translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        checkState(model.get("id") != null, "Unable to generate report for unsaved requestForQuotation! (missing id)");

        Long requestForQuotationId = Long.valueOf(model.get("id").toString());

        Entity requestForQuotation = dataDefinitionService.get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_REQUEST_FOR_QUOTATION).get(requestForQuotationId);
        createHeaderTable(document, locale, requestForQuotation);
        createTableWith(document, locale, requestForQuotation);
        String endOfPrint = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);

        pdfHelper.addEndOfDocument(document, writer, endOfPrint);

        return translationService.translate("supplyNegotiations.requestForQuotation.report.requestForQuotationReport.fileName",
                locale, requestForQuotation.getStringField("number"), DateUtils.REPORT_D_T_F.format(new Date()));
    }

    private void createHeaderTable(final Document document, final Locale locale, final Entity requestForQuotation)
            throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(3);
        panelTable.setSpacingBefore(7);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("supplyNegotiations.requestForQuotation.report.columnHeader.number", locale),
                requestForQuotation.getStringField(RequestForQuotationFields.NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(
                panelTable,
                translationService.translate("supplyNegotiations.requestForQuotation.report.columnHeader.name", locale),
                requestForQuotation.getStringField(RequestForQuotationFields.NAME) == null ? "" : requestForQuotation
                        .getStringField(RequestForQuotationFields.NAME));
        pdfHelper.addTableCellAsOneColumnTable(
                panelTable,
                translationService.translate("supplyNegotiations.requestForQuotation.report.columnHeader.description", locale),
                requestForQuotation.getStringField(RequestForQuotationFields.DESCRIPTION) == null ? "" : requestForQuotation
                        .getStringField(RequestForQuotationFields.DESCRIPTION));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "supplyNegotiations.requestForQuotation.report.columnHeader.desiredDate", locale), requestForQuotation
                .getField("despireTime") == null ? "" : requestForQuotation.getField(RequestForQuotationFields.DESIRED_DATE));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "supplyNegotiations.requestForQuotation.report.columnHeader.supplier", locale),
                requestForQuotation.getBelongsToField(RequestForQuotationFields.SUPPLIER) == null ? "" : requestForQuotation
                        .getBelongsToField(RequestForQuotationFields.SUPPLIER).getStringField("name"));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");
        document.add(panelTable);
    }

    private void createTableWith(final Document document, final Locale locale, final Entity requestForQuotation)
            throws DocumentException {
        Paragraph productTableTitle = new Paragraph(new Phrase(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.products", locale),
                FontUtils.getDejavuBold11Dark()));
        productTableTitle.setSpacingBefore(7f);
        productTableTitle.setSpacingAfter(7f);
        document.add(productTableTitle);
        List<String> resourcesTableHeader = new ArrayList<String>();

        resourcesTableHeader.add(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.number", locale));
        resourcesTableHeader.add(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.name", locale));
        resourcesTableHeader.add(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.ordererQuantity", locale));
        resourcesTableHeader.add(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.unit", locale));
        resourcesTableHeader.add(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.annualVolume", locale));

        PdfPTable resourcesTable = pdfHelper.createTableWithHeader(5, resourcesTableHeader, false);

        for (Entity requestForQuotationProduct : requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS)) {
            resourcesTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            resourcesTable.addCell(new Phrase(requestForQuotationProduct.getBelongsToField(PRODUCT).getStringField(
                    ProductFields.NUMBER), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(requestForQuotationProduct.getBelongsToField(PRODUCT).getStringField(
                    ProductFields.NAME), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
            resourcesTable.addCell(new Phrase(numberService.format(requestForQuotationProduct
                    .getDecimalField(RequestForQuotationProductFields.ORDERED_QUANTITY)), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(requestForQuotationProduct.getBelongsToField(PRODUCT).getStringField(
                    ProductFields.UNIT), FontUtils.getDejavuRegular9Dark()));
            resourcesTable.addCell(new Phrase(numberService.format(requestForQuotationProduct
                    .getDecimalField(RequestForQuotationProductFields.ANNUAL_VOLUME)), FontUtils.getDejavuRegular9Dark()));
        }
        document.add(resourcesTable);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationReport.title", locale));
    }

}
