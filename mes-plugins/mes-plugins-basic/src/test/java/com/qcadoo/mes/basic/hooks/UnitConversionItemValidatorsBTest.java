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
