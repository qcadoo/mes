/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.technologiesGenerator.customization.product;

import static com.qcadoo.testing.model.EntityTestUtils.mockDataDefinition;
import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubId;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductNameSuffix;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductNumberSuffix;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductSuffixes;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ProductCustomizerTest {

    private static final String MAIN_PROD_NAME = "some concrete main product";

    private static final String MAIN_PROD_NUMBER = "some-concrete-main-product-001";

    private static final ProductSuffixes PRODUCT_SUFFIXES = new ProductSuffixes(new ProductNumberSuffix(MAIN_PROD_NUMBER),
            new ProductNameSuffix(MAIN_PROD_NAME));

    private static final String PROD_NAME = "some product";

    private static final String PROD_NUMBER = "some-product-001";

    private ProductCustomizer productCustomizer;

    @Mock
    private CustomizedProductDataProvider customizedProductDataProvider;

    @Mock
    private GeneratorSettings generatorSettings;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productCustomizer = new ProductCustomizer();
        ReflectionTestUtils.setField(productCustomizer, "customizedProductDataProvider", customizedProductDataProvider);

        stubExistingProductSearchResults(null);
    }

    private Entity mockProduct(final Long id, final String number, final String name) {
        Entity product = mockEntity(mockDataDefinition());
        stubId(product, id);
        stubStringField(product, ProductFields.NUMBER, number);
        stubStringField(product, ProductFields.NAME, name);
        return product;
    }

    private void stubExistingProductSearchResults(final Entity matchingProduct) {
        given(customizedProductDataProvider.tryFind(any(Entity.class), anyString())).willReturn(
                Optional.ofNullable(matchingProduct));
    }

    @Test
    public final void shouldReturnExistingCustomizedProduct() {
        // given
        Entity existingCustomizedProduct = mockEntity();
        Entity product = mockProduct(1L, PROD_NUMBER, PROD_NAME);
        stubExistingProductSearchResults(existingCustomizedProduct);

        // when
        Entity customizedProduct = productCustomizer.findOrCreate(product, product, PRODUCT_SUFFIXES, generatorSettings);

        // then
        assertEquals(existingCustomizedProduct, customizedProduct);
    }

    @Test
    public final void shouldCreateConcreteProduct() {
        // given
        Entity product = mockProduct(1L, PROD_NUMBER, PROD_NAME);
        stubExistingProductSearchResults(null);

        DataDefinition productDD = product.getDataDefinition();
        List<Entity> copyResults = Lists.newArrayList(mockEntity(productDD));
        given(productDD.copy(anyLong())).willReturn(copyResults);

        // when
        Entity customizedProduct = productCustomizer.findOrCreate(product, product, PRODUCT_SUFFIXES, generatorSettings);

        // then
        verify(product.getDataDefinition()).copy(1L);
        verify(customizedProduct).setField(ProductFields.NAME, String.format("%s - %s", PROD_NAME, MAIN_PROD_NAME));
        verify(customizedProduct).setField(ProductFields.NUMBER, String.format("%s - %s", PROD_NUMBER, MAIN_PROD_NUMBER));
        verify(customizedProduct).setField(ProductFields.PARENT, product);
        verify(customizedProduct).setField(ProductFields.ENTITY_TYPE,
                ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
    }

}
