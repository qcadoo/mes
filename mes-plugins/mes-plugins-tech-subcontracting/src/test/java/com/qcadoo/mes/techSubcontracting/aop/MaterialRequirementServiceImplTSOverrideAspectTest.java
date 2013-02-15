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

import org.junit.Test;

import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementServiceImpl;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;

public class MaterialRequirementServiceImplTSOverrideAspectTest {

    @Test
    public final void checkGetDefaultMrpAlgorithmExecution() throws NoSuchMethodException {
        Class<?> clazz = MaterialRequirementServiceImpl.class;
        assertEquals("com.qcadoo.mes.materialRequirements.internal.MaterialRequirementServiceImpl", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("getDefaultMrpAlgorithm");
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
        assertEquals(MrpAlgorithm.class, method.getReturnType());
    }

}
