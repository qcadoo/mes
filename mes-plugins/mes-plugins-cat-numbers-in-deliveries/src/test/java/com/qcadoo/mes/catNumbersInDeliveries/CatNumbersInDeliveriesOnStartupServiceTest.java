/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.catNumbersInDeliveries;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.catNumbersInDeliveries.deliveriesColumnExtension.CNIDcolumnLoader;

public class CatNumbersInDeliveriesOnStartupServiceTest {

    private CatNumbersInDeliveriesOnStartupService catNumbersInDeliveriesOnStartupService;

    @Mock
    private CNIDcolumnLoader cNIDcolumnLoader;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        catNumbersInDeliveriesOnStartupService = new CatNumbersInDeliveriesOnStartupService();

        ReflectionTestUtils.setField(catNumbersInDeliveriesOnStartupService, "cNIDcolumnLoader", cNIDcolumnLoader);
    }

    @Test
    public void shouldMultiTenantEnable() {
        // given

        // when
        catNumbersInDeliveriesOnStartupService.multiTenantEnable();

        // then
        verify(cNIDcolumnLoader).addCNIDcolumnsForDeliveries();
        verify(cNIDcolumnLoader).addCNIDcolumnsForOrders();
    }

    @Test
    public void shouldMultiTenantDisable() {
        // given

        // when
        catNumbersInDeliveriesOnStartupService.multiTenantDisable();

        // then
        verify(cNIDcolumnLoader).deleteCNIDcolumnsForDeliveries();
        verify(cNIDcolumnLoader).deleteCNIDcolumnsForOrders();
    }

}
