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
package com.qcadoo.mes.workPlans.print;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ColumnFetcher {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ApplicationContext applicationContext;

    public Map<Entity, Map<String, String>> getOrderColumnValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> valuesMap = new HashMap<Entity, Map<String, String>>();

        fetchColumnValues(orders, "columnForOrders", "getOrderValues", valuesMap);

        return valuesMap;
    }

    public Map<Entity, Map<String, String>> getColumnValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> valuesMap = new HashMap<Entity, Map<String, String>>();

        for (String columnDefinitionModel : Arrays.asList("columnForInputProducts", "columnForOutputProducts")) {
            fetchColumnValues(orders, columnDefinitionModel, "getValues", valuesMap);
        }

        return valuesMap;
    }

    @SuppressWarnings("unchecked")
    private void fetchColumnValues(final List<Entity> orders, final String ddModel, final String methodString,
            final Map<Entity, Map<String, String>> valuesMap) {
        DataDefinition dd = dataDefinitionService.get("workPlans", ddModel);

        List<Entity> columnDefinitions = dd.find().list().getEntities();

        Set<String> classesStrings = new HashSet<String>();

        for (Entity columnDefinition : columnDefinitions) {
            String classString = columnDefinition.getStringField("columnFiller");
            classesStrings.add(classString);
        }

        for (String classString : classesStrings) {
            Class<?> clazz;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(classString);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to find class: " + classString, e);
            }

            Object bean = applicationContext.getBean(clazz);

            if (bean == null) {
                throw new IllegalStateException("Failed to find bean for class: " + classString);
            }

            Method method;

            try {
                method = clazz.getMethod(methodString, List.class);
            } catch (SecurityException e) {
                throw new IllegalStateException("Failed to find column evaulator method in class: " + classString, e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to find column evaulator method in class: " + classString, e);
            }

            Map<Entity, Map<String, String>> values;

            String invokeMethodError = "Failed to invoke column evaulator method";
            try {
                values = (Map<Entity, Map<String, String>>) method.invoke(bean, orders);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(invokeMethodError, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(invokeMethodError, e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(invokeMethodError, e);
            }

            for (Entry<Entity, Map<String, String>> entry : values.entrySet()) {
                if (valuesMap.containsKey(entry.getKey())) {
                    for (Entry<String, String> deepEntry : entry.getValue().entrySet()) {
                        valuesMap.get(entry.getKey()).put(deepEntry.getKey(), deepEntry.getValue());
                    }
                } else {
                    valuesMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
