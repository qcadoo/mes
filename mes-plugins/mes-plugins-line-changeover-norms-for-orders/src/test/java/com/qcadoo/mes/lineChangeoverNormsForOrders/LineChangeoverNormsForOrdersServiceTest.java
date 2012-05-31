package com.qcadoo.mes.lineChangeoverNormsForOrders;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.ORDER_FIELDS;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class LineChangeoverNormsForOrdersServiceTest {

    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    private static final long L_ID = 1L;

    private static final long L_DATE_TIME_10 = 10L;

    private static final long L_DATE_TIME_20 = 20L;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition orderDD, technologyDD, previousOrderDD;

    @Mock
    private Entity previousOrder, technology, order;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private Date dateTo, dateFrom;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent orderField, technologyNumberField, technologyGroupNumberField, dateFromField, dateToField;

    @Mock
    private List<String> orderFields;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        lineChangeoverNormsForOrdersService = new LineChangeoverNormsForOrdersServiceImpl();

        setField(lineChangeoverNormsForOrdersService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldntFillOrderForm() {
        // given
        given(orderFields.get(0)).willReturn(ORDER_FIELDS.get(0));
        given(orderFields.get(1)).willReturn(ORDER_FIELDS.get(1));
        given(orderFields.get(2)).willReturn(ORDER_FIELDS.get(2));
        given(orderFields.get(3)).willReturn(ORDER_FIELDS.get(3));
        given(orderFields.get(4)).willReturn(ORDER_FIELDS.get(4));

        given(view.getComponentByReference(ORDER_FIELDS.get(0))).willReturn(orderField);

        given(orderField.getFieldValue()).willReturn(null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, orderFields);

        // then
        verify(orderField, never()).setFieldValue(Mockito.any());
        verify(technologyNumberField, never()).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.any());
        verify(dateFromField, never()).setFieldValue(Mockito.any());
        verify(dateToField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldntFillOrderFormIfOrderIsNull() {
        // given
        given(orderFields.get(0)).willReturn(ORDER_FIELDS.get(0));
        given(orderFields.get(1)).willReturn(ORDER_FIELDS.get(1));
        given(orderFields.get(2)).willReturn(ORDER_FIELDS.get(2));
        given(orderFields.get(3)).willReturn(ORDER_FIELDS.get(3));
        given(orderFields.get(4)).willReturn(ORDER_FIELDS.get(4));

        given(view.getComponentByReference(ORDER_FIELDS.get(0))).willReturn(orderField);

        given(orderField.getFieldValue()).willReturn(L_ID);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(orderDD.get(L_ID)).willReturn(null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, orderFields);

        // then
        verify(orderField, never()).setFieldValue(Mockito.any());
        verify(technologyNumberField, never()).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.any());
        verify(dateFromField, never()).setFieldValue(Mockito.any());
        verify(dateToField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillOrderFormIfFormIsntNullAndFieldsAreNull() {
        // given
        given(orderFields.get(0)).willReturn(ORDER_FIELDS.get(0));
        given(orderFields.get(1)).willReturn(ORDER_FIELDS.get(1));
        given(orderFields.get(2)).willReturn(ORDER_FIELDS.get(2));
        given(orderFields.get(3)).willReturn(ORDER_FIELDS.get(3));
        given(orderFields.get(4)).willReturn(ORDER_FIELDS.get(4));

        given(view.getComponentByReference(ORDER_FIELDS.get(0))).willReturn(orderField);

        given(orderField.getFieldValue()).willReturn(L_ID);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(orderDD.get(L_ID)).willReturn(order);

        given(view.getComponentByReference(ORDER_FIELDS.get(1))).willReturn(technologyNumberField);
        given(view.getComponentByReference(ORDER_FIELDS.get(2))).willReturn(technologyGroupNumberField);
        given(view.getComponentByReference(ORDER_FIELDS.get(3))).willReturn(dateFromField);
        given(view.getComponentByReference(ORDER_FIELDS.get(4))).willReturn(dateToField);

        given(order.getId()).willReturn(L_ID);

        given(order.getBelongsToField(TECHNOLOGY)).willReturn(null);

        given(order.getField(DATE_FROM)).willReturn(null);
        given(order.getField(CORRECTED_DATE_FROM)).willReturn(null);
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(null);

        given(order.getField(DATE_TO)).willReturn(null);
        given(order.getField(CORRECTED_DATE_TO)).willReturn(null);
        given(order.getField(EFFECTIVE_DATE_TO)).willReturn(null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, orderFields);

        // then
        verify(orderField).setFieldValue(Mockito.any());
        verify(technologyNumberField, never()).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.any());
        verify(dateFromField, never()).setFieldValue(Mockito.any());
        verify(dateToField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillOrderFormIfFormIsntNullAndFieldsArentNull() {
        // given
        given(orderFields.get(0)).willReturn(ORDER_FIELDS.get(0));
        given(orderFields.get(1)).willReturn(ORDER_FIELDS.get(1));
        given(orderFields.get(2)).willReturn(ORDER_FIELDS.get(2));
        given(orderFields.get(3)).willReturn(ORDER_FIELDS.get(3));
        given(orderFields.get(4)).willReturn(ORDER_FIELDS.get(4));

        given(view.getComponentByReference(ORDER_FIELDS.get(0))).willReturn(orderField);

        given(orderField.getFieldValue()).willReturn(L_ID);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(orderDD.get(L_ID)).willReturn(order);

        given(view.getComponentByReference(ORDER_FIELDS.get(1))).willReturn(technologyNumberField);
        given(view.getComponentByReference(ORDER_FIELDS.get(2))).willReturn(technologyGroupNumberField);
        given(view.getComponentByReference(ORDER_FIELDS.get(3))).willReturn(dateFromField);
        given(view.getComponentByReference(ORDER_FIELDS.get(4))).willReturn(dateToField);

        given(order.getId()).willReturn(L_ID);

        given(order.getBelongsToField(TECHNOLOGY)).willReturn(null);

        given(order.getField(DATE_FROM)).willReturn(null);
        given(order.getField(CORRECTED_DATE_FROM)).willReturn(null);
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(null);

        given(order.getField(DATE_TO)).willReturn(null);
        given(order.getField(CORRECTED_DATE_TO)).willReturn(null);
        given(order.getField(EFFECTIVE_DATE_TO)).willReturn(null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, orderFields);

        // then
        verify(orderField).setFieldValue(Mockito.any());
        verify(technologyNumberField, never()).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.any());
        verify(dateFromField, never()).setFieldValue(Mockito.any());
        verify(dateToField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersAreNull() {
        // given

        // when
        boolean result = lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(null, null);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersArentNullAndPreviousOrderIsCorrectAndPrevious() {
        // given
        given(previousOrder.getStringField(STATE)).willReturn(OrderStates.ACCEPTED.getStringValue());

        given(previousOrder.getField(DATE_TO)).willReturn(dateTo);
        given(order.getField(DATE_FROM)).willReturn(dateFrom);

        given(dateTo.getTime()).willReturn(L_DATE_TIME_10);
        given(dateFrom.getTime()).willReturn(L_DATE_TIME_20);

        // when
        boolean result = lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrder, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersArentNullAndPreviousOrderIsntCorrectAndPrevious() {
        // given
        given(previousOrder.getStringField(STATE)).willReturn(OrderStates.DECLINED.getStringValue());

        given(previousOrder.getField(DATE_TO)).willReturn(dateTo);
        given(order.getField(DATE_FROM)).willReturn(dateFrom);

        given(dateTo.getTime()).willReturn(L_DATE_TIME_10);
        given(dateFrom.getTime()).willReturn(L_DATE_TIME_20);

        // when
        boolean result = lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrder, order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnOrderWhenGetOrderFromDB() {
        // given
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(orderDD.get(L_ID)).willReturn(order);

        // when
        Entity entity = lineChangeoverNormsForOrdersService.getOrderFromDB(L_ID);

        // then
        assertSame(order, entity);
    }

    @Test
    public void shouldReturnTechnologyWhengetTechnologyFromDB() {
        // given
        given(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY))
                .willReturn(technologyDD);
        given(technologyDD.get(L_ID)).willReturn(technology);

        // when
        Entity entity = lineChangeoverNormsForOrdersService.getTechnologyFromDB(L_ID);

        // then
        assertSame(technology, entity);
    }

    @Test
    public void shouldReturnPeviousOrderWhenGetPreviousOrderFromDB() {
        // given
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(
                previousOrderDD);
        given(previousOrderDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.orderDescBy(DATE_TO)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(previousOrder);

        // when
        Entity entity = lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order);

        // then
        assertSame(previousOrder, entity);
    }
}
