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
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.supplyNegotiations.NegotiationServiceImpl;
import com.qcadoo.model.api.Entity;

public class NegotiationServiceImplTSFNOverrideAspectTest {

    @Test
    public final void checkGetCompaniesForNegotiationProductExecution() throws NoSuchMethodException {
        Class<?> clazz = NegotiationServiceImpl.class;
        assertEquals("com.qcadoo.mes.supplyNegotiations.NegotiationServiceImpl", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("getCompaniesForNegotiationProduct", Entity.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    public final void checkCreateRequestForQuotationProductExecution() throws NoSuchMethodException {
        Class<?> clazz = NegotiationServiceImpl.class;
        assertEquals("com.qcadoo.mes.supplyNegotiations.NegotiationServiceImpl", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("createRequestForQuotationProduct", Entity.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(Entity.class, method.getReturnType());
    }

}
