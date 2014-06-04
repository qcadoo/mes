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
package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.GROUPS;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.SUPPORTS_ALL_TECHNOLOGIES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.TECHNOLOGIES;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class ProductionLineDetailsViewHooksTest {

    private static final String L_FORM = "form";

    private ProductionLineDetailsViewHooks productionLinesViewHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productionLineForm;

    @Mock
    private ComponentState supportsAllTechnologies, groupsGrid, technologiesGrid;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionLinesViewHooks = new ProductionLineDetailsViewHooks();

        given(view.getComponentByReference(L_FORM)).willReturn(productionLineForm);
        given(view.getComponentByReference(SUPPORTS_ALL_TECHNOLOGIES)).willReturn(supportsAllTechnologies);
        given(view.getComponentByReference(TECHNOLOGIES)).willReturn(technologiesGrid);
        given(view.getComponentByReference(GROUPS)).willReturn(groupsGrid);
    }

    @Test
    public void shouldDisableBothGridsIfTheCheckboxSupportAllTechnoogiesIsSet() {
        // given
        given(productionLineForm.getEntityId()).willReturn(null);
        given(supportsAllTechnologies.getFieldValue()).willReturn("1");

        // when
        productionLinesViewHooks.disableSupportedTechnologiesGrids(view, null, null);

        // then
        verify(technologiesGrid).setEnabled(false);
        verify(groupsGrid).setEnabled(false);
    }

    @Test
    public void shouldEnableBothGridsIfTheCheckboxSupportAllTechnoogiesIsntSet() {
        // given
        given(productionLineForm.getEntityId()).willReturn(1L);
        given(supportsAllTechnologies.getFieldValue()).willReturn("0");

        // when
        productionLinesViewHooks.disableSupportedTechnologiesGrids(view, null, null);

        // then
        verify(technologiesGrid).setEnabled(true);
        verify(groupsGrid).setEnabled(true);
    }
}
