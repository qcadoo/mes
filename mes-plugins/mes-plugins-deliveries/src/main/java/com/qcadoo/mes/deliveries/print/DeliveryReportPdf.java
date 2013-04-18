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
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "deliveryReportPdf")
public class DeliveryReportPdf extends ReportPdfView {

    private static final Integer REPORT_WIDTH = 515;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveryColumnFetcher deliveryColumnFetcher;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

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

        String documentTitle = translationService.translate("deliveries.delivery.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        Long deliveryId = Long.valueOf(model.get("id").toString());

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        createHeaderTable(document, delivery, locale);
        createProductsTable(document, delivery, locale);

        return translationService.translate("deliveries.delivery.report.fileName", locale, delivery.getStringField(NUMBER),
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
        Map<String, Object> thirdColumn = createThirdColumn(delivery, locale);

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
        if (delivery.getBelongsToField(SUPPLIER) != null) {
            column.put("deliveries.delivery.report.columnHeader.supplier",
                    delivery.getBelongsToField(SUPPLIER).getStringField(NAME));
        }
        if (delivery.getField(DELIVERY_DATE) != null) {
            column.put("deliveries.delivery.report.columnHeader.deliveryDate",
                    getStringFromDate((Date) delivery.getField(DELIVERY_DATE)));
        }
        if (delivery.getStringField(DELIVERY_ADDRESS) != null) {
            column.put("deliveries.order.report.columnHeader.deliveryAddress", delivery.getStringField(DELIVERY_ADDRESS));
        }
        return column;
    }

    private Map<String, Object> createThirdColumn(final Entity delivery, final Locale locale) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (delivery.getStringField(STATE) != null) {
            column.put("deliveries.delivery.report.columnHeader.state",
                    translationService.translate("deliveries.delivery.state.value." + delivery.getStringField(STATE), locale));
        }
        if (delivery.getField("createDate") != null) {
            column.put("deliveries.delivery.report.columnHeader.createDate",
                    getStringFromDate((Date) delivery.getField("createDate")));
        }
        if (getPrepareOrderDate(delivery) != null) {
            column.put("deliveries.delivery.report.columnHeader.createOrderDate",
                    getStringFromDate((Date) getPrepareOrderDate(delivery).getField("dateAndTime")));
        }
        if (getReceivedOrderDate(delivery) != null) {
            column.put("deliveries.delivery.report.columnHeader.receivedOrderDate",
                    getStringFromDate((Date) getReceivedOrderDate(delivery).getField("dateAndTime")));
        }
        return column;
    }

    private void createProductsTable(final Document document, final Entity delivery, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForDeliveries = deliveriesService.getColumnsForDeliveries();

        if (!columnsForDeliveries.isEmpty()) {
            List<DeliveryProduct> deliveryProducts = deliveryColumnFetcher.getDeliveryProducts(delivery);

            Map<DeliveryProduct, Map<String, String>> deliveryProductsColumnValues = deliveryColumnFetcher
                    .getDeliveryProductsColumnValues(deliveryProducts);

            List<Entity> filteredColumnsForDeliveries = filterEmptyColumns(columnsForDeliveries, deliveryProducts,
                    deliveryProductsColumnValues);

            if (!filteredColumnsForDeliveries.isEmpty()) {

                List<String> columnsName = new ArrayList<String>();
                for (Entity entity : filteredColumnsForDeliveries) {
                    columnsName.add(entity.getStringField(IDENTIFIER));
                }

                PdfPTable productsTable = pdfHelper.createTableWithHeader(filteredColumnsForDeliveries.size(),
                        prepareProductsTableHeader(document, filteredColumnsForDeliveries, locale), false,
                        pdfHelper.getReportColumnWidths(REPORT_WIDTH, parameterService.getReportColumnWidths(), columnsName),
                        HeaderAlignment.CENTER);

                for (DeliveryProduct deliveryProduct : deliveryProducts) {
                    for (Entity columnForDeliveries : filteredColumnsForDeliveries) {
                        String identifier = columnForDeliveries.getStringField(IDENTIFIER);
                        String alignment = columnForDeliveries.getStringField(ALIGNMENT);

                        String value = deliveryProductsColumnValues.get(deliveryProduct).get(identifier);

                        prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                        productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));
                    }
                }

                addTotalProductsCosts(deliveryProducts, filteredColumnsForDeliveries, deliveryProductsColumnValues,
                        productsTable, locale, columnsName);

                document.add(productsTable);
                document.add(Chunk.NEWLINE);
            }
        }
    }

    private void addTotalProductsCosts(final List<DeliveryProduct> deliveryProducts,
            final List<Entity> filteredColumnsForDeliveries,
            final Map<DeliveryProduct, Map<String, String>> deliveryProductsColumnValues, final PdfPTable productsTable,
            final Locale locale, final List<String> columnsName) {
        if (columnsName.contains(DeliveredProductFields.TOTAL_PRICE)) {
            BigDecimal totalProductsCosts = BigDecimal.ZERO;
            MathContext mc = numberService.getMathContext();
            for (DeliveryProduct deliveryProduct : deliveryProducts) {
                for (Entity columnForDeliveries : filteredColumnsForDeliveries) {
                    String identifier = columnForDeliveries.getStringField(IDENTIFIER);
                    String value = deliveryProductsColumnValues.get(deliveryProduct).get(identifier);

                    if (identifier.equals("totalPrice")) {
                        if (StringUtils.isNotEmpty(value)) {
                            BigDecimal totalPrice = new BigDecimal(value);
                            totalProductsCosts = totalProductsCosts.add(BigDecimalUtils.convertNullToZero(totalPrice), mc);
                        }
                    }
                }
            }
            productsTable.addCell(new Phrase(translationService.translate("deliveries.delivery.report.totalCost", locale),
                    FontUtils.getDejavuRegular9Dark()));
            int columnQuantity = columnsName.size() - 2;
            for (int i = 0; i < columnQuantity; i++) {
                productsTable.addCell("");
            }
            productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            productsTable.addCell(new Phrase(numberService.format(totalProductsCosts), FontUtils.getDejavuRegular9Dark()));
            productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

        }

    }

    private List<Entity> filterEmptyColumns(final List<Entity> columnsForDeliveries,
            final List<DeliveryProduct> productWithDeliveryProducts,
            final Map<DeliveryProduct, Map<String, String>> deliveryProductsColumnValues) {
        List<Entity> filteredColumns = Lists.newArrayList();

        for (Entity columnForDeliveries : columnsForDeliveries) {
            String identifier = columnForDeliveries.getStringField(IDENTIFIER);

            boolean isEmpty = true;

            for (DeliveryProduct deliveryProduct : productWithDeliveryProducts) {
                String value = deliveryProductsColumnValues.get(deliveryProduct).get(identifier);

                if (StringUtils.isNotEmpty(value)) {
                    isEmpty = false;

                    break;
                }
            }

            if (!isEmpty) {
                filteredColumns.add(columnForDeliveries);
            }
        }

        return filteredColumns;
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
