package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields.ORDER;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationalTaskHooksOTFOTest {

    private OperationalTaskHooksOTFO hooksOTFO;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, order, technology;

    @Before
    public void init() {
        hooksOTFO = new OperationalTaskHooksOTFO();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnTrueWhenOrderIsNotSave() throws Exception {
        // given
        when(entity.getBelongsToField(ORDER)).thenReturn(null);

        // when
        boolean result = hooksOTFO.checkIfOrderHasTechnology(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenOrderDoesNotHaveTechnology() throws Exception {
        // given
        when(entity.getBelongsToField(ORDER)).thenReturn(order);
        when(order.getBelongsToField("technology")).thenReturn(null);
        // when
        boolean result = hooksOTFO.checkIfOrderHasTechnology(dataDefinition, entity);
        // then
        Assert.assertFalse(result);
        Mockito.verify(entity).addError(dataDefinition.getField(ORDER),
                "operationalTasks.operationalTask.order.error.technologyIsNull");
    }

    @Test
    public void shouldReturnTrueWhenOrderHaveTechnology() throws Exception {
        // given
        when(entity.getBelongsToField(ORDER)).thenReturn(order);
        when(order.getBelongsToField("technology")).thenReturn(technology);
        // when
        boolean result = hooksOTFO.checkIfOrderHasTechnology(dataDefinition, entity);

        // then
        Assert.assertTrue(result);
    }
}
