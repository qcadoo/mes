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
package com.qcadoo.mes.deliveries.columnExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.DeliveriesColumnLoaderService;

public class DeliveriesColumnLoaderTest {

    private DeliveriesColumnLoader deliveriesColumnLoader;

    @Mock
    private DeliveriesColumnLoaderService deliveriesColumnLoaderService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesColumnLoader = new DeliveriesColumnLoader();

        ReflectionTestUtils.setField(deliveriesColumnLoader, "deliveriesColumnLoaderService", deliveriesColumnLoaderService);
    }

    @Test
    public void shouldAddColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.addColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService).fillColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldntAddColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.addColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService, never()).fillColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldDeleteColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.deleteColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService).clearColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldntDeleteColumnsForDeliveries() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.deleteColumnsForDeliveries();

        // then
        verify(deliveriesColumnLoaderService, never()).clearColumnsForDeliveries(Mockito.anyString());
    }

    @Test
    public void shouldAddColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.addColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService).fillColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shouldntAddColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.addColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService, never()).fillColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shouldDeleteColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(false);

        // when
        deliveriesColumnLoader.deleteColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService).clearColumnsForOrders(Mockito.anyString());
    }

    @Test
    public void shoulntdDeleteColumnsForOrders() {
        // given
        given(deliveriesColumnLoaderService.isColumnsForOrdersEmpty()).willReturn(true);

        // when
        deliveriesColumnLoader.deleteColumnsForOrders();

        // then
        verify(deliveriesColumnLoaderService, never()).clearColumnsForOrders(Mockito.anyString());
    }

}
