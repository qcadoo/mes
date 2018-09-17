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
package com.qcadoo.mes.technologiesGenerator.hooks;

import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.view.GeneratorView;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBooleanField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class GeneratorViewHooksTest {

    private GeneratorViewHooks generatorViewHooks;

    @Mock
    private GeneratorView generatorView;

    @Mock
    private ViewDefinitionState view;

    private Entity contextEntity;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        generatorViewHooks = new GeneratorViewHooks();

        contextEntity = mockEntity((Long) null);
        given(generatorView.getFormEntity()).willReturn(contextEntity);
    }

    @Test
    @Ignore

    public final void shouldDisableGenerationForAlreadyGeneratedStructure() {
        // given
        stubBooleanField(contextEntity, GeneratorContextFields.GENERATED, true);

        // when
        generatorViewHooks.showRibbonButtons(generatorView, view);

        // then
        verify(generatorView).setGenerationEnabled(false);
        verify(generatorView, never()).setGenerationEnabled(true);
    }

    @Test
    @Ignore
    public final void shouldNotDisableGenerationForNotYetGeneratedStructure() {
        // given
        stubBooleanField(contextEntity, GeneratorContextFields.GENERATED, false);

        // when
        generatorViewHooks.showRibbonButtons(generatorView, view);

        // then
        verify(generatorView).setGenerationEnabled(true);
        verify(generatorView, never()).setGenerationEnabled(false);
    }

}
