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
package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF.LOCATION;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class ParameterHooksDTMFTest {

    private ParameterHooksDTMF parameterHooksDTMF;

    @Mock
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private Entity parameter, location;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parameterHooksDTMF = new ParameterHooksDTMF();

        ReflectionTestUtils.setField(parameterHooksDTMF, "materialFlowResourcesService", materialFlowResourcesService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfLocationIsWarehouse() {
        // given
        given(parameter.getBelongsToField(LOCATION)).willReturn(null);

        // when
        boolean result = parameterHooksDTMF.checkIfLocationIsWarehouse(parameterDD, parameter);

        // then
        Assert.assertTrue(result);

        verify(parameter, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfLocationIsWarehouse() {
        // given
        given(parameter.getBelongsToField(LOCATION)).willReturn(location);
        given(materialFlowResourcesService.isLocationIsWarehouse(location)).willReturn(false);

        // when
        boolean result = parameterHooksDTMF.checkIfLocationIsWarehouse(parameterDD, parameter);

        // then
        Assert.assertFalse(result);

        verify(parameter).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
