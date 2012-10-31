/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
