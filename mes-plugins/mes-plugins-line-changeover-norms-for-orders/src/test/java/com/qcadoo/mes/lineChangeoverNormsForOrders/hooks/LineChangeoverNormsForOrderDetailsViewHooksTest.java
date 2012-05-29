package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.view.api.ViewDefinitionState;

public class LineChangeoverNormsForOrderDetailsViewHooksTest {

    private LineChangeoverNormsForOrderDetailsViewHooks lineChangeoverNormsForOrderDetailsViewHooks;

    @Mock
    private OrderService orderService;

    @Mock
    private ChangeoverNormsService changeoverNormsService;

    @Mock
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Mock
    private ViewDefinitionState view;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        lineChangeoverNormsForOrderDetailsViewHooks = new LineChangeoverNormsForOrderDetailsViewHooks();

        setField(lineChangeoverNormsForOrderDetailsViewHooks, "orderService", orderService);
        setField(lineChangeoverNormsForOrderDetailsViewHooks, "changeoverNormsService", changeoverNormsService);
        setField(lineChangeoverNormsForOrderDetailsViewHooks, "lineChangeoverNormsForOrdersService",
                lineChangeoverNormsForOrdersService);
    }

    @Test
    public void shouldShowOwnLineChangeoverDurationField() {
        // given

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.showOwnLineChangeoverDurationField(view);

        // then
        verify(orderService).changeFieldState(view, OWN_LINE_CHANGEOVER, OWN_LINE_CHANGEOVER_DURATION);
    }

}
