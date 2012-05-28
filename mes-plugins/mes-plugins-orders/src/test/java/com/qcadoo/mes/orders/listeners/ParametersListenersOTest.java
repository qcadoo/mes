package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ParametersListenersOTest {

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState componentState;

    @Mock
    private DataDefinition dictionaryDD;

    @Mock
    private Entity dictionary;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private OrderService orderService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    private ParametersListenersO parametersListenersO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parametersListenersO = new ParametersListenersO();

        setField(parametersListenersO, "orderService", orderService);
        setField(parametersListenersO, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldRedirectToOrdersParametersIfParameterIdIsntNull() {
        // given
        Long parameterId = 1L;

        given(componentState.getFieldValue()).willReturn(parameterId);

        String url = "../page/orders/ordersParameters.html?context={\"form.id\":\"" + parameterId + "\"}";

        // when
        parametersListenersO.redirectToOrdersParameters(view, componentState, null);

        // then
        verify(view).redirectTo(url, false, true);
    }

    @Test
    public void shouldntRedirectToOrdersParametersIfParameterIdIsNull() {
        // given
        Long parameterId = null;

        given(componentState.getFieldValue()).willReturn(parameterId);

        String url = "../page/orders/ordersParameters.html?context={\"form.id\":\"" + parameterId + "\"}";

        // when
        parametersListenersO.redirectToOrdersParameters(view, componentState, null);

        // then
        verify(view, never()).redirectTo(url, false, true);
    }

    @Test
    public void shouldRedirectToDeviationsDictionaryIfDictionaryIdIsntNull() {
        // given
        Long dictionaryId = 1L;

        given(dataDefinitionService.get("qcadooModel", "dictionary")).willReturn(dictionaryDD);
        given(dictionaryDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(SearchRestrictions.eq("name", "reasonTypeOfChaningOrderState"))).willReturn(
                searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(dictionary);

        given(dictionary.getId()).willReturn(dictionaryId);

        String url = "../page/qcadooDictionaries/dictionaryDetails.html?context={\"form.id\":\"" + dictionaryId + "\"}";

        // when
        parametersListenersO.redirectToDeviationsDictionary(view, componentState, null);

        // then
        verify(view).redirectTo(url, false, true);
    }

    @Test
    public void shouldntRedirectToDeviationsDictionaryfDictionaryIdIsnull() {
        // given
        Long dictionaryId = null;

        given(dataDefinitionService.get("qcadooModel", "dictionary")).willReturn(dictionaryDD);
        given(dictionaryDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(SearchRestrictions.eq("name", "reasonTypeOfChaningOrderState"))).willReturn(
                searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        String url = "../page/qcadooDictionaries/dictionaryDetails.html?context={\"form.id\":\"" + dictionaryId + "\"}";

        // when
        parametersListenersO.redirectToDeviationsDictionary(view, componentState, null);

        // then
        verify(view, never()).redirectTo(url, false, true);
    }

    @Test
    public void shouldShowTimeFieldForDelayedEffectiveDateFrom() {
        // given
        given(componentState.getName()).willReturn(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM);

        // when
        parametersListenersO.showTimeField(view, componentState, null);

        // then
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM,
                DELAYED_EFFECTIVE_DATE_FROM_TIME);
    }

    @Test
    public void shouldShowTimeFieldForEarlierEffectiveDateFrom() {
        // given
        given(componentState.getName()).willReturn(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM);

        // when
        parametersListenersO.showTimeField(view, componentState, null);

        // then
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM,
                EARLIER_EFFECTIVE_DATE_FROM_TIME);
    }

    @Test
    public void shouldShowTimeFieldForDelayedEffectiveDateTo() {
        // given
        given(componentState.getName()).willReturn(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO);

        // when
        parametersListenersO.showTimeField(view, componentState, null);

        // then
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO,
                DELAYED_EFFECTIVE_DATE_TO_TIME);
    }

    @Test
    public void shouldShowTimeFieldForEarlierEffectiveDateTo() {
        // given
        given(componentState.getName()).willReturn(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO);

        // when
        parametersListenersO.showTimeField(view, componentState, null);

        // then
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO,
                EARLIER_EFFECTIVE_DATE_TO_TIME);
    }

}
