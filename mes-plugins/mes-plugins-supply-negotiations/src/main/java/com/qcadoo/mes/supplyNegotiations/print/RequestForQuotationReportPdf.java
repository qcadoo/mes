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
import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.NAME;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.supplyNegotiations.constants.ColumnForRequestsFields.ALIGNMENT;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferFields.WORKING_DAYS_AFTER_ORDER;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationFields.*;

import java.io.IOException;
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
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "requestsForQuotationReportPdf")
public class RequestForQuotationReportPdf extends ReportPdfView {

    private static final Integer REPORT_WIDTH = 515;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private RequestForQuotationColumnFetcher requestForQuotationColumnFetcher;

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ParameterService parameterService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
            LocaleContextHolder.getLocale());

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        checkState(model.get("id") != null, "Unable to generate report for unsaved requestForQuotation! (missing id)");

        String documentTitle = translationService.translate("supplyNegotiations.requestForQuotation.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date());

        Long requestForQuotationId = Long.valueOf(model.get("id").toString());

        Entity requestForQuotation = supplyNegotiationsService.getRequestForQuotation(requestForQuotationId);

        createHeaderTable(document, requestForQuotation, locale);
        createProductsTable(document, requestForQuotation, locale);

        return translationService.translate("supplyNegotiations.requestForQuotation.report.fileName", locale,
                requestForQuotation.getStringField(NUMBER), getStringFromDate((Date) requestForQuotation.getField("updateDate")));
    }

    private void createHeaderTable(final Document document, final Entity requestForQuotation, final Locale locale)
            throws DocumentException {
        PdfPTable dynaminHeaderTable = pdfHelper.createPanelTable(4);

        dynaminHeaderTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable firstColumnHeaderTable = new PdfPTable(1);
        PdfPTable secondColumnHeaderTable = new PdfPTable(1);
        PdfPTable thirdColumnHeaderTable = new PdfPTable(1);
        PdfPTable fourthColumnHeaderTable = new PdfPTable(1);

        setSimpleFormat(firstColumnHeaderTable);
        setSimpleFormat(secondColumnHeaderTable);
        setSimpleFormat(thirdColumnHeaderTable);
        setSimpleFormat(fourthColumnHeaderTable);

        dynaminHeaderTable.setSpacingBefore(7);

        Map<String, Object> firstColumn = createFirstColumn(requestForQuotation);
        Map<String, Object> secondColumn = createSecondColumn(requestForQuotation);
        Map<String, Object> thirdColumn = createThirdColumn(requestForQuotation);
        Map<String, Object> fourthColumn = createFourthColumn(requestForQuotation, locale);

        int maxSize = pdfHelper.getMaxSizeOfColumnsRows(Lists.newArrayList(Integer.valueOf(firstColumn.values().size()),
                Integer.valueOf(secondColumn.values().size()), Integer.valueOf(thirdColumn.values().size()),
                Integer.valueOf(fourthColumn.values().size())));

        for (int i = 0; i < maxSize; i++) {
            firstColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(firstColumnHeaderTable, firstColumn, locale);
            secondColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(secondColumnHeaderTable, secondColumn, locale);
            thirdColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(thirdColumnHeaderTable, thirdColumn, locale);
            fourthColumnHeaderTable = pdfHelper.addDynamicHeaderTableCell(fourthColumnHeaderTable, fourthColumn, locale);
        }
        dynaminHeaderTable.addCell(firstColumnHeaderTable);
        dynaminHeaderTable.addCell(secondColumnHeaderTable);
        dynaminHeaderTable.addCell(thirdColumnHeaderTable);
        dynaminHeaderTable.addCell(fourthColumnHeaderTable);

        document.add(dynaminHeaderTable);
        document.add(Chunk.NEWLINE);
    }

    private void setSimpleFormat(final PdfPTable headerTable) {
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        headerTable.getDefaultCell().setPadding(6.0f);
        headerTable.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_TOP);
    }

    private Map<String, Object> createFirstColumn(final Entity requestForQuotation) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (requestForQuotation.getStringField(NUMBER) != null) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.number",
                    requestForQuotation.getStringField(NUMBER));
        }
        if (requestForQuotation.getStringField(NAME) != null) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.name",
                    requestForQuotation.getStringField(NAME));
        }
        return column;
    }

    private Map<String, Object> createSecondColumn(final Entity requestForQuotation) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (requestForQuotation.getStringField(DESCRIPTION) != null) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.description",
                    requestForQuotation.getStringField(DESCRIPTION));
        }
        return column;
    }

    private Map<String, Object> createThirdColumn(final Entity requestForQuotation) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        Entity company = companyService.getCompany();
        if (company != null) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.contracting", company.getStringField(NAME));
        }
        if (requestForQuotation.getField("createDate") != null) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.createDate",
                    getStringFromDate((Date) requestForQuotation.getField("createDate")));
        }
        return column;
    }

    private Map<String, Object> createFourthColumn(final Entity requestForQuotation, final Locale locale) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        if (requestForQuotation.getBelongsToField(SUPPLIER) != null) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.supplier", requestForQuotation
                    .getBelongsToField(SUPPLIER).getStringField(NAME));
        }
        if ((requestForQuotation.getField(DESIRED_DATE) != null)
                || (requestForQuotation.getField(WORKING_DAYS_AFTER_ORDER) != null)) {
            column.put("supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate",
                    getOfferedDateOrWorkingDaysAfterOrder(requestForQuotation, locale));
        }
        return column;
    }

    private void createProductsTable(final Document document, final Entity requestForQuotation, final Locale locale)
            throws DocumentException {
        List<Entity> columnsForRequests = supplyNegotiationsService.getColumnsForRequests();

        if (!columnsForRequests.isEmpty()) {
            List<Entity> requestForQuotationProducts = requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS);

            Map<Entity, Map<String, String>> requestForQuotationProductsColumnValues = requestForQuotationColumnFetcher
                    .getRequestForQuotationProductsColumnValues(requestForQuotationProducts);

            List<Entity> filteredColumnsForRequests = getRequestForQuotationReportColumns(columnsForRequests,
                    requestForQuotationProducts, requestForQuotationProductsColumnValues);

            if (!filteredColumnsForRequests.isEmpty()) {
                List<String> columnsName = Lists.newArrayList();

                for (Entity entity : filteredColumnsForRequests) {
                    columnsName.add(entity.getStringField(IDENTIFIER));
                }

                Map<String, HeaderAlignment> alignments = prepareHeaderAlignment(filteredColumnsForRequests, locale);

                PdfPTable productsTable = pdfHelper.createTableWithHeader(filteredColumnsForRequests.size(),
                        prepareProductsTableHeader(document, filteredColumnsForRequests, locale), false,
                        pdfHelper.getReportColumnWidths(REPORT_WIDTH, parameterService.getReportColumnWidths(), columnsName),
                        alignments);

                for (Entity requestForQuotationProduct : requestForQuotationProducts) {
                    for (Entity columnForRequests : filteredColumnsForRequests) {
                        String identifier = columnForRequests.getStringField(IDENTIFIER);
                        String alignment = columnForRequests.getStringField(ALIGNMENT);

                        String value = requestForQuotationProductsColumnValues.get(requestForQuotationProduct).get(identifier);

                        prepareProductColumnAlignment(productsTable.getDefaultCell(), ColumnAlignment.parseString(alignment));

                        productsTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                    }
                }

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

    private List<Entity> getRequestForQuotationReportColumns(final List<Entity> columnsForRequests,
            final List<Entity> requestForQuotationProducts,
            final Map<Entity, Map<String, String>> requestForQuotationProductsColumnValues) {
        return columnExtensionService.filterEmptyColumns(columnsForRequests, requestForQuotationProducts,
                requestForQuotationProductsColumnValues);
    }

    private List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForRequests,
            final Locale locale) throws DocumentException {
        document.add(new Paragraph(translationService.translate(
                "supplyNegotiations.requestForQuotation.report.requestForQuotationProducts.title", locale), FontUtils
                .getDejavuBold11Dark()));

        List<String> productsHeader = Lists.newArrayList();

        for (Entity columnForRequest : columnsForRequests) {
            String name = columnForRequest.getStringField(NAME);

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
        document.addTitle(translationService.translate("supplyNegotiations.requestForQuotation.report.title", locale));
    }

    private String getStringFromDate(final Date date) {
        return simpleDateFormat.format(date);
    }

    private String getOfferedDateOrWorkingDaysAfterOrder(final Entity requestForQuotation, final Locale locale) {
        StringBuffer offeredDate = new StringBuffer();

        boolean desiredDateExists = requestForQuotation.getField(DESIRED_DATE) != null;
        boolean workingDaysExists = requestForQuotation.getField(WORKING_DAYS_AFTER_ORDER) != null;

        if (desiredDateExists) {
            offeredDate.append(translationService.translate(
                    "supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate.toDate", locale));
            offeredDate.append(" ");
            offeredDate.append(getStringFromDate((Date) requestForQuotation.getField(DESIRED_DATE)));
        }

        if (desiredDateExists && workingDaysExists) {
            offeredDate.append(" ");
            offeredDate.append(translationService.translate(
                    "supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate.or", locale));
            offeredDate.append(" ");
        }

        if (workingDaysExists) {
            offeredDate.append(requestForQuotation.getField(WORKING_DAYS_AFTER_ORDER));
            offeredDate.append(" ");
            offeredDate.append(translationService.translate(
                    "supplyNegotiations.requestForQuotation.report.columnHeader.offeredDate.workingDaysAfterOrder", locale));
        }

        return offeredDate.toString();
    }

    public List<String> getUsedColumnsInRequestReport(final Entity requestForQuotation) {
        List<Entity> columnsForRequests = supplyNegotiationsService.getColumnsForRequests();
        List<String> columnsName = Lists.newArrayList();
        if (!columnsForRequests.isEmpty()) {
            List<Entity> requestForQuotationProducts = requestForQuotation.getHasManyField(REQUEST_FOR_QUOTATION_PRODUCTS);

            Map<Entity, Map<String, String>> requestForQuotationProductsColumnValues = requestForQuotationColumnFetcher
                    .getRequestForQuotationProductsColumnValues(requestForQuotationProducts);

            List<Entity> filteredColumnsForRequests = getRequestForQuotationReportColumns(columnsForRequests,
                    requestForQuotationProducts, requestForQuotationProductsColumnValues);

            if (!filteredColumnsForRequests.isEmpty()) {

                for (Entity entity : filteredColumnsForRequests) {
                    columnsName.add(entity.getStringField(IDENTIFIER));
                }
            }
        }
        return columnsName;
    }
}
