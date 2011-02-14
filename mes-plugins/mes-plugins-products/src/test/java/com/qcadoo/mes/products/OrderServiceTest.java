package com.qcadoo.mes.products;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FormComponentState.class })
public class OrderServiceTest {

    private OrderService orderService;

    private SecurityService securityService;

    private DataDefinitionService dataDefinitionService;

    private TranslationService translationService;

    @Before
    public void init() {
        securityService = mock(SecurityService.class);
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        translationService = mock(TranslationService.class);
        orderService = new OrderService();
        setField(orderService, "securityService", securityService);
        setField(orderService, "dataDefinitionService", dataDefinitionService);
        setField(orderService, "translationService", translationService);
    }

    @Test
    public void shouldClearOrderFieldsOnCopy() throws Exception {
        // given
        Entity order = mock(Entity.class);
        DataDefinition dataDefinition = mock(DataDefinition.class);

        // when
        boolean result = orderService.clearOrderDatesAndWorkersOnCopy(dataDefinition, order);

        // then
        assertTrue(result);
        verify(order).setField("state", "01pending");
        verify(order).setField("effectiveDateTo", null);
        verify(order).setField("endWorker", null);
        verify(order).setField("effectiveDateFrom", null);
        verify(order).setField("startWorker", null);
        verify(order).setField("doneQuantity", null);
    }

    @Test
    public void shouldPrintOrder() throws Exception {
        // given
        Entity order = mock(Entity.class);
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(order);

        // when
        orderService.printOrder(viewDefinitionState, state, new String[] { "pdf" });

        // then
        verify(viewDefinitionState).redirectTo("/products/order.pdf?id=13", true, false);
    }

    @Test
    public void shouldFailPrintIfEntityNotFound() throws Exception {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(dataDefinitionService.get("products", "order").get(13L)).willReturn(null);
        given(translationService.translate("core.message.entityNotFound", Locale.ENGLISH)).willReturn(
                "core.message.entityNotFound.pl");

        // when
        orderService.printOrder(viewDefinitionState, state, new String[] { "pdf" });

        // then
        verify(state).addMessage("core.message.entityNotFound.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailPrintIfNoRowIsSelected() throws Exception {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(translationService.translate("core.grid.noRowSelectedError", Locale.ENGLISH)).willReturn(
                "core.grid.noRowSelectedError.pl");

        // when
        orderService.printOrder(viewDefinitionState, state, new String[] { "pdf" });

        // then
        verify(state).addMessage("core.grid.noRowSelectedError.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailPrintIfFormHasNoIdentifier() throws Exception {
        // given
        FormComponentState state = mock(FormComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(translationService.translate("core.form.entityWithoutIdentifier", Locale.ENGLISH)).willReturn(
                "core.form.entityWithoutIdentifier.pl");

        // when
        orderService.printOrder(viewDefinitionState, state, new String[] { "pdf" });

        // then
        verify(state).addMessage("core.form.entityWithoutIdentifier.pl", MessageType.FAILURE);
    }

}
