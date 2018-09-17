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
package com.qcadoo.mes.supplyNegotiations;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;

public class SupplyNegotiationsColumnLoaderServiceImplTest {

    private SupplyNegotiationsColumnLoaderService supplyNegotiationsColumnLoaderService;

    @Mock
    private ColumnExtensionService columnExtensionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        supplyNegotiationsColumnLoaderService = new SupplyNegotiationsColumnLoaderServiceImpl();

        ReflectionTestUtils.setField(supplyNegotiationsColumnLoaderService, "columnExtensionService", columnExtensionService);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForRequestsEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS)).willReturn(true);

        // when
        boolean result = supplyNegotiationsColumnLoaderService.isColumnsForRequestsEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForRequestsEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_COLUMN_FOR_REQUESTS)).willReturn(false);

        // when
        boolean result = supplyNegotiationsColumnLoaderService.isColumnsForRequestsEmpty();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForOffersEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS)).willReturn(true);

        // when
        boolean result = supplyNegotiationsColumnLoaderService.isColumnsForOffersEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForOffersEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                        SupplyNegotiationsConstants.MODEL_COLUMN_FOR_OFFERS)).willReturn(false);

        // when
        boolean result = supplyNegotiationsColumnLoaderService.isColumnsForOffersEmpty();

        // then
        assertFalse(result);
    }

}
