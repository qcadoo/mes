/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basicProductionCounting.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.print.HeaderPair;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

@Component(value = "manifestoReportPdf")
public class ManifestoReportPdf extends ReportPdfView {

    private static final String L_MANIFESTO_REPORT_HEADER = "basicProductionCounting.detailedProductionCountingAndProgress.report.%s.label";

    private static final String L_MANIFESTO_REPORT_COLUMN = "basicProductionCounting.detailedProductionCountingAndProgress.report.column.%s";

    private static final String L_ORDER_NUMBER = "orderNumber";

    private static final String L_DESCRIPTION = "description";

    private static final String L_COMPANY_NAME = "companyName";

    private static final String L_PRODUCT_NUMBER = "productNumber";

    private static final String L_PRODUCT_NAME = "productName";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_OPERATION_NUMBER = "operationNumber";

    public static final String L_BARCODE = "barcode";

    private static final String L_NUMBER = "number";

    private static final String L_NAME = "name";

    private static final String L_QUANTITY = "quantity";

    private static final String L_UNIT = "unit";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private BarcodeOperationComponentService barcodeOperationComponentService;

    private Entity order;

    @Override
    protected void prepareWriter(final Map<String, Object> model, final PdfWriter writer, final HttpServletRequest request)
            throws DocumentException {
        super.prepareWriter(model, writer, request);

        Long orderId = Long.valueOf(model.get("id").toString());

        order = orderService.getOrder(orderId);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("basicProductionCounting.detailedProductionCountingAndProgress.report.title", locale, order.getStringField(OrderFields.NUMBER)));
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale, final PdfWriter writer)
            throws DocumentException, IOException {
        pdfHelper.addDocumentHeader(document, "", translationService.translate("basicProductionCounting.detailedProductionCountingAndProgress.report.title", locale, order.getStringField(OrderFields.NUMBER)), "", new Date());

        createHeaderTable(document, order, locale);
        createOperationsTable(document, writer, order, locale);
        createProductsTable(document, order, locale);

        return translationService.translate("basicProductionCounting.detailedProductionCountingAndProgress.report.fileName", locale, order.getStringField(OrderFields.NUMBER));
    }

    private void createHeaderTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        List<HeaderPair> headerValues = getHeaderTableContents(order);

        PdfPTable headerTable = pdfHelper.createPanelTable(2);

        for (HeaderPair pair : headerValues) {
            headerTable.getDefaultCell().setPadding(4.0f);

            if (L_ORDER_NUMBER.equals(pair.getLabel()) || L_DESCRIPTION.equals(pair.getLabel()) || L_COMPANY_NAME.equals(pair.getLabel())) {
                headerTable.getDefaultCell().setColspan(2);
            } else {
                headerTable.getDefaultCell().setColspan(1);
            }

            if (StringUtils.isNotEmpty(pair.getValue())) {
                if (L_ORDER_NUMBER.equals(pair.getLabel())) {
                    headerTable.getDefaultCell().setPaddingBottom(20.0f);

                    pdfHelper.addTableCellAsTable(headerTable, translationService.translate(String.format(L_MANIFESTO_REPORT_HEADER, pair.getLabel()), locale), pair.getValue(), FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold19Dark(), 1);
                } else if (L_COMPANY_NAME.equals(pair.getLabel())) {
                    pdfHelper.addTableCellAsTable(headerTable, translationService.translate(String.format(L_MANIFESTO_REPORT_HEADER, pair.getLabel()), locale), pair.getValue(), FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuBold17Dark(), 1);
                } else {
                    pdfHelper.addTableCellAsTable(headerTable, translationService.translate(String.format(L_MANIFESTO_REPORT_HEADER, pair.getLabel()), locale), pair.getValue(), FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular10Dark(), 1);
                }
            } else {
                if (L_DESCRIPTION.equals(pair.getLabel())) {
                    headerTable.getDefaultCell().setPaddingBottom(20.0f);

                    pdfHelper.addTableCellAsTable(headerTable, translationService.translate(String.format(L_MANIFESTO_REPORT_HEADER, pair.getLabel()), locale), pair.getValue(), FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular10Dark(), 1);
                } else {
                    pdfHelper.addTableCellAsTable(headerTable, StringUtils.EMPTY, StringUtils.EMPTY, FontUtils.getDejavuBold9Dark(), FontUtils.getDejavuRegular10Dark(), 1);
                }
            }
        }

        headerTable.completeRow();

        headerTable.setSpacingAfter(10);
        headerTable.setSpacingBefore(10);

        document.add(headerTable);
    }


    private List<HeaderPair> getHeaderTableContents(final Entity order) {
        List<HeaderPair> headerValues = Lists.newLinkedList();

        String description = order.getStringField(OrderFields.DESCRIPTION);
        Entity company = order.getBelongsToField(OrderFields.COMPANY);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        headerValues.add(new HeaderPair(L_ORDER_NUMBER, order.getStringField(OrderFields.NUMBER)));
        headerValues.add(new HeaderPair(L_COMPANY_NAME, Objects.isNull(company) ? "" : company.getStringField(CompanyFields.NAME)));
        headerValues.add(new HeaderPair(L_DESCRIPTION, StringUtils.isEmpty(description) ? "" : description));
        headerValues.add(new HeaderPair(L_PRODUCT_NUMBER, Objects.isNull(product) ? "" : product.getStringField(ProductFields.NUMBER)));
        headerValues.add(new HeaderPair(L_PRODUCT_NAME, Objects.isNull(product) ? "" : product.getStringField(ProductFields.NAME)));
        headerValues.add(new HeaderPair(L_PLANNED_QUANTITY, numberService.formatWithMinimumFractionDigits(order.getDecimalField(OrderFields.PLANNED_QUANTITY), 2)));

        return headerValues;
    }

    private void createOperationsTable(final Document document, final PdfWriter writer, final Entity order, final Locale locale) throws DocumentException {
        List<HeaderPair> headerValues = getOrdersTableHeaders();

        List<String> header = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (HeaderPair pair : headerValues) {
            header.add(translationService.translate(String.format(L_MANIFESTO_REPORT_COLUMN, pair.getLabel()), locale));
            alignments.put(translationService.translate(String.format(L_MANIFESTO_REPORT_COLUMN, pair.getLabel()), locale), HeaderAlignment.parseString(pair.getValue()));
        }

        int[] columnWidths = {60, 40};

        PdfPTable operationsTable = pdfHelper.createTableWithHeader(header.size(), header, false, columnWidths, alignments);

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            List<Entity> operationComponents = Lists.newArrayList(technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS));

            operationComponents.sort(Comparator.comparing(operationComponent -> operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER), Comparator.reverseOrder()));

            for (Entity operationComponent : operationComponents) {
                Entity operation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                barcodeOperationComponentService.createBarcodeOperationComponent(order, operationComponent);

                Optional<String> mayBeBarcode = barcodeOperationComponentService.findBarcode(order, operationComponent);

                operationsTable.addCell(new Phrase(operation.getStringField(OperationFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
                operationsTable.addCell(createBarcode(writer, mayBeBarcode.orElse(StringUtils.EMPTY)));
            }
        }

        operationsTable.setSpacingAfter(10);
        operationsTable.setSpacingBefore(10);

        document.add(
                new Paragraph(translationService.translate("basicProductionCounting.detailedProductionCountingAndProgress.report.operations", locale),
                        FontUtils.getDejavuBold11Dark()));

        document.add(operationsTable);
    }

    private List<HeaderPair> getOrdersTableHeaders() {
        List<HeaderPair> headerValues = Lists.newLinkedList();

        headerValues.add(new HeaderPair(L_OPERATION_NUMBER, HeaderAlignment.LEFT.getStringValue()));
        headerValues.add(new HeaderPair(L_BARCODE, HeaderAlignment.CENTER.getStringValue()));

        return headerValues;
    }

    private Image createBarcode(final PdfWriter writer, final String code) {
        Barcode128 code128 = new Barcode128();

        code128.setCode(code);
        code128.setBarHeight(5f);
        code128.setSize(2f);
        code128.setBaseline(3f);

        PdfContentByte cb = writer.getDirectContent();

        return code128.createImageWithBarcode(cb, null, null);
    }

    private void createProductsTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        List<HeaderPair> headerValues = getProductsTableHeaders();

        List<String> header = Lists.newArrayList();
        Map<String, HeaderAlignment> alignments = Maps.newHashMap();

        for (HeaderPair pair : headerValues) {
            header.add(translationService.translate(String.format(L_MANIFESTO_REPORT_COLUMN, pair.getLabel()), locale));
            alignments.put(translationService.translate(String.format(L_MANIFESTO_REPORT_COLUMN, pair.getLabel()), locale), HeaderAlignment.parseString(pair.getValue()));
        }

        int[] columnWidths = {60, 120, 40, 35};

        PdfPTable productsTable = pdfHelper.createTableWithHeader(header.size(), header, false, columnWidths, alignments);

        Map<Long, BigDecimal> neededProductQuantities = basicProductionCountingService.getNeededProductQuantities(Lists.newArrayList(order), MrpAlgorithm.ONLY_MATERIALS);

        List<Entity> products = Lists.newArrayList(getProductDD().find().add(SearchRestrictions.in("id", neededProductQuantities.keySet())).list().getEntities());

        products.sort(Comparator.comparing(product -> product.getStringField(ProductFields.NUMBER)));

        for (Entity product : products) {
            productsTable.addCell(new Phrase(product.getStringField(ProductFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            productsTable.addCell(new Phrase(product.getStringField(ProductFields.NAME), FontUtils.getDejavuRegular7Dark()));
            productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            productsTable.addCell(new Phrase(numberService.format(neededProductQuantities.get(product.getId())), FontUtils.getDejavuRegular7Dark()));
            productsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            productsTable.addCell(new Phrase(product.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
        }

        productsTable.setSpacingAfter(10);
        productsTable.setSpacingBefore(10);

        document.add(Chunk.NEXTPAGE);

        document.add(
                new Paragraph(translationService.translate("basicProductionCounting.detailedProductionCountingAndProgress.report.products", locale),
                        FontUtils.getDejavuBold11Dark()));

        document.add(productsTable);
    }

    private List<HeaderPair> getProductsTableHeaders() {
        List<HeaderPair> headerValues = Lists.newLinkedList();

        headerValues.add(new HeaderPair(L_NUMBER, HeaderAlignment.LEFT.getStringValue()));
        headerValues.add(new HeaderPair(L_NAME, HeaderAlignment.LEFT.getStringValue()));
        headerValues.add(new HeaderPair(L_QUANTITY, HeaderAlignment.RIGHT.getStringValue()));
        headerValues.add(new HeaderPair(L_UNIT, HeaderAlignment.LEFT.getStringValue()));

        return headerValues;
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
