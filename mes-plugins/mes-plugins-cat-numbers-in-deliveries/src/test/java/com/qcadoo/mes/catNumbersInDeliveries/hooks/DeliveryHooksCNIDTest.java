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
package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.catNumbersInDeliveries.CatNumbersInDeliveriesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class DeliveryHooksCNIDTest {

    private DeliveryHooksCNID deliveryHooksCNID;

    @Mock
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Mock
    private DataDefinition deliveryDD;

    @Mock
    private Entity delivery;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveryHooksCNID = new DeliveryHooksCNID();

        ReflectionTestUtils.setField(deliveryHooksCNID, "catNumbersInDeliveriesService", catNumbersInDeliveriesService);
    }

    @Test
    public void shouldUpdateOrderedProductsCatalogNumbers() {
        // given

        // when
        deliveryHooksCNID.updateOrderedProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(catNumbersInDeliveriesService).updateProductsCatalogNumbers(delivery, ORDERED_PRODUCTS);
    }

    @Test
    public void shouldUpdateDeliveredProductsCatalogNumbers() {
        // given

        // when
        deliveryHooksCNID.updateDeliveredProductsCatalogNumbers(deliveryDD, delivery);

        // then
        verify(catNumbersInDeliveriesService).updateProductsCatalogNumbers(delivery, DELIVERED_PRODUCTS);
    }

}
