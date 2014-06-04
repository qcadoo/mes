/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.workPlans.hooks;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class WorkPlanHooksTest {

    private WorkPlanHooks workPlanHooks;

    @Mock
    private Entity workPlan;

    @Mock
    private DataDefinition workPlanDD;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);

        workPlanHooks = new WorkPlanHooks();
    }

    @Test
    public void shouldClearGeneratedOnCopy() {
        // given

        // when
        workPlanHooks.onCopy(workPlanDD, workPlan);

        // then
        verify(workPlan).setField(WorkPlanFields.FILE_NAME, null);
        verify(workPlan).setField(WorkPlanFields.GENERATED, false);
        verify(workPlan).setField(WorkPlanFields.DATE, null);
        verify(workPlan).setField(WorkPlanFields.WORKER, null);
    }

}
