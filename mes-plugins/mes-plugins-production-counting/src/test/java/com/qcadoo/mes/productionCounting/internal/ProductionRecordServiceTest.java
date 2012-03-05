package com.qcadoo.mes.productionCounting.internal;

import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class ProductionRecordServiceTest {

    private ProductionRecordService productionRecordService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity productionRecord;

    @Mock
    private Entity counting, operation;

    @Mock
    private FieldDefinition orderField, operationField;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordService = new ProductionRecordService();

        given(dataDefinition.getField("order")).willReturn(orderField);
        given(dataDefinition.getField("orderOperationComponent")).willReturn(operationField);

        given(productionRecord.getDataDefinition()).willReturn(dataDefinition);
    }

    @Test
    public void shouldNotAllowToAddProductionCountingWhenThereAreFinalOnesForNullOperation() {
        // given
        List<Entity> productionCountings = Arrays.asList(counting);
        given(counting.getBooleanField("lastRecord")).willReturn(true);
        given(productionRecord.getBelongsToField("orderOperationComponent")).willReturn(null);

        // when
        boolean canIAdd = productionRecordService.willOrderAcceptOneMoreValidator(productionCountings, productionRecord,
                dataDefinition);

        // then
        assertFalse(canIAdd);
        verify(productionRecord).addError(orderField, "productionCounting.record.messages.error.final");
    }

    @Test
    public void shouldNotAllowToAddProductionCountingWhenThereAreFinalOnesForGivenOperation() {
        // given
        List<Entity> productionCountings = Arrays.asList(counting);
        given(counting.getBooleanField("lastRecord")).willReturn(true);

        given(productionRecord.getBelongsToField("orderOperationComponent")).willReturn(operation);

        // when
        boolean canIAdd = productionRecordService.willOrderAcceptOneMoreValidator(productionCountings, productionRecord,
                dataDefinition);

        // then
        assertFalse(canIAdd);
        verify(productionRecord).addError(operationField, "productionCounting.record.messages.error.operationFinal");
    }
}
