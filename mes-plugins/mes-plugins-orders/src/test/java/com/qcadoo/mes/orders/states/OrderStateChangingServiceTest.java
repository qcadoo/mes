package com.qcadoo.mes.orders.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;

public class OrderStateChangingServiceTest {

    private OrderStateChangingService orderStateChangingService;

    private DataDefinitionService dataDefinitionService;

    private DataDefinition dataDefinition;

    private SecurityService securityService;

    private ShiftsServiceImpl shiftsServiceImpl;

    private Entity entity;

    private Entity order;

    private Entity shift;

    private String previousState;

    private String currentState;

    @Before
    public void init() {
        orderStateChangingService = new OrderStateChangingService();

        dataDefinitionService = mock(DataDefinitionService.class);
        securityService = mock(SecurityService.class);
        shiftsServiceImpl = mock(ShiftsServiceImpl.class);
        entity = mock(Entity.class);
        order = mock(Entity.class);
        dataDefinition = mock(DataDefinition.class);
        shift = mock(Entity.class);

        setField(orderStateChangingService, "dataDefinitionService", dataDefinitionService);
        setField(orderStateChangingService, "securityService", securityService);
        setField(orderStateChangingService, "shiftsServiceImpl", shiftsServiceImpl);
    }

    @Test
    public void shouldSetValueToEntity() throws Exception {
        // given

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING)).willReturn(
                dataDefinition);
        given(dataDefinition.create()).willReturn(entity);
        given(shiftsServiceImpl.getShiftFromDate(Mockito.any(Date.class))).willReturn(shift);
        given(securityService.getCurrentUserName()).willReturn("userName");
        given(entity.getDataDefinition()).willReturn(dataDefinition);

        orderStateChangingService.saveLogging(order, previousState, currentState);
        // then
        verify(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING)).create();
        verify(entity).setField("order", order);
        verify(entity).setField("previousState", previousState);
        verify(entity).setField("currentState", currentState);
        verify(entity).setField("shift", shift);
        verify(entity).setField("worker", "userName");
        verify(dataDefinition).save(entity);
    }

    @Test
    public void shouldThrowExceptionWhenShiftIsNull() throws Exception {
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING)).willReturn(
                dataDefinition);
        given(dataDefinition.create()).willReturn(entity);
        given(shiftsServiceImpl.getShiftFromDate(Mockito.any(Date.class))).willReturn(null);
        given(entity.getDataDefinition()).willReturn(dataDefinition);

        orderStateChangingService.saveLogging(order, previousState, currentState);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationPending() throws Exception {
        // when
        orderStateChangingService.validationPending(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationAccepted() throws Exception {
        // when
        orderStateChangingService.validationAccepted(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationInProgress() throws Exception {
        // when
        orderStateChangingService.validationInProgress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationCompleted() throws Exception {
        // when
        orderStateChangingService.validationCompleted(null);
    }

    @Test
    public void shouldReturnErrorWhenValidatingAccepted() throws Exception {
        // when
        orderStateChangingService.validationAccepted(order);
    }

    @Test
    public void shouldPerformValidationAccepted() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateError> errors = orderStateChangingService.validationAccepted(order);
        // then
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void shouldReturnErrorWhenValidatingInProgress() throws Exception {
        // when
        orderStateChangingService.validationInProgress(order);
    }

    @Test
    public void shouldPerformValidationInProgress() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateError> errors = orderStateChangingService.validationInProgress(order);
        // then
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void shouldReturnErrorWhenValidatingCompleted() throws Exception {
        // when
        orderStateChangingService.validationCompleted(order);
    }

    @Test
    public void shouldPerformValidationCompleted() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateError> errors = orderStateChangingService.validationCompleted(order);
        // then
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void shouldReturnErrorWhenValidatingPending() throws Exception {
        // when
        orderStateChangingService.validationPending(order);
    }

    @Test
    public void shouldPerformValidationPending() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateError> errors = orderStateChangingService.validationPending(order);
        // then
        Assert.assertEquals(0, errors.size());
    }
}