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
package com.qcadoo.mes.orderSupplies;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchResult;

public class OrderSuppliesServiceImplTest {

    private OrderSuppliesService orderSuppliesService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition materialRequirementCoverageDD, coverageLocationDD, coverageProductDD, coverageProductLoggingDD,
            columnForCoveragesDD;

    @Mock
    private Entity materialRequirementCoverage, coverageLocation, coverageProduct, coverageProductLogging;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> columnsForCoverages;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderSuppliesService = new OrderSuppliesServiceImpl();

        ReflectionTestUtils.setField(orderSuppliesService, "dataDefinitionService", dataDefinitionService);

        given(
                dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                        OrderSuppliesConstants.MODEL_MATERIAL_REQUIREMENT_COVERAGE)).willReturn(materialRequirementCoverageDD);
        given(dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_LOCATION))
                .willReturn(coverageLocationDD);
        given(dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT))
                .willReturn(coverageProductDD);
        given(
                dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                        OrderSuppliesConstants.MODEL_COVERAGE_PRODUCT_LOGGING)).willReturn(coverageProductLoggingDD);
        given(
                dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                        OrderSuppliesConstants.MODEL_COLUMN_FOR_COVERAGES)).willReturn(columnForCoveragesDD);
    }

    @Test
    public void shouldReturnNullWhenGetMaterialRequirementCoverage() {
        // given
        Long materialRequirementCoverageId = null;

        given(materialRequirementCoverageDD.get(materialRequirementCoverageId)).willReturn(null);

        // when
        Entity result = orderSuppliesService.getMaterialRequirementCoverage(materialRequirementCoverageId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldMaterialRequirementCoverageWhenGetMaterialRequirementCoverage() {
        // given
        Long materialRequirementCoverageId = 1L;

        given(materialRequirementCoverageDD.get(materialRequirementCoverageId)).willReturn(materialRequirementCoverage);

        // when
        Entity result = orderSuppliesService.getMaterialRequirementCoverage(materialRequirementCoverageId);

        // then
        assertEquals(materialRequirementCoverage, result);
    }

    @Test
    public void shouldReturnNullWhenGetCoverageLocation() {
        // given
        Long coverageLocationId = null;

        given(coverageLocationDD.get(coverageLocationId)).willReturn(null);

        // when
        Entity result = orderSuppliesService.getCoverageLocation(coverageLocationId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldCoverageProductWhenGetCoverageLocation() {
        // given
        Long coverageLocationId = 1L;

        given(coverageLocationDD.get(coverageLocationId)).willReturn(coverageLocation);

        // when
        Entity result = orderSuppliesService.getCoverageLocation(coverageLocationId);

        // then
        assertEquals(coverageLocation, result);
    }

    @Test
    public void shouldReturnNullWhenGetCoverageProduct() {
        // given
        Long coverageProductId = null;

        given(coverageProductDD.get(coverageProductId)).willReturn(null);

        // when
        Entity result = orderSuppliesService.getCoverageProduct(coverageProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldCoverageProductWhenGetCoverageProduct() {
        // given
        Long coverageProductId = 1L;

        given(coverageProductDD.get(coverageProductId)).willReturn(coverageProduct);

        // when
        Entity result = orderSuppliesService.getCoverageProduct(coverageProductId);

        // then
        assertEquals(coverageProduct, result);
    }

    @Test
    public void shouldReturnNullWhenGetCoverageProductLogging() {
        // given
        Long coverageProductLoggingId = null;

        given(coverageProductLoggingDD.get(coverageProductLoggingId)).willReturn(null);

        // when
        Entity result = orderSuppliesService.getCoverageProductLogging(coverageProductLoggingId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldCoverageProductWhenGetCoverageProductLogging() {
        // given
        Long coverageProductLoggingId = 1L;

        given(coverageProductLoggingDD.get(coverageProductLoggingId)).willReturn(coverageProductLogging);

        // when
        Entity result = orderSuppliesService.getCoverageProductLogging(coverageProductLoggingId);

        // then
        assertEquals(coverageProductLogging, result);
    }

    @Test
    public void shouldReturnNullWhenGetColumnsForCoveragesIfColumnsForCoveragesAreNull() {
        // given
        given(columnForCoveragesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        List<Entity> result = orderSuppliesService.getColumnsForCoverages();

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnColumnsForCoveragesWhenGetColumnsForCoveragesIfColumnsForCoveragesArentNull() {
        // given
        given(columnForCoveragesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(columnsForCoverages);

        // when
        List<Entity> result = orderSuppliesService.getColumnsForCoverages();

        // then
        assertEquals(columnsForCoverages, result);
    }

    @Test
    public void shouldReturnMaterialRequirementCoverageDD() {
        // given

        // when
        DataDefinition result = orderSuppliesService.getMaterialRequirementCoverageDD();

        // then
        assertEquals(materialRequirementCoverageDD, result);
    }

    @Test
    public void shouldReturnCoverageLocationDD() {
        // given

        // when
        DataDefinition result = orderSuppliesService.getCoverageLocationDD();

        // then
        assertEquals(coverageLocationDD, result);
    }

    @Test
    public void shouldReturnCoverageProductDD() {
        // given

        // when
        DataDefinition result = orderSuppliesService.getCoverageProductDD();

        // then
        assertEquals(coverageProductDD, result);
    }

    @Test
    public void shouldReturnCoverageProductLoggingDD() {
        // given

        // when
        DataDefinition result = orderSuppliesService.getCoverageProductLoggingDD();

        // then
        assertEquals(coverageProductLoggingDD, result);
    }

    @Test
    public void shouldReturnColumnForCoveragesDD() {
        // given

        // when
        DataDefinition result = orderSuppliesService.getColumnForCoveragesDD();

        // then
        assertEquals(columnForCoveragesDD, result);
    }

}
