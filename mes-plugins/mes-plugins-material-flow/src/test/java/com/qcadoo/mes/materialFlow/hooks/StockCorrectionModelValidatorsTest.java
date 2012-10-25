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
