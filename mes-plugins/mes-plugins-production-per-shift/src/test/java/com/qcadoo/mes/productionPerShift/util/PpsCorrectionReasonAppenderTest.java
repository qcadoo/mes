/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
