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
package com.qcadoo.mes.orders.aop;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.ColorService;
import com.qcadoo.mes.orders.constants.OrderCategoryColorFields;
import com.qcadoo.mes.orders.constants.OrderPlanningListDtoFields;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.FontUtils;

@Service
public class ExportToPdfControllerOOverrideUtil {

    private static final String L_ORDERS_PLANNING_LIST = "ordersPlanningList";

    private static final String L_ORDERS_LIST = "ordersList";

    @Autowired
    private ColorService colorService;

    @Autowired
    private ParameterService parameterService;

    public boolean shouldOverride(final String viewName) {
        return L_ORDERS_PLANNING_LIST.equals(viewName) || L_ORDERS_LIST.equals(viewName);
    }

    public void addPdfTableCells(final PdfPTable pdfTable, final List<Map<String, String>> rows, final List<String> columns,
            final String viewName) {
        if (L_ORDERS_PLANNING_LIST.equals(viewName) || L_ORDERS_LIST.equals(viewName)) {
            rows.forEach(row -> {
                columns.forEach(column -> {
                    String value = row.get(column);

                    pdfTable.getDefaultCell().setBackgroundColor(getBackgroundColor(value, column));
                    pdfTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                });
            });
        }
    }

    private Color getBackgroundColor(final String orderCategory, final String column) {
        Color backgroundColor = Color.WHITE;

        if (OrderPlanningListDtoFields.ORDER_CATEGORY.equals(column)) {
            Optional<Entity> mayBeOrderCategoryColor = getOrderCategoryColor(orderCategory);

            if (mayBeOrderCategoryColor.isPresent()) {
                backgroundColor = colorService
                        .getBackgroundColor(mayBeOrderCategoryColor.get().getStringField(OrderCategoryColorFields.COLOR));
            }
        }

        return backgroundColor;
    }

    private Optional<Entity> getOrderCategoryColor(final String orderCategory) {
        return Optional.ofNullable(parameterService.getParameter().getHasManyField(ParameterFieldsO.ORDER_CATEGORY_COLORS).find()
                .add(SearchRestrictions.eq(OrderCategoryColorFields.ORDER_CATEGORY, orderCategory)).setMaxResults(1)
                .uniqueResult());
    }
}
