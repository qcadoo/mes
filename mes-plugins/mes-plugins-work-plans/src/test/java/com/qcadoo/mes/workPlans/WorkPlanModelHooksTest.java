/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.workPlans;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.workPlans.hooks.WorkPlanModelHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class WorkPlanModelHooksTest {

    private WorkPlanModelHooks workPlanModelHooks;

    @Before
    public final void init() {
        workPlanModelHooks = new WorkPlanModelHooks();
    }

    @Test
    public void shouldClearGeneratedOnCopy() {
        // given
        Entity workPlan = mock(Entity.class);
        DataDefinition workPlanDD = mock(DataDefinition.class);

        // when
        boolean result = workPlanModelHooks.clearGeneratedOnCopy(workPlanDD, workPlan);

        // then
        assertTrue(result);

        verify(workPlan).setField("fileName", null);
        verify(workPlan).setField("generated", false);
        verify(workPlan).setField("date", null);
        verify(workPlan).setField("worker", null);
    }
}
