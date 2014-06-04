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
package com.qcadoo.mes.basic.hooks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.constants.UnitConversionItemFields;

public class UnitConversionItemValidatorsBTest {

    private UnitConversionItemValidatorsB unitConversionItemValidatorsB;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity unitConversionItem;

    @Mock
    private Entity product;

    private static final String SOME_UNIT = "someUnit";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        unitConversionItemValidatorsB = new UnitConversionItemValidatorsB();

        given(unitConversionItem.getBelongsToField(UnitConversionItemFieldsB.PRODUCT)).willReturn(product);
    }

    private void stubProductUnit(final String unit) {
        given(product.getStringField(ProductFields.UNIT)).willReturn(unit);
    }

    private void stubConversionSourceUnit(final String unit) {
        given(unitConversionItem.getStringField(UnitConversionItemFields.UNIT_FROM)).willReturn(unit);
    }

    @Test
    public void sholudReturnTrueIfProductIsNull() {
        // given
        given(unitConversionItem.getBelongsToField(UnitConversionItemFieldsB.PRODUCT)).willReturn(null);

        // when
        boolean result = unitConversionItemValidatorsB.validateUnitOnConversionWithProduct(dataDefinition, unitConversionItem);

        // then
        assertTrue(result);

    }

    @Test
    public void sholudReturnTrueIfProductUnitIsEqualFieldUnit() {
        // given
        stubConversionSourceUnit(SOME_UNIT);
        stubProductUnit(SOME_UNIT);

        // when
        boolean result = unitConversionItemValidatorsB.validateUnitOnConversionWithProduct(dataDefinition, unitConversionItem);

        // then
        assertTrue(result);
        verify(unitConversionItem, Mockito.never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());

    }

    @Test
    public void sholudReturnErrorIfProductUnitIsNotEqualFieldUnit() {
        // given
        stubConversionSourceUnit(SOME_UNIT);
        stubProductUnit("otherNotEqualSzt");

        // when
        boolean result = unitConversionItemValidatorsB.validateUnitOnConversionWithProduct(dataDefinition, unitConversionItem);

        // then
        assertFalse(result);
        verify(unitConversionItem).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());

    }
}
