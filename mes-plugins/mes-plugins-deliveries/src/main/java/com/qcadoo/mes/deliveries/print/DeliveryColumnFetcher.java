/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.deliveries.print;

import static com.qcadoo.mes.deliveries.constants.ColumnForDeliveriesFields.COLUMN_FILLER;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryColumnFetcher {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ApplicationContext applicationContext;

    public Map<Entity, DeliveryProduct> getProductWithDeliveryProducts(final Entity delivery) {
        Map<Entity, DeliveryProduct> productWithDeliveryProducts = new LinkedHashMap<Entity, DeliveryProduct>();

        List<Entity> orderedProducts = delivery.getHasManyField(ORDERED_PRODUCTS);
        List<Entity> deliveredProducts = delivery.getHasManyField(DELIVERED_PRODUCTS);

        for (Entity orderedProduct : orderedProducts) {
            DeliveryProduct deliveryProduct = new DeliveryProduct();

            deliveryProduct.setOrderedProductId(orderedProduct.getId());

            productWithDeliveryProducts.put(orderedProduct.getBelongsToField(PRODUCT), deliveryProduct);
        }

        for (Entity deliveredProduct : deliveredProducts) {
            if (productWithDeliveryProducts.containsKey(deliveredProduct.getBelongsToField(PRODUCT))) {
                DeliveryProduct deliveryProduct = productWithDeliveryProducts.get(deliveredProduct.getBelongsToField(PRODUCT));

                deliveryProduct.setDeliveredProductId(deliveredProduct.getId());

                productWithDeliveryProducts.put(deliveredProduct.getBelongsToField(PRODUCT), deliveryProduct);
            } else {
                DeliveryProduct deliveryProduct = new DeliveryProduct();

                deliveryProduct.setDeliveredProductId(deliveredProduct.getId());

                productWithDeliveryProducts.put(deliveredProduct.getBelongsToField(PRODUCT), deliveryProduct);
            }
        }

        return productWithDeliveryProducts;
    }

    public Map<Entity, Map<String, String>> getDeliveryProductsColumnValues(
            final Map<Entity, DeliveryProduct> productWithDeliveryProducts) {
        Map<Entity, Map<String, String>> deliveryProductsColumnValues = new HashMap<Entity, Map<String, String>>();

        fetchColumnValues(deliveryProductsColumnValues, "getDeliveryProductsColumnValues", productWithDeliveryProducts);

        return deliveryProductsColumnValues;
    }

    @SuppressWarnings("unchecked")
    private void fetchColumnValues(final Map<Entity, Map<String, String>> columnValues, final String methodName,
            final Map<Entity, DeliveryProduct> productWithDeliveryProducts) {
        List<Entity> columnsForDeliveries = deliveriesService.getColumnsForDeliveries();

        Set<String> classNames = new HashSet<String>();

        for (Entity columnForDeliveries : columnsForDeliveries) {
            String className = columnForDeliveries.getStringField(COLUMN_FILLER);
            classNames.add(className);
        }

        for (String className : classNames) {
            Class<?> clazz;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to find class: " + className, e);
            }

            Object bean = applicationContext.getBean(clazz);

            if (bean == null) {
                throw new IllegalStateException("Failed to find bean for class: " + className);
            }

            Method method;

            try {
                method = clazz.getMethod(methodName, Map.class);
            } catch (SecurityException e) {
                throw new IllegalStateException("Failed to find column evaulator method in class: " + className, e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to find column evaulator method in class: " + className, e);
            }

            Map<Entity, Map<String, String>> values;

            String invokeMethodError = "Failed to invoke column evaulator method";
            try {
                values = (Map<Entity, Map<String, String>>) method.invoke(bean, productWithDeliveryProducts);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(invokeMethodError, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(invokeMethodError, e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(invokeMethodError, e);
            }

            for (Entry<Entity, Map<String, String>> entry : values.entrySet()) {
                if (columnValues.containsKey(entry.getKey())) {
                    for (Entry<String, String> deepEntry : entry.getValue().entrySet()) {
                        columnValues.get(entry.getKey()).put(deepEntry.getKey(), deepEntry.getValue());
                    }
                } else {
                    columnValues.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
