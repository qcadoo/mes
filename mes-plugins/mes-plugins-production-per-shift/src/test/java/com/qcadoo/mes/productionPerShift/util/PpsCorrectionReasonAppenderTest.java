package com.qcadoo.mes.productionPerShift.util;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.QcadooModelMatchers.anyEntity;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ReasonTypeOfCorrectionPlanFields;
import com.qcadoo.mes.productionPerShift.domain.PpsCorrectionReason;
import com.qcadoo.mes.productionPerShift.domain.ProductionPerShiftId;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityTestUtils;

public class PpsCorrectionReasonAppenderTest {

    private static final ProductionPerShiftId PPS_ID = new ProductionPerShiftId(1L);

    private static final PpsCorrectionReason CORRECTION_REASON = new PpsCorrectionReason("XYZ");

    private PpsCorrectionReasonAppender ppsCorrectionReasonAppender;

    private DataDefinition correctionReasonDataDef;

    private Entity newlyCreatedReasonEntity;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

        ppsCorrectionReasonAppender = new PpsCorrectionReasonAppender(dataDefinitionService);

        correctionReasonDataDef = EntityTestUtils.mockDataDefinition();
        given(correctionReasonDataDef.getPluginIdentifier()).willReturn(ProductionPerShiftConstants.PLUGIN_IDENTIFIER);
        given(correctionReasonDataDef.getName()).willReturn(ProductionPerShiftConstants.MODEL_REASON_TYPE_OF_CORRECTION_PLAN);
        given(
                dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                        ProductionPerShiftConstants.MODEL_REASON_TYPE_OF_CORRECTION_PLAN)).willReturn(correctionReasonDataDef);

        newlyCreatedReasonEntity = mockEntity(null, correctionReasonDataDef);
        given(newlyCreatedReasonEntity.isValid()).willReturn(true);
        given(correctionReasonDataDef.create()).willReturn(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldFailDueToMissingPpsId() {
        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(null, CORRECTION_REASON);

        // then
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals("Missing pps id!", res.getLeft());
        verify(correctionReasonDataDef, never()).save(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldFailDueToMissingReason() {
        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(PPS_ID, null);

        // then
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals("Missing or blank reason type value!", res.getLeft());
        verify(correctionReasonDataDef, never()).save(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldFailDueToMissingReasonValue() {
        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(PPS_ID, new PpsCorrectionReason(null));

        // then
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals("Missing or blank reason type value!", res.getLeft());
        verify(correctionReasonDataDef, never()).save(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldFailDueToEmptyReasonValue() {
        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(PPS_ID, new PpsCorrectionReason(""));

        // then
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals("Missing or blank reason type value!", res.getLeft());
        verify(correctionReasonDataDef, never()).save(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldFailDueToBlankReasonValue() {
        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(PPS_ID, new PpsCorrectionReason("  "));

        // then
        Assert.assertTrue(res.isLeft());
        Assert.assertEquals("Missing or blank reason type value!", res.getLeft());
        verify(correctionReasonDataDef, never()).save(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldFailDueToValidationErrors() {
        // given
        Entity invalidEntity = mockEntity(null, correctionReasonDataDef);
        given(invalidEntity.isValid()).willReturn(false);
        given(correctionReasonDataDef.save(anyEntity())).willReturn(invalidEntity);

        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(PPS_ID, CORRECTION_REASON);

        // then
        Assert.assertTrue(res.isLeft());
        String expectedMsg = String.format("Cannot save %s.%s because of validation errors",
                ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_REASON_TYPE_OF_CORRECTION_PLAN);
        Assert.assertEquals(expectedMsg, res.getLeft());
        verify(correctionReasonDataDef, times(1)).save(newlyCreatedReasonEntity);
    }

    @Test
    public final void shouldAppendReason() {
        // given
        Entity validSavedEntity = mockEntity(null, correctionReasonDataDef);
        given(validSavedEntity.isValid()).willReturn(true);
        given(correctionReasonDataDef.save(anyEntity())).willReturn(validSavedEntity);

        // when
        Either<String, Entity> res = ppsCorrectionReasonAppender.append(PPS_ID, CORRECTION_REASON);

        // then
        Assert.assertTrue(res.isRight());
        Assert.assertEquals(validSavedEntity, res.getRight());

        verify(correctionReasonDataDef, times(1)).save(newlyCreatedReasonEntity);
        verify(newlyCreatedReasonEntity).setField(ReasonTypeOfCorrectionPlanFields.PRODUCTION_PER_SHIFT, PPS_ID.get());
        verify(newlyCreatedReasonEntity).setField(ReasonTypeOfCorrectionPlanFields.REASON_TYPE_OF_CORRECTION_PLAN,
                CORRECTION_REASON.get());
    }

}
