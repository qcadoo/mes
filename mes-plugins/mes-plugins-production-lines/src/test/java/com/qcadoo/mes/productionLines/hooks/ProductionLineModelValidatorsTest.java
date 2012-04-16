package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.QUANTITYFOROTHERWORKSTATIONTYPES;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ProductionLineModelValidatorsTest {

    private ProductionLineModelValidators productionLineModelValidators;

    @Mock
    private DataDefinition productionLineDD;

    private static final Integer L_QUANTITYFOROTHERWORKSTATIONTYPES = 1;

    @Mock
    private Entity productionLine;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionLineModelValidators = new ProductionLineModelValidators();
    }

    @Test
    public void shouldReturnFalseIfQuantityForOthersWorkstationTypesIsNull() {
        // given
        given(productionLine.getBooleanField(ProductionLineFields.SUPPORTSOTHERTECHNOLOGIESWORKSTATIONTYPES)).willReturn(true);
        given(productionLine.getField(QUANTITYFOROTHERWORKSTATIONTYPES)).willReturn(null);

        // when
        boolean result = productionLineModelValidators.checkIfQuantityForOthersWorkstationTypesIsNotNull(productionLineDD,
                productionLine);

        // then
        assertFalse(result);

        verify(productionLine).addError(Mockito.eq(productionLineDD.getField(QUANTITYFOROTHERWORKSTATIONTYPES)),
                Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueIfQuantityForOthersWorkstationTypesIsNotNull() {
        // given
        given(productionLine.getBooleanField(ProductionLineFields.SUPPORTSOTHERTECHNOLOGIESWORKSTATIONTYPES)).willReturn(true);
        given(productionLine.getField(QUANTITYFOROTHERWORKSTATIONTYPES)).willReturn(L_QUANTITYFOROTHERWORKSTATIONTYPES);

        // when
        boolean result = productionLineModelValidators.checkIfQuantityForOthersWorkstationTypesIsNotNull(productionLineDD,
                productionLine);

        // then
        assertTrue(result);

        verify(productionLine, never()).addError(Mockito.eq(productionLineDD.getField(QUANTITYFOROTHERWORKSTATIONTYPES)),
                Mockito.anyString());
    }

}
