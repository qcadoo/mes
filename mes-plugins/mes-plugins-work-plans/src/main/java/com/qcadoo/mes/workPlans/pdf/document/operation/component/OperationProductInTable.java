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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationProductInTable {

    private PdfHelper pdfHelper;

    private static final String L_PRIORITY = "priority";

    @Autowired
    private WorkPlansService workPlansService;

    @Autowired
    public OperationProductInTable(PdfHelper pdfHelper) {
        this.pdfHelper = pdfHelper;
    }

    public void print(GroupingContainer groupingContainer, Entity operationComponent, Document document, Locale locale)
            throws DocumentException {
        Map<Long, Map<OperationProductColumn, ColumnAlignment>> map = groupingContainer
                .getOperationComponentIdProductInColumnToAlignment();
        Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap = map.get(operationComponent.getId());

        int columnCount = operationProductColumnAlignmentMap.size();

        Map<String, HeaderAlignment> headerAlignments = new HashMap<String, HeaderAlignment>(columnCount);
        List<String> headers = new ArrayList<String>(columnCount);
        fill(locale, operationProductColumnAlignmentMap, headers, headerAlignments);

        PdfPTable table = pdfHelper.createTableWithHeader(columnCount, headers, false, headerAlignments);
        PdfPCell defaultCell = table.getDefaultCell();
        for (Entity operationProduct : operationProductInComponents(operationComponent)) {
            for (Map.Entry<OperationProductColumn, ColumnAlignment> e : operationProductColumnAlignmentMap.entrySet()) {
                alignColumn(defaultCell, e.getValue());
                table.addCell(operationProductPhrase(operationProduct, e.getKey()));
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

    private List<Entity> operationProductInComponents(Entity operationComponent) {
        List<Entity> productOutComponents = operationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
        /*
         * TODO kama - it's unnecessary for now boolean sortProducts =
         * operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.MANUAL_ORDER_FOR_PRODUCTS); if (sortProducts) {
         * List<Entity> sortedProducts = new ArrayList<Entity>(productOutComponents); sortedProducts.sort(new Comparator<Entity>()
         * {
         * @Override public int compare(Entity product1, Entity product2) { return
         * product1.getIntegerField(L_PRIORITY).compareTo(product2.getIntegerField(L_PRIORITY)); } }); productOutComponents =
         * sortedProducts; }
         */
        return productOutComponents;
    }

    private void alignColumn(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    private void fill(Locale locale, Map<OperationProductColumn, ColumnAlignment> operationProductColumnAlignmentMap,
            List<String> headers, Map<String, HeaderAlignment> headerAlignments) {
        // for optimization method fills two collections simultaneously
        for (Map.Entry<OperationProductColumn, ColumnAlignment> entry : operationProductColumnAlignmentMap.entrySet()) {
            String name = entry.getKey().getName(locale, ProductDirection.IN);
            headerAlignments.put(name, headerAlignment(entry.getValue()));
            headers.add(name);
        }

    }

    private Phrase operationProductPhrase(Entity operationProduct, OperationProductColumn operationProductColumn) {
        return new Phrase(operationProductColumn.getColumnValue(operationProduct), FontUtils.getDejavuRegular7Dark());
    }

    private HeaderAlignment headerAlignment(ColumnAlignment value) {
        return ColumnAlignment.LEFT.equals(value) ? HeaderAlignment.LEFT : HeaderAlignment.RIGHT;
    }
}
