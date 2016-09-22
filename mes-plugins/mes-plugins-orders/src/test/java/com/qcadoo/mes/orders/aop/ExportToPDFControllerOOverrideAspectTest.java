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
package com.qcadoo.mes.orders.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;

import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.plugins.qcadooExport.internal.ExportToPDFController;

public class ExportToPDFControllerOOverrideAspectTest {

    @Test
    public final void checkAopXmlClassNameIntegrity() {
        assertEquals(ExportToPDFControllerOOverrideAspect.class.getCanonicalName(),
                "com.qcadoo.mes.orders.aop.ExportToPDFControllerOOverrideAspect");
    }

    @Test
    public final void checkPointcutIntegrity() throws SecurityException, NoSuchMethodException {
        Class<?> clazz = ExportToPDFController.class;

        assertEquals(clazz.getCanonicalName(), "com.qcadoo.plugins.qcadooExport.internal.ExportToPDFController");

        Method putMethod = clazz.getDeclaredMethod("addPdfTableCells", PdfPTable.class, List.class, List.class, String.class);

        assertNotNull(putMethod);
        assertEquals(void.class, putMethod.getReturnType());
        assertTrue(Modifier.isPrivate(putMethod.getModifiers()));
    }

    @Test
    public final void checkAspectAnnotations() {
        Class<?> clazz = ExportToPDFControllerOOverrideAspect.class;

        assertNotNull(clazz.getAnnotation(Aspect.class));
        RunIfEnabled runIfEnabledAnnotation = clazz.getAnnotation(RunIfEnabled.class);
        assertEquals(OrdersConstants.PLUGIN_IDENTIFIER, runIfEnabledAnnotation.value()[0]);
    }

}
