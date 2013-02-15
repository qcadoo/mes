/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType.FOR_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType.FOR_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class LineChangeoverNormsDetailsHooksTest {

    private LineChangeoverNormsDetailsHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent changeoverType, fromTechnology, toTechnology, fromTechnologyGroup, toTechnologyGroup;

    @Before
    public void init() {
        hooks = new LineChangeoverNormsDetailsHooks();

        MockitoAnnotations.initMocks(this);

        given(view.getComponentByReference(CHANGEOVER_TYPE)).willReturn(changeoverType);
        given(view.getComponentByReference(FROM_TECHNOLOGY)).willReturn(fromTechnology);
        given(view.getComponentByReference(TO_TECHNOLOGY)).willReturn(toTechnology);
        given(view.getComponentByReference(FROM_TECHNOLOGY_GROUP)).willReturn(fromTechnologyGroup);
        given(view.getComponentByReference(TO_TECHNOLOGY_GROUP)).willReturn(toTechnologyGroup);
    }

    @Test
    public void shouldSetFieldsVisibleAndRequiredWhenChangeoverTypeIsForTechnology() {
        // given
        given(changeoverType.getFieldValue()).willReturn(FOR_TECHNOLOGY.getStringValue());

        // given
        hooks.setFieldsVisibleAndRequired(view, null, null);

        // then
        verify(fromTechnology).setVisible(true);
        verify(fromTechnology).setRequired(true);

        verify(toTechnology).setVisible(true);
        verify(toTechnology).setRequired(true);

        verify(fromTechnologyGroup).setVisible(false);
        verify(fromTechnologyGroup).setRequired(false);
        verify(fromTechnologyGroup).setFieldValue(null);

        verify(toTechnologyGroup).setVisible(false);
        verify(toTechnologyGroup).setRequired(false);
        verify(toTechnologyGroup).setFieldValue(null);
    }

    @Test
    public void shouldSetFieldsVisibleAndRequiredWhenChangeoverTypeIsForTechnologyGroup() {
        // given
        given(changeoverType.getFieldValue()).willReturn(FOR_TECHNOLOGY_GROUP.getStringValue());

        // given
        hooks.setFieldsVisibleAndRequired(view, null, null);

        verify(fromTechnology).setVisible(false);
        verify(fromTechnology).setRequired(false);
        verify(fromTechnology).setFieldValue(null);

        verify(toTechnology).setVisible(false);
        verify(toTechnology).setRequired(false);
        verify(toTechnology).setFieldValue(null);

        verify(fromTechnologyGroup).setVisible(true);
        verify(fromTechnologyGroup).setRequired(true);

        verify(toTechnologyGroup).setVisible(true);
        verify(toTechnologyGroup).setRequired(true);
    }

}
