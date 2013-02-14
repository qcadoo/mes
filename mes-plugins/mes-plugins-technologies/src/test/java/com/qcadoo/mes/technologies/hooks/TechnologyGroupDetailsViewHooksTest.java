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
package com.qcadoo.mes.technologies.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class TechnologyGroupDetailsViewHooksTest {

    private static final long L_ID = 1L;

    private TechnologyGroupDetailsViewHooks technologyGroupDetailsViewHooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent technologyGroupForm, productForm;

    @Mock
    private Entity technologyGroup, product;

    @Mock
    private DataDefinition productDD;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyGroupDetailsViewHooks = new TechnologyGroupDetailsViewHooks();

        ReflectionTestUtils.setField(technologyGroupDetailsViewHooks, "dataDefinitionService", dataDefinitionService);

        given(view.getComponentByReference("form")).willReturn(technologyGroupForm);
        given(technologyGroupForm.getEntity()).willReturn(technologyGroup);

        given(view.getComponentByReference("product")).willReturn(productForm);
        given(productForm.getEntity()).willReturn(product);

        given(product.getDataDefinition()).willReturn(productDD);
    }

    @Test
    public void shouldntAddTechnologyGroupToProductIfTechnologyGroupIsntSaved() {
        // given
        given(technologyGroup.getId()).willReturn(null);

        // when
        technologyGroupDetailsViewHooks.addTechnologyGroupToProduct(view, null, null);

        // then
        verify(product, never()).setField("technologyGroup", technologyGroup);
        verify(productDD, never()).save(product);
    }

    @Test
    public void shouldntAddTechnologyGroupToProductIfTechnologyGroupIsSavedAndProductIsNull() {
        // given
        given(technologyGroup.getId()).willReturn(L_ID);

        given(product.getId()).willReturn(null);
        // when
        technologyGroupDetailsViewHooks.addTechnologyGroupToProduct(view, null, null);

        // then
        verify(product, never()).setField("technologyGroup", technologyGroup);
        verify(productDD, never()).save(product);
    }

    @Test
    public void shouldAddTechnologyGroupToProductIfTechnologyGroupIsSavedAndProductIsntNull() {
        // given
        given(technologyGroup.getId()).willReturn(L_ID);

        given(product.getId()).willReturn(L_ID);

        given(product.getDataDefinition()).willReturn(productDD);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).willReturn(productDD);
        given(productDD.get(L_ID)).willReturn(product);

        // when
        technologyGroupDetailsViewHooks.addTechnologyGroupToProduct(view, null, null);

        // then
        verify(product).setField("technologyGroup", technologyGroup);
        verify(productDD).save(product);
    }
}
