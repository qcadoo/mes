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

import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ParameterHooksMRTest {

    private ParameterHooksMR parameterHooksMR;

    @Mock
    private MaterialRequirementService materialRequirementService;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private Entity parameter;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parameterHooksMR = new ParameterHooksMR();

        ReflectionTestUtils.setField(parameterHooksMR, "materialRequirementService", materialRequirementService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "basic.parameter.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(true);

        // when
        boolean result = parameterHooksMR.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "basic.parameter.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(false);

        // when
        boolean result = parameterHooksMR.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldSetInputProductsRequiredForTypeDefaultValue() {
        // given

        // when
        parameterHooksMR.setInputProductsRequiredForTypeDefaultValue(parameterDD, parameter);

        // then
        Mockito.verify(materialRequirementService).setInputProductsRequiredForTypeDefaultValue(parameter,
                INPUT_PRODUCTS_REQUIRED_FOR_TYPE, START_ORDER.getStringValue());
    }

}
