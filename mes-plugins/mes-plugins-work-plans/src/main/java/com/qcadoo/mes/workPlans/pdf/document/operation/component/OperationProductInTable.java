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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import com.google.common.collect.Lists;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.UnitOperationProductColumn;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OperationProductInTable {

    public static final String L_UNIT_OPERATION_PRODUCT_COLUMN = "unitOperationProductColumn";
    public static final String L_PRODUCT_NAME_OPERATION_PRODUCT_COLUMN = "productNameOperationProductColumn";
    public static final String L_PLANNED_QUANTITY_OPERATION_PRODUCT_COLUMN = "plannedQuantityOperationProductColumn";
    public static final String L_PLANED_QUANTITY = "planedQuantity";
    public static final String L_RESOURCE_NUMBER = "resourceNumber";
    public static final String L_RESOURCE_UNIT = "resourceUnit";
    private final PdfHelper pdfHelper;

    @Autowired
    private WorkPlansService workPlansService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    public OperationProductInTable(final PdfHelper pdfHelper) {
        this.pdfHelper = pdfHelper;
    }

    public void print(final Entity workPlan, final GroupingContainer groupingContainer,
                      final OrderOperationComponent orderOperationComponent, final Document document, final Locale locale)
            throws DocumentException {
        Entity order = orderOperationComponent.getOrder();
        Entity operationComponent = orderOperationComponent.getOperationComponent();
        Map<Long, Map<OperationProductColumn, ColumnAlignment>> map = groupingContainer
                .getOperationComponentIdProductInColumnToAlignment();
        Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap = map.get(operationComponent.getId());

        int columnCount = operationProductColumnAlignmentMap.size();

        Map<String, HeaderAlignment> headerAlignments = new HashMap<>(columnCount);
        List<String> headers = new ArrayList<>(columnCount);
        fill(locale, operationProductColumnAlignmentMap, headers, headerAlignments);

        PdfPTable table = pdfHelper.createTableWithHeader(columnCount, headers, false, headerAlignments);
        PdfPCell defaultCell = table.getDefaultCell();

        List<OperationProductHelper> operationProductsValue = prepareOperationProductsValue(order,
                orderOperationComponent.getProductionCountingQuantitiesIn(), operationProductColumnAlignmentMap.entrySet());
        operationProductsValue = workPlansService.sortByColumn(workPlan, operationProductsValue, headers);

        for (OperationProductHelper operationProduct : operationProductsValue) {
            if(operationProduct.isContainsResources() && !operationProduct.isLastResource()) {
               table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            } else {
               table.getDefaultCell().setBorder(Rectangle.TOP);
               table.getDefaultCell().setBorder(Rectangle.BOTTOM);
            }


            for (OperationProductColumnHelper e : operationProduct.getOperationProductColumnHelpers()) {
                alignColumn(defaultCell, e.getColumnAlignment());
                table.addCell(operationProductPhrase(e.getValue()));
            }
        }

        int additionalRows = workPlansService.getAdditionalRowsFromParameter(ParameterFieldsWP.ADDITIONAL_INPUT_ROWS);

        for (int i = 0; i < additionalRows; i++) {
            for (Map.Entry<OperationProductColumn, ColumnAlignment> e : operationProductColumnAlignmentMap.entrySet()) {
                alignColumn(defaultCell, e.getValue());
                table.addCell(" ");
            }
        }

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);

        document.add(table);
    }

    private void alignColumn(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    private void fill(final Locale locale, final Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap,
                      final List<String> headers, final Map<String, HeaderAlignment> headerAlignments) {
        // for optimization method fills two collections simultaneously
        for (Map.Entry<OperationProductColumn, ColumnAlignment> entry : operationProductColumnAlignmentMap.entrySet()) {
            String name = entry.getKey().getName(locale, ProductDirection.IN);
            headerAlignments.put(name, headerAlignment(entry.getValue()));
            headers.add(name);
        }
    }

    private Phrase operationProductPhrase(final Entity operationProduct, final OperationProductColumn operationProductColumn) {
        return new Phrase(operationProductColumn.getColumnValue(operationProduct), FontUtils.getDejavuRegular7Dark());
    }

    private Phrase operationProductPhrase(final String value) {
        return new Phrase(value, FontUtils.getDejavuRegular7Dark());
    }

    private HeaderAlignment headerAlignment(final ColumnAlignment value) {
        return ColumnAlignment.LEFT.equals(value) ? HeaderAlignment.LEFT : HeaderAlignment.RIGHT;
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

}
