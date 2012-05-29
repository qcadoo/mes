package com.qcadoo.mes.lineChangeoverNormsForOrders.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class OrderDetailsListenersLCNFOTest {

    private static final long L_ID = 1L;

    private OrderDetailsListenersLCNFO orderDetailsListenersLCNFO;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState componentState;

    private Map<String, Object> parameters = Maps.newHashMap();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderDetailsListenersLCNFO = new OrderDetailsListenersLCNFO();
    }

    @Test
    public void shouldntShowChangeoverIfOrderIdIsNull() {
        // given
        given(componentState.getFieldValue()).willReturn(null);

        String url = "../page/lineChangeoverNormsForOrders/lineChangeoverNormsForOrderDetails.html";

        // when
        orderDetailsListenersLCNFO.showChangeover(view, componentState, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowChangeoverIfOrderIdIsntNull() {
        // given
        given(componentState.getFieldValue()).willReturn(L_ID);

        parameters.put("form.id", L_ID);

        String url = "../page/lineChangeoverNormsForOrders/lineChangeoverNormsForOrderDetails.html";

        // when
        orderDetailsListenersLCNFO.showChangeover(view, componentState, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

}
