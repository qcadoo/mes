/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.PRODUCTS_FAMILIES;
import static com.qcadoo.mes.deliveries.constants.CompanyProductFields.COMPANY;
import static com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields.PRODUCT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Ignore;
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

public class CompanyProductsFamilyHooksTest {

    private CompanyProductsFamilyHooks companyProductsFamilyHooks;

    @Mock
    private CompanyProductService companyProductService;

    @Mock
    private ProductService productService;

    @Mock
    private DataDefinition companyProductFamilyDD;

    @Mock
    private Entity companyProductsFamily, product;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        companyProductsFamilyHooks = new CompanyProductsFamilyHooks();

        ReflectionTestUtils.setField(companyProductsFamilyHooks, "companyProductService", companyProductService);
        ReflectionTestUtils.setField(companyProductsFamilyHooks, "productService", productService);
    }

    @Ignore
    @Test
    public void shouldReturnTrueWhenCheckIfProductIsProductsFamily() {
        // given
        given(companyProductsFamily.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PRODUCTS_FAMILY)).willReturn(true);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductIsProductsFamily(companyProductFamilyDD, companyProductsFamily);

        // then
        assertTrue(result);

        verify(companyProductsFamily, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Ignore
    @Test
    public void shouldReturnFalseWhenCheckIfProductIsProductsFamily() {
        // given
        given(companyProductsFamily.getBelongsToField(PRODUCT)).willReturn(product);

        given(productService.checkIfProductEntityTypeIsCorrect(product, PRODUCTS_FAMILY)).willReturn(false);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductIsProductsFamily(companyProductFamilyDD, companyProductsFamily);

        // then
        assertFalse(result);

        verify(companyProductsFamily).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Ignore
    @Test
    public void shouldReturnTrueWhenCheckIfProductsFamilyIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProductsFamily, PRODUCT, COMPANY, PRODUCTS_FAMILIES))
                .willReturn(true);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductsFamilyIsNotAlreadyUsed(companyProductFamilyDD,
                companyProductsFamily);

        // then
        assertTrue(result);

        verify(companyProductsFamily, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Ignore
    @Test
    public void shouldReturnFalseWhenCheckIfProductsFamilyIsNotAlreadyUsed() {
        // given
        given(companyProductService.checkIfProductIsNotUsed(companyProductsFamily, PRODUCT, COMPANY, PRODUCTS_FAMILIES))
                .willReturn(false);

        // when
        boolean result = companyProductsFamilyHooks.checkIfProductsFamilyIsNotAlreadyUsed(companyProductFamilyDD,
                companyProductsFamily);

        // then
        assertFalse(result);

        verify(companyProductsFamily).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }
}
