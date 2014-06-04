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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.LocationFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.LocationType.CONTROL_POINT;
import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class StockCorrectionModelValidatorsTest {

    private StockCorrectionModelValidators stockCorrectionModelValidators;

    @Mock
    private DataDefinition stockCorrectionDD;

    @Mock
    private Entity stockCorrection, location;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        stockCorrectionModelValidators = new StockCorrectionModelValidators();
    }

    @Test
    public void shouldReturnFalseAndAddErrorWhenValidateStockCorrectionIfLocationIsntNullAndLocationTypeIsntControlPoint() {
        // given
        given(stockCorrection.getBelongsToField(LOCATION)).willReturn(location);
        given(location.getStringField(TYPE)).willReturn("otherLocation");

        // when
        boolean result = stockCorrectionModelValidators.validateStockCorrection(stockCorrectionDD, stockCorrection);

        // then
        assertFalse(result);

        verify(stockCorrection).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenValidateStockCorrectionIfLocationIsntNullAndLocationTypeIsControlPoint() {
        // given
        given(stockCorrection.getBelongsToField(LOCATION)).willReturn(location);
        given(location.getStringField(TYPE)).willReturn(CONTROL_POINT.getStringValue());

        // when
        boolean result = stockCorrectionModelValidators.validateStockCorrection(stockCorrectionDD, stockCorrection);

        // then
        assertTrue(result);

        verify(stockCorrection, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenValidateStockCorrectionIfLocationIsNull() {
        // given
        given(stockCorrection.getBelongsToField(LOCATION)).willReturn(null);

        // when
        boolean result = stockCorrectionModelValidators.validateStockCorrection(stockCorrectionDD, stockCorrection);

        // then
        assertTrue(result);

        verify(stockCorrection, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
