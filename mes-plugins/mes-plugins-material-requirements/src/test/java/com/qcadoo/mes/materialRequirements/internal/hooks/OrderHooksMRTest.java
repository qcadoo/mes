package com.qcadoo.mes.materialRequirements.internal.hooks;

import static com.qcadoo.mes.materialRequirements.internal.constants.InputProductsRequiredForType.START_ORDER;
import static com.qcadoo.mes.materialRequirements.internal.constants.OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderHooksMRTest {

    private OrderHooksMR orderHooksMR;

    @Mock
    private MaterialRequirementService materialRequirementService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private Entity order, parameter;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooksMR = new OrderHooksMR();

        ReflectionTestUtils.setField(orderHooksMR, "materialRequirementService", materialRequirementService);
        ReflectionTestUtils.setField(orderHooksMR, "parameterService", parameterService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "orders.order.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(true);

        // when
        boolean result = orderHooksMR.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "orders.order.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(false);

        // when
        boolean result = orderHooksMR.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldSetInputProductsRequiredForTypeDefaultValue() {
        // given
        given(parameterService.getParameter()).willReturn(parameter);
        given(parameter.getStringField(INPUT_PRODUCTS_REQUIRED_FOR_TYPE)).willReturn(START_ORDER.getStringValue());

        // when
        orderHooksMR.setInputProductsRequiredForTypeDefaultValue(orderDD, order);

        // then
        Mockito.verify(materialRequirementService).setInputProductsRequiredForTypeDefaultValue(order,
                INPUT_PRODUCTS_REQUIRED_FOR_TYPE, START_ORDER.getStringValue());
    }

}
