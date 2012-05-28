package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.view.api.ViewDefinitionState;

public class ParametersHooksOTest {

    @Mock
    private ViewDefinitionState view;

    @Mock
    private OrderService orderService;

    private ParametersHooksO parametersHooksO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parametersHooksO = new ParametersHooksO();

        setField(parametersHooksO, "orderService", orderService);

    }

    @Test
    public void shouldShowTimeFields() {
        // given

        // when
        parametersHooksO.showTimeFields(view);

        // then
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM,
                DELAYED_EFFECTIVE_DATE_FROM_TIME);
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM,
                EARLIER_EFFECTIVE_DATE_FROM_TIME);
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO,
                DELAYED_EFFECTIVE_DATE_TO_TIME);
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO,
                EARLIER_EFFECTIVE_DATE_TO_TIME);
    }

}
