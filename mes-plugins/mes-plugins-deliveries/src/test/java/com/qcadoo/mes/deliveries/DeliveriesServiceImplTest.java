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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;

public class DeliveriesServiceImplTest {

    private DeliveriesService deliveriesService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition deliveryDD, orderedProductDD, deliveredProductDD, columnForDeliveriesDD, columnForOrdersDD;

    @Mock
    private Entity delivery, orderedProduct, deliveredProduct;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

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
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES))
                .willReturn(columnForDeliveriesDD);
        given(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS))
                .willReturn(columnForOrdersDD);
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
    public void shouldReturnNullWhenGetColumnsForDeliveriesIfColumnsForDeliveriesAreNull() {
        // given
        given(columnForDeliveriesDD.find()).willReturn(searchCriteriaBuilder);
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
