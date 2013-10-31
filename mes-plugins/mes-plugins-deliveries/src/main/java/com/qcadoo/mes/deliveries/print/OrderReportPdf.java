/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import static com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields.ALIGNMENT;
import static com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields.IDENTIFIER;
import static com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields.NAME;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.util.DeliveryPricesAndQuantities;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ColorUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "orderReportPdf")
public class OrderReportPdf extends ReportPdfView {

    private static final Integer REPORT_WIDTH = 515;

    private static final String L_CURRENCY = "currency";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private OrderColumnFetcher orderColumnFetcher;

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved delivery! (missing id)");

        String documentTitle = translationService.translate("deliveries.order.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        createHeaderTable(document, delivery, locale);
        createProductsTable(document, delivery, locale);

        return translationService.translate("deliveries.order.report.fileName", locale, delivery.getStringField(NUMBER),
                getStringFromDate((Date) delivery.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Entity delivery, final Locale locale) throws DocumentException {
        PdfPTable dynaminHeaderTable = pdfHelper.createPanelTable(3);
        dynaminHeaderTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable firstColumnHeaderTable = new PdfPTable(1);
        PdfPTable secondColumnHeaderTable = new PdfPTable(1);
        PdfPTable thirdColumnHeaderTable = new PdfPTable(1);

        setSimpleFormat(firstColumnHeaderTable);
        setSimpleFormat(secondColumnHeaderTable);
        setSimpleFormat(thirdColumnHeaderTable);

        dynaminHeaderTable.setSpacingBefore(7);

        Map<String, Object> firstColumn = createFirstColumn(delivery);
        Map<String, Object> secondColumn = createSecondColumn(delivery);
        Map<String, Object> thirdColumn = createThirdColumn(delivery);

        int maxSize = pdfHelper.getMaxSizeOfColumnsRows(Lists.newArrayList(Integer.valueOf(firstColumn.values().size()),
                Integer.valueOf(secondColumn.values().size()), Integer.valueOf(thirdColumn.values().size())));

        for (int i = 0; i < maxSize; i++) {
            firstColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(firstColumnHeaderTable, firstColumn, locale);
            secondColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(secondColumnHeaderTable, secondColumn, locale);
            thirdColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(thirdColumnHeaderTable, thirdColumn, locale);
        }

        dynaminHeaderTable.addCell(firstColumnHeaderTable);
        dynaminHeaderTable.addCell(secondColumnHeaderTable);
        dynaminHeaderTable.addCell(thirdColumnHeaderTable);

        document.add(dynaminHeaderTable);
        document.add(Chunk.NEWLINE);
    }

    private void setSimpleFormat(final PdfPTable headerTable) {
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        headerTable.getDefaultCell().setPadding(6.0f);
        headerTable.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);
    }

    private Map<String, Object> createFirstColumn(final Entity delivery) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (delivery.getStringField(NUMBER) != null) {
            column.put("deliveries.delivery.report.columnHeader.number", delivery.getStringField(NUMBER));
        }
        if (delivery.getStringField(NAME) != null) {
            column.put("deliveries.delivery.report.columnHeader.name", delivery.getStringField(NAME));
        }
        if (delivery.getStringField(DESCRIPTION) != null) {
            column.put("deliveries.delivery.report.columnHeader.description", delivery.getStringField(DESCRIPTION));
        }
        return column;
    }

