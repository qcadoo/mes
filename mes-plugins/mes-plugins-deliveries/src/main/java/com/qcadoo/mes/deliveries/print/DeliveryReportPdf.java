package com.qcadoo.mes.deliveries.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.ALIGNMENT;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.IDENTIFIER;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.NAME;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveriesColumnAlignment;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "deliveryReportPdf")
public class DeliveryReportPdf extends ReportPdfView {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveryColumnFetcher deliveryColumnFetcher;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved delivery! (missing id)");

        String documentTitle = translationService.translate("deliveries.deliveryReport.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        createHeaderTable(document, delivery, locale);
        createProductsTable(document, delivery, locale);

        String endOfPrint = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);

        pdfHelper.addEndOfDocument(document, writer, endOfPrint);

        return translationService.translate("deliveries.deliveryReport.report.fileName", locale, delivery.getStringField(NUMBER),
                DateUtils.REPORT_D_T_F.format((Date) delivery.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Entity delivery, final Locale locale) throws DocumentException {
        PdfPTable panelTable = pdfHelper.createPanelTable(3);

        panelTable.setSpacingBefore(7);

        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.number", locale),
                delivery.getStringField(NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.name", locale),
                (delivery.getStringField(DeliveryFields.NAME) == null) ? "" : delivery.getStringField(NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.description", locale),
                (delivery.getStringField(DESCRIPTION) == null) ? "" : delivery.getStringField(DESCRIPTION));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.deliveryDate", locale),
                (delivery.getField(DELIVERY_DATE) == null) ? "" : delivery.getField(DELIVERY_DATE));
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("deliveries.deliveryReport.report.columnHeader.supplier", locale),
                (delivery.getBelongsToField(SUPPLIER) == null) ? "" : delivery.getBelongsToField(SUPPLIER).getStringField(NAME));
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");

        document.add(panelTable);
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForDeliveries = deliveriesService.getColumnsForDeliveries();

        if (!columnsForDeliveries.isEmpty()) {
            PdfPTable productsTable = pdfHelper.createTableWithHeader(columnsForDeliveries.size(),
                    prepareProductsTableHeader(document, columnsForDeliveries, locale), false);

            Map<Entity, DeliveryProduct> productWithDeliveryProducts = deliveryColumnFetcher
                    .getProductWithDeliveryProducts(delivery);

            Map<Entity, Map<String, String>> deliveryProductsColumnValues = deliveryColumnFetcher
                    .getDeliveryProductsColumnValues(productWithDeliveryProducts);

            for (Entry<Entity, DeliveryProduct> productWithDeliveryProduct : productWithDeliveryProducts.entrySet()) {
                Entity product = productWithDeliveryProduct.getKey();

                for (Entity columnForDeliveries : columnsForDeliveries) {
                    String identifier = columnForDeliveries.getStringField(IDENTIFIER);
                    String alignment = columnForDeliveries.getStringField(ALIGNMENT);

                    String value = deliveryProductsColumnValues.get(product).get(identifier);

                    prepareProductColumnAlignment(productsTable.getDefaultCell(),
                            DeliveriesColumnAlignment.parseString(alignment));

                    productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));
                }
            }

            document.add(productsTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForDeliveries,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService
                .translate("deliveries.deliveryReport.report.deliveryProducts.title", locale), FontUtils.getDejavuBold11Dark()));

        List<String> productsHeader = new ArrayList<String>();

        for (Entity columnForDeliveries : columnsForDeliveries) {
            String name = columnForDeliveries.getStringField(NAME);

            productsHeader.add(translationService.translate(name, locale));
        }

        return productsHeader;
    }

    private void prepareProductColumnAlignment(final PdfPCell cell, final DeliveriesColumnAlignment columnAlignment) {
        if (DeliveriesColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (DeliveriesColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("deliveries.deliveryReport.report.title", locale));
    }

}
