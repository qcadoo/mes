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
package com.qcadoo.mes.negotForOrderSupplies;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.negotForOrderSupplies.columnExtension.OrderSuppliesColumnLoaderNFOS;

public class NegotForOrderSuppliesOnStartupServiceTest {

    private NegotForOrderSuppliesOnStartupService negotForOrderSuppliesOnStartupService;

    @Mock
    private OrderSuppliesColumnLoaderNFOS orderSuppliesColumnLoaderNFOS;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        negotForOrderSuppliesOnStartupService = new NegotForOrderSuppliesOnStartupService();

        ReflectionTestUtils.setField(negotForOrderSuppliesOnStartupService, "orderSuppliesColumnLoaderNFOS",
                orderSuppliesColumnLoaderNFOS);
    }

    @Test
    public void shouldMultiTenantEnable() {
        // given

        // when
        negotForOrderSuppliesOnStartupService.multiTenantEnable();

        // then
        verify(orderSuppliesColumnLoaderNFOS).addColumnsForCoveragesNFOS();
    }

    @Test
    public void shouldMultiTenantDisable() {
        // given

        // when
        negotForOrderSuppliesOnStartupService.multiTenantDisable();

        // then
        verify(orderSuppliesColumnLoaderNFOS).deleteColumnsForCoveragesNFOS();
    }

}
