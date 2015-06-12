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
package com.qcadoo.mes.workPlans.pdf.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class WorkPlanPdfForDivision {

    public static final String MSG_TITLE = "workPlans.workPlan.report.title";

    public static final String DIVISION = "workPlans.workPlan.report.division";

    private static final String L_PARENT = "parent";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private BarcodeOperationComponentService barcodeOperationComponentService;

    public void print(PdfWriter pdfWriter, GroupingContainer groupingContainer, Entity workPlan, Document document, Locale locale)
            throws DocumentException {

        ListMultimap<String, OrderOperationComponent> titleToOperationComponent = groupingContainer
                .getTitleToOperationComponent();

        for (String title : titleToOperationComponent.keySet()) {
            addWorkPlanTitle(document, workPlan, title, locale);
            List<OrderOperationComponent> components = titleToOperationComponent.get(title);
            Collections.reverse(components);
            addMainOrders(document, components, locale);
            for (OrderOperationComponent orderOperationComponent : components) {
                addOperationTable(pdfWriter, groupingContainer, document, orderOperationComponent, locale);

            }
            document.newPage();
        }
    }

    private void addWorkPlanTitle(Document document, Entity workPlan, String title, Locale locale) throws DocumentException {

        PdfPTable headerTable = pdfHelper.createPanelTable(2);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        Paragraph workPlanTitle = new Paragraph(new Phrase(getWorkPlanTitle(locale), FontUtils.getDejavuBold11Light()));
        workPlanTitle.add(new Phrase(" " + getWorkPlanName(workPlan), FontUtils.getDejavuBold11Dark()));
        titleCell.addElement(workPlanTitle);

        PdfPCell divisionCell = new PdfPCell();
        divisionCell.setBorder(Rectangle.NO_BORDER);
        Paragraph divisionTitle = new Paragraph(new Phrase(getDivisionTitle(locale), FontUtils.getDejavuBold11Light()));
        divisionTitle.add(new Phrase(" " + getDivisionFromTitle(title, locale), FontUtils.getDejavuBold11Dark()));
        divisionTitle.setAlignment(Element.ALIGN_RIGHT);
        divisionCell.addElement(divisionTitle);

        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        headerTable.setTableEvent(null);
        headerTable.setSpacingAfter(4.0f);
        headerTable.addCell(titleCell);
        headerTable.addCell(divisionCell);
        document.add(headerTable);
    }

    private void addMainOrders(Document document, List<OrderOperationComponent> orderOperationComponents, Locale locale)
            throws DocumentException {
        List<Entity> orders = getMainOrdersForOperationComponents(orderOperationComponents);
        for (Entity order : orders) {
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);
            Paragraph mainOrder = new Paragraph(new Phrase(prepareMainOrderSummary(order, product, locale),
                    FontUtils.getDejavuBold9Dark()));
            document.add(mainOrder);
        }
    }

    private void addOperationTable(PdfWriter pdfWriter, GroupingContainer groupingContainer, Document document,
            OrderOperationComponent orderOperationComponent, Locale locale) throws DocumentException {

        Map<Long, Map<OperationProductColumn, ColumnAlignment>> outputProductsMap = groupingContainer
                .getOperationComponentIdProductOutColumnToAlignment();

        Map<Long, Map<OperationProductColumn, ColumnAlignment>> inputProductsMap = groupingContainer
                .getOperationComponentIdProductInColumnToAlignment();

        Entity operationComponent = orderOperationComponent.getOperationComponent();
        Entity order = orderOperationComponent.getOrder();
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        Map<OperationProductColumn, ColumnAlignment> inputProductColumnAlignmentMap = inputProductsMap.get(operationComponent
                .getId());
        Map<OperationProductColumn, ColumnAlignment> outputProductColumnAlignmentMap = outputProductsMap.get(operationComponent
                .getId());

        PdfPTable table = pdfHelper.createPanelTable(3);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setColspan(2);

        PdfPCell inputCell = new PdfPCell();
        inputCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell outputCell = new PdfPCell();
        outputCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell codeCell = new PdfPCell();
        codeCell.setBorder(Rectangle.NO_BORDER);
        codeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        codeCell.setVerticalAlignment(Element.ALIGN_TOP);
        codeCell.setRowspan(2);

        // addOperationSummary(headerCell, operationComponent);

        addOrderSummary(headerCell, order, product, operationComponent);

        addOperationProductsTable(inputCell, operationProductInComponents(operationComponent), inputProductColumnAlignmentMap,
                ProductDirection.IN, locale);
        addOperationProductsTable(outputCell, operationProductOutComponents(operationComponent), outputProductColumnAlignmentMap,
                ProductDirection.OUT, locale);

        codeCell.addElement(createBarcode(pdfWriter, operationComponent));

        float[] tableColumnWidths = new float[] { 70f, 70f, 10f };
        table.setWidths(tableColumnWidths);
        table.setTableEvent(null);
        table.addCell(headerCell);
        table.addCell(codeCell);
        table.addCell(inputCell);
        table.addCell(outputCell);
        document.add(table);
    }

    private void addOperationSummary(PdfPCell cell, Entity operationComponent) throws DocumentException {
        Entity operation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        PdfPTable operationTable = pdfHelper.createPanelTable(1);

        PdfPCell numberCell = new PdfPCell();
        numberCell.setBorder(Rectangle.NO_BORDER);
        Paragraph operationName = new Paragraph(operation.getStringField(OperationFields.NUMBER) + " - "
                + operation.getStringField(OperationFields.NAME), FontUtils.getDejavuBold7Dark());
        numberCell.addElement(operationName);

        PdfPCell descriptionCell = new PdfPCell();
        descriptionCell.setBorder(Rectangle.NO_BORDER);
        String comment = operation.getStringField(OperationFields.COMMENT);
        Paragraph description = null;
        if (!StringUtils.isEmpty(comment)) {
            description = new Paragraph(comment, FontUtils.getDejavuBold7Dark());
        }

        operationTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        operationTable.setTableEvent(null);
        operationTable.addCell(numberCell);
        if (description != null) {
            descriptionCell.addElement(description);
            operationTable.addCell(descriptionCell);
        } else {
            operationTable.addCell("");
        }
        cell.addElement(operationTable);
    }

    private void addOrderSummary(PdfPCell cell, Entity order, Entity product, Entity operationComponent) throws DocumentException {
        Entity operation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
        PdfPTable orderTable = pdfHelper.createPanelTable(3);
        PdfPCell operationCell = new PdfPCell();
        operationCell.setBorder(Rectangle.NO_BORDER);
        Paragraph operationName = new Paragraph(operation.getStringField(OperationFields.NUMBER) + " - "
                + operation.getStringField(OperationFields.NAME), FontUtils.getDejavuBold7Dark());
        operationCell.addElement(operationName);

        PdfPCell numberCell = new PdfPCell();
        numberCell.setBorder(Rectangle.NO_BORDER);
        Paragraph number = new Paragraph(order.getStringField(OrderFields.NUMBER), FontUtils.getDejavuBold7Dark());
        number.setAlignment(Element.ALIGN_RIGHT);
        numberCell.addElement(number);

        PdfPCell quantityCell = new PdfPCell();
        quantityCell.setBorder(Rectangle.NO_BORDER);
        Paragraph quantity = new Paragraph(numberService.formatWithMinimumFractionDigits(
                order.getDecimalField(OrderFields.PLANNED_QUANTITY), 0)
                + " " + product.getStringField(ProductFields.UNIT), FontUtils.getDejavuBold7Dark());
        quantity.setAlignment(Element.ALIGN_CENTER);
        quantityCell.addElement(quantity);

        PdfPCell descriptionCell = new PdfPCell();
        descriptionCell.setBorder(Rectangle.NO_BORDER);
        descriptionCell.setColspan(3);
        String comment = operation.getStringField(OperationFields.COMMENT);
        Paragraph description = null;
        if (!StringUtils.isEmpty(comment)) {
            description = new Paragraph(comment, FontUtils.getDejavuBold7Dark());
        }

        float[] tableColumnWidths = new float[] { 160f, 30f, 10f };
        orderTable.setWidths(tableColumnWidths);
        orderTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        orderTable.setTableEvent(null);
        orderTable.addCell(operationCell);
        orderTable.addCell(numberCell);
        orderTable.addCell(quantityCell);
        if (description != null) {
            descriptionCell.addElement(description);
            orderTable.addCell(descriptionCell);
        }
        cell.addElement(orderTable);
    }

    private void addOperationProductsTable(PdfPCell cell, EntityList operationProductComponents,
            Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap, ProductDirection direction,
            Locale locale) throws DocumentException {

        int columnCount = operationProductColumnAlignmentMap.size();

        Map<String, HeaderAlignment> headerAlignments = new HashMap<String, HeaderAlignment>(columnCount);
        List<String> headers = new ArrayList<String>(columnCount);
        float[] widths = fill(locale, operationProductColumnAlignmentMap, headers, headerAlignments, direction);

        PdfPTable table = pdfHelper.createTableWithHeader(columnCount, headers, false, headerAlignments);
        table.setWidths(widths);
        PdfPCell defaultCell = table.getDefaultCell();
        for (Entity operationProduct : operationProductComponents) {
            for (Map.Entry<OperationProductColumn, ColumnAlignment> e : operationProductColumnAlignmentMap.entrySet()) {
                alignColumn(defaultCell, e.getValue());
                table.addCell(operationProductPhrase(operationProduct, e.getKey()));
            }

        }
        cell.addElement(table);
    }

    private EntityList operationProductOutComponents(Entity operationComponent) {
        return operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
    }

    private EntityList operationProductInComponents(Entity operationComponent) {
        return operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
    }

    private void alignColumn(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(2f);
    }

    private float[] fill(Locale locale, Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap,
            List<String> headers, Map<String, HeaderAlignment> headerAlignments, ProductDirection direction) {
        // for optimization method fills two collections simultaneously
        float[] widths = new float[operationProductColumnAlignmentMap.size()];
        if (widths.length > 3) {
            widths[0] = 12f;
            widths[1] = 4f;
            widths[2] = 3f;
        }
        for (int i = 3; i < operationProductColumnAlignmentMap.size(); i++) {
            widths[i] = 5f;
        }
        for (Map.Entry<OperationProductColumn, ColumnAlignment> entry : operationProductColumnAlignmentMap.entrySet()) {
            String name = entry.getKey().getName(locale, direction);
            headerAlignments.put(name, headerAlignment(entry.getValue()));
            headers.add(name);
        }
        return widths;
    }

    private Phrase operationProductPhrase(Entity operationProduct, OperationProductColumn operationProductColumn) {
        return new Phrase(operationProductColumn.getColumnValue(operationProduct), FontUtils.getDejavuRegular7Dark());
    }

    private HeaderAlignment headerAlignment(ColumnAlignment value) {
        return ColumnAlignment.LEFT.equals(value) ? HeaderAlignment.LEFT : HeaderAlignment.RIGHT;
    }

    private Image createBarcode(PdfWriter pdfWriter, Entity operationComponent) throws DocumentException {
        PdfContentByte cb = pdfWriter.getDirectContent();
        Barcode128 code128 = new Barcode128();
        code128.setCode(barcodeOperationComponentService.getCodeFromBarcodeForOperationComponet(operationComponent));
        Image barcodeImage = code128.createImageWithBarcode(cb, null, null);
        return barcodeImage;
    }

    private String prepareMainOrderSummary(Entity order, Entity product, Locale locale) {

        StringBuilder summary = new StringBuilder(translationService.translate("workPlans.workPlan.report.mainOrder", locale));
        summary.append(" ");
        summary.append(order.getStringField(OrderFields.NUMBER));
        summary.append(", ");
        summary.append(product.getStringField(ProductFields.NAME));
        summary.append(", ");
        summary.append(numberService.formatWithMinimumFractionDigits(order.getDecimalField(OrderFields.PLANNED_QUANTITY), 0));
        summary.append(" ");
        summary.append(product.getStringField(ProductFields.UNIT));
        return summary.toString();
    }

    private List<Entity> getMainOrdersForOperationComponents(List<OrderOperationComponent> orderOperationComponents) {

        List<Entity> orders = orderOperationComponents.stream().map(component -> component.getOrder()).distinct()
                .collect(Collectors.toList());
        List<Entity> mainOrders = Lists.newArrayList();
        for (Entity order : orders) {
            if (order.getBelongsToField(L_PARENT) == null) {
                mainOrders.add(order);
            } else {
                Entity parent = order.getBelongsToField(L_PARENT);
                while (parent.getBelongsToField(L_PARENT) != null) {
                    parent = parent.getBelongsToField(L_PARENT);
                }
                mainOrders.add(parent);
            }
        }
        return mainOrders.stream().distinct().collect(Collectors.toList());
    }

    private String getWorkPlanName(Entity workPlan) {
        return workPlan.getStringField(WorkPlanFields.NAME);
    }

    private String getWorkPlanTitle(Locale locale) {
        return translationService.translate(MSG_TITLE, locale);
    }

    private String getDivisionTitle(Locale locale) {
        return translationService.translate(DIVISION, locale);
    }

    private String getDivisionFromTitle(String title, Locale locale) {
        return title.replace(translationService.translate("workPlans.workPlan.report.title.byDivision", locale) + " ", "");
    }
}
