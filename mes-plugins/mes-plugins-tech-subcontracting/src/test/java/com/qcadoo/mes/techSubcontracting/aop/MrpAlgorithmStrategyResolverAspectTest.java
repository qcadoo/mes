/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.techSubcontracting.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;

public class MrpAlgorithmStrategyResolverAspectTest {

    @Test
    public final void checkGetProductsPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = ProductQuantitiesServiceImpl.class;
        assertEquals("com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("getProductWithQuantities",
                OperationProductComponentWithQuantityContainer.class, Set.class, MrpAlgorithm.class, String.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(Map.class, method.getReturnType());
    }

}
