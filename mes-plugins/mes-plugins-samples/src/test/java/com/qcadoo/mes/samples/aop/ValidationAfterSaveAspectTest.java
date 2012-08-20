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
package com.qcadoo.mes.samples.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.junit.Test;

import com.qcadoo.mes.samples.api.SamplesLoader;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ValidationAfterSaveAspectTest {

    @Test
    public final void checkDataDefinitionSaveCallPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = DataDefinition.class;
        assertEquals("com.qcadoo.model.api.DataDefinition", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("save", Entity.class);
        assertEquals("com.qcadoo.model.api.Entity", Entity.class.getCanonicalName());
        assertNotNull(method);
        assertEquals("com.qcadoo.mes.samples.api.SamplesLoader", SamplesLoader.class.getCanonicalName());
    }

}
