/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.2.0
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
package com.qcadoo.mes.basicProductionCounting.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;

public class ProductQuantitiesServiceImplBPCOverrideAspectTest {

    @Test
    public final void checkGetProductComponentWithQuantitiesForOrdersExecution() throws NoSuchMethodException {
        Class<?> clazz = ProductQuantitiesServiceImpl.class;
        assertEquals("com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("getProductComponentWithQuantitiesForOrders", List.class, Map.class,
                Set.class, boolean.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(Map.class, method.getReturnType());
    }

}
