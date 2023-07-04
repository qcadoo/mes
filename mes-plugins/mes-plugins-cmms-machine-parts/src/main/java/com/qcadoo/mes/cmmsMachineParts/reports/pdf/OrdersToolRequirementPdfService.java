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
package com.qcadoo.mes.cmmsMachineParts.reports.pdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.cmmsMachineParts.constants.*;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Service
public final class OrdersToolRequirementPdfService extends PdfDocumentService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity ordersToolRequirement, final Locale locale)
            throws DocumentException {
        String documentTitle = translationService.translate("cmmsMachineParts.ordersToolRequirement.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, StringUtils.EMPTY, documentTitle, documentAuthor,
                ordersToolRequirement.getDateField(OrdersToolRequirementFields.DATE));

        addInfoPanel(document, ordersToolRequirement, locale);
        addGroupedDataSeries(document, ordersToolRequirement, locale);
    }

    private void addInfoPanel(final Document document, final Entity ordersToolRequirement, final Locale locale)
            throws DocumentException {
        String number = ordersToolRequirement.getStringField(OrdersToolRequirementFields.NUMBER);
        String name = ordersToolRequirement.getStringField(OrdersToolRequirementFields.NAME);

        PdfPTable panelTable = pdfHelper.createPanelTable(4);

        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "cmmsMachineParts.ordersToolRequirement.report.number", locale), number);
        pdfHelper.addTableCellAsOneColumnTable(panelTable, translationService.translate(
                "cmmsMachineParts.ordersToolRequirement.report.name", locale), name);

        panelTable.completeRow();

        panelTable.setSpacingAfter(20);
        panelTable.setSpacingBefore(20);

        document.add(panelTable);
    }

    private void addGroupedDataSeries(final Document document, final Entity ordersToolRequirement, final Locale locale)
            throws DocumentException {
        List<Integer> defaultHeaderColumnWidth = Lists.newArrayList();
        Map<String, HeaderAlignment> headersWithAlignments = Maps.newLinkedHashMap();

        prepareTableHeaders(defaultHeaderColumnWidth, headersWithAlignments, locale);

        List<String> headers = Lists.newLinkedList(headersWithAlignments.keySet());

        int[] defaultHeaderColumnWidthInt = new int[defaultHeaderColumnWidth.size()];

        for (int i = 0; i < defaultHeaderColumnWidth.size(); i++) {
            defaultHeaderColumnWidthInt[i] = defaultHeaderColumnWidth.get(i);
        }

        List<Entity> orders = ordersToolRequirement.getHasManyField(OrdersToolRequirementFields.ORDERS);
        List<Entity> ordersToolRequirementTools = Lists.newArrayList(ordersToolRequirement.getHasManyField(OrdersToolRequirementFields.ORDERS_TOOL_REQUIREMENT_TOOLS));

        addNotIncludedOrders(orders, ordersToolRequirementTools);

        List<Entity> sortedOrdersToolRequirementTools = ordersToolRequirementTools.stream().sorted(Comparator
                .comparing((Entity ordersToolRequirementTool) -> ordersToolRequirementTool.getDateField(OrdersToolRequirementToolFields.DATE), nullsFirst(naturalOrder()))
                .thenComparing((Entity ordersToolRequirementTool) -> ordersToolRequirementTool.getBelongsToField(OrdersToolRequirementToolFields.ORDER).getStringField(OrderFields.NUMBER))
                .thenComparing((Entity ordersToolRequirementTool) -> ordersToolRequirementTool.getBelongsToField(OrdersToolRequirementToolFields.OPERATION).getStringField(OperationFields.NUMBER))
        ).collect(Collectors.toList());

        PdfPTable table = pdfHelper.createTableWithHeader(headersWithAlignments.size(), headers, true,
                defaultHeaderColumnWidthInt, headersWithAlignments);

        table.getDefaultCell().disableBorderSide(PdfCell.BOTTOM);

        Long actualDate = 0L;
        String actualOrderNumber = StringUtils.EMPTY;

        for (Entity ordersToolRequirementTool : sortedOrdersToolRequirementTools) {
            Date date = ordersToolRequirementTool.getDateField(OrdersToolRequirementToolFields.DATE);
            Entity order = ordersToolRequirementTool.getBelongsToField(OrdersToolRequirementToolFields.ORDER);
            Entity operation = ordersToolRequirementTool.getBelongsToField(OrdersToolRequirementToolFields.OPERATION);
            Entity technologyOperationTool = ordersToolRequirementTool.getBelongsToField(OrdersToolRequirementToolFields.TECHNOLOGY_OPERATION_TOOL);
            BigDecimal quantity = ordersToolRequirementTool.getDecimalField(OrdersToolRequirementToolFields.QUANTITY);

            table.getDefaultCell().disableBorderSide(PdfCell.TOP);

            if (checkIfAddDate(actualDate, date)) {
                table.getDefaultCell().enableBorderSide(PdfCell.TOP);

                if (Objects.nonNull(date)) {
                    table.addCell(new Phrase(DateUtils.toDateString(date), FontUtils.getDejavuRegular7Dark()));

                    actualDate = date.getTime();
                } else {
                    table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));

                    actualDate = null;
                }
            } else {
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
            }

            String orderNumber = order.getStringField(OrderFields.NUMBER);

            if (!actualOrderNumber.equals(orderNumber)) {
                table.getDefaultCell().enableBorderSide(PdfCell.TOP);

                table.addCell(new Phrase(orderNumber, FontUtils.getDejavuRegular7Dark()));

                actualOrderNumber = orderNumber;
            } else {
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
            }

            if (Objects.nonNull(operation)) {
                table.addCell(new Phrase(operation.getStringField(OperationFields.NUMBER), FontUtils.getDejavuRegular7Dark()));
            } else {
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
            }

            if (Objects.nonNull(technologyOperationTool)) {
                Entity tool = technologyOperationTool.getBelongsToField(TechnologyOperationToolFields.TOOL);
                String description = technologyOperationTool.getStringField(TechnologyOperationToolFields.DESCRIPTION);

                table.addCell(new Phrase(technologyOperationTool.getStringField(TechnologyOperationToolFields.TOOL_CATEGORY), FontUtils.getDejavuRegular7Dark()));

                if (Objects.nonNull(tool)) {
                    table.addCell(new Phrase(tool.getStringField(ToolFields.NUMBER) + ", " + tool.getStringField(ToolFields.NAME), FontUtils.getDejavuRegular7Dark()));
                } else {
                    table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
                }

                table.addCell(new Phrase(StringUtils.isNotEmpty(description) ? description : StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(numberService.format(quantity), FontUtils.getDejavuBold7Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase(technologyOperationTool.getStringField(ProductFields.UNIT), FontUtils.getDejavuRegular7Dark()));
            } else {
                table.getDefaultCell().setColspan(5);
                table.addCell(new Phrase(StringUtils.EMPTY, FontUtils.getDejavuRegular7Dark()));
                table.getDefaultCell().setColspan(1);
            }
        }

        document.add(table);
    }

    private void prepareTableHeaders(final List<Integer> defaultOrderHeaderColumnWidth, final Map<String, HeaderAlignment> headersWithAlignments, final Locale locale) {
        defaultOrderHeaderColumnWidth.add(30);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.date", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(40);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.orderNumber", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(40);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.operationNumber", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(50);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.toolCategory", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(60);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.toolNumberAndName", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(60);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.toolDescription", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(30);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.quantity", locale), HeaderAlignment.LEFT);
        defaultOrderHeaderColumnWidth.add(20);
        headersWithAlignments.put(translationService.translate("cmmsMachineParts.ordersToolRequirement.report.column.toolUnit", locale), HeaderAlignment.LEFT);
    }

    private void addNotIncludedOrders(final List<Entity> orders, final List<Entity> ordersToolRequirementTools) {
        Set<Long> orderIds = ordersToolRequirementTools.stream().map(ordersToolRequirementTool
                -> ordersToolRequirementTool.getBelongsToField(OrdersToolRequirementToolFields.ORDER).getId()).collect(Collectors.toSet());

        List<Entity> ordersNotIncluded = orders.stream().filter(order -> !orderIds.contains(order.getId())).collect(Collectors.toList());

        ordersNotIncluded.forEach(order -> {
            Date dateFrom = order.getDateField(OrderFields.DATE_FROM);

            Entity ordersToolRequirementTool = getOrdersToolRequirementToolDD().create();

            ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.DATE, dateFrom);
            ordersToolRequirementTool.setField(OrdersToolRequirementToolFields.ORDER, order);

            ordersToolRequirementTools.add(ordersToolRequirementTool);
        });
    }

    private boolean checkIfAddDate(final Long actualDate, final Date date) {
        return Objects.isNull(date) && Objects.nonNull(actualDate)
                || Objects.nonNull(date) && Objects.isNull(actualDate)
                || Objects.nonNull(date) && !Instant.ofEpochMilli(actualDate).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay().isEqual(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("cmmsMachineParts.ordersToolRequirement.report.title", locale);
    }

    private DataDefinition getOrdersToolRequirementToolDD() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_ORDERS_TOOL_REQUIREMENT_TOOL);
    }

}
