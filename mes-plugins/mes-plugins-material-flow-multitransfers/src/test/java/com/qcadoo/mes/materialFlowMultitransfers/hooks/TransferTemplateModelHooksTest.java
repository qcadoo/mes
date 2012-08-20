/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.materialFlowMultitransfers.hooks;

import static com.qcadoo.mes.materialFlowMultitransfers.constants.TransferTemplateFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlowMultitransfers.constants.TransferTemplateFields.LOCATION_TO;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class TransferTemplateModelHooksTest {

    private TransferTemplateModelValidators transferTemplateModelHooks;

    @Mock
    private DataDefinition transferTemplateDD;

    @Mock
    private Entity transferTemplate, locationFrom, locationTo;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transferTemplateModelHooks = new TransferTemplateModelValidators();

    }

    @Test
    public void shouldReturnFalseWhenCheckIfOneOfLocationsIsNotNullAndLocationsAreNull() {
        // given
        given(transferTemplate.getBelongsToField(LOCATION_FROM)).willReturn(null);
        given(transferTemplate.getBelongsToField(LOCATION_TO)).willReturn(null);

        // when
        boolean result = transferTemplateModelHooks.checkIfOneOfLocationsIsNotNull(transferTemplateDD, transferTemplate);

        // then
        Assert.assertFalse(result);

        verify(transferTemplate, Mockito.times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOneOfLocationsIsNotNullAndLocationsArentNull() {
        // given
        given(transferTemplate.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transferTemplate.getBelongsToField(LOCATION_TO)).willReturn(locationTo);

        // when
        boolean result = transferTemplateModelHooks.checkIfOneOfLocationsIsNotNull(transferTemplateDD, transferTemplate);

        // then
        Assert.assertTrue(result);

        verify(transferTemplate, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }
}
