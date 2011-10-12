package com.qcadoo.mes.orders.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class OrderStatesViewServiceTest {

    private OrderStatesViewService orderStatesViewService;

    private ViewDefinitionState view;

    private ComponentState state;

    private FormComponent form;

    private Entity order;

    private Entity parameter;

    private DataDefinition dataDefinition;

    private FieldComponent stateFromField, externalSynchronizedState, externalNumber;

    private PluginAccessor pluginAccessor;

    private Plugin plugin;

    private FieldComponent field;

    private Entity orderFromDB;

    private Locale locale;

    private TranslationService translationService;

    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        orderStatesViewService = new OrderStatesViewService();

        order = mock(Entity.class);
        view = mock(ViewDefinitionState.class);
        form = mock(FormComponent.class);
        state = mock(ComponentState.class);
        dataDefinition = mock(DataDefinition.class);
        stateFromField = mock(FieldComponent.class);
        externalSynchronizedState = mock(FieldComponent.class);
        externalNumber = mock(FieldComponent.class);
        pluginAccessor = mock(PluginAccessor.class);
        dataDefinitionService = mock(DataDefinitionService.class);
        translationService = mock(TranslationService.class);
        plugin = mock(Plugin.class);
        field = mock(FieldComponent.class);
        parameter = mock(Entity.class);
        dataDefinitionService = mock(DataDefinitionService.class);
        orderFromDB = mock(Entity.class);

        when(order.getDataDefinition()).thenReturn(dataDefinition);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference("state")).thenReturn(stateFromField);
        when(view.getComponentByReference("externalSynchronized")).thenReturn(externalSynchronizedState);
        when(view.getComponentByReference("externalNumber")).thenReturn(externalNumber);
        when(externalNumber.getFieldValue()).thenReturn("1");
        when(form.getEntity()).thenReturn(order);
        when(pluginAccessor.getPlugin("mesPluginsIntegrationErp")).thenReturn(plugin);
        when(plugin.getState()).thenReturn(PluginState.ENABLED);
        when(dataDefinition.get(order.getId())).thenReturn(orderFromDB);
        when(orderFromDB.getStringField("state")).thenReturn("state");
        when(view.getLocale()).thenReturn(locale);
        when(translationService.translate("orders.order.orderStates.fieldRequired", view.getLocale())).thenReturn("translate");

        for (String reference : Arrays.asList("product", "plannedQuantity", "dateTo", "dateFrom", "defaultTechnology",
                "technology")) {
            when(view.getComponentByReference(reference)).thenReturn(field);
        }
        setField(orderStatesViewService, "dataDefinitionService", dataDefinitionService);
        setField(orderStatesViewService, "translationService", translationService);
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
        when(view.getComponentByReference("doneQuantity")).thenReturn(field);
        // when
        orderStatesViewService.changeOrderStateToInProgress(view, state, new String[0]);

    }

    @Test
    public void shouldChangeOrderStateToInProgressFromInterrupted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        given(view.getComponentByReference("externalSynchronized")).willReturn(externalSynchronizedState);
        when(view.getComponentByReference("doneQuantity")).thenReturn(field);
        // when
        orderStatesViewService.changeOrderStateToInProgress(view, state, new String[0]);
    }

    // TODO ALBR
    // @Test
    // public void shouldChangeOrderStateToCompleted() throws Exception {
    // // given
    //
    // // when
    // orderStatesViewService.changeOrderStateToCompleted(view, state, new String[0]);
    // }

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
