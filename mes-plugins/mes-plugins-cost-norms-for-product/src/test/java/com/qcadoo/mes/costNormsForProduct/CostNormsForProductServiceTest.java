/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.costNormsForProduct;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class CostNormsForProductServiceTest {

    @Mock
    private ViewDefinitionState viewDefinitionState;

    private CostNormsForProductService costNormsForProductService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent field1, field2, field3;

    @Mock
    private Entity entity;

    @Before
    public void init() {
        costNormsForProductService = new CostNormsForProductService();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(costNormsForProductService, "currencyService", currencyService);
        ReflectionTestUtils.setField(costNormsForProductService, "dataDefinitionService", dataDefinitionService);
        when(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT))
                .thenReturn(dataDefinition);

        when(viewDefinitionState.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(3L);
        when(dataDefinition.get(anyLong())).thenReturn(entity);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowExceptionWhenViewDefinitionStateIsNull() throws Exception {
        costNormsForProductService.fillCurrencyFieldsInProduct(null);
    }

    @Test
    public void shouldFillCurrencyFields() throws Exception {
        // given
        String currency = "PLN";

        when(viewDefinitionState.getComponentByReference("nominalCostCurrency")).thenReturn(field1);
        when(viewDefinitionState.getComponentByReference("lastPurchaseCostCurrency")).thenReturn(field2);
        when(viewDefinitionState.getComponentByReference("averageCostCurrency")).thenReturn(field3);

        when(currencyService.getCurrencyAlphabeticCode()).thenReturn(currency);
        // when
        costNormsForProductService.fillCurrencyFieldsInProduct(viewDefinitionState);
        // then
    }

    @Test
    public void shouldEnabledFieldForExternalID() throws Exception {
        // given
        Long productId = 1L;
        String externalId = "0001";
        when(form.getEntityId()).thenReturn(productId);
        when(dataDefinition.get(productId)).thenReturn(entity);
        when(entity.getStringField("externalNumber")).thenReturn(externalId);
        when(viewDefinitionState.getComponentByReference("nominalCost")).thenReturn(field1);
        // when
        costNormsForProductService.enabledFieldForExternalID(viewDefinitionState);
        // then
        Mockito.verify(field1).setEnabled(true);
    }

    @Test
    public void shouldFillUnitFieldWhenInProductIsTrue() throws Exception {
        // given
        when(viewDefinitionState.getComponentByReference("nominalCost")).thenReturn(field1);
        // when
        costNormsForProductService.fillUnitField(viewDefinitionState, "nominalCost", true);
        // then
    }

    @Test
    public void shouldFillUnitFieldWhenInProductIsFalse() throws Exception {
        // given
        when(viewDefinitionState.getComponentByReference("nominalCost")).thenReturn(field1);
        when(viewDefinitionState.getComponentByReference("product")).thenReturn(field2);
        // when
        costNormsForProductService.fillUnitField(viewDefinitionState, "nominalCost", false);
        // then
    }
}
