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
