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
package com.qcadoo.mes.technologiesGenerator;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.plugin.api.RunIfEnabled;

public class UnusedContextsCleanUpServiceTest {

    private Method getPerformCleanUpMethod() throws NoSuchMethodException {
        Class<?> clazz = UnusedContextsCleanUpService.class;
        return clazz.getMethod("performCleanUp");
    }

    @Test
    public final void shouldBeAnnotatedWithRunIfEnabled() throws NoSuchMethodException {
        RunIfEnabled runIfEnabledAnnotation = getPerformCleanUpMethod().getAnnotation(RunIfEnabled.class);
        Assert.assertNotNull("class should be annotated with @RunIfEnabled!", runIfEnabledAnnotation);
        Assert.assertArrayEquals(new String[] { TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER },
                runIfEnabledAnnotation.value());
    }

    @Test
    public final void shouldBeAnnotatedWithAsync() throws NoSuchMethodException {
        Async asyncAnnotation = getPerformCleanUpMethod().getAnnotation(Async.class);
        Assert.assertNotNull("class should be annotated with @Async!", asyncAnnotation);
    }

    @Test
    public final void shouldBeAnnotatedWithScheduled() throws NoSuchMethodException {
        Scheduled scheduledAnnotation = getPerformCleanUpMethod().getAnnotation(Scheduled.class);
        Assert.assertNotNull("class should be annotated with @Scheduled!", scheduledAnnotation);
        Assert.assertTrue("cron expression has to be given in @Scheduled annotation!",
                StringUtils.isNotBlank(scheduledAnnotation.cron()));
    }

}
