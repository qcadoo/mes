/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.workPlans.print.ColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@Component
public class WorkPlansColumnFiller implements ColumnFiller {

    // TODO mici, those constants will end up as duplication somewhere,
    // in the columnLoader probably, they should be either here or there.

    private static final String L_OPERATION_PRODUCT_OUT_COMPONENTS = "operationProductOutComponents";

    private static final String L_OPERATION_PRODUCT_IN_COMPONENTS = "operationProductInComponents";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_REFERENCE_TECHNOLOGY = "referenceTechnology";

    private static final String PRODUCT_COLUMN = "productName";

    private static final String QUANTITY_COLUMN = "plannedQuantity";

    private static final String ORDER_NAME_COLUMN = "orderName";

    private static final String ORDER_NUMBER_COLUMN = "orderNumber";

    private static final String ORDER_PRODUCT_NAME_COLUMN = "productName";

    private static final String PLANNED_QUANTITY_COLUMN = "plannedQuantity";

    private static final String PLANNED_END_DATE_COLUMN = "plannedEndDate";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private NumberService numberService;

    @Override
    public Map<Entity, Map<String, String>> getOrderValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity order : orders) {
            fillOrderNames(order, values);
            fillOrderNumbers(order, values);
            fillOrderPlannedEndDate(order, values);
            fillOrderPlannedQuantity(order, values);
            fillOrderProductNumbers(order, values);
        }

        return values;
    }

    private void initMap(final Map<Entity, Map<String, String>> valuesMap, final Entity order) {
        if (valuesMap.get(order) == null) {
            valuesMap.put(order, new HashMap<String, String>());
        }
    }

    private void fillOrderNames(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);
        valuesMap.get(order).put(ORDER_NAME_COLUMN, order.getStringField(NAME));
    }

    private void fillOrderNumbers(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);
        valuesMap.get(order).put(ORDER_NUMBER_COLUMN, order.getStringField(NUMBER));
    }

    private void fillOrderPlannedQuantity(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        String qty = "-";
        if (order.getField(PLANNED_QUANTITY_COLUMN) != null) {
            qty = numberService.format(order.getField(PLANNED_QUANTITY_COLUMN)) + " "
                    + order.getBelongsToField(PRODUCT).getStringField(UNIT);
        }

        valuesMap.get(order).put(PLANNED_QUANTITY_COLUMN, qty);
    }

    private void fillOrderPlannedEndDate(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        String formattedDateTo = "-";

        if (order.getField(DATE_TO) != null) {
            synchronized (this) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.L_DATE_FORMAT, LocaleContextHolder.getLocale());
                formattedDateTo = dateFormat.format((Date) order.getField(DATE_TO));
            }
        }

        valuesMap.get(order).put(PLANNED_END_DATE_COLUMN, formattedDateTo);
    }

    private void fillOrderProductNumbers(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        Entity product = order.getBelongsToField(PRODUCT);
        String name = product.getStringField(NAME) + " (" + product.getStringField(NUMBER) + ")";
        valuesMap.get(order).put(ORDER_PRODUCT_NAME_COLUMN, name);
    }

    @Override
    public Map<Entity, Map<String, String>> getValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getProductComponentQuantities(orders);

        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(TECHNOLOGY);
            fillProductNames(technology, values);
            fillPlannedQuantities(technology, productQuantities, values);
        }

        return values;
    }

    private void fillProductNames(final Entity technology, final Map<Entity, Map<String, String>> valuesMap) {
        // TODO mici, change those to technologyInstanceOperationComponents?
        EntityTree operationComponents = technology.getTreeField(OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            if (L_REFERENCE_TECHNOLOGY.equals(operationComponent.getStringField(L_ENTITY_TYPE))) {
                Entity refTech = operationComponent.getBelongsToField(L_REFERENCE_TECHNOLOGY);
                fillProductNames(refTech, valuesMap);
                continue;
            }

            EntityList inputProducts = operationComponent.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);
            EntityList outputProducts = operationComponent.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);

            for (Entity productComponent : outputProducts) {
                initMap(valuesMap, productComponent);

                valuesMap.get(productComponent).put(PRODUCT_COLUMN, getProductName(productComponent));
            }

            for (Entity productComponent : inputProducts) {
                initMap(valuesMap, productComponent);

                valuesMap.get(productComponent).put(PRODUCT_COLUMN, getProductName(productComponent));
            }
        }
    }

    private void fillPlannedQuantities(final Entity technology, final Map<Entity, BigDecimal> productQuantities,
            final Map<Entity, Map<String, String>> valuesMap) {
        // TODO mici, change those to technologyInstanceOperationComponents?
        EntityTree operationComponents = technology.getTreeField(OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            if (L_REFERENCE_TECHNOLOGY.equals(operationComponent.getStringField(L_ENTITY_TYPE))) {
                Entity refTech = operationComponent.getBelongsToField(L_REFERENCE_TECHNOLOGY);
                fillPlannedQuantities(refTech, productQuantities, valuesMap);
                continue;
            }

            EntityList inputProducts = operationComponent.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);
            EntityList outputProducts = operationComponent.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);

            for (Entity productComponent : outputProducts) {
                initMap(valuesMap, productComponent);

                String unit = productComponent.getBelongsToField(MODEL_PRODUCT).getStringField(UNIT);
                String quantityString = numberService.format(productQuantities.get(productComponent)) + " " + unit;
                valuesMap.get(productComponent).put(QUANTITY_COLUMN, quantityString);
            }

            for (Entity productComponent : inputProducts) {
                initMap(valuesMap, productComponent);

                String unit = productComponent.getBelongsToField(MODEL_PRODUCT).getStringField(UNIT);
                String quantityString = numberService.format(productQuantities.get(productComponent)) + " " + unit;
                valuesMap.get(productComponent).put(QUANTITY_COLUMN, quantityString);
            }
        }
    }

    private String getProductName(final Entity productComponent) {
        Entity product = productComponent.getBelongsToField(MODEL_PRODUCT);

        StringBuilder productString = new StringBuilder(product.getStringField(NAME));
        productString.append(" (");
        productString.append(product.getStringField(NUMBER));
        productString.append(")");

        return productString.toString();
    }
}
