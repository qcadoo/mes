/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.supplyNegotiations.print;

import static com.google.common.base.Preconditions.checkState;
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.IDENTIFIER;
import static com.qcadoo.mes.supplyNegotiations.constants.ColumnForOffersFields.ALIGNMENT;
import static com.qcadoo.mes.supplyNegotiations.constants.ColumnForOffersFields.NAME;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.FARTHEST_LIMIT_DATE;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferFields.*;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.DESIRED_DATE;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields;
import com.qcadoo.mes.supplyNegotiations.util.OfferPricesAndQuantities;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.ColorUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "offerReportPdf")
public class OfferReportPdf extends ReportPdfView {

    private static final Integer REPORT_WIDTH = 515;

    private static final String L_CURRENCY = "currency";

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private OfferColumnFetcher offerColumnFetcher;

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private NumberService numberService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved offer! (missing id)");

        String documentTitle = translationService.translate("supplyNegotiations.offer.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        Long offerId = Long.valueOf(model.get("id").toString());

        Entity offer = supplyNegotiationsService.getOffer(offerId);

        createDynamicHeaderTable(document, offer, locale);
        createProductsTable(document, offer, locale);

        return translationService.translate("supplyNegotiations.offer.report.fileName", locale, offer.getStringField(NUMBER),
                getStringFromDate((Date) offer.getField("updateDate")));
    }

    private void createDynamicHeaderTable(final Document document, final Entity offer, final Locale locale)
            throws DocumentException {
        PdfPTable dynaminHeaderTable = pdfHelper.createPanelTable(3);
        dynaminHeaderTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable firstColumnHeaderTable = new PdfPTable(1);
        PdfPTable secondColumnHeaderTable = new PdfPTable(1);
        PdfPTable thirdColumnHeaderTable = new PdfPTable(1);

        setSimpleFormat(firstColumnHeaderTable);
        setSimpleFormat(secondColumnHeaderTable);
        setSimpleFormat(thirdColumnHeaderTable);

        dynaminHeaderTable.setSpacingBefore(7);

        Map<String, Object> firstColumn = createFirstColumn(offer);
        Map<String, Object> secondColumn = createSecondColumn(offer, locale);
        Map<String, Object> thirdColumn = createThirdColumn(offer);

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

    private Map<String, Object> createFirstColumn(final Entity offer) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (offer.getStringField(NUMBER) != null) {
            column.put("supplyNegotiations.offer.report.columnHeader.number", offer.getStringField(NUMBER));
        }
        if (offer.getStringField(NAME) != null) {
            column.put("supplyNegotiations.offer.report.columnHeader.name", offer.getStringField(NAME));
        }
        if (offer.getStringField(DESCRIPTION) != null) {
            column.put("supplyNegotiations.offer.report.columnHeader.description", offer.getStringField(DESCRIPTION));
        }
        return column;
    }

