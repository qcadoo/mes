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
package com.qcadoo.mes.techSubcontrForDeliveries;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.techSubcontrForDeliveries.deliveriesColumnExtension.TSFDcolumnLoader;

public class TechSubcontrForDeliveriesOnStartupServiceTest {

    private TechSubcontrForDeliveriesOnStartupService techSubcontrForDeliveriesOnStartupService;

    @Mock
    private TSFDcolumnLoader tSFDcolumnLoader;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        techSubcontrForDeliveriesOnStartupService = new TechSubcontrForDeliveriesOnStartupService();

        ReflectionTestUtils.setField(techSubcontrForDeliveriesOnStartupService, "tSFDcolumnLoader", tSFDcolumnLoader);
    }

    @Test
    public void shouldMultiTenantEnable() {
        // given

        // when
        techSubcontrForDeliveriesOnStartupService.multiTenantEnable();

        // then
        verify(tSFDcolumnLoader).addTSFDcolumnsForDeliveries();
        verify(tSFDcolumnLoader).addTSFDcolumnsForOrders();
    }

    @Test
    public void shouldMultiTenantDisable() {
        // given

        // when
        techSubcontrForDeliveriesOnStartupService.multiTenantDisable();

        // then
        verify(tSFDcolumnLoader).deleteTSFDcolumnsForDeliveries();
        verify(tSFDcolumnLoader).deleteTSFDcolumnsForOrders();
    }

}
