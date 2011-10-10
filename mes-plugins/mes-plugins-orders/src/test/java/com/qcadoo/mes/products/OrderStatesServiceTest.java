package com.qcadoo.mes.products;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.orders.states.OrderStateChangingService;
import com.qcadoo.mes.orders.states.OrderStatesViewService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class OrderStatesServiceTest {

    private OrderStatesViewService orderStatesService;

    private DataDefinitionService dataDefinitionService;

    private ViewDefinitionState viewDefinitionState;

    private ComponentState componentState;

    private Entity order;

    private OrderStateChangingService orderStateChangingService;

    private Entity logging;

    private FormComponent form;

    private DataDefinition dataDefinition;

    @Before
    public void init() {

        orderStatesService = new OrderStatesViewService();

        dataDefinitionService = mock(DataDefinitionService.class);
        viewDefinitionState = mock(ViewDefinitionState.class);
        dataDefinition = mock(DataDefinition.class);
        form = mock(FormComponent.class);
        order = mock(Entity.class);
        logging = mock(Entity.class);
        orderStateChangingService = mock(OrderStateChangingService.class);

        when(viewDefinitionState.getComponentByReference("form")).thenReturn(form);
        when(form.getEntity()).thenReturn(order);
        when(order.getDataDefinition()).thenReturn(dataDefinition);

        setField(orderStatesService, "dataDefinitionService", dataDefinitionService);
        setField(orderStatesService, "orderStateChangingService", orderStateChangingService);

    }

    @Test
    public void shouldChangeStateToAcceptedWhenOrderStatePending() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("01pending");
        orderStatesService.changeOrderStateToAccepted(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "01pending", "02accepted");

        // then
        verify(order).setField("state", "02accepted");
        verify(order.getDataDefinition()).save(order);
    }

    //
    // @Test
    // public void shouldChangeStateToInProgressWhenOrderStateAccepted() throws Exception {
    // // when
    // when(order.getStringField("state")).thenReturn("02accepted");
    // orderStatesService.changeOrderStateToInProgress(viewDefinitionState, componentState, new String[0]);
    // orderStateChangingService.saveLogging(order, "02pending", "03inProgress");
    // // then
    // verify(order).setField("state", "03inProgress");
    // verify(order.getDataDefinition()).save(order);
    // }

    @Test
    public void shouldChangeStateToDeclinedWhenOrderStatePending() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("01pending");
        orderStatesService.changeOrderStateToDeclined(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "01pending", "05declined");
        // then
        verify(order).setField("state", "05declined");
        verify(order.getDataDefinition()).save(order);
    }

    @Test
    public void shouldChangeStateToDeclinedWhenOrderStateAccepted() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("02accepted");
        orderStatesService.changeOrderStateToDeclined(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "02accepted", "05declined");
        // then
        verify(order).setField("state", "05declined");
        verify(order.getDataDefinition()).save(order);
    }

    //
    // @Test
    // public void shouldChangeStateToInProgressWhenOrderStateInterrupted() throws Exception {
    // // when
    // when(order.getStringField("state")).thenReturn("06interrupted");
    // orderStatesService.changeOrderStateToInProgress(viewDefinitionState, componentState, new String[0]);
    // orderStateChangingService.saveLogging(order, "06interrupted", "03inProgress");
    // // then
    // verify(order).setField("state", "03inProgress");
    // verify(order.getDataDefinition()).save(order);
    // }

    @Test
    public void shouldChangeStateToAbandonedWhenOrderStateInProgress() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("03inProgress");
        orderStatesService.changeOrderStateToAbandoned(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "03inProgress", "07abandoned");
        // then
        verify(order).setField("state", "07abandoned");
        verify(order.getDataDefinition()).save(order);
    }

    @Test
    public void shouldChangeStateToAbandonedWhenOrderStateInterrupted() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("06interrupted");
        orderStatesService.changeOrderStateToAbandoned(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "06interrupted", "07abandoned");
        // then
        verify(order).setField("state", "07abandoned");
        verify(order.getDataDefinition()).save(order);
    }

    @Test
    public void shouldChangeStateToInterruptedWhenOrderStateInProgress() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("03inProgress");
        orderStatesService.changeOrderStateToInterrupted(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "03inProgress", "06interrupted");
        // then
        verify(order).setField("state", "06interrupted");
        verify(order.getDataDefinition()).save(order);
    }

    @Test
    public void shouldChangeStateToCompletedWhenOrderStateInProgress() throws Exception {
        // when
        when(order.getStringField("state")).thenReturn("03inProgress");
        orderStatesService.changeOrderStateToCompleted(viewDefinitionState, componentState, new String[0]);
        orderStateChangingService.saveLogging(order, "03inProgress", "04completed");
        // then
        verify(order).setField("state", "04completed");
        verify(order.getDataDefinition()).save(order);
    }
}