    private Map<String, Object> createSecondColumn(final Entity offer, final Locale locale) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (offer.getBelongsToField(SUPPLIER) != null) {
            column.put("supplyNegotiations.offer.report.columnHeader.supplier",
                    offer.getBelongsToField(SUPPLIER).getStringField(NAME));
        }
        if ((offer.getField(OFFERED_DATE) != null) || (offer.getField(WORKING_DAYS_AFTER_ORDER) != null)) {
            column.put("supplyNegotiations.offer.report.columnHeader.offeredDate",
                    getOfferedDateOrWorkingDaysAfterOrder(offer, locale));
        }
        if (offer.getBelongsToField(REQUEST_FOR_QUOTATION) != null) {
            column.put("supplyNegotiations.offer.report.columnHeader.requestForQuotationNumber",
                    offer.getBelongsToField(REQUEST_FOR_QUOTATION).getStringField(RequestForQuotationFields.NUMBER));
        }
        if (offer.getBelongsToField(NEGOTIATION) != null) {
            column.put("supplyNegotiations.offer.report.columnHeader.negotiationNumber", offer.getBelongsToField(NEGOTIATION)
                    .getStringField(NegotiationFields.NUMBER));
        }
        return column;
    }

    private Map<String, Object> createThirdColumn(final Entity offer) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();

        if ((offer.getBelongsToField(SUPPLIER) != null)
                && (offer.getBelongsToField(SUPPLIER).getIntegerField(CompanyFieldsD.BUFFER) != null)) {
            column.put("supplyNegotiations.offer.report.columnHeader.supplier.buffer", offer.getBelongsToField(SUPPLIER)
                    .getIntegerField(CompanyFieldsD.BUFFER));
        }

        if ((offer.getField(REQUEST_FOR_QUOTATION) != null)
                && (offer.getBelongsToField(REQUEST_FOR_QUOTATION).getField(DESIRED_DATE) != null)) {
            column.put("supplyNegotiations.offer.report.columnHeader.desiredDate", getStringFromDate((Date) offer
                    .getBelongsToField(REQUEST_FOR_QUOTATION).getField(DESIRED_DATE)));
        }

        if ((offer.getBelongsToField(NEGOTIATION) != null)
                && (offer.getBelongsToField(NEGOTIATION).getField(FARTHEST_LIMIT_DATE) != null)) {
            column.put("supplyNegotiations.offer.report.columnHeader.negotiationDate", getStringFromDate((Date) offer
                    .getBelongsToField(NEGOTIATION).getField(FARTHEST_LIMIT_DATE)));
        }

        return column;
    }

    private void createProductsTable(final Document document, final Entity offer, final Locale locale) throws DocumentException {
        List<Entity> columnsForOffers = supplyNegotiationsService.getColumnsForOffers();

        if (!columnsForOffers.isEmpty()) {
            List<Entity> offerProducts = offer.getHasManyField(OFFER_PRODUCTS);

            Map<Entity, Map<String, String>> offerProductsColumnValues = offerColumnFetcher
                    .getOfferProductsColumnValues(offerProducts);

            List<Entity> filteredColumnsForOffers = getOfferReportColumns(columnsForOffers, offerProducts,
                    offerProductsColumnValues);

            if (!filteredColumnsForOffers.isEmpty()) {
                List<String> columnsName = Lists.newArrayList();

                for (Entity entity : filteredColumnsForOffers) {
                    columnsName.add(entity.getStringField(IDENTIFIER));
                }

                Map<String, HeaderAlignment> alignments = prepareHeaderAlignment(filteredColumnsForOffers, locale);

                PdfPTable productsTable = pdfHelper.createTableWithHeader(filteredColumnsForOffers.size(),
                        prepareProductsTableHeader(document, filteredColumnsForOffers, locale, offer), false,
                        pdfHelper.getReportColumnWidths(REPORT_WIDTH, parameterService.getReportColumnWidths(), columnsName),
                        alignments);

                for (Entity offerProduct : offerProducts) {
                    for (Entity columnForRequests : filteredColumnsForOffers) {
                        String identifier = columnForRequests.getStringField(IDENTIFIER);
                        String alignment = columnForRequests.getStringField(ALIGNMENT);

                        String value = offerProductsColumnValues.get(offerProduct).get(identifier);

                        prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                        productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                    }
                }

                addTotalRow(productsTable, locale, columnsName, offer);

                document.add(productsTable);
                document.add(Chunk.NEWLINE);
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

    private List<Entity> getOfferReportColumns(final List<Entity> columnsForOffers, final List<Entity> offerProducts,
            final Map<Entity, Map<String, String>> offerProductsColumnValues) {
        return columnExtensionService.filterEmptyColumns(columnsForOffers, offerProducts, offerProductsColumnValues);
    }

    private void addTotalRow(final PdfPTable productsTable, final Locale locale, final List<String> columnsName, Entity offer) {
        OfferPricesAndQuantities pricesAndQntts = new OfferPricesAndQuantities(offer, numberService);

        PdfPCell total = new PdfPCell(new Phrase(
                translationService.translate("supplyNegotiations.offer.report.totalCost", locale),
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
            if (columnsName.contains(OfferProductFields.QUANTITY) && columnsName.indexOf(OfferProductFields.QUANTITY) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(new Phrase(numberService.format(pricesAndQntts.getOfferCumulatedQuantity()), FontUtils
                        .getDejavuRegular7Dark()));
            } else if (columnsName.contains(OfferProductFields.TOTAL_PRICE)
                    && columnsName.indexOf(OfferProductFields.TOTAL_PRICE) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(new Phrase(numberService.format(pricesAndQntts.getOfferTotalPrice()), FontUtils
                        .getDejavuRegular7Dark()));
            } else if (columnsName.contains(L_CURRENCY) && columnsName.indexOf(L_CURRENCY) == i) {
                productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                productsTable.addCell(new Phrase(currencyService.getCurrencyAlphabeticCode(), FontUtils.getDejavuRegular7Dark()));
            } else {
                productsTable.addCell("");
            }
        }
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForOffers,
            final Locale locale, final Entity offer) throws DocumentException {
        document.add(new Paragraph(translationService.translate("supplyNegotiations.offer.report.offerProducts.title", locale),
                FontUtils.getDejavuBold11Dark()));

        BigDecimal transportCost = offer.getDecimalField(TRANSPORT_COST);
        if (transportCost != null) {
            String costLine = translationService.translate("supplyNegotiations.offer.report.transportCost", locale,
                    numberService.format(transportCost), currencyService.getCurrencyAlphabeticCode());
            document.add(new Paragraph(costLine, FontUtils.getDejavuRegular10Dark()));
        }

        List<String> productsHeader = Lists.newArrayList();

        for (Entity columnForOffer : columnsForOffers) {
            String name = columnForOffer.getStringField(NAME);

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

    private String getOfferedDateOrWorkingDaysAfterOrder(final Entity offer, final Locale locale) {
        StringBuffer offeredDate = new StringBuffer();
        boolean desiredDateExists = offer.getField(OFFERED_DATE) != null;
        boolean workingDaysExists = offer.getField(WORKING_DAYS_AFTER_ORDER) != null;
        if (desiredDateExists) {
            offeredDate.append(translationService.translate(
                    "supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate.toDate", locale));
            offeredDate.append(" ");
            offeredDate.append(getStringFromDate((Date) offer.getField(OFFERED_DATE)));
        }
        if (desiredDateExists && workingDaysExists) {
            offeredDate.append(" ");
            offeredDate.append(translationService.translate(
                    "supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate.or", locale));
            offeredDate.append(" ");
        }
        if (workingDaysExists) {
            offeredDate.append(offer.getField(WORKING_DAYS_AFTER_ORDER));
            offeredDate.append(" ");
            offeredDate.append(translationService.translate(
                    "supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate.workingDaysAfterOrder", locale));
        }
        return offeredDate.toString();
    }

    @Override
    protected final void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("supplyNegotiations.offer.report.title", locale));
    }

    public List<String> getUsedColumnsInOfferReport(final Entity offer) {
        List<Entity> columnsForOffers = supplyNegotiationsService.getColumnsForOffers();
        List<String> columnsName = Lists.newArrayList();
        if (!columnsForOffers.isEmpty()) {
            List<Entity> offerProducts = offer.getHasManyField(OFFER_PRODUCTS);

            Map<Entity, Map<String, String>> offerProductsColumnValues = offerColumnFetcher
                    .getOfferProductsColumnValues(offerProducts);

            List<Entity> filteredColumnsForOffers = getOfferReportColumns(columnsForOffers, offerProducts,
                    offerProductsColumnValues);

            if (!filteredColumnsForOffers.isEmpty()) {

                for (Entity entity : filteredColumnsForOffers) {
                    columnsName.add(entity.getStringField(IDENTIFIER));
                }
            }
        }
        return columnsName;
    }

}
