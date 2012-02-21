package com.qcadoo.mes.workPlans;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

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
