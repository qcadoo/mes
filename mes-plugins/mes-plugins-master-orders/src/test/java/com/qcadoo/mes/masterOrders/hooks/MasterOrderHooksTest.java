package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.DEADLINE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class MasterOrderHooksTest {

    private MasterOrderHooks masterOrderHooks;

    @Mock
    private DataDefinition masterOrderDD, orderDD;

    @Mock
    private Entity masterOrder, product, order1, order2, customer;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private SearchCriteriaBuilder builder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> orders;

    @Before
    public void init() {
        masterOrderHooks = new MasterOrderHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(masterOrderHooks, "dataDefinitionService", dataDefinitionService);

        PowerMockito.mockStatic(SearchRestrictions.class);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(orderDD.find()).willReturn(builder);
        given(builder.add(Mockito.any(SearchCriterion.class))).willReturn(builder);
        given(builder.add(Mockito.any(SearchCriterion.class))).willReturn(builder);
        given(builder.list()).willReturn(searchResult);
    }

    @Test
    public final void shouldReturnWhenMasterOrderDoesnotSave() {
        // given
        given(masterOrder.getId()).willReturn(null);
        // when
        masterOrderHooks.countCumulatedOrderQuantity(masterOrderDD, masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, BigDecimal.ONE);
    }

    @Test
    public final void shouldReturnWhenMasterOrderTypeIsIncorrect() {
        Long masterOrderId = 1L;
        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn("01undefined");
        // when
        masterOrderHooks.countCumulatedOrderQuantity(masterOrderDD, masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, BigDecimal.ONE);
    }

    @Test
    public final void shouldSetCumulatedQuantity() {
        Long masterOrderId = 1L;

        BigDecimal plannedQuantityOrder1 = BigDecimal.TEN;
        BigDecimal plannedQuantityOrder2 = BigDecimal.TEN;
        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn("02oneProduct");
        given(masterOrder.getBelongsToField(MasterOrderFields.PRODUCT)).willReturn(product);

        given(order1.getDecimalField(OrderFields.PLANNED_QUANTITY)).willReturn(plannedQuantityOrder1);
        given(order2.getDecimalField(OrderFields.PLANNED_QUANTITY)).willReturn(plannedQuantityOrder2);
        List<Entity> orders = mockEntityList(Lists.newArrayList(order1, order2));

        given(searchResult.getEntities()).willReturn(orders);

        // when
        masterOrderHooks.countCumulatedOrderQuantity(masterOrderDD, masterOrder);

        // then
        verify(masterOrder).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, new BigDecimal(20));
    }

    @Test
    public final void shouldSetZeroWhenOrderDoesnotExists() {
        Long masterOrderId = 1L;

        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn("02oneProduct");
        given(masterOrder.getBelongsToField(MasterOrderFields.PRODUCT)).willReturn(product);

        List<Entity> orders = Lists.newArrayList();

        given(searchResult.getEntities()).willReturn(orders);

        // when
        masterOrderHooks.countCumulatedOrderQuantity(masterOrderDD, masterOrder);

        // then
        verify(masterOrder).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, BigDecimal.ZERO);

    }

    @Test
    public final void shouldSetExternalSynchronized() {
        // when
        masterOrderHooks.setExternalSynchronizedField(masterOrderDD, masterOrder);
        // then
        verify(masterOrder).setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);

    }

    @Test
    public final void shouldReturnWhenMasterOrderIsNotSave() {
        // given
        given(masterOrder.getId()).willReturn(null);
        // when
        masterOrderHooks.changedDeadlineInOrder(masterOrderDD, masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());
    }

    @Test
    public final void shouldReturnWhenDeadlineIsNull() {
        Long masterOrderId = 1L;
        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getDateField(MasterOrderFields.DEADLINE)).willReturn(null);
        // when
        masterOrderHooks.changedDeadlineInOrder(masterOrderDD, masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());

    }

    @Test
    public final void shouldSetDeadline() {
        Long masterOrderId = 1L;
        Date deadline = new Date();
        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getDateField(DEADLINE)).willReturn(deadline);

        given(order1.getStringField(OrderFields.STATE)).willReturn(OrderState.PENDING.getStringValue());
        given(order2.getStringField(OrderFields.STATE)).willReturn(OrderState.IN_PROGRESS.getStringValue());
        orders = mockEntityList(Lists.newArrayList(order1, order2));
        order1.setField(DEADLINE, deadline);

        List<Entity> actualOrders = Lists.newArrayList(order1, order2);
        given(masterOrder.getHasManyField(MasterOrderFields.ORDERS)).willReturn((EntityList) orders);

        // when
        masterOrderHooks.changedDeadlineInOrder(masterOrderDD, masterOrder);
        // then
        verify(masterOrder).setField(MasterOrderFields.ORDERS, actualOrders);
    }

    @Test
    public final void shouldReturnWhenMasterOrderDoesnotHaveId() {
        // given
        given(masterOrder.getId()).willReturn(null);
        // when
        masterOrderHooks.changedCustomerInOrder(masterOrderDD, masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());
    }

    @Test
    public final void shouldReturnWhenDeadlineInMasterOrderIsNull() {
        Long masterOrderId = 1L;
        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getBelongsToField(MasterOrderFields.COMPANY)).willReturn(null);
        // when
        masterOrderHooks.changedCustomerInOrder(masterOrderDD, masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());

    }

    @Test
    public final void shouldSetCustomer() {
        Long masterOrderId = 1L;
        // given
        given(masterOrder.getId()).willReturn(masterOrderId);
        given(masterOrder.getBelongsToField(MasterOrderFields.COMPANY)).willReturn(customer);

        given(order1.getStringField(OrderFields.STATE)).willReturn(OrderState.PENDING.getStringValue());
        given(order2.getStringField(OrderFields.STATE)).willReturn(OrderState.IN_PROGRESS.getStringValue());
        orders = mockEntityList(Lists.newArrayList(order1, order2));
        order1.setField(OrderFields.COMPANY, customer);

        List<Entity> actualOrders = Lists.newArrayList(order1, order2);
        given(masterOrder.getHasManyField(MasterOrderFields.ORDERS)).willReturn((EntityList) orders);

        // when
        masterOrderHooks.changedCustomerInOrder(masterOrderDD, masterOrder);
        // then
        verify(masterOrder).setField(MasterOrderFields.ORDERS, actualOrders);
    }

    private static EntityList mockEntityList(final List<Entity> entities) {
        final EntityList entitiesList = mock(EntityList.class);

        given(entitiesList.iterator()).willAnswer(new Answer<Iterator<Entity>>() {

            @Override
            public Iterator<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(entities).iterator();
            }
        });

        given(entitiesList.isEmpty()).willReturn(entities.isEmpty());

        return entitiesList;
    }
}
