package com.qcadoo.mes.wageGroups.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class WageGroupsDetailsHooksTest {

    private WageGroupsDetailsHooks hooks;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private FieldComponent field;

    @Mock
    private ViewDefinitionState view;

    @Before
    public void init() {
        hooks = new WageGroupsDetailsHooks();
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "currencyService", currencyService);
    }

    @Test
    public void shouldFillFieldWithCurrency() throws Exception {
        // given
        String currency = "pln";
        when(view.getComponentByReference("laborHourlyCostCURRENCY")).thenReturn(field);
        when(currencyService.getCurrencyAlphabeticCode()).thenReturn(currency);
        // when
        hooks.setCurrency(view);
        // then
        Mockito.verify(field).setFieldValue(currency);
    }
}
