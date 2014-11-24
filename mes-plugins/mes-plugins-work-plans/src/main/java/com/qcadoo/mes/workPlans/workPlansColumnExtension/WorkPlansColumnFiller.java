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
package com.qcadoo.mes.workPlans.workPlansColumnExtension;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentEntityType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.workPlans.print.ColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkPlansColumnFiller implements ColumnFiller {

    private static final String L_ORDER_NAME = "orderName";

    private static final String L_ORDER_NUMBER = "orderNumber";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_PLANNED_END_DATE = "plannedEndDate";

    private static final String L_PRODUCT_NAME = "productName";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

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

    @Override
    public Map<Entity, Map<String, String>> getValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        OperationProductComponentWithQuantityContainer productQuantities = productQuantitiesService
                .getProductComponentQuantities(orders);

        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            fillProductNames(technology, values);
            fillPlannedQuantities(technology, productQuantities, values);
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

        valuesMap.get(order).put(L_ORDER_NAME, order.getStringField(OrderFields.NAME));
    }

    private void fillOrderNumbers(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        valuesMap.get(order).put(L_ORDER_NUMBER, order.getStringField(OrderFields.NUMBER));
    }

    private void fillOrderPlannedQuantity(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        String qty = "-";
        if (order.getField(OrderFields.PLANNED_QUANTITY) != null) {
            qty = numberService.format(order.getField(OrderFields.PLANNED_QUANTITY)) + " "
                    + order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT);
        }

        valuesMap.get(order).put(L_PLANNED_QUANTITY, qty);
    }

    private void fillOrderPlannedEndDate(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        String formattedDateTo = "-";

        if (order.getField(OrderFields.DATE_TO) != null) {
            synchronized (this) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.L_DATE_FORMAT, LocaleContextHolder.getLocale());
                formattedDateTo = dateFormat.format((Date) order.getField(OrderFields.DATE_TO));
            }
        }

        valuesMap.get(order).put(L_PLANNED_END_DATE, formattedDateTo);
    }

    private void fillOrderProductNumbers(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        initMap(valuesMap, order);

        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        String name = product.getStringField(ProductFields.NAME) + " (" + product.getStringField(ProductFields.NUMBER) + ")";

        valuesMap.get(order).put(L_PRODUCT_NAME, name);
    }

    private void fillProductNames(final Entity technology, final Map<Entity, Map<String, String>> valuesMap) {
        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            if (TechnologyOperationComponentEntityType.REFERENCE_TECHNOLOGY.getStringValue().equals(
                    operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                Entity referenceTechnology = operationComponent
                        .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY);

                fillProductNames(referenceTechnology, valuesMap);

                continue;
            }

            EntityList operationProductInComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
            EntityList operationProductOutComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            for (Entity operationProductInComponent : operationProductInComponents) {
                initMap(valuesMap, operationProductInComponent);

                valuesMap.get(operationProductInComponent).put(L_PRODUCT_NAME,
                        getProductNameAndNumber(operationProductInComponent));
            }

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                initMap(valuesMap, operationProductOutComponent);

                valuesMap.get(operationProductOutComponent).put(L_PRODUCT_NAME,
                        getProductNameAndNumber(operationProductOutComponent));
            }
        }
    }

    private void fillPlannedQuantities(final Entity technology,
            final OperationProductComponentWithQuantityContainer productQuantities,
            final Map<Entity, Map<String, String>> valuesMap) {
        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            if (TechnologyOperationComponentEntityType.REFERENCE_TECHNOLOGY.getStringValue().equals(
                    operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                Entity referenceTechnology = operationComponent
                        .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY);

                fillPlannedQuantities(referenceTechnology, productQuantities, valuesMap);

                continue;
            }

            EntityList operationProductInComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
            EntityList operationProductOutComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            for (Entity operationProductInComponent : operationProductInComponents) {
                initMap(valuesMap, operationProductInComponent);

                valuesMap.get(operationProductInComponent).put(L_PLANNED_QUANTITY,
                        getProductQuantityAndUnit(operationProductInComponent, productQuantities));
            }

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                initMap(valuesMap, operationProductOutComponent);

                valuesMap.get(operationProductOutComponent).put(L_PLANNED_QUANTITY,
                        getProductQuantityAndUnit(operationProductOutComponent, productQuantities));
            }
        }
    }

    private String getProductNameAndNumber(final Entity operationProductComponent) {
        Entity product = operationProductComponent.getBelongsToField("product");

        String name = product.getStringField(ProductFields.NAME);
        String number = product.getStringField(ProductFields.NUMBER);

        String productNameAndNumber = name + " (" + number + ")";

        return productNameAndNumber;
    }

    private String getProductQuantityAndUnit(final Entity operationProductComponent,
            final OperationProductComponentWithQuantityContainer productQuantities) {
        Entity product = operationProductComponent.getBelongsToField("product");

        String unit = product.getStringField(ProductFields.UNIT);
        String quantity = numberService.format(productQuantities.get(operationProductComponent));
        String productQuantityAndUnit = quantity + " " + unit;

        return productQuantityAndUnit;
    }

}
