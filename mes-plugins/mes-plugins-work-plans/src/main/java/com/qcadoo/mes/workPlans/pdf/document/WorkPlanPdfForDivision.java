/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans.pdf.document;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.OperationProductInComponentFieldsWP;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.pdf.document.operation.component.OperationProductColumnHelper;
import com.qcadoo.mes.workPlans.pdf.document.operation.component.OperationProductHelper;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WorkPlanPdfForDivision {

    public static final String L_UNIT_OPERATION_PRODUCT_COLUMN = "unitOperationProductColumn";
    public static final String L_PRODUCT_NAME_OPERATION_PRODUCT_COLUMN = "productNameOperationProductColumn";
    public static final String L_PLANNED_QUANTITY_OPERATION_PRODUCT_COLUMN = "plannedQuantityOperationProductColumn";
    public static final String L_PLANED_QUANTITY = "planedQuantity";
    public static final String L_RESOURCE_NUMBER = "resourceNumber";
    public static final String L_RESOURCE_UNIT = "resourceUnit";

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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;


    @Autowired
    private WorkPlansService workPlansService;

    public void print(PdfWriter pdfWriter, GroupingContainer groupingContainer, Entity workPlan, Document document, Locale locale)
            throws DocumentException {

        ListMultimap<String, OrderOperationComponent> titleToOperationComponent = groupingContainer
                .getTitleToOperationComponent();

        for (String title : titleToOperationComponent.keySet()) {
            addWorkPlanTitle(document, workPlan, title, locale);
            List<OrderOperationComponent> components = titleToOperationComponent.get(title);
            List<OrderOperationComponent> sorted = sortOrderOperationComponents(components);
            addMainOrders(document, sorted, locale);
            for (OrderOperationComponent orderOperationComponent : sorted) {
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
            mainOrder.setIndentationLeft(3f);
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

        addOrderSummary(headerCell, order, product, operationComponent);

        addOperationProductsTableIn(order, inputCell,
                addMaterialComponents(orderOperationComponent.getProductionCountingQuantitiesIn(), order),
                inputProductColumnAlignmentMap, ProductDirection.IN, locale);

        addOperationProductsTable(outputCell, orderOperationComponent.getProductionCountingQuantitiesOut(),
                outputProductColumnAlignmentMap, ProductDirection.OUT, locale);

        codeCell.addElement(createBarcode(pdfWriter, order, operationComponent));

        float[] tableColumnWidths = new float[]{70f, 70f, 10f};
        table.setWidths(tableColumnWidths);
        table.setTableEvent(null);
        table.addCell(headerCell);
        table.addCell(codeCell);
        table.addCell(inputCell);
        table.addCell(outputCell);
        table.setKeepTogether(true);
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
        String comment = operationComponent.getStringField(TechnologyOperationComponentFields.COMMENT);
        Paragraph description = null;
        if (!StringUtils.isEmpty(comment)) {
            description = new Paragraph(comment, FontUtils.getDejavuBold7Dark());
        }

        float[] tableColumnWidths = new float[]{160f, 30f, 10f};
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

    private void addOperationProductsTableIn(Entity order, PdfPCell cell, List<Entity> operationProductComponents,
                                             Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap, ProductDirection direction,
                                             Locale locale) throws DocumentException {
        if (operationProductComponents.isEmpty()) {
            return;
        }

        int columnCount = operationProductColumnAlignmentMap.size();

        Map<String, HeaderAlignment> headerAlignments = new HashMap<>(columnCount);
        List<String> headers = new ArrayList<String>(columnCount);
        float[] widths = fill(locale, operationProductColumnAlignmentMap, headers, headerAlignments, direction);

        PdfPTable table = pdfHelper.createTableWithHeader(columnCount, headers, false, headerAlignments);
        table.setWidths(widths);
        PdfPCell defaultCell = table.getDefaultCell();
        List<OperationProductHelper> operationProductsValue = prepareOperationProductsValue(order,
                operationProductComponents, operationProductColumnAlignmentMap.entrySet());

        //operationProductsValue = workPlansService.sortByColumn(workPlan, operationProductsValue, headers);
        defaultCell.setBorder(Rectangle.BOX);

        for (OperationProductHelper operationProduct : operationProductsValue) {
            if(operationProduct.isContainsResources() && !operationProduct.isLastResource()) {
                defaultCell.setBorder(Rectangle.NO_BORDER);
                defaultCell.enableBorderSide(Rectangle.LEFT);
                defaultCell.enableBorderSide(Rectangle.RIGHT);
            } else {
                defaultCell.setBorder(Rectangle.TOP);
                defaultCell.setBorder(Rectangle.BOTTOM);
                defaultCell.enableBorderSide(Rectangle.LEFT);
                defaultCell.enableBorderSide(Rectangle.RIGHT);
            }


            for (OperationProductColumnHelper e : operationProduct.getOperationProductColumnHelpers()) {
                alignColumn(defaultCell, e.getColumnAlignment(), false);
                table.addCell(operationProductPhrase(e.getValue()));
            }
        }

        cell.addElement(table);

    }

    private List<OperationProductHelper> prepareOperationProductsValue(Entity order, final List<Entity> operationProducts,
                                                                       final Set<Map.Entry<OperationProductColumn, ColumnAlignment>> alignments) {
        List<OperationProductHelper> operationProductsValue = Lists.newArrayList();

        for (Entity operationProduct : operationProducts) {
            OperationProductHelper operationProductHelper = new OperationProductHelper();
            List<OperationProductColumnHelper> operationProductColumnHelpers = Lists.newArrayList();

            for (Map.Entry<OperationProductColumn, ColumnAlignment> e : alignments) {
                String columnValue = e.getKey().getColumnValue(operationProduct);
                if (StringUtils.isEmpty(columnValue)) {
                    columnValue = e.getKey().getColumnValueForOrder(order, operationProduct);
                }
                OperationProductColumnHelper operationProductColumnHelper = new OperationProductColumnHelper(e.getValue(),
                        columnValue, e.getKey().getIdentifier());
                operationProductColumnHelpers.add(operationProductColumnHelper);
            }

            operationProductHelper.setOperationProductColumnHelpers(operationProductColumnHelpers);


            Entity pcq = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                    .get(operationProduct.getId());

            List<Entity> orderProductResourceReservations = pcq.getHasManyField("orderProductResourceReservations");

            if (!orderProductResourceReservations.isEmpty()) {
                operationProductHelper.setContainsResources(true);
            }


            operationProductsValue.add(operationProductHelper);

            int index = 1;
            for (Entity orderProductResourceReservation : orderProductResourceReservations) {

                OperationProductHelper resourceOperationProductHelper = new OperationProductHelper();
                resourceOperationProductHelper.setResource(true);
                resourceOperationProductHelper.setContainsResources(true);
                resourceOperationProductHelper.setContainsResources(true);
                resourceOperationProductHelper.setLastResource(index == orderProductResourceReservations.size());
                List<OperationProductColumnHelper> resourceOperationProductColumnHelpers = Lists.newArrayList();
                BigDecimal planedQuantity = orderProductResourceReservation.getDecimalField(L_PLANED_QUANTITY);
                String resourceNumber = orderProductResourceReservation.getStringField(L_RESOURCE_NUMBER);
                String resourceUnit = orderProductResourceReservation.getStringField(L_RESOURCE_UNIT);

                for (Map.Entry<OperationProductColumn, ColumnAlignment> e : alignments) {
                    OperationProductColumnHelper operationProductColumnHelper;
                    if (L_PRODUCT_NAME_OPERATION_PRODUCT_COLUMN.equals(e.getKey().getIdentifier())) {
                        operationProductColumnHelper = new OperationProductColumnHelper(e.getValue(),
                                "- " + resourceNumber, e.getKey().getIdentifier());

                    } else if (L_UNIT_OPERATION_PRODUCT_COLUMN.equals(e.getKey().getIdentifier())) {
                        operationProductColumnHelper = new OperationProductColumnHelper(e.getValue(),
                                resourceUnit, e.getKey().getIdentifier());
                    } else if (L_PLANNED_QUANTITY_OPERATION_PRODUCT_COLUMN.equals(e.getKey().getIdentifier())) {
                        operationProductColumnHelper = new OperationProductColumnHelper(e.getValue(),
                                String.valueOf(numberService.format(numberService.setScaleWithDefaultMathContext(planedQuantity))), e.getKey().getIdentifier());
                    } else {
                        operationProductColumnHelper = new OperationProductColumnHelper(e.getValue(),
                                "", e.getKey().getIdentifier());
                    }

                    resourceOperationProductColumnHelpers.add(operationProductColumnHelper);
                }
                index++;
                resourceOperationProductHelper.setOperationProductColumnHelpers(resourceOperationProductColumnHelpers);
                operationProductsValue.add(resourceOperationProductHelper);
            }


        }

        return operationProductsValue;
    }

    private void addOperationProductsTable(PdfPCell cell, List<Entity> operationProductComponents,
                                           Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap, ProductDirection direction,
                                           Locale locale) throws DocumentException {

        if (operationProductComponents.isEmpty()) {
            return;
        }

        int columnCount = operationProductColumnAlignmentMap.size();

        Map<String, HeaderAlignment> headerAlignments = new HashMap<>(columnCount);
        List<String> headers = new ArrayList<String>(columnCount);
        float[] widths = fill(locale, operationProductColumnAlignmentMap, headers, headerAlignments, direction);

        PdfPTable table = pdfHelper.createTableWithHeader(columnCount, headers, false, headerAlignments);
        table.setWidths(widths);
        PdfPCell defaultCell = table.getDefaultCell();
        for (Entity operationProduct : operationProductComponents) {
            for (Map.Entry<OperationProductColumn, ColumnAlignment> e : operationProductColumnAlignmentMap.entrySet()) {
                alignColumn(defaultCell, e.getValue(), true);
                table.addCell(operationProductPhrase(operationProduct, e.getKey()));
            }

        }
        cell.addElement(table);
    }

    private List<Entity> addMaterialComponents(List<Entity> productComponents, Entity order) {
        for (Entity productComponent : productComponents) {
            if (productComponent.getBooleanField(OperationProductInComponentFieldsWP.SHOW_MATERIAL_COMPONENT)) {
                Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                        productComponent.getIntegerField("productId").longValue());

                Entity technology = getTechnologyForComponent(productComponent, order);
                if (technology != null) {
                    Set<String> distinctProductNames = new HashSet<>();
                    EntityList operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
                    for (Entity operationComponent : operationComponents) {
                        EntityList operationProductsInComponents = operationComponent
                                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
                        List<String> ProductNames = operationProductsInComponents.stream()
                                .filter(opic -> !technologyService.isIntermediateProduct(opic))
                                .map(opic -> opic.getBelongsToField(OperationProductInComponentFields.PRODUCT))
                                .map(p -> p.getStringField(ProductFields.NAME)).collect(Collectors.toList());

                        distinctProductNames.addAll(ProductNames);
                    }
                    if (!distinctProductNames.isEmpty()) {
                        String name = product.getStringField(ProductFields.NAME) + "\n- "
                                + String.join("\n- ", distinctProductNames);
                        product.setField(ProductFields.NAME, name);
                        productComponent.setField(OperationProductInComponentFields.PRODUCT, product);
                    }
                }
            }
        }
        return productComponents;
    }

    private Entity getTechnologyForComponent(Entity productComponent, Entity order) {
        List<Entity> productOrders = order
                .getDataDefinition()
                .find()
                .add(SearchRestrictions.belongsTo("parent", order))
                .add(SearchRestrictions.belongsTo("product", BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                        productComponent.getIntegerField("productId").longValue())).list().getEntities();
        if (productOrders != null && !productOrders.isEmpty()) {
            return productOrders.get(0).getBelongsToField(OrderFields.TECHNOLOGY);
        } else {
            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            return technologyDD
                    .find()
                    .add(SearchRestrictions.belongsTo("product", BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                            productComponent.getIntegerField("productId").longValue()))
                    .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).setMaxResults(1).uniqueResult();
        }
    }

    private void alignColumn(final PdfPCell cell, final ColumnAlignment columnAlignment, boolean setBorder) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        if(setBorder) {
            cell.setBorder(Rectangle.BOX);
        }
        cell.setPadding(2f);
    }

    private float[] fill(Locale locale, Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap,
                         List<String> headers, Map<String, HeaderAlignment> headerAlignments, ProductDirection direction) {
        // for optimization method fills two collections simultaneously
        float[] widths = new float[operationProductColumnAlignmentMap.size()];
        if (widths.length > 3) {
            widths[0] = 12f;
            widths[1] = 4f;
            widths[2] = 4f;
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

    private Phrase operationProductPhrase(final String value) {
        return new Phrase(value, FontUtils.getDejavuRegular7Dark());
    }


    private HeaderAlignment headerAlignment(ColumnAlignment value) {
        return ColumnAlignment.LEFT.equals(value) ? HeaderAlignment.LEFT : HeaderAlignment.RIGHT;
    }

    private Image createBarcode(PdfWriter pdfWriter, Entity order, Entity operationComponent) throws DocumentException {
        PdfContentByte cb = pdfWriter.getDirectContent();
        Barcode128 code128 = new Barcode128();
        code128.setCode(barcodeOperationComponentService.getCodeFromBarcode(order, operationComponent));
        return code128.createImageWithBarcode(cb, null, null);
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
        Entity pcq = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .setMaxResults(1)
                .uniqueResult();
        if (Objects.nonNull(pcq)) {
            appendAttribiutes(summary, pcq.getHasManyField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES));
        }
        Entity form = product.getBelongsToField(ProductFields.PRODUCT_FORM);
        if (Objects.nonNull(form)) {
            summary.append(", ");
            summary.append(form.getStringField("number"));
        }

        return summary.toString();
    }

    private void appendAttribiutes(StringBuilder builder, List<Entity> attrValues) {
        Map<String, List<String>> valuesByAttribute = Maps.newHashMap();
        attrValues.forEach(prodAttrVal -> {
            if (prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).isActive()) {
                String number = prodAttrVal.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getStringField(AttributeFields.NUMBER);
                if (valuesByAttribute.containsKey(number)) {
                    valuesByAttribute.get(number).add(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE));
                } else {
                    valuesByAttribute.put(number, Lists.newArrayList(prodAttrVal.getStringField(ProductAttributeValueFields.VALUE)));
                }
            }
        });
        for (Map.Entry<String, List<String>> entry : valuesByAttribute.entrySet()) {
            builder.append(", ");
            builder.append(entry.getKey()).append(": ");
            builder.append(String.join(", ", entry.getValue()));
        }
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

    private List<OrderOperationComponent> sortOrderOperationComponents(final List<OrderOperationComponent> components) {

        List<OrderOperationComponent> sorted = components.stream().sorted(new Comparator<OrderOperationComponent>() {

            @Override
            public int compare(OrderOperationComponent o1, OrderOperationComponent o2) {
                Entity order1 = o1.getOrder();
                Entity order2 = o2.getOrder();
                return order1.getStringField(OrderFields.NUMBER).compareTo(order2.getStringField(OrderFields.NUMBER));
            }
        }).collect(Collectors.toList());
        Collections.reverse(sorted);
        return sorted;
    }

}
