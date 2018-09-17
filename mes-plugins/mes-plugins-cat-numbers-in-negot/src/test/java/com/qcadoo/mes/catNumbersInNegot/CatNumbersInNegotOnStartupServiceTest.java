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
package com.qcadoo.mes.catNumbersInNegot;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.catNumbersInNegot.columnExtension.SupplyNegotiationsColumnLoaderCNIN;

public class CatNumbersInNegotOnStartupServiceTest {

    private CatNumbersInNegotOnStartupService catNumbersInNegotOnStartupService;

    @Mock
    private SupplyNegotiationsColumnLoaderCNIN supplyNegotiationsColumnLoaderCNIN;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        catNumbersInNegotOnStartupService = new CatNumbersInNegotOnStartupService();

        ReflectionTestUtils.setField(catNumbersInNegotOnStartupService, "supplyNegotiationsColumnLoaderCNIN",
                supplyNegotiationsColumnLoaderCNIN);
    }

    @Test
    public void shouldMultiTenantEnable() {
        // given

        // when
        catNumbersInNegotOnStartupService.multiTenantEnable();

        // then
        verify(supplyNegotiationsColumnLoaderCNIN).addColumnsForRequestsCNIN();
    }

    @Test
    public void shouldMultiTenantDisable() {
        // given

        // when
        catNumbersInNegotOnStartupService.multiTenantDisable();

        // then
        verify(supplyNegotiationsColumnLoaderCNIN).deleteColumnsForRequestsCNIN();
    }

}
