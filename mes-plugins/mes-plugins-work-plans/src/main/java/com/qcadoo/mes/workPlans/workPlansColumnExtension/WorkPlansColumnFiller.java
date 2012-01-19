/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.workPlans.workPlansColumnExtension;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;

import com.qcadoo.mes.workPlans.print.ColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

public class WorkPlansColumnFiller implements ColumnFiller {

    // TODO mici, those constants will end up as duplication somewhere,
    // in the columnLoader probably, they should be either here or there.

    private static final String PRODUCT_COLUMN = "productName";

    private static final String QUANTITY_COLUMN = "plannedQuantity";

    /**
     * 
     * @param orders
     *            List of orders
     * @return The Keys of the map are productComponents, values are Maps columnIdentifier -> columnValue
     */
    public Map<Entity, Map<String, String>> getValues(List<Entity> orders) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        fillProductNames(orders, values);
        fillPlannedQuantities(orders, values);

        return values;
    }

    private void fillProductNames(List<Entity> orders, Map<Entity, Map<String, String>> valuesMap) {
        for (Entity order : orders) {

            // TODO mici, change those to orderOperationComponents?
            Entity technology = order.getBelongsToField("technology");
            EntityTree operationComponents = technology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
                EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");

                for (Entity productComponent : outputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    valuesMap.get(productComponent).put(PRODUCT_COLUMN, getProductName(productComponent));
                }

                for (Entity productComponent : inputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    valuesMap.get(productComponent).put(PRODUCT_COLUMN, getProductName(productComponent));
                }

            }
        }
    }

    private void fillPlannedQuantities(List<Entity> orders, Map<Entity, Map<String, String>> valuesMap) {
        WorkPlansProductsService workPlansProductsSerivce = new WorkPlansProductsService();
        Map<Entity, BigDecimal> productQuantities = workPlansProductsSerivce.getProductQuantities(orders);

        Locale locale = LocaleContextHolder.getLocale();
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);

        for (Entity order : orders) {
            // TODO mici, change those to orderOperationComponents?
            Entity technology = order.getBelongsToField("technology");
            EntityTree operationComponents = technology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
                EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");

                for (Entity productComponent : outputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    String unit = productComponent.getBelongsToField("product").getStringField("unit");
                    String quantityString = decimalFormat.format(productQuantities.get(productComponent)) + " " + unit;
                    valuesMap.get(productComponent).put(QUANTITY_COLUMN, quantityString);
                }

                for (Entity productComponent : inputProducts) {
                    if (valuesMap.get(productComponent) == null) {
                        valuesMap.put(productComponent, new HashMap<String, String>());
                    }
                    String unit = productComponent.getBelongsToField("product").getStringField("unit");
                    String quantityString = decimalFormat.format(productQuantities.get(productComponent)) + " " + unit;
                    valuesMap.get(productComponent).put(QUANTITY_COLUMN, quantityString);
                }

            }
        }
    }

    private String getProductName(Entity productComponent) {
        Entity product = productComponent.getBelongsToField("product");

        StringBuilder productString = new StringBuilder(product.getStringField("name"));
        productString.append(" (");
        productString.append(product.getStringField("number"));
        productString.append(")");

        return productString.toString();
    }
}
