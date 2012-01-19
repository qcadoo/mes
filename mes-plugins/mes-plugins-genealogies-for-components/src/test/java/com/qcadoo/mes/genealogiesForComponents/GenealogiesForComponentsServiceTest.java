/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.genealogiesForComponents;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.internal.EntityTreeImpl;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class GenealogiesForComponentsServiceTest {

    private GenealogiesForComponentsService genealogyService;

    private TechnologyService technologyService;

    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        genealogyService = new GenealogiesForComponentsService();
        technologyService = mock(TechnologyService.class);
        setField(genealogyService, "dataDefinitionService", dataDefinitionService);
        setField(genealogyService, "technologyService", technologyService);
    }

    @Test
    public void shouldNoFillProductInComponentsIfFormIsNotValid() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(false);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        genealogyService.fillProductInComponents(state);

        // then
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldHideProductInComponentsIfThereIsNoTechnology() throws Exception {
        // given
        ComponentState products = mock(ComponentState.class);

        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(true);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(null);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("form")).willReturn(form);
        given(state.getComponentByReference("productGridLayout")).willReturn(products);

        // when
        genealogyService.fillProductInComponents(state);

        // then
        verify(products).setVisible(false);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldHideProductInComponentsIfThereIsNoProductsForGenealogy() throws Exception {
        // given
        ComponentState products = mock(ComponentState.class);

        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(true);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        DataDefinition dataDefinition = mock(DataDefinition.class);

        EntityTree operationComponents = new EntityTreeImpl(dataDefinition, "joinField", null);

        Entity technology = mock(Entity.class);
        given(technology.getTreeField("operationComponents")).willReturn(operationComponents);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("form")).willReturn(form);
        given(state.getComponentByReference("productGridLayout")).willReturn(products);

        // when
        genealogyService.fillProductInComponents(state);

        // then
        verify(products).setVisible(false);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

}
