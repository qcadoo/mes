package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.ORDER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderModelValidatorsLCNFOTest {

    private OrderModelValidatorsLCNFO orderModelValidatorsLCNFO;

    @Mock
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private Entity order, previousOrderDB, orderDB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderModelValidatorsLCNFO = new OrderModelValidatorsLCNFO();

        setField(orderModelValidatorsLCNFO, "lineChangeoverNormsForOrdersService", lineChangeoverNormsForOrdersService);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfOrderHasCorrectStateAndIsPreviousIfPreviousOrderIsntCorrectAndPrevious() {
        // given
        given(order.getBelongsToField(PREVIOUS_ORDER)).willReturn(previousOrderDB);
        given(order.getBelongsToField(ORDER)).willReturn(orderDB);

        given(lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)).willReturn(
                false);

        // when
        boolean result = orderModelValidatorsLCNFO.checkIfOrderHasCorrectStateAndIsPrevious(orderDD, order);

        // then
        assertFalse(result);

        verify(order).addError(Mockito.eq(orderDD.getField(PREVIOUS_ORDER)), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOrderHasCorrectStateAndIsPreviousIfPreviousOrderIsCorrectAndPrevious() {
        // given
        given(order.getBelongsToField(PREVIOUS_ORDER)).willReturn(previousOrderDB);
        given(order.getBelongsToField(ORDER)).willReturn(orderDB);

        given(lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)).willReturn(
                true);

        // when
        boolean result = orderModelValidatorsLCNFO.checkIfOrderHasCorrectStateAndIsPrevious(orderDD, order);

        // then
        assertTrue(result);

        verify(order, never()).addError(Mockito.eq(orderDD.getField(PREVIOUS_ORDER)), Mockito.anyString());
    }
}
