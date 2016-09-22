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
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String L_HEX_COLOR_PATTERN = "^#(?:[0-9a-fA-F]{3}){1,2}$";

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
            Optional<Entity> mayBeOrderCategoryColor = getOrderCategoryColor(orderCategory);

            if (mayBeOrderCategoryColor.isPresent()) {
                Entity orderCategoryColor = mayBeOrderCategoryColor.get();

                String color = orderCategoryColor.getStringField(OrderCategoryColorFields.COLOR);

                Optional<Entity> mayBeColorDictionaryItem = getColorDictionaryItem(color);

                if (mayBeColorDictionaryItem.isPresent()) {
                    Entity colorDictionaryItem = mayBeColorDictionaryItem.get();

                    String description = colorDictionaryItem.getStringField(DictionaryItemFields.DESCRIPTION);

                    if (StringUtils.isNotEmpty(description)) {
                        if (description.contains("#") && checkIfIsHexColor(description)) {
                            backgroundColor = Color.decode(description);
                        } else {
                            try {
                                Field field = Color.class.getField(description);

                                backgroundColor = (Color) field.get(null);
                            } catch (Exception e) {
                                backgroundColor = Color.WHITE;
                            }
                        }
                    }
                }
            }
        }

        return backgroundColor;
    }

    private boolean checkIfIsHexColor(final String description) {
        Pattern pattern = Pattern.compile(L_HEX_COLOR_PATTERN);
        Matcher matcher = pattern.matcher(description);

        return matcher.matches();
    }

    private Optional<Entity> getOrderCategoryColor(final String orderCategory) {
        return Optional.ofNullable(parameterService.getParameter().getHasManyField(ParameterFieldsO.ORDER_CATEGORY_COLORS).find()
                .add(SearchRestrictions.eq(OrderCategoryColorFields.ORDER_CATEGORY, orderCategory)).setMaxResults(1)
                .uniqueResult());
    }

    private Optional<Entity> getColorDictionaryItem(final String name) {
        return Optional.ofNullable(dictionariesService.getDictionaryItemDD().find()
                .add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, getColorDictionary()))
                .add(SearchRestrictions.eq(DictionaryItemFields.NAME, name)).setMaxResults(1).uniqueResult());
    }

    private Entity getColorDictionary() {
        return dictionariesService.getDictionaryDD().find().add(SearchRestrictions.eq(DictionaryFields.NAME, L_COLOR))
                .setMaxResults(1).uniqueResult();
    }

}
