/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
import com.google.common.collect.Maps;
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
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields;
import com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeFields;
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
    private ParameterService parameterService;

    @Autowired
    private CompanyService companyService;

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

        return translationService.translate("deliveries.order.report.fileName", locale,
                delivery.getStringField(DeliveryFields.NUMBER), getStringFromDate(delivery.getDateField("updateDate")));
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

        String number = delivery.getStringField(DeliveryFields.NUMBER);
        String name = delivery.getStringField(DeliveryFields.NAME);
        String description = delivery.getStringField(DeliveryFields.DESCRIPTION);

        if (number != null) {
            column.put("deliveries.delivery.report.columnHeader.number", number);
        }
        if (name != null) {
            column.put("deliveries.delivery.report.columnHeader.name", name);
        }
        if (description != null) {
            column.put("deliveries.delivery.report.columnHeader.description", description);
        }

        return column;
    }

    private Map<String, Object> createSecondColumn(final Entity delivery) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();

        Entity company = companyService.getCompany();
        String deliveryAddress = delivery.getStringField(DeliveryFields.DELIVERY_ADDRESS);
        Date deliveryDate = delivery.getDateField(DeliveryFields.DELIVERY_DATE);

        if (company != null) {
            String name = company.getStringField(CompanyFields.NAME);

            column.put("deliveries.order.report.columnHeader.contracting", name);
        }
        if (deliveryAddress != null) {
            column.put("deliveries.order.report.columnHeader.deliveryAddress", deliveryAddress);
        }
        if (deliveryDate != null) {
            column.put("deliveries.order.report.columnHeader.deliveryDate", getStringFromDate(deliveryDate));
        }

        return column;
    }

    private Map<String, Object> createThirdColumn(final Entity delivery) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();

        Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);
        String supplierAddress = deliveriesService.generateAddressFromCompany(supplier);
        Entity prepareOrderDate = getPrepareOrderDate(delivery);

        if (supplier != null) {
            String name = supplier.getStringField(CompanyFields.NAME);

            column.put("deliveries.order.report.columnHeader.supplier", name);
        }
        if (StringUtils.isNotEmpty(supplierAddress)) {
            column.put("deliveries.order.report.columnHeader.supplierAddress", supplierAddress);
        }
        if (prepareOrderDate != null) {
            column.put("deliveries.delivery.report.columnHeader.createOrderDate",
                    getStringFromDate(prepareOrderDate.getDateField(DeliveryStateChangeFields.DATE_AND_TIME)));
        }
        if (supplier != null) {
            String paymentForm = supplier.getStringField(CompanyFieldsD.PAYMENT_FORM);

            if (StringUtils.isNotEmpty(paymentForm)) {
                column.put("deliveries.delivery.report.columnHeader.paymentForm", paymentForm);
            }
        }

        return column;
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForOrders = deliveriesService.getColumnsForOrders();

        if (!columnsForOrders.isEmpty()) {
            List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

            Map<Entity, Map<String, String>> orderedProductsColumnValues = orderColumnFetcher
                    .getOrderedProductsColumnValues(orderedProducts);

            List<Entity> filteredColumnsForOrders = getOrderReportColumns(columnsForOrders, orderedProducts,
                    orderedProductsColumnValues);

            if (!filteredColumnsForOrders.isEmpty()) {
                List<String> columnsName = Lists.newArrayList();

                for (Entity entity : filteredColumnsForOrders) {
                    columnsName.add(entity.getStringField(ColumnForOrdersFields.IDENTIFIER));
                }

                Map<String, HeaderAlignment> alignments = prepareHeaderAlignment(filteredColumnsForOrders, locale);
                PdfPTable productsTable = pdfHelper.createTableWithHeader(filteredColumnsForOrders.size(),
                        prepareProductsTableHeader(document, filteredColumnsForOrders, locale), false,
                        pdfHelper.getReportColumnWidths(REPORT_WIDTH, parameterService.getReportColumnWidths(), columnsName),
                        alignments);

                for (Entity orderedProduct : orderedProducts) {
                    for (Entity columnForOrders : filteredColumnsForOrders) {
                        String identifier = columnForOrders.getStringField(ColumnForOrdersFields.IDENTIFIER);
                        String alignment = columnForOrders.getStringField(ColumnForOrdersFields.ALIGNMENT);

                        String value = orderedProductsColumnValues.get(orderedProduct).get(identifier);

                        prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                        productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                    }
                }

                addTotalRow(productsTable, locale, columnsName, delivery);

                document.add(productsTable);
                // document.add(Chunk.NEWLINE);
            }
        }
    }

    private Map<String, HeaderAlignment> prepareHeaderAlignment(List<Entity> filteredColumnsForDeliveries, Locale locale) {
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();
        for (Entity column : filteredColumnsForDeliveries) {
            String alignment = column.getStringField(ColumnForDeliveriesFields.ALIGNMENT);
            HeaderAlignment headerAlignment = HeaderAlignment.RIGHT;
            if (ColumnAlignment.LEFT.equals(ColumnAlignment.parseString(alignment))) {
                headerAlignment = HeaderAlignment.LEFT;
            } else if (ColumnAlignment.RIGHT.equals(ColumnAlignment.parseString(alignment))) {
                headerAlignment = HeaderAlignment.RIGHT;
            }
            alignments.put(prepareHeaderTranslation(column.getStringField(ColumnForDeliveriesFields.NAME), locale),
                    headerAlignment);
        }
        return alignments;
    }

    private String prepareHeaderTranslation(final String name, final Locale locale) {
        String translatedName = translationService.translate(name, locale);
        return translatedName;
    }

    private List<Entity> getOrderReportColumns(final List<Entity> columnsForOrders, final List<Entity> orderedProducts,
            final Map<Entity, Map<String, String>> orderedProductsColumnValues) {
        return deliveriesService.getColumnsWithFilteredCurrencies(columnExtensionService.filterEmptyColumns(columnsForOrders,
                orderedProducts, orderedProductsColumnValues));
    }

    private void addTotalRow(final PdfPTable productsTable, final Locale locale, final List<String> columnsName, Entity delivery) {
        DeliveryPricesAndQuantities deliveryPricesAndQuantities = new DeliveryPricesAndQuantities(delivery, numberService);

        PdfPCell total = new PdfPCell(new Phrase(translationService.translate("deliveries.delivery.report.totalCost", locale),
                FontUtils.getDejavuRegular7Dark()));

        total.setColspan(2);
        total.setHorizontalAlignment(Element.ALIGN_LEFT);
        total.setVerticalAlignment(Element.ALIGN_MIDDLE);
        total.setBackgroundColor(null);
        total.disableBorderSide(Rectangle.RIGHT);
        total.disableBorderSide(Rectangle.LEFT);
        total.setBorderColor(ColorUtils.getLineLightColor());

        productsTable.addCell(total);

        for (int i = 2; i < columnsName.size(); i++) {
            if (columnsName.contains(OrderedProductFields.ORDERED_QUANTITY)
                    && columnsName.indexOf(OrderedProductFields.ORDERED_QUANTITY) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(new Phrase(numberService.format(deliveryPricesAndQuantities.getOrderedCumulatedQuantity()),
                        FontUtils.getDejavuRegular7Dark()));
            } else if (columnsName.contains(OrderedProductFields.TOTAL_PRICE)
                    && columnsName.indexOf(OrderedProductFields.TOTAL_PRICE) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(new Phrase(numberService.format(deliveryPricesAndQuantities.getOrderedTotalPrice()),
                        FontUtils.getDejavuRegular7Dark()));
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
            String name = columnForOrders.getStringField(ColumnForOrdersFields.NAME);

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
        document.addTitle(translationService.translate("deliveries.order.report.title", locale));
    }

    private Entity getPrepareOrderDate(final Entity delivery) {
        return delivery.getHasManyField(DeliveryFields.STATE_CHANGES).find()
                .add(SearchRestrictions.eq(DeliveryStateChangeFields.TARGET_STATE, "02prepared"))
                .add(SearchRestrictions.eq(DeliveryStateChangeFields.STATUS, "03successful")).setMaxResults(1).uniqueResult();
    }

    private String getStringFromDate(final Date date) {
        return simpleDateFormat.format(date);
    }

    public List<String> getUsedColumnsInOrderReport(final Entity delivery) {
        List<Entity> columnsForOrders = deliveriesService.getColumnsForOrders();
        List<String> columnNames = Lists.newArrayList();
        if (!columnsForOrders.isEmpty()) {
            List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

            Map<Entity, Map<String, String>> orderedProductsColumnValues = orderColumnFetcher
                    .getOrderedProductsColumnValues(orderedProducts);

            List<Entity> filteredColumnsForOrders = getOrderReportColumns(columnsForOrders, orderedProducts,
                    orderedProductsColumnValues);
            if (!filteredColumnsForOrders.isEmpty()) {

                for (Entity entity : filteredColumnsForOrders) {
                    columnNames.add(entity.getStringField(ColumnForOrdersFields.IDENTIFIER));
                }

            }
        }
        return columnNames;
    }

}
