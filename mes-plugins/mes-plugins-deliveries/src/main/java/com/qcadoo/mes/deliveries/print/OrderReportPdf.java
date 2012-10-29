package com.qcadoo.mes.deliveries.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.NAME;
import static com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields.ALIGNMENT;
import static com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields.IDENTIFIER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import com.qcadoo.security.api.SecurityService;

@Component(value = "orderReportPdf")
public class OrderReportPdf extends ReportPdfView {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private OrderColumnFetcher orderColumnFetcher;

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

        String documentTitle = translationService.translate("deliveries.order.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        createHeaderTable(document, delivery, locale);
        createProductsTable(document, delivery, locale);

        String endOfPrint = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);

        pdfHelper.addEndOfDocument(document, writer, endOfPrint);

        return translationService.translate("deliveries.order.report.fileName", locale, delivery.getStringField(NUMBER),
                DateUtils.REPORT_D_T_F.format((Date) delivery.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Entity delivery, final Locale locale) throws DocumentException {
        PdfPTable headerTable = pdfHelper.createPanelTable(3);

        headerTable.setSpacingBefore(7);

        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.order.report.columnHeader.number", locale),
                delivery.getStringField(NUMBER));
        if (delivery.getStringField(NAME) == null) {
            pdfHelper.addTableCellAsOneColumnTable(
                    headerTable,
                    translationService.translate("deliveries.order.report.columnHeader.supplier", locale),
                    (delivery.getBelongsToField(SUPPLIER) == null) ? "" : delivery.getBelongsToField(SUPPLIER).getStringField(
                            NAME));
        } else {
            pdfHelper.addTableCellAsOneColumnTable(headerTable,
                    translationService.translate("deliveries.order.report.columnHeader.name", locale),
                    delivery.getStringField(NAME));
        }
        if (delivery.getStringField(DESCRIPTION) == null) {
            pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        } else {
            pdfHelper.addTableCellAsOneColumnTable(headerTable,
                    translationService.translate("deliveries.order.report.columnHeader.description", locale),
                    delivery.getStringField(DESCRIPTION));
        }

        if (delivery.getField(DELIVERY_DATE) == null) {
            pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        } else {
            pdfHelper.addTableCellAsOneColumnTable(headerTable,
                    translationService.translate("deliveries.order.report.columnHeader.deliveryDate", locale),
                    new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, locale).format((Date) delivery.getField(DELIVERY_DATE)));
        }
        if (delivery.getStringField(NAME) == null) {
            pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        } else {
            pdfHelper.addTableCellAsOneColumnTable(
                    headerTable,
                    translationService.translate("deliveries.order.report.columnHeader.supplier", locale),
                    (delivery.getBelongsToField(SUPPLIER) == null) ? "" : delivery.getBelongsToField(SUPPLIER).getStringField(
                            NAME));
        }
        pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");

        document.add(headerTable);
        document.add(Chunk.NEWLINE);
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForOrders = deliveriesService.getColumnsForOrders();

        if (!columnsForOrders.isEmpty()) {
            PdfPTable productsTable = pdfHelper.createTableWithHeader(columnsForOrders.size(),
                    prepareProductsTableHeader(document, columnsForOrders, locale), false);

            List<Entity> orderedProducts = delivery.getHasManyField(ORDERED_PRODUCTS);

            Map<Entity, Map<String, String>> orderedProductsColumnValues = orderColumnFetcher
                    .getOrderedProductsColumnValues(orderedProducts);

            for (Entity orderedProduct : orderedProducts) {
                Entity product = orderedProduct.getBelongsToField(PRODUCT);

                for (Entity columnForOrders : columnsForOrders) {
                    String identifier = columnForOrders.getStringField(IDENTIFIER);
                    String alignment = columnForOrders.getStringField(ALIGNMENT);

                    String value = orderedProductsColumnValues.get(product).get(identifier);

                    prepareProductColumnAlignment(productsTable.getDefaultCell(),
                            DeliveriesColumnAlignment.parseString(alignment));

                    productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));
                }
            }

            document.add(productsTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForOrders,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate("deliveries.order.report.orderedProducts.title", locale),
                FontUtils.getDejavuBold11Dark()));

        List<String> productsHeader = new ArrayList<String>();

        for (Entity columnForOrders : columnsForOrders) {
            String name = columnForOrders.getStringField(NAME);

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
        document.addTitle(translationService.translate("deliveries.order.report.title", locale));
    }

}
