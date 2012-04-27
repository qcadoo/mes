package com.qcadoo.mes.basic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class CurrencyViewServiceTest {

    private CurrencyViewService currencyViewService;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private Entity currency;

    @org.junit.Before
    public void init() {
        currencyViewService = new CurrencyViewService();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(currencyViewService, "currencyService", currencyService);
    }

    @Test
    public void shouldApplyCurrentCurrency() throws Exception {
        // given
        FieldComponent field = mock(FieldComponent.class);
        Long currencyId = 1L;
        when(view.getComponentByReference("currency")).thenReturn(field);
        when(currencyService.getCurrentCurrency()).thenReturn(currency);
        when(currency.getId()).thenReturn(currencyId);
        // when
        currencyViewService.applyCurrentCurrency(view);
        // then
        Mockito.verify(field).setFieldValue(currencyId);

    }

}
