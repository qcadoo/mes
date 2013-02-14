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
package com.qcadoo.mes.basic.listeners;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductsFamiliesListenersTest {

    private ProductsFamiliesListeners productsFamiliesListeners;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState state;

    @Mock
    private WindowComponentState windowComponent;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup ribbonGroup;

    @Mock
    private RibbonActionItem actionItem;

    @Before
    public void init() {
        productsFamiliesListeners = new ProductsFamiliesListeners();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldEnabledButtonWhenProductOnTreeIsSelected() throws Exception {
        // given
        when(view.getComponentByReference("window")).thenReturn((ComponentState) windowComponent);
        when(windowComponent.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("edit")).thenReturn(ribbonGroup);
        when(ribbonGroup.getItemByName("editSelectedProduct")).thenReturn(actionItem);
        // when
        productsFamiliesListeners.enabledEditButton(view, state, new String[0]);
        // then
        Mockito.verify(actionItem).setEnabled(true);
    }
}
