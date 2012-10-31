/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.productionPerShift;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class PPSHelperTest {

    private PPSHelper helper;

    @Mock
    private ViewDefinitionState viewState;

    @Mock
    private ComponentState lookup;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity entity;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private FieldComponent field;

    @Before
    public void init() {
        helper = new PPSHelper();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(helper, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldReturnFalseWhenProgressTypeIsPlanned() throws Exception {
        // given
        when(viewState.getComponentByReference("plannedProgressType")).thenReturn(field);
        when(field.getFieldValue()).thenReturn("01planned");
        // when
        boolean result = helper.shouldHasCorrections(viewState);
        // then
        Assert.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenProgressTypeIsPlanned() throws Exception {
        // given
        when(viewState.getComponentByReference("plannedProgressType")).thenReturn(field);
        when(field.getFieldValue()).thenReturn("02corrected");
        // when
        boolean result = helper.shouldHasCorrections(viewState);
        // then
        Assert.assertTrue(result);
    }

}