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
package com.qcadoo.mes.technologies.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductDetailsViewHooksTTest {

    private static final long L_ID = 1L;

    private ProductDetailsViewHooksT productDetailsViewHooksT;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productForm;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup technologies;

    @Mock
    private RibbonActionItem addTechnologyGroup, showTechnologiesWithTechnologyGroup, showTechnologiesWithProduct;

    @Mock
    private Entity product, technologyGroup;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productDetailsViewHooksT = new ProductDetailsViewHooksT();

        given(view.getComponentByReference("form")).willReturn(productForm);
        given(productForm.getEntity()).willReturn(product);
    }

    // TODO lupo fix when problem with navigation will be done
    @Test
    public void shouldntUpdateRibbonStateIfProductIsntSaved() {
        // given
        given(product.getId()).willReturn(null);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("technologies")).willReturn(technologies);

        // given(technologies.getItemByName("addTechnologyGroup")).willReturn(addTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithTechnologyGroup")).willReturn(showTechnologiesWithTechnologyGroup);
        // given(technologies.getItemByName("showTechnologiesWithProduct")).willReturn(showTechnologiesWithProduct);

        // when
        productDetailsViewHooksT.updateRibbonState(view);

        // then
        // verify(addTechnologyGroup).setEnabled(false);
        verify(showTechnologiesWithTechnologyGroup).setEnabled(false);
        // verify(showTechnologiesWithProduct).setEnabled(false);
    }

    @Test
    public void shouldUpdateRibbonStateIfProductIsSavedAndTechnologyGroupIsNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(null);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("technologies")).willReturn(technologies);

        // given(technologies.getItemByName("addTechnologyGroup")).willReturn(addTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithTechnologyGroup")).willReturn(showTechnologiesWithTechnologyGroup);
        // given(technologies.getItemByName("showTechnologiesWithProduct")).willReturn(showTechnologiesWithProduct);

        // when
        productDetailsViewHooksT.updateRibbonState(view);

        // then
        // verify(addTechnologyGroup).setEnabled(true);
        verify(showTechnologiesWithTechnologyGroup).setEnabled(false);
        // verify(showTechnologiesWithProduct).setEnabled(true);
    }

    @Test
    public void shouldUpdateRibbonStateIfProductIsSavedAndTechnologyGroupIsntNull() {
        // given
        given(product.getId()).willReturn(L_ID);

        given(product.getBelongsToField("technologyGroup")).willReturn(technologyGroup);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("technologies")).willReturn(technologies);

        // given(technologies.getItemByName("addTechnologyGroup")).willReturn(addTechnologyGroup);
        given(technologies.getItemByName("showTechnologiesWithTechnologyGroup")).willReturn(showTechnologiesWithTechnologyGroup);
        // given(technologies.getItemByName("showTechnologiesWithProduct")).willReturn(showTechnologiesWithProduct);

        // when
        productDetailsViewHooksT.updateRibbonState(view);

        // then
        // verify(addTechnologyGroup).setEnabled(true);
        verify(showTechnologiesWithTechnologyGroup).setEnabled(true);
        // verify(showTechnologiesWithProduct).setEnabled(true);
    }

}
