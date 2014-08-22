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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.LocationFields.TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.materialFlow.constants.LocationType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class SupplyParameterHooksTest {

    private SupplyParameterHooks supplyParameterHooks;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private Entity parameter, location;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        supplyParameterHooks = new SupplyParameterHooks();
    }

    @Test
    public void shouldReturnTrueWhenCheckIfLocationIsWarehouse() {
        // given
        given(parameter.getBelongsToField(LOCATION)).willReturn(null);
        given(location.getStringField(TYPE)).willReturn(LocationType.WAREHOUSE.getStringValue());
        
        // when
        boolean result = supplyParameterHooks.checkIfLocationIsWarehouse(parameterDD, parameter);

        // then
        Assert.assertTrue(result);

        verify(parameter, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfLocationIsWarehouse() {
        // given
        given(parameter.getBelongsToField(LOCATION)).willReturn(location);
        given(location.getStringField(TYPE)).willReturn(LocationType.CONTROL_POINT.getStringValue());

        // when
        boolean result = supplyParameterHooks.checkIfLocationIsWarehouse(parameterDD, parameter);

        // then
        Assert.assertFalse(result);

        verify(parameter).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
