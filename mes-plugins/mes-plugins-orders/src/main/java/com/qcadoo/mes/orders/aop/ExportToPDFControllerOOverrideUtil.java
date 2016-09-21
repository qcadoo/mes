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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderCategoryColorFields;
import com.qcadoo.mes.orders.constants.OrderPlanningListDtoFields;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.plugins.dictionaries.DictionariesService;
import com.qcadoo.report.api.FontUtils;

@Service
public class ExportToPDFControllerOOverrideUtil {

    private static final String L_ORDERS_PLANNING_LIST = "ordersPlanningList";

    private static final String L_COLOR = "color";

    @Autowired
    private DictionariesService dictionariesService;

    @Autowired
    private ParameterService parameterService;

    public boolean shouldOverride(final String viewName) {
        return L_ORDERS_PLANNING_LIST.equals(viewName);
    }

    public void addPdfTableCells(final PdfPTable pdfTable, final List<Map<String, String>> rows, final List<String> columns,
            final String viewName) {
        if (L_ORDERS_PLANNING_LIST.equals(viewName)) {
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
            Entity orderCategoryColor = getOrderCategoryColor(orderCategory);

            if (orderCategoryColor != null) {
                String color = orderCategoryColor.getStringField(OrderCategoryColorFields.COLOR);

                Entity colorDictionaryItem = getColorDictionaryItem(color);

                if (colorDictionaryItem != null) {
                    String description = colorDictionaryItem.getStringField(DictionaryItemFields.DESCRIPTION);

                    if (StringUtils.isNotEmpty(description)) {
                        backgroundColor = Color.decode(description);
                    }
                }
            }
        }

        return backgroundColor;
    }

    private Entity getOrderCategoryColor(String orderCategory) {
        return parameterService.getParameter().getHasManyField(ParameterFieldsO.ORDER_CATEGORY_COLORS).find()
                .add(SearchRestrictions.eq(OrderCategoryColorFields.ORDER_CATEGORY, orderCategory)).setMaxResults(1)
                .uniqueResult();
    }

    private Entity getColorDictionaryItem(final String name) {
        return dictionariesService.getDictionaryItemDD().find()
                .add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, getColorDictionary()))
                .add(SearchRestrictions.eq(DictionaryItemFields.NAME, name)).setMaxResults(1).uniqueResult();
    }

    public Entity getColorDictionary() {
        return dictionariesService.getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_COLOR))
                .setMaxResults(1).uniqueResult();
    }

}
