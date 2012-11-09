package com.qcadoo.mes.materialRequirements.internal.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class OrderDetailsHooksMRTest {

    private OrderDetailsHooksMR orderDetailsHooksMR;

    @Mock
    private ParameterService parameterService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private Entity parameter;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent inputProductsRequiredForTypeField;

    @Before
    public void init() {
        orderDetailsHooksMR = new OrderDetailsHooksMR();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(orderDetailsHooksMR, "parameterService", parameterService);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(null);
        when(view.getComponentByReference("inputProductsRequiredForType")).thenReturn(inputProductsRequiredForTypeField);
        when(parameterService.getParameter()).thenReturn(parameter);
    }

    @Test
    public final void shouldSetToFieldValueFromParameter() {
        // given
        String inputProductsRequiredForTypeParameter = "02startOperationalTask";
        when(parameter.getStringField("inputProductsRequiredForType")).thenReturn(inputProductsRequiredForTypeParameter);

        // when
        orderDetailsHooksMR.setInputProductsRequiredForTypeFromDefaultParameter(view);
        // then
        Mockito.verify(inputProductsRequiredForTypeField).setFieldValue("02startOperationalTask");
    }

    @Test
    public final void shouldSetDefaultValueWhenValueInOrderIsEmpty() {
        // given
        when(parameter.getStringField("inputProductsRequiredForType")).thenReturn(null);

        // when
        orderDetailsHooksMR.setInputProductsRequiredForTypeFromDefaultParameter(view);
        // then
        Mockito.verify(inputProductsRequiredForTypeField).setFieldValue("01startOrder");

    }
}
