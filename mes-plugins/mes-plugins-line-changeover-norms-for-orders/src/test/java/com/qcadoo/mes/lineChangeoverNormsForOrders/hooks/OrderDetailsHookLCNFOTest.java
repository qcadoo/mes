package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class OrderDetailsHookLCNFOTest {

    private OrderDetailsHooksLCNFO orderDetailsHooksLCNFO;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private RibbonActionItem showChangeoverButton;

    @Mock
    private LookupComponent technologyPrototypeLookup;

    @Mock
    private ComponentState orderTypeSelect;

    @Before
    public void init() {
        orderDetailsHooksLCNFO = new OrderDetailsHooksLCNFO();

        MockitoAnnotations.initMocks(this);

        given(view.getComponentByReference("orderType")).willReturn(orderTypeSelect);
        given(view.getComponentByReference("technologyPrototype")).willReturn(technologyPrototypeLookup);

        RibbonGroup changeoverGroup = mock(RibbonGroup.class);
        given(changeoverGroup.getItemByName("showChangeover")).willReturn(showChangeoverButton);

        Ribbon ribbon = mock(Ribbon.class);
        given(ribbon.getGroupByName("changeover")).willReturn(changeoverGroup);

        WindowComponentState window = mock(WindowComponentState.class);
        given(window.getRibbon()).willReturn(ribbon);

        given(view.getComponentByReference("window")).willReturn(window);
    }

    private void stubOrderType(final OrderType type) {
        given(orderTypeSelect.getFieldValue()).willReturn(type.getStringValue());
    }

    private void techPrototypeIsDefined(final boolean hasValue) {
        given(technologyPrototypeLookup.isEmpty()).willReturn(!hasValue);
    }

    private void verifyButtonEnabled(final boolean expectedValue) {
        verify(showChangeoverButton).setEnabled(expectedValue);
        verify(showChangeoverButton, never()).setEnabled(!expectedValue);
    }

    @Test
    public final void shouldDisableButtonWhenOrderHasOwnTechnologyAndTechnologyPrototypeIsNull() {
        // given
        stubOrderType(OrderType.WITH_OWN_TECHNOLOGY);
        techPrototypeIsDefined(false);

        // when
        orderDetailsHooksLCNFO.onBeforeRender(view);

        // then
        verifyButtonEnabled(false);
    }

    @Test
    public final void shouldDisableButtonWhenOrderHasOwnTechnologyAndTechnologyPrototypeIsNotNull() {
        // given
        stubOrderType(OrderType.WITH_OWN_TECHNOLOGY);
        techPrototypeIsDefined(true);

        // when
        orderDetailsHooksLCNFO.onBeforeRender(view);

        // then
        verifyButtonEnabled(false);
    }

    @Test
    public final void shouldDisableButtonWhenOrderHasPatternTechnologyAndTechnologyPrototypeIsNull() {
        // given
        stubOrderType(OrderType.WITH_PATTERN_TECHNOLOGY);
        techPrototypeIsDefined(false);

        // when
        orderDetailsHooksLCNFO.onBeforeRender(view);

        // then
        verifyButtonEnabled(false);
    }

    @Test
    public final void shouldEnableButtonWhenOrderHasPatternTechnologyAndTechnologyPrototypeIsNotNull() {
        // given
        stubOrderType(OrderType.WITH_PATTERN_TECHNOLOGY);
        techPrototypeIsDefined(true);

        // when
        orderDetailsHooksLCNFO.onBeforeRender(view);

        // then
        verifyButtonEnabled(true);
    }

}
