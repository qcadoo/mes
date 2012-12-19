/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.ALIGNMENT;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.IDENTIFIER;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.NAME;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
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

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved delivery! (missing id)");

        String documentTitle = translationService.translate("deliveries.delivery.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        createHeaderTable(document, delivery, locale);
        createProductsTable(document, delivery, locale);

        return translationService.translate("deliveries.delivery.report.fileName", locale, delivery.getStringField(NUMBER),
                getStringFromDate((Date) delivery.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Entity delivery, final Locale locale) throws DocumentException {
        PdfPTable headerTable = pdfHelper.createPanelTable(3);

        headerTable.setSpacingBefore(7);

        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.number", locale),
                delivery.getStringField(NUMBER));
        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.supplier", locale),
                (delivery.getBelongsToField(SUPPLIER) == null) ? "" : delivery.getBelongsToField(SUPPLIER).getStringField(NAME));
        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.state", locale),
                translationService.translate("deliveries.delivery.state.value." + delivery.getStringField(STATE), locale));

        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.name", locale),
                delivery.getStringField(NAME));
        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.deliveryDate", locale),
                getStringFromDate((Date) delivery.getField(DELIVERY_DATE)));
        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.createDate", locale),
                getStringFromDate((Date) delivery.getField("createDate")));

        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.description", locale),
                delivery.getStringField(DESCRIPTION));
        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.order.report.columnHeader.deliveryAddress", locale),
                (delivery.getStringField(DELIVERY_ADDRESS) == null) ? "" : delivery.getStringField(DELIVERY_ADDRESS));
        pdfHelper.addTableCellAsOneColumnTable(
                headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.createOrderDate", locale),
                (getPrepareOrderDate(delivery) == null ? "" : getStringFromDate((Date) getPrepareOrderDate(delivery).getField(
                        "dateAndTime"))));
        pdfHelper.addTableCellAsOneColumnTable(
                headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.receivedOrderDate", locale),
                (getReceivedOrderDate(delivery) == null ? "" : getStringFromDate((Date) getReceivedOrderDate(delivery).getField(
                        "dateAndTime"))));
        pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");

        document.add(headerTable);
        document.add(Chunk.NEWLINE);
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForDeliveries = deliveriesService.getColumnsForDeliveries();

        if (!columnsForDeliveries.isEmpty()) {
            PdfPTable productsTable = pdfHelper.createTableWithHeader(columnsForDeliveries.size(),
                    prepareProductsTableHeader(document, columnsForDeliveries, locale), false);

            List<DeliveryProduct> productWithDeliveryProducts = deliveryColumnFetcher.getProductWithDeliveryProducts(delivery);

            Map<DeliveryProduct, Map<String, String>> deliveryProductsColumnValues = deliveryColumnFetcher
                    .getDeliveryProductsColumnValues(productWithDeliveryProducts);

            for (DeliveryProduct deliveryProduct : productWithDeliveryProducts) {

                for (Entity columnForDeliveries : columnsForDeliveries) {
                    String identifier = columnForDeliveries.getStringField(IDENTIFIER);
                    String alignment = columnForDeliveries.getStringField(ALIGNMENT);

                    String value = deliveryProductsColumnValues.get(deliveryProduct).get(identifier);

                    prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                    productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));
                }
            }

            document.add(productsTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForDeliveries,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate("deliveries.delivery.report.deliveryProducts.title", locale),
                FontUtils.getDejavuBold11Dark()));

        List<String> productsHeader = new ArrayList<String>();

        for (Entity columnForDeliveries : columnsForDeliveries) {
            String name = columnForDeliveries.getStringField(NAME);

            productsHeader.add(translationService.translate(name, locale));
        }

        return productsHeader;
    }

    private void prepareProductColumnAlignment(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    private String getStringFromDate(final Date date) {
        return simpleDateFormat.format(date);
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("deliveries.delivery.report.title", locale));
    }

    private Entity getPrepareOrderDate(final Entity delivery) {
        return delivery.getHasManyField("stateChanges").find().add(SearchRestrictions.eq("targetState", "02prepared"))
                .add(SearchRestrictions.eq("status", "03successful")).uniqueResult();
    }

    private Entity getReceivedOrderDate(final Entity delivery) {
        return delivery.getHasManyField("stateChanges").find().add(SearchRestrictions.eq("targetState", "06received"))
                .add(SearchRestrictions.eq("status", "03successful")).uniqueResult();
    }

}
