/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.costNormsForProduct.hooks.ProductDetailsHooksCNFP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

public class ProductDetailsHooksCNFPTest {

    private ProductDetailsHooksCNFP costNormsForProductService;

    @Mock
    private ViewDefinitionState viewDefinitionState;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private FormComponent productForm;

    @Mock
    private FieldComponent field1, field2, field3, field4, field5, field6;

    @Mock
    private Entity product;

    @Before
    public void init() {
        costNormsForProductService = new ProductDetailsHooksCNFP();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(costNormsForProductService, "currencyService", currencyService);
        ReflectionTestUtils.setField(costNormsForProductService, "dataDefinitionService", dataDefinitionService);
        when(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT))
                .thenReturn(dataDefinition);

        when(viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM)).thenReturn(productForm);
        when(productForm.getEntityId()).thenReturn(3L);
        when(dataDefinition.get(anyLong())).thenReturn(product);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowExceptionWhenViewDefinitionStateIsNull() throws Exception {
        costNormsForProductService.fillCurrencyFieldsInProduct(null);
    }

    @Test
    public void shouldFillCurrencyFields() throws Exception {
        // given
        String currency = "PLN";

        when(viewDefinitionState.getComponentByReference("averageOfferCostCurrency")).thenReturn(field4);
        when(viewDefinitionState.getComponentByReference("lastOfferCostCurrency")).thenReturn(field5);
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
        when(productForm.getEntityId()).thenReturn(productId);
        when(dataDefinition.get(productId)).thenReturn(product);
        when(product.getStringField(ProductFields.EXTERNAL_NUMBER)).thenReturn(externalId);
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.NOMINAL_COST)).thenReturn(field1);
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.AVERAGE_OFFER_COST)).thenReturn(field2);
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.LAST_OFFER_COST)).thenReturn(field3);
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.LAST_PURCHASE_COST)).thenReturn(field4);
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.AVERAGE_COST)).thenReturn(field5);
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.NOMINAL_COST_CURRENCY)).thenReturn(field6);
        // when
        costNormsForProductService.enableFieldForExternalID(viewDefinitionState);
        // then
        Mockito.verify(field1).setEnabled(true);
        Mockito.verify(field2).setEnabled(true);
        Mockito.verify(field3).setEnabled(true);
        Mockito.verify(field4).setEnabled(true);
        Mockito.verify(field5).setEnabled(true);
        Mockito.verify(field6).setEnabled(true);
    }

    @Test
    public void shouldFillUnitFieldWhenInProductIsTrue() throws Exception {
        // given
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.NOMINAL_COST)).thenReturn(field1);
        // when
        costNormsForProductService.fillUnitField(viewDefinitionState, ProductFieldsCNFP.NOMINAL_COST, true);
        // then
    }

    @Test
    public void shouldFillUnitFieldWhenInProductIsFalse() throws Exception {
        // given
        when(viewDefinitionState.getComponentByReference(ProductFieldsCNFP.NOMINAL_COST)).thenReturn(field1);
        when(viewDefinitionState.getComponentByReference("product")).thenReturn(field2);
        // when
        costNormsForProductService.fillUnitField(viewDefinitionState, ProductFieldsCNFP.NOMINAL_COST, false);
        // then
    }

}
