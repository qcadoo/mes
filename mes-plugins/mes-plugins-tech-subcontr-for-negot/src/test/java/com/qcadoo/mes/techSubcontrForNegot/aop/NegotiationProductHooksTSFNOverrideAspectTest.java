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
package com.qcadoo.mes.techSubcontrForNegot.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.qcadoo.mes.supplyNegotiations.hooks.NegotiationProductHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class NegotiationProductHooksTSFNOverrideAspectTest {

    @Test
    public final void checkCheckIfNegotitionProductAlreadyExistsExecution() throws NoSuchMethodException {
        Class<?> clazz = NegotiationProductHooks.class;
        assertEquals("com.qcadoo.mes.supplyNegotiations.hooks.NegotiationProductHooks", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("checkIfNegotiationProductAlreadyExists", DataDefinition.class,
                Entity.class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    public final void updateRequestForQuotationsNumberExecution() throws NoSuchMethodException {
        Class<?> clazz = NegotiationProductHooks.class;
        assertEquals("com.qcadoo.mes.supplyNegotiations.hooks.NegotiationProductHooks", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("updateRequestForQuotationsNumber", DataDefinition.class, Entity.class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
        assertEquals(void.class, method.getReturnType());
    }

}
