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
package com.qcadoo.mes.workPlans.pdf.document.order.component;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.HeaderAlignment;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;

@Component
public class OrderTable {

    private TranslationService translationService;
    private PdfHelper pdfHelper;

    @Autowired
    public OrderTable(TranslationService translationService, PdfHelper pdfHelper){
        this.translationService = translationService;
        this.pdfHelper = pdfHelper;
    }

    public void print(GroupingContainer groupingContainer, Document document, Locale locale) throws DocumentException {
        document.add(ordersTableHeaderParagraph(locale));
        Map<OrderColumn, ColumnAlignment> orderColumnToAlignment = groupingContainer.getOrderColumnToAlignment();
        int columnCount = orderColumnToAlignment.size();

        Map<String, HeaderAlignment> headerAlignments = new HashMap<String, HeaderAlignment>(columnCount);
        List<String> headers = new ArrayList<String>(columnCount);
        fill(locale, orderColumnToAlignment, headers, headerAlignments);

        PdfPTable orderTable = pdfHelper.createTableWithHeader(columnCount,headers, false, headerAlignments);
        PdfPCell defaultCell = orderTable.getDefaultCell();
        for (Entity order : groupingContainer.getOrders()) {
            for (Map.Entry<OrderColumn, ColumnAlignment> e : orderColumnToAlignment.entrySet()) {
                alignColumn(defaultCell, e.getValue());
                orderTable.addCell(orderColumnValuePhrase(order, e.getKey()));
            }
        }

        document.add(orderTable);
        document.add(Chunk.NEWLINE);
    }

    private void fill(Locale locale, Map<OrderColumn, ColumnAlignment> orderColumnToAlignment,
                      List<String> headers, Map<String, HeaderAlignment> headerAlignments) {
        //for optimization method fills two collections simultaneously
        for (Map.Entry<OrderColumn, ColumnAlignment> entry : orderColumnToAlignment.entrySet()) {
            String name = entry.getKey().getName(locale);
            headerAlignments.put(name, headerAlignment(entry.getValue()));
            headers.add(name);
        }

    }

    private void alignColumn(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    private Phrase orderColumnValuePhrase(Entity order, OrderColumn workPlanPdfOrderColumn) {
        return new Phrase(workPlanPdfOrderColumn.getColumnValue(order), FontUtils.getDejavuRegular7Dark());
    }

    private Paragraph ordersTableHeaderParagraph(Locale locale) {
        return new Paragraph(ordersTableHeader(locale), FontUtils.getDejavuBold11Dark());
    }

    private String ordersTableHeader(Locale locale) {
        return translationService.translate("workPlans.workPlan.report.ordersTable", locale);
    }

    private HeaderAlignment headerAlignment(ColumnAlignment value) {
        return ColumnAlignment.LEFT.equals(value) ? HeaderAlignment.LEFT : HeaderAlignment.RIGHT;
    }

}
