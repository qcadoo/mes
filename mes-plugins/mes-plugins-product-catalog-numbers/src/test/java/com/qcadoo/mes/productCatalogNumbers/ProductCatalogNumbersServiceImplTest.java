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
package com.qcadoo.mes.productCatalogNumbers;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumbersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class ProductCatalogNumbersServiceImplTest {

    private ProductCatalogNumbersService productCatalogNumbersService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition productCatalogNumbersDD;

    @Mock
    private Entity productCatalogNumbers, product, supplier;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productCatalogNumbersService = new ProductCatalogNumbersServiceImpl();

        PowerMockito.mockStatic(SearchRestrictions.class);

        ReflectionTestUtils.setField(productCatalogNumbersService, "dataDefinitionService", dataDefinitionService);

        given(
                dataDefinitionService.get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS)).willReturn(productCatalogNumbersDD);
    }

    @Test
    public void shouldReturnNullWhenGetProductCatalogNumber() {
        // given
        given(productCatalogNumbersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(null);

        // when
        Entity result = productCatalogNumbersService.getProductCatalogNumber(null, null);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnProductCatalogNumberWhenFetProductCatalogNumber() {
        // given
        given(productCatalogNumbersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.uniqueResult()).willReturn(productCatalogNumbers);

        // when
        Entity result = productCatalogNumbersService.getProductCatalogNumber(product, supplier);

        // then
        assertEquals(productCatalogNumbers, result);
    }

}
