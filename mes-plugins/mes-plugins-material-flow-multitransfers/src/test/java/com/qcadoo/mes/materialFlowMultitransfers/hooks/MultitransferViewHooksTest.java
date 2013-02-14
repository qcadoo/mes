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
package com.qcadoo.mes.materialFlowMultitransfers.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class MultitransferViewHooksTest {

    private MultitransferViewHooks multitransferViewHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent time, type;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        multitransferViewHooks = new MultitransferViewHooks();
    }

    @Test
    public void shouldMakeTimeAndTypeFieldsRequired() {
        // given
        given(view.getComponentByReference(TIME)).willReturn(time);
        given(view.getComponentByReference(TYPE)).willReturn(type);

        // when
        multitransferViewHooks.makeFieldsRequired(view);

        // then
        verify(time).setRequired(true);
        verify(type).setRequired(true);
    }
}
