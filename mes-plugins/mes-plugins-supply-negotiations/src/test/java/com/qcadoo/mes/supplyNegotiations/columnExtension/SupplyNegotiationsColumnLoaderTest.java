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
package com.qcadoo.mes.supplyNegotiations.columnExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsColumnLoaderService;

public class SupplyNegotiationsColumnLoaderTest {

    private SupplyNegotiationsColumnLoader supplyNegotiationsColumnLoader;

    @Mock
    private SupplyNegotiationsColumnLoaderService supplyNegotiationsColumnLoaderService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        supplyNegotiationsColumnLoader = new SupplyNegotiationsColumnLoader();

        ReflectionTestUtils.setField(supplyNegotiationsColumnLoader, "supplyNegotiationsColumnLoaderService",
                supplyNegotiationsColumnLoaderService);
    }

    @Test
    public void shouldAddColumnsForRequests() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForRequestsEmpty()).willReturn(true);

        // when
        supplyNegotiationsColumnLoader.addColumnsForRequests();

        // then
        verify(supplyNegotiationsColumnLoaderService).fillColumnsForRequests(Mockito.anyString());
    }

    @Test
    public void shouldntAddColumnsForRequests() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForRequestsEmpty()).willReturn(false);

        // when
        supplyNegotiationsColumnLoader.addColumnsForRequests();

        // then
        verify(supplyNegotiationsColumnLoaderService, never()).fillColumnsForRequests(Mockito.anyString());
    }

    @Test
    public void shouldDeleteColumnsForRequests() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForRequestsEmpty()).willReturn(false);

        // when
        supplyNegotiationsColumnLoader.deleteColumnsForRequests();

        // then
        verify(supplyNegotiationsColumnLoaderService).clearColumnsForRequests(Mockito.anyString());
    }

    @Test
    public void shouldntDeleteColumnsForRequests() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForRequestsEmpty()).willReturn(true);

        // when
        supplyNegotiationsColumnLoader.deleteColumnsForRequests();

        // then
        verify(supplyNegotiationsColumnLoaderService, never()).clearColumnsForRequests(Mockito.anyString());
    }

    @Test
    public void shouldAddColumnsForOffers() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForOffersEmpty()).willReturn(true);

        // when
        supplyNegotiationsColumnLoader.addColumnsForOffers();

        // then
        verify(supplyNegotiationsColumnLoaderService).fillColumnsForOffers(Mockito.anyString());
    }

    @Test
    public void shouldntAddColumnsForOffers() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForOffersEmpty()).willReturn(false);

        // when
        supplyNegotiationsColumnLoader.addColumnsForOffers();

        // then
        verify(supplyNegotiationsColumnLoaderService, never()).fillColumnsForOffers(Mockito.anyString());
    }

    @Test
    public void shouldDeleteColumnsForOffers() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForOffersEmpty()).willReturn(false);

        // when
        supplyNegotiationsColumnLoader.deleteColumnsForOffers();

        // then
        verify(supplyNegotiationsColumnLoaderService).clearColumnsForOffers(Mockito.anyString());
    }

    @Test
    public void shouldntDeleteColumnsForOffers() {
        // given
        given(supplyNegotiationsColumnLoaderService.isColumnsForOffersEmpty()).willReturn(true);

        // when
        supplyNegotiationsColumnLoader.deleteColumnsForOffers();

        // then
        verify(supplyNegotiationsColumnLoaderService, never()).clearColumnsForOffers(Mockito.anyString());
    }

}
