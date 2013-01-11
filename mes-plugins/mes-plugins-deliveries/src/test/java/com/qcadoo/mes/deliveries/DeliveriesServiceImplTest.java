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
package com.qcadoo.mes.deliveries;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.List;

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

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class DeliveriesServiceImplTest {

    private DeliveriesService deliveriesService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition deliveryDD, orderedProductDD, deliveredProductDD, companyProductDD, companyProductsFamilyDD,
            columnForDeliveriesDD, columnForOrdersDD;

    @Mock
    private Entity delivery, orderedProduct, deliveredProduct, companyProduct, companyProductsFamily, product;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Mock
    private List<Entity> companyProducts;

    @Mock
    private List<Entity> companyProductsFamilies;

    @Mock
    private List<Entity> columnsForDeliveries;

    @Mock
    private List<Entity> columnsForOrders;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesService = new DeliveriesServiceImpl();

        ReflectionTestUtils.setField(deliveriesService, "dataDefinitionService", dataDefinitionService);

        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY)).willReturn(
                deliveryDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT))
                .willReturn(orderedProductDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT))
                .willReturn(deliveredProductDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COMPANY_PRODUCT))
                .willReturn(companyProductDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COMPANY_PRODUCTS_FAMILY))
                .willReturn(companyProductsFamilyDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES))
                .willReturn(columnForDeliveriesDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS))
                .willReturn(columnForOrdersDD);

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public void shouldReturnNullWhenGetDelivery() {
        // given
        Long deliveryId = null;

        given(deliveryDD.get(deliveryId)).willReturn(null);

        // when
        Entity result = deliveriesService.getDelivery(deliveryId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnDeliveryWhenGetDelivery() {
        // given
        Long deliveryId = 1L;

        given(deliveryDD.get(deliveryId)).willReturn(delivery);

        // when
        Entity result = deliveriesService.getDelivery(deliveryId);

        // then
        assertEquals(delivery, result);
    }

    @Test
    public void shouldReturnNullWhenGetOrderedProduct() {
        // given
        Long orderedProductId = null;

        given(orderedProductDD.get(orderedProductId)).willReturn(null);

        // when
        Entity result = deliveriesService.getOrderedProduct(orderedProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnOrderedProductWhenGetOrderedProduct() {
        // given
        Long orderedProductId = 1L;

        given(orderedProductDD.get(orderedProductId)).willReturn(orderedProduct);

        // when
        Entity result = deliveriesService.getOrderedProduct(orderedProductId);

        // then
        assertEquals(orderedProduct, result);
    }

    @Test
    public void shouldReturnNullWhenGetDeliveredProduct() {
        // given
        Long deliveredProductId = null;

        given(deliveredProductDD.get(deliveredProductId)).willReturn(null);

        // when
        Entity result = deliveriesService.getDeliveredProduct(deliveredProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnDeliveredProductWhenGetDeliveredProduct() {
        // given
        Long deliveredProductId = 1L;

        given(deliveredProductDD.get(deliveredProductId)).willReturn(deliveredProduct);

        // when
        Entity result = deliveriesService.getDeliveredProduct(deliveredProductId);

        // then
        assertEquals(deliveredProduct, result);
    }

    @Test
    public void shouldReturnNullWhenGetCompanyProduct() {
        // given
        Long companyProductId = null;

        given(companyProductDD.get(companyProductId)).willReturn(null);

        // when
        Entity result = deliveriesService.getDeliveredProduct(companyProductId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnCompanyProductWhenGetCompanyProduct() {
        // given
        Long companyProductId = 1L;

        given(companyProductDD.get(companyProductId)).willReturn(companyProduct);

        // when
        Entity result = deliveriesService.getCompanyProduct(companyProductId);

        // then
        assertEquals(companyProduct, result);
    }

    @Test
    public void shouldReturnNullWhenGetCompanyProductsIfCompanyProductsAreNull() {
        // given
        given(companyProductDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        List<Entity> result = deliveriesService.getCompanyProducts(product);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnCompanyProductsWhenGetCompanyProductsIfCompanyProductsArentNull() {
        // given
        given(companyProductDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(companyProducts);

        // when
        List<Entity> result = deliveriesService.getCompanyProducts(product);

        // then
        assertEquals(companyProducts, result);
    }

    @Test
    public void shouldReturnNullWhenGetCompanyProductsFamily() {
        // given
        Long companyProductsFamilyId = null;

        given(companyProductsFamilyDD.get(companyProductsFamilyId)).willReturn(null);

        // when
        Entity result = deliveriesService.getDeliveredProduct(companyProductsFamilyId);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnCompanyProductsFamilyWhenGetCompanyProductsFamily() {
        // given
        Long companyProductsFamilyId = 1L;

        given(companyProductsFamilyDD.get(companyProductsFamilyId)).willReturn(companyProductsFamily);

        // when
        Entity result = deliveriesService.getCompanyProductsFamily(companyProductsFamilyId);

        // then
        assertEquals(companyProductsFamily, result);
    }

    @Test
    public void shouldReturnNullWhenGetCompanyProductFamiliesIfCompanyProductFamiliesAreNull() {
        // given
        given(companyProductsFamilyDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        List<Entity> result = deliveriesService.getCompanyProductsFamilies(product);

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnCompanyProductFamiliesWhenGetCompanyProductFamiliesIfCompanyProductFamiliesArentNull() {
        // given
        given(companyProductsFamilyDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(companyProductsFamilies);

        // when
        List<Entity> result = deliveriesService.getCompanyProductsFamilies(product);

        // then
        assertEquals(companyProductsFamilies, result);
    }

    @Test
    public void shouldReturnNullWhenGetColumnsForDeliveriesIfColumnsForDeliveriesAreNull() {
        // given
        given(columnForDeliveriesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        List<Entity> result = deliveriesService.getColumnsForDeliveries();

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnColumnsForDeliveriesWhenGetColumnsForDeliveriesIfColumnsForDeliveriesArentNull() {
        // given
        given(columnForDeliveriesDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(columnsForDeliveries);

        // when
        List<Entity> result = deliveriesService.getColumnsForDeliveries();

        // then
        assertEquals(columnsForDeliveries, result);
    }

    @Test
    public void shouldReturnNullWhenGetColumnsForOrdersIfColumnsForOrdersAreNull() {
        // given
        given(columnForOrdersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(null);

        // when
        List<Entity> result = deliveriesService.getColumnsForOrders();

        // then
        assertEquals(null, result);
    }

    @Test
    public void shouldReturnColumnsForOrdersWhenGetColumnsForOrdersIfColumnsForOrdersArentNull() {
        // given
        given(columnForOrdersDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getEntities()).willReturn(columnsForOrders);

        // when
        List<Entity> result = deliveriesService.getColumnsForOrders();

        // then
        assertEquals(columnsForOrders, result);
    }

    @Test
    public void shouldReturnDeliveryDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getDeliveryDD();

        // then
        assertEquals(deliveryDD, result);
    }

    @Test
    public void shouldReturnOrderedProductDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getOrderedProductDD();

        // then
        assertEquals(orderedProductDD, result);
    }

    @Test
    public void shouldReturnDeliveredProductDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getDeliveredProductDD();

        // then
        assertEquals(deliveredProductDD, result);
    }

    @Test
    public void shouldReturnCompanyProductDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getCompanyProductDD();

        // then
        assertEquals(companyProductDD, result);
    }

    @Test
    public void shouldReturnCompanyProductsFamilyDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getCompanyProductsFamilyDD();

        // then
        assertEquals(companyProductsFamilyDD, result);
    }

    @Test
    public void shouldReturnColumnForDeliveriesDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getColumnForDeliveriesDD();

        // then
        assertEquals(columnForDeliveriesDD, result);
    }

    @Test
    public void shouldReturnColumnForOrdersDD() {
        // given

        // when
        DataDefinition result = deliveriesService.getColumnForOrdersDD();

        // then
        assertEquals(columnForOrdersDD, result);
    }

}