    private Map<String, Object> createSecondColumn(final Entity delivery) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (companyService.getCompany() != null) {
            column.put("deliveries.order.report.columnHeader.contracting", companyService.getCompany().getStringField(NAME));
        }
        if (delivery.getStringField(DELIVERY_ADDRESS) != null) {
            column.put("deliveries.order.report.columnHeader.deliveryAddress", delivery.getStringField(DELIVERY_ADDRESS));
        }
        return column;
    }

    private Map<String, Object> createThirdColumn(final Entity delivery) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (delivery.getBelongsToField(SUPPLIER) != null) {
            column.put("deliveries.order.report.columnHeader.supplier", delivery.getBelongsToField(SUPPLIER).getStringField(NAME));
        }
        if (delivery.getField(DELIVERY_DATE) != null) {
            column.put("deliveries.order.report.columnHeader.deliveryDate",
                    getStringFromDate((Date) delivery.getField(DELIVERY_DATE)));
        }
        if (getPrepareOrderDate(delivery) != null) {
            column.put("deliveries.delivery.report.columnHeader.createOrderDate",
                    getStringFromDate((Date) getPrepareOrderDate(delivery).getField("dateAndTime")));
        }
        if (StringUtils.isNotEmpty(delivery.getBelongsToField(SUPPLIER).getStringField(CompanyFieldsD.PAYMENT_FORM))) {
            column.put("deliveries.delivery.report.columnHeader.paymentForm", delivery.getBelongsToField(SUPPLIER)
                    .getStringField(CompanyFieldsD.PAYMENT_FORM));
        }
        return column;
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForOrders = deliveriesService.getColumnsForOrders();

        if (!columnsForOrders.isEmpty()) {
            List<Entity> orderedProducts = delivery.getHasManyField(ORDERED_PRODUCTS);

            Map<Entity, Map<String, String>> orderedProductsColumnValues = orderColumnFetcher
                    .getOrderedProductsColumnValues(orderedProducts);

            List<Entity> filteredColumnsForOrders = getOrderReportColumns(columnsForOrders, orderedProducts,
                    orderedProductsColumnValues);

            if (!filteredColumnsForOrders.isEmpty()) {
                List<String> columnsName = Lists.newArrayList();

                for (Entity entity : filteredColumnsForOrders) {
                    columnsName.add(entity.getStringField(IDENTIFIER));
                }

                PdfPTable productsTable = pdfHelper.createTableWithHeader(filteredColumnsForOrders.size(),
                        prepareProductsTableHeader(document, filteredColumnsForOrders, locale), false,
                        pdfHelper.getReportColumnWidths(REPORT_WIDTH, parameterService.getReportColumnWidths(), columnsName),
                        HeaderAlignment.CENTER);

                for (Entity orderedProduct : orderedProducts) {
                    for (Entity columnForOrders : filteredColumnsForOrders) {
                        String identifier = columnForOrders.getStringField(IDENTIFIER);
                        String alignment = columnForOrders.getStringField(ALIGNMENT);

                        String value = orderedProductsColumnValues.get(orderedProduct).get(identifier);

                        prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                        productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                    }
                }

                addTotalRow(productsTable, locale, columnsName, delivery);

                document.add(productsTable);
                document.add(Chunk.NEWLINE);
            }
        }
    }

    private List<Entity> getOrderReportColumns(final List<Entity> columnsForOrders, final List<Entity> orderedProducts,
            final Map<Entity, Map<String, String>> orderedProductsColumnValues) {
        return deliveriesService.getColumnsWithFilteredCurrencies(columnExtensionService.filterEmptyColumns(columnsForOrders,
                orderedProducts, orderedProductsColumnValues));
    }

    private void addTotalRow(final PdfPTable productsTable, final Locale locale, final List<String> columnsName, Entity delivery) {
        DeliveryPricesAndQuantities pricesAndQntts = new DeliveryPricesAndQuantities(delivery, numberService);

        PdfPCell total = new PdfPCell(new Phrase(translationService.translate("deliveries.delivery.report.totalCost", locale),
                FontUtils.getDejavuRegular7Dark()));

        total.setColspan(2);
        total.setHorizontalAlignment(Element.ALIGN_LEFT);
        total.setBackgroundColor(null);
        total.disableBorderSide(Rectangle.RIGHT);
        total.disableBorderSide(Rectangle.LEFT);
        total.setBorderColor(ColorUtils.getLineLightColor());

        productsTable.addCell(total);

        for (int i = 2; i < columnsName.size(); i++) {
            if (columnsName.contains(OrderedProductFields.ORDERED_QUANTITY)
                    && columnsName.indexOf(OrderedProductFields.ORDERED_QUANTITY) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(new Phrase(numberService.format(pricesAndQntts.getOrderedCumulatedQuantity()), FontUtils
                        .getDejavuRegular7Dark()));
            } else if (columnsName.contains(OrderedProductFields.TOTAL_PRICE)
                    && columnsName.indexOf(OrderedProductFields.TOTAL_PRICE) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(new Phrase(numberService.format(pricesAndQntts.getOrderedTotalPrice()), FontUtils
                        .getDejavuRegular7Dark()));
            } else if (columnsName.contains(L_CURRENCY) && columnsName.indexOf(L_CURRENCY) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                productsTable.addCell(new Phrase(deliveriesService.getCurrency(delivery), FontUtils.getDejavuRegular7Dark()));
            } else {
                productsTable.addCell("");
            }
        }
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForOrders,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate("deliveries.order.report.orderedProducts.title", locale),
                FontUtils.getDejavuBold10Dark()));

        List<String> productsHeader = Lists.newArrayList();

        for (Entity columnForOrders : columnsForOrders) {
            String name = columnForOrders.getStringField(NAME);

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
        document.addTitle(translationService.translate("deliveries.order.report.title", locale));
    }

    private Entity getPrepareOrderDate(final Entity delivery) {
        return delivery.getHasManyField("stateChanges").find().add(SearchRestrictions.eq("targetState", "02prepared"))
                .add(SearchRestrictions.eq("status", "03successful")).uniqueResult();
    }

}
