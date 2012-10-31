/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class OperationalTasksDetailsListenersOTFOOverrideAspectTest {

    @Test
    public final void checkSetProductionLineFromOrderPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = OperationalTasksDetailsListenersOTFO.class;
        assertEquals("com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO",
                clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("setProductionLineFromOrderAndClearOperation", ViewDefinitionState.class,
                ComponentState.class, String[].class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public final void checkSetOperationalNameAndDescriptionPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = OperationalTasksDetailsListenersOTFO.class;
        assertEquals("com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO",
                clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("setOperationalNameAndDescription", ViewDefinitionState.class,
                ComponentState.class, String[].class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }
}
