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
package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.lineChangeoverNorms.listeners.MatchingChangeoverNormsDetailsListeners;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class MatchingChangeoverNormsDetailsHooksTest {

    private MatchingChangeoverNormsDetailsHooks hooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private MatchingChangeoverNormsDetailsListeners listeners;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private ComponentState matchingNorm, matchingNormNotFound;

    @Mock
    private DataDefinition changeoverDD;

    @Mock
    private Entity changeover;

    @Before
    public void init() {
        hooks = new MatchingChangeoverNormsDetailsHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(hooks, "listeners", listeners);

        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference("matchingNorm")).willReturn(matchingNorm);
        given(view.getComponentByReference("matchingNormNotFound")).willReturn(matchingNormNotFound);
    }

    @Test
    public void shouldntSetFieldsVisibleWhenNormsNotFound() {
        // given
        given(form.getEntityId()).willReturn(null);

        // when
        hooks.setFieldsVisible(view);

        // then
        verify(matchingNorm).setVisible(false);
        verify(matchingNormNotFound).setVisible(true);
    }

    @Test
    public void shouldSetFieldsVisibleWhenNormsFound() {
        // given
        given(form.getEntityId()).willReturn(1L);

        // when
        hooks.setFieldsVisible(view);

        // then
        verify(matchingNorm).setVisible(true);
        verify(matchingNormNotFound).setVisible(false);
    }

    @Test
    public void shouldFillOrCleanFieldsNormsNotFound() {
        // given
        given(form.getEntityId()).willReturn(null);

        // when
        hooks.fillOrCleanFields(view);

        // then
        verify(listeners).clearField(view);
        verify(listeners).changeStateEditButton(view, false);
    }

    @Test
    public void shouldFillOrCleanFieldsWhenNormsFound() {
        // given
        given(form.getEntityId()).willReturn(1L);
        given(
                dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                        LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)).willReturn(changeoverDD);
        given(changeoverDD.get(1L)).willReturn(changeover);

        // when
        hooks.fillOrCleanFields(view);

        // then
        verify(listeners).fillField(view, changeover);
        verify(listeners).changeStateEditButton(view, true);
    }

}
