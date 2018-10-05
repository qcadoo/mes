/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.advancedGenealogy.util;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.types.FieldType;

public class BatchUtilTest {

    private static final String BATCH_FIELD_NAME = "batch";

    private static final String BATCH_NUMBER = "aa0001";

    @Mock
    private Entity entity;

    @Mock
    private FieldDefinition fieldDefinition;

    @Mock
    private BelongsToType belongsToType;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        given(dataDefinition.getField(BATCH_FIELD_NAME)).willReturn(fieldDefinition);
        given(entity.getDataDefinition()).willReturn(dataDefinition);
    }

    private void stubBatchFieldEntityBatchNumber(final String batchNumber) {
        Entity batchEntity = mock(Entity.class);

        given(batchEntity.getStringField(BatchFields.NUMBER)).willReturn(batchNumber);
        given(batchEntity.getField(BatchFields.NUMBER)).willReturn(batchNumber);

        given(entity.getField(BATCH_FIELD_NAME)).willReturn(batchEntity);
        given(entity.getBelongsToField(BATCH_FIELD_NAME)).willReturn(batchEntity);
    }

    private void stubFieldDefinition(final FieldType fieldType) {
        given(fieldDefinition.getType()).willReturn(fieldType);
    }

    private void stubBelongsToType(final String pluginIdentifier, final String modelName) {
        DataDefinition correspondingDataDefinition = mock(DataDefinition.class);
        given(correspondingDataDefinition.getPluginIdentifier()).willReturn(pluginIdentifier);
        given(correspondingDataDefinition.getName()).willReturn(modelName);
        given(belongsToType.getDataDefinition()).willReturn(correspondingDataDefinition);
    }

    @Test
    public void shouldReturnBatchNumber() {
        // given
        stubFieldDefinition(belongsToType);
        stubBelongsToType(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);
        stubBatchFieldEntityBatchNumber(BATCH_NUMBER);

        // when
        String result = BatchUtil.extractNumberFrom(entity, BATCH_FIELD_NAME);

        // then
        assertEquals(BATCH_NUMBER, result);
    }

    @Test
    public void shouldReturnNullIfBatchDoesNotHaveSpecifiedNumber() {
        // given
        stubFieldDefinition(belongsToType);
        stubBelongsToType(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);
        stubBatchFieldEntityBatchNumber(null);

        // when
        String result = BatchUtil.extractNumberFrom(entity, BATCH_FIELD_NAME);

        // then
        assertNull(result);
    }

    @Test
    public void shouldReturnNullIfBatchBelongsToFieldIsEmpty() {
        // given
        stubFieldDefinition(belongsToType);
        stubBelongsToType(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH);

        // when
        String result = BatchUtil.extractNumberFrom(entity, BATCH_FIELD_NAME);

        // then
        assertNull(result);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfBelongsToFieldDoesNotPointToTheBatchModel() {
        // given
        stubFieldDefinition(belongsToType);
        stubBelongsToType(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, "DefinitelyNotA" + AdvancedGenealogyConstants.MODEL_BATCH);

        // when & then
        try {
            BatchUtil.extractNumberFrom(entity, BATCH_FIELD_NAME);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            verify(entity, never()).getBelongsToField(Mockito.anyString());
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionIfFieldIsNotAKindOfTheBelongsToType() {
        // given
        FieldType notABelongsToType = mock(FieldType.class);
        stubFieldDefinition(notABelongsToType);

        // when & then
        try {
            BatchUtil.extractNumberFrom(entity, BATCH_FIELD_NAME);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            verify(entity, never()).getBelongsToField(Mockito.anyString());
        }
    }

}
