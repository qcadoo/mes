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
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.columnExtension.utils.ColumnSuccessionComparator;
import com.qcadoo.mes.deliveries.DeliveriesService;
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

        String documentTitle = translationService.translate("deliveries.delivery.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper
                .addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), securityService.getCurrentUserName());

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        createHeaderTable(document, delivery, locale);
        createProductsTable(document, delivery, locale);

        String endOfPrint = translationService.translate("qcadooReport.commons.endOfPrint.label", locale);

        pdfHelper.addEndOfDocument(document, writer, endOfPrint);

        return translationService.translate("deliveries.delivery.report.fileName", locale, delivery.getStringField(NUMBER),
                DateUtils.REPORT_D_T_F.format((Date) delivery.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Entity delivery, final Locale locale) throws DocumentException {
        PdfPTable headerTable = pdfHelper.createPanelTable(3);

        headerTable.setSpacingBefore(7);

        pdfHelper.addTableCellAsOneColumnTable(headerTable,
                translationService.translate("deliveries.delivery.report.columnHeader.number", locale),
                delivery.getStringField(NUMBER));
        if (delivery.getStringField(NAME) == null) {
            pdfHelper.addTableCellAsOneColumnTable(
                    headerTable,
                    translationService.translate("deliveries.delivery.report.columnHeader.supplier", locale),
                    (delivery.getBelongsToField(SUPPLIER) == null) ? "" : delivery.getBelongsToField(SUPPLIER).getStringField(
                            NAME));
        } else {
            pdfHelper.addTableCellAsOneColumnTable(headerTable,
                    translationService.translate("deliveries.delivery.report.columnHeader.name", locale),
                    delivery.getStringField(NAME));
        }
        if (delivery.getStringField(DESCRIPTION) == null) {
            pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        } else {
            pdfHelper.addTableCellAsOneColumnTable(headerTable,
                    translationService.translate("deliveries.delivery.report.columnHeader.description", locale),
                    delivery.getStringField(DESCRIPTION));
        }

        if (delivery.getField(DELIVERY_DATE) == null) {
            pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        } else {
            pdfHelper.addTableCellAsOneColumnTable(headerTable,
                    translationService.translate("deliveries.delivery.report.columnHeader.deliveryDate", locale),
                    new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, locale).format((Date) delivery.getField(DELIVERY_DATE)));
        }
        if (delivery.getStringField(NAME) == null) {
            pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");
        } else {
            pdfHelper.addTableCellAsOneColumnTable(
                    headerTable,
                    translationService.translate("deliveries.delivery.report.columnHeader.supplier", locale),
                    (delivery.getBelongsToField(SUPPLIER) == null) ? "" : delivery.getBelongsToField(SUPPLIER).getStringField(
                            NAME));
        }
        pdfHelper.addTableCellAsOneColumnTable(headerTable, "", "");

        document.add(headerTable);
        document.add(Chunk.NEWLINE);
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForDeliveries = deliveriesService.getColumnsForDeliveries();

        Collections.sort(columnsForDeliveries, new ColumnSuccessionComparator());

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

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("deliveries.delivery.report.title", locale));
    }

}
