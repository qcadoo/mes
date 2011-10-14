package com.qcadoo.mes.orders.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.Entity;

public class OrderStatesChangingServiceTest {

    private OrderStatesChangingService orderStatesChangingService;

    private OrderStateValidationService orderStateValidationService;

    private OrderStateListener orderStateListener;

    private ChangeOrderStateMessage changeOrderStateMessage;

    private List<OrderStateListener> listeners = new LinkedList<OrderStateListener>();

    private Entity newOrder, oldOrder;

    private Iterator<OrderStateListener> listenersIterator;

    @Before
    public void init() {

        orderStatesChangingService = new OrderStatesChangingService();
        orderStateListener = mock(OrderStateListener.class);
        listeners = mock(LinkedList.class);
        newOrder = mock(Entity.class);
        oldOrder = mock(Entity.class);
        listenersIterator = mock(Iterator.class);
        orderStateValidationService = mock(OrderStateValidationService.class);

        when(listenersIterator.hasNext()).thenReturn(true, false);
        when(listenersIterator.next()).thenReturn(orderStateListener);
        when(listeners.iterator()).thenReturn(listenersIterator);

        setField(orderStatesChangingService, "orderStateValidationService", orderStateValidationService);
        setField(orderStatesChangingService, "listeners", listeners);

    }

    @Test
    public void shouldAddOrderStateListener() throws Exception {
        // given
        given(listeners.add(orderStateListener)).willReturn(true);

        // when
        orderStatesChangingService.addOrderStateListener(orderStateListener);
    }

    @Test
    public void shouldRemoveOrderStateListener() throws Exception {
        // given
        given(listeners.remove(orderStateListener)).willReturn(true);

        // when
        orderStatesChangingService.removeOrderStateListener(orderStateListener);
    }

    @Test
    public void shouldFailChangeStateToAcceptedWhenNameFieldIsNull() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.PENDING.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn(null);

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then
    }

    @Test
    public void shouldFailChangeStateToAcceptedWhenTechnologyFieldValueIsNull() throws Exception {
        // given
        Entity product = mock(Entity.class);
        given(newOrder.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.PENDING.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("order1");
        given(newOrder.getField("product")).willReturn(product);
        given(newOrder.getField("plannedQuantity")).willReturn(10L);
        given(newOrder.getField("dateTo")).willReturn(null);

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then
    }

    @Test
    public void shouldPerformChangeStateToAccepted() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.PENDING.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToDeclinedFromPending() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.DECLINED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.PENDING.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToInProgressFromAccepted() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToInProgressFromInterrupted() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToCompleted() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.COMPLETED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToAbandonedFromInterrupted() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.ABANDONED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToAbandonedFromInProgress() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.ABANDONED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToInterruptedFromInProgress() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }

    @Test
    public void shouldPerformChangeStateToDeclinedFromAccepted() throws Exception {
        // given
        given(newOrder.getStringField("state")).willReturn(OrderStates.DECLINED.getStringValue());
        given(oldOrder.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(newOrder.getStringField("number")).willReturn("00001");
        given(newOrder.getStringField("name")).willReturn("Order1");

        // when
        List<ChangeOrderStateMessage> errors = orderStatesChangingService.performChangeState(newOrder, oldOrder);
        // then

    }
}
