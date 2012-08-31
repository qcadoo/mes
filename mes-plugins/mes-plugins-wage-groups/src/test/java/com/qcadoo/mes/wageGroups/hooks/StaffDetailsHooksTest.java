/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.LABOR_HOURLY_COST;
import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.SUPERIOR_WAGE_GROUP;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class StaffDetailsHooksTest {

    private StaffDetailsHooks detailsHooks;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private FieldComponent field1, field2;

    @Mock
    private LookupComponent lookup;

    @Mock
    private Entity wageGroup;

    @Mock
    private ViewDefinitionState view;

    @Before
    public void init() {
        detailsHooks = new StaffDetailsHooks();
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(detailsHooks, "currencyService", currencyService);

    }

    @Test
    public void shouldEnabledFieldWhenCheckBoxIsSelected() throws Exception {
        // given
        String result = "1";
        when(view.getComponentByReference("determinedIndividual")).thenReturn(field1);
        when(view.getComponentByReference("individualLaborCost")).thenReturn(field2);
        when(field1.getFieldValue()).thenReturn(result);

        // when
        detailsHooks.enabledIndividualCost(view);
        // then
        Mockito.verify(field2).setEnabled(true);
    }

    @Test
    public void shouldDisabledFieldWhenCheckBoxIsSelected() throws Exception {
        // given
        String result = "0";
        when(view.getComponentByReference("determinedIndividual")).thenReturn(field1);
        when(view.getComponentByReference("individualLaborCost")).thenReturn(field2);
        when(field1.getFieldValue()).thenReturn(result);

        // when
        detailsHooks.enabledIndividualCost(view);
        // then
        Mockito.verify(field2).setEnabled(false);
    }

    @Test
    public void shouldFillFieldCurrency() throws Exception {
        // given
        String currency = "PLN";
        when(view.getComponentByReference("individualLaborCostCURRENCY")).thenReturn(field1);
        when(view.getComponentByReference("laborCostFromWageGroupsCURRENCY")).thenReturn(field2);
        when(currencyService.getCurrencyAlphabeticCode()).thenReturn(currency);
        // when
        detailsHooks.setCurrency(view);
        // then
        Mockito.verify(field1).setFieldValue(currency);
        Mockito.verify(field2).setFieldValue(currency);
    }

    @Test
    public void shouldReturnWhenWageGroupsLookupIsEmpty() throws Exception {
        // given
        when(view.getComponentByReference("wageGroup")).thenReturn(lookup);
        when(field1.getFieldValue()).thenReturn(null);
        // when
        detailsHooks.fillFieldAboutWageGroup(view);
    }

    @Test
    public void shouldFillFieldValuesOfSelectedWageGroup() throws Exception {
        // given
        String superiorWageGroup = "1234";

        when(view.getComponentByReference("wageGroup")).thenReturn(lookup);
        when(lookup.getEntity()).thenReturn(wageGroup);

        when(view.getComponentByReference("laborCostFromWageGroups")).thenReturn(field1);
        when(view.getComponentByReference("superiorWageGroups")).thenReturn(field2);

        when(wageGroup.getField(LABOR_HOURLY_COST)).thenReturn(BigDecimal.ONE);
        when(wageGroup.getStringField(SUPERIOR_WAGE_GROUP)).thenReturn(superiorWageGroup);
        // when
        detailsHooks.fillFieldAboutWageGroup(view);
        // then
        Mockito.verify(field1).setFieldValue(BigDecimal.ONE);
        Mockito.verify(field2).setFieldValue(superiorWageGroup);
    }
}
