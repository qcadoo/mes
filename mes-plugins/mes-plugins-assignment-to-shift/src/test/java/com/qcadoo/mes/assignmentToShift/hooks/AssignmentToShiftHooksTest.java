package com.qcadoo.mes.assignmentToShift.hooks;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class AssignmentToShiftHooksTest {

    private AssignmentToShiftHooks hooks;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity;

    @Before
    public void init() {
        hooks = new AssignmentToShiftHooks();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldClearFieldStateOnCopy() throws Exception {
        // given
        String state = "01draft";
        Mockito.when(entity.getStringField("state")).thenReturn(state);
        // when

        hooks.clearState(dataDefinition, entity);
        // then
        Assert.assertEquals("01draft", state);
    }

}
