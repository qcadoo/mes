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
package com.qcadoo.mes.orders.print;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Component(value = "ordersOrderReportPdf")
public class OrderReportPdf extends ReportPdfView {

    private static final String L_TRANSLATION_PATH = "orders.order.report.%s.label";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private NumberService numberService;

    @Autowired
    private OrderService orderService;

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
        document.addTitle(translationService.translate("orders.order.report.order", locale, order.getStringField(OrderFields.NUMBER)));
    }

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale, final PdfWriter writer)
            throws DocumentException, IOException {
        pdfHelper.addDocumentHeader(document, "", translationService.translate("orders.order.report.order", locale, order.getStringField(OrderFields.NUMBER)), "", new Date());

        addHeaderTable(document, order, locale);
        addPlannedDateTable(document, order, locale);
        addProductQuantityTable(document, order, locale);
        addProductionLineAndDivision(document, order, locale);
        addTechnologyTable(document, order, locale);
        addMasterOrderTable(document, order, locale);
        addOperations(document, order, locale);

        return translationService.translate("orders.order.report.fileName", locale, order.getStringField(OrderFields.NUMBER));
    }

    private void addHeaderTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(3);

        List<HeaderPair> headerValues = getDocumentHeaderTableContent(order, locale);

        for (HeaderPair pair : headerValues) {
            if (OrderFields.DESCRIPTION.equals(pair.getLabel())) {
                table.getDefaultCell().setColspan(2);
            } else {
                table.getDefaultCell().setColspan(1);
            }

            if (Objects.nonNull(pair.getValue()) && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, translationService.translate(String.format(L_TRANSLATION_PATH, pair.getLabel()), locale), pair.getValue());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }

        table.setSpacingAfter(20);

        document.add(table);
    }

    private List<HeaderPair> getDocumentHeaderTableContent(final Entity order, final Locale locale) {
        List<HeaderPair> headerValues = Lists.newLinkedList();

        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        String orderCategory = order.getStringField(OrderFields.ORDER_CATEGORY);
        String description = order.getStringField(OrderFields.DESCRIPTION);

        headerValues.add(new HeaderPair(OrderFields.NUMBER, order.getStringField(OrderFields.NUMBER)));
        headerValues.add(new HeaderPair(OrderFields.NAME, order.getStringField(OrderFields.NAME)));
        headerValues.add(new HeaderPair(OrderFields.STATE, translationService.translate("orders.order.state.value." + order.getStringField(OrderFields.STATE), locale)));
        headerValues.add(new HeaderPair(OrderFields.ORDER_CATEGORY, StringUtils.isEmpty(orderCategory) ? "" : orderCategory));
        headerValues.add(new HeaderPair(OrderFields.DESCRIPTION, StringUtils.isEmpty(description) ? "" : description));
        headerValues.add(new HeaderPair("productNumber", Objects.isNull(product) ? "" : product.getStringField(ProductFields.NUMBER)));
        headerValues.add(new HeaderPair("productName", Objects.isNull(product) ? "" : product.getStringField(ProductFields.NAME)));

        if (PluginUtils.isEnabled("productionCounting")) {
            headerValues.add(new HeaderPair(L_TYPE_OF_PRODUCTION_RECORDING, translationService.translate("orders.order.typeOfProductionRecording.value." + order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING), locale)));
        }

        return headerValues;
    }

    private void addPlannedDateTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Map<String, String> values = Maps.newLinkedHashMap();

        values.put("plannedDateFrom", DateUtils.toDateTimeString(order.getDateField(OrderFields.DATE_FROM)));
        values.put("plannedDateTo", DateUtils.toDateTimeString(order.getDateField(OrderFields.DATE_TO)));

        addTableToDocument(document, locale, "orders.order.report.date.label", values);
    }

    private void addProductQuantityTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Map<String, String> values = Maps.newLinkedHashMap();

        String unit = order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
        String plannedQuantity = Objects.isNull(order.getField(OrderFields.PLANNED_QUANTITY)) ? " - " : numberService.format(order.getDecimalField(OrderFields.PLANNED_QUANTITY)) + " " + unit;
        String doneQuantity = Objects.isNull(order.getField(OrderFields.DONE_QUANTITY)) ? " - " : numberService.format(order.getDecimalField(OrderFields.DONE_QUANTITY)) + " " + unit;

        values.put("plannedQuantity", plannedQuantity);
        values.put("doneQuantity", doneQuantity);

        addTableToDocument(document, locale, "orders.order.report.productQuantity.label", values);
    }

    private void addProductionLineAndDivision(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Map<String, String> values = Maps.newLinkedHashMap();

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        Entity division = order.getBelongsToField(OrderFields.DIVISION);

        if (Objects.nonNull(productionLine) || Objects.nonNull(division)) {
            if (Objects.nonNull(productionLine)) {
                values.put("productionLineNumber", productionLine.getStringField(ProductionLineFields.NUMBER));
            }
            if (Objects.nonNull(division)) {
                values.put("divisionNumber", division.getStringField(DivisionFields.NUMBER));
            }

            addTableToDocument(document, locale, "orders.order.report.productionLineAndDivision.label", values);
        }
    }

    private void addTechnologyTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Map<String, String> values = Maps.newLinkedHashMap();

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            values.put("technologyNumber", technology.getStringField(TechnologyFields.NUMBER));
            values.put("technologyName", technology.getStringField(TechnologyFields.NAME));

            addTableToDocument(document, locale, "orders.order.report.technology.label", values);
        }
    }

    private void addMasterOrderTable(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Entity masterOrder = order.getBelongsToField("masterOrder");

        if (Objects.nonNull(masterOrder)) {
            Map<String, String> values = Maps.newLinkedHashMap();

            values.put("masterOrderNumber", masterOrder.getStringField(OrderFields.NUMBER));
            values.put("masterOrderName", masterOrder.getStringField(OrderFields.NAME));

            addTableToDocument(document, locale, "orders.order.report.masterOrder.label", values);
        }
    }

    private void addOperations(final Document document, final Entity order, final Locale locale) throws DocumentException {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            document.add(new Paragraph(translationService.translate("orders.order.report.operations.label", locale), FontUtils.getDejavuBold10Dark()));

            List<String> headerKeys = Lists.newArrayList("nodeNumber", "operationNumber", "operationName");

            Map<String, HeaderAlignment> headerValues = Maps.newLinkedHashMap();

            for (String key : headerKeys) {
                headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, key), locale), HeaderAlignment.LEFT);
            }

            PdfPTable table = pdfHelper.createTableWithHeader(headerValues.size(), Lists.newArrayList(headerValues.keySet()), false, headerValues);

            table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
            table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
            table.setHeaderRows(1);

            for (Entity technologyOperationComponent : getTechnologyOperationsComponents(technology)) {
                Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                table.addCell(createCell(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER), Element.ALIGN_LEFT));
                table.addCell(createCell(operation.getStringField(OperationFields.NUMBER), Element.ALIGN_LEFT));
                table.addCell(createCell(operation.getStringField(OperationFields.NAME), Element.ALIGN_LEFT));
            }

            table.setSpacingAfter(20);

            document.add(table);
        }
    }

    private List<Entity> getTechnologyOperationsComponents(final Entity technology) {
        return technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS).stream().sorted(Comparator.comparing(
                        technologyOperationComponent -> technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER)))
                .collect(Collectors.toList());
    }

    private void addTableToDocument(final Document document, final Locale locale, final String headerKey, final Map<String, String> values) throws DocumentException {
        document.add(new Paragraph(translationService.translate(headerKey, locale), FontUtils.getDejavuBold10Dark()));

        Map<String, HeaderAlignment> headerValues = Maps.newLinkedHashMap();

        for (String key : values.keySet()) {
            headerValues.put(translationService.translate(String.format(L_TRANSLATION_PATH, key), locale), HeaderAlignment.LEFT);
        }

        PdfPTable table = pdfHelper.createTableWithHeader(headerValues.size(), Lists.newArrayList(headerValues.keySet()), false, headerValues);

        table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        table.setHeaderRows(1);

        for (String value : values.values()) {
            table.addCell(createCell(value, Element.ALIGN_LEFT));
        }

        table.setSpacingAfter(20);

        document.add(table);
    }

    private PdfPCell createCell(final String content, final int alignment) {
        PdfPCell cell = new PdfPCell();

        float border = 0.2f;

        cell.setPhrase(new Phrase(content, FontUtils.getDejavuRegular7Dark()));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(border);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.setPadding(5);

        return cell;
    }

}
