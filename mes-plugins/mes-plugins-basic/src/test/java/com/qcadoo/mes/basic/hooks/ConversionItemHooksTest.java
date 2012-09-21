package com.qcadoo.mes.basic.hooks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class ConversionItemHooksTest {

    private ConversionItemHooks basicConversionItemHooksTest;

    @Mock
    private DataDefinition dd;

    @Mock
    private Entity entity;

    @Mock
    private Entity product;

    private BigDecimal bdOne = new BigDecimal(1L);

    private BigDecimal bdTwo = new BigDecimal(2L);

    private static final String L_SZT = "szt";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        basicConversionItemHooksTest = new ConversionItemHooks();
    }

    @Test
    public void shouldReturnTrueIfQuantityFromIsOne() {
        // given
        given(entity.getDecimalField("quantityFrom")).willReturn(bdOne);

        // when
        boolean result = basicConversionItemHooksTest.validateQuantityFrom(dd, entity);

        // then
        assertTrue(result);
        verify(entity, Mockito.never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseIfQuantityFromIsNotOne() {
        // given
        given(entity.getDecimalField("quantityFrom")).willReturn(bdTwo);

        // when
        boolean result = basicConversionItemHooksTest.validateQuantityFrom(dd, entity);

        // then
        assertFalse(result);
        verify(entity).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void sholudReturnNullIfProductIsNull() {
        // given
        given(entity.getBelongsToField("product")).willReturn(null);

        // when
        boolean result = basicConversionItemHooksTest.validateUnitOnConversionWithProduct(dd, entity);

        // then
        assertTrue(result);

    }

    @Test
    public void sholudReturnTrueIfProductUnitIsEqualFieldUnit() {
        // given
        given(entity.getBelongsToField("product")).willReturn(product);
        given(entity.getStringField("unitFrom")).willReturn(L_SZT);
        given(product.getStringField("unit")).willReturn(L_SZT);

        // when
        boolean result = basicConversionItemHooksTest.validateUnitOnConversionWithProduct(dd, entity);

        // then
        assertTrue(result);
        verify(entity, Mockito.never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());

    }

    @Test
    public void sholudReturnErrorIfProductUnitIsNotEqualFieldUnit() {
        // given
        given(entity.getBelongsToField("product")).willReturn(product);
        given(entity.getStringField("unitFrom")).willReturn(L_SZT);
        given(product.getStringField("unit")).willReturn(Mockito.anyString());

        // when
        boolean result = basicConversionItemHooksTest.validateUnitOnConversionWithProduct(dd, entity);

        // then
        assertFalse(result);
        verify(entity).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());

    }
}
