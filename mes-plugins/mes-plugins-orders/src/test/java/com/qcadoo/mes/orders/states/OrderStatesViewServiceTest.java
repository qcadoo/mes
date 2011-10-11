package com.qcadoo.mes.orders.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class OrderStatesViewServiceTest {

    private OrderStatesViewService orderStatesViewService;

    private OrderStateChangingService orderStateChangingService;

    private ViewDefinitionState view;

    private ComponentState state;

    private FormComponent form;

    private Entity order;

    private DataDefinition dataDefinition;

    private FieldComponent stateFromField, externalSynchronizedState;

    private PluginAccessor pluginAccessor;

    private Plugin plugin;

    @Before
    public void init() {
        orderStatesViewService = new OrderStatesViewService();

        orderStateChangingService = Mockito.mock(OrderStateChangingService.class);
        view = mock(ViewDefinitionState.class);
        state = mock(ComponentState.class);
        form = mock(FormComponent.class);
        order = mock(Entity.class);
        dataDefinition = mock(DataDefinition.class);
        stateFromField = mock(FieldComponent.class);
        externalSynchronizedState = mock(FieldComponent.class);
        pluginAccessor = mock(PluginAccessor.class);
        plugin = mock(Plugin.class);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntity()).thenReturn(order);
        when(order.getDataDefinition()).thenReturn(dataDefinition);
        when(view.getComponentByReference("state")).thenReturn(stateFromField);
        when(pluginAccessor.getEnabledPlugin("mesPluginsIntegrationErp")).thenReturn(null);
        when(view.getComponentByReference("externalSynchronized")).thenReturn(externalSynchronizedState);

        setField(orderStatesViewService, "orderStateChangingService", orderStateChangingService);
        setField(orderStatesViewService, "pluginAccessor", pluginAccessor);
    }

    @Test
    public void shouldChangeOrderStateToAccepted() throws Exception {
        // when
        orderStatesViewService.changeOrderStateToAccepted(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInProgressFromAccepted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(view.getComponentByReference("externalSynchronized")).willReturn(externalSynchronizedState);
        // when
        orderStatesViewService.changeOrderStateToInProgress(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInProgressFromInterrupted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        given(view.getComponentByReference("externalSynchronized")).willReturn(externalSynchronizedState);
        // when
        orderStatesViewService.changeOrderStateToInProgress(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToCompleted() throws Exception {
        // when
        orderStatesViewService.changeOrderStateToCompleted(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToDeclinedFromPending() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.PENDING.getStringValue());
        // when
        orderStatesViewService.changeOrderStateToDeclined(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToDeclinedFromAccepted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        // when
        orderStatesViewService.changeOrderStateToDeclined(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToAbandonedFromInProgress() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        // when
        orderStatesViewService.changeOrderStateToAbandoned(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToAbandonedFromInterrupted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        // when
        orderStatesViewService.changeOrderStateToAbandoned(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInterrupted() throws Exception {
        // when
        orderStatesViewService.changeOrderStateToInterrupted(view, state, new String[0]);
    }
}
