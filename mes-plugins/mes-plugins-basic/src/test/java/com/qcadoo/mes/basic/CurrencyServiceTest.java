package com.qcadoo.mes.basic;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.Entity;

public class CurrencyServiceTest {

    private static final String FIELD_CURRENCY = "currency";

    private CurrencyService currencyService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private Entity parameter;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        currencyService = new CurrencyService();
        ReflectionTestUtils.setField(currencyService, "parameterService", parameterService);

        given(parameterService.getParameter()).willReturn(parameter);
    }

    @Test
    public final void shouldReturnCurrentlyUsedCurrency() throws Exception {
        // given
        Entity currency = mock(Entity.class);
        setCurrencyParameterField(currency);

        // when
        Entity returnedCurrency = currencyService.getCurrentCurrency();

        // then
        assertEquals(currency, returnedCurrency);
    }

    @Test
    public final void shouldReturnCurrencyAlphabeticCode() throws Exception {
        // given
        Entity currency = mock(Entity.class);
        setCurrencyParameterField(currency);

        given(currency.getStringField("alphabeticCode")).willReturn("EUR");

        // when
        String returnedCode = currencyService.getCurrencyAlphabeticCode();

        // then
        assertEquals("EUR", returnedCode);
    }

    private void setCurrencyParameterField(final Entity currency) {
        given(parameter.getField(FIELD_CURRENCY)).willReturn(currency);
        given(parameter.getBelongsToField(FIELD_CURRENCY)).willReturn(currency);
    }

}
