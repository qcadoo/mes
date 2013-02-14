/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.CompanyProductFields.COMPANY;
import static com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields.PRODUCT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class CompanyProductHooksTest {

    private CompanyProductHooks companyProductHooks;

    @Mock
    private CompanyProductService companyProductService;

    @Mock
    private ProductService productService;

    @Mock
    private DataDefinition companyProductDD;

    @Mock
    private Entity companyProduct, product;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        companyProductHooks = new CompanyProductHooks();

        ReflectionTestUtils.setField(companyProductHooks, "companyProductService", companyProductService);
        ReflectionTestUtils.setField(companyProductHooks, "productService", productService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductIsParticularProduct() {
        // given
        given(companyProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PARTICULAR_PRODUCT)).willReturn(true);

        // when
        boolean result = companyProductHooks.checkIfProductIsParticularProduct(companyProductDD, companyProduct);

        // then
        assertTrue(result);

        verify(companyProduct, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductIsParticularProduct() {
        // given
        given(companyProduct.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PARTICULAR_PRODUCT)).willReturn(false);

        // when
        boolean result = companyProductHooks.checkIfProductIsParticularProduct(companyProductDD, companyProduct);

        // then
        assertFalse(result);

        verify(companyProduct).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProduct, PRODUCT, COMPANY, PRODUCTS)).willReturn(true);

        // when
        boolean result = companyProductHooks.checkIfProductIsNotAlreadyUsed(companyProductDD, companyProduct);

        // then
        assertTrue(result);

        verify(companyProduct, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProduct, PRODUCT, COMPANY, PRODUCTS)).willReturn(false);

        // when
        boolean result = companyProductHooks.checkIfProductIsNotAlreadyUsed(companyProductDD, companyProduct);

        // then
        assertFalse(result);

        verify(companyProduct).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
