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
package com.qcadoo.mes.genealogies;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class GenealogyServiceTest {

    private GenealogyService genealogyService;

    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        genealogyService = new GenealogyService();
        setField(genealogyService, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldRedirectToGenealogiesForGivenOrder() throws Exception {
        // given
        ComponentState triggerState = mock(ComponentState.class);
        given(triggerState.getFieldValue()).willReturn(13L);

        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        // when
        genealogyService.showGenealogy(viewDefinitionState, triggerState, new String[0]);

        // then
        verify(viewDefinitionState).redirectTo("../page/genealogies/orderGenealogiesList.html?context={\"order.id\":\"13\"}",
                false, true);
        verify(triggerState).getFieldValue();
        verifyNoMoreInteractions(triggerState);
        verifyNoMoreInteractions(viewDefinitionState);
    }

    @Test
    public void shouldNotRedirectToGenealogiesIfNoOrderIsSelected() throws Exception {
        // given
        ComponentState triggerState = mock(ComponentState.class);
        given(triggerState.getFieldValue()).willReturn(null);

        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        // when
        genealogyService.showGenealogy(viewDefinitionState, triggerState, new String[0]);

        // then
        verify(triggerState).getFieldValue();
        verifyNoMoreInteractions(triggerState);
        verifyNoMoreInteractions(viewDefinitionState);
    }

    @Test
    public void shouldRedirectToNewGenealogyForGivenOrder() throws Exception {
        // given
        ComponentState triggerState = mock(ComponentState.class);
        given(triggerState.getFieldValue()).willReturn(13L);

        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        // when
        genealogyService.newGenealogy(viewDefinitionState, triggerState, new String[0]);

        // then
        verify(viewDefinitionState).redirectTo("../page/genealogies/orderGenealogyDetails.html?context={\"form.order\":\"13\"}",
                false, true);
        verify(triggerState).getFieldValue();
        verifyNoMoreInteractions(triggerState);
        verifyNoMoreInteractions(viewDefinitionState);
    }

    @Test
    public void shouldNotRedirectToNewGenealogyIfNoOrderIsSelected() throws Exception {
        // given
        ComponentState triggerState = mock(ComponentState.class);
        given(triggerState.getFieldValue()).willReturn(null);

        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        // when
        genealogyService.newGenealogy(viewDefinitionState, triggerState, new String[0]);

        // then
        verify(triggerState).getFieldValue();
        verifyNoMoreInteractions(triggerState);
        verifyNoMoreInteractions(viewDefinitionState);
    }

    @Test
    public void shouldHideFeaturesIfThereIsNoTechnology() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(null);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        ComponentState features = mock(ComponentState.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("featuresLayout")).willReturn(features);
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        genealogyService.hideComponents(state);

        // then
        verify(features).setVisible(false);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldHideFeaturesIfTechnologyDoesNotRequireThem() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity technology = mock(Entity.class);
        given(technology.getField("shiftFeatureRequired")).willReturn(false);
        given(technology.getField("postFeatureRequired")).willReturn(false);
        given(technology.getField("otherFeatureRequired")).willReturn(false);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        ComponentState features = mock(ComponentState.class);
        ComponentState shiftFeature = mock(ComponentState.class);
        ComponentState postFeature = mock(ComponentState.class);
        ComponentState otherFeature = mock(ComponentState.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("featuresLayout")).willReturn(features);
        given(state.getComponentByReference("shiftBorderLayout")).willReturn(shiftFeature);
        given(state.getComponentByReference("postBorderLayout")).willReturn(postFeature);
        given(state.getComponentByReference("otherBorderLayout")).willReturn(otherFeature);
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        genealogyService.hideComponents(state);

        // then
        verify(features).setVisible(false);
        verify(shiftFeature).setVisible(false);
        verify(postFeature).setVisible(false);
        verify(otherFeature).setVisible(false);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldNotHideFeaturesIfAnyFeatureIsRequired() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity technology = mock(Entity.class);
        given(technology.getField("shiftFeatureRequired")).willReturn(true);
        given(technology.getField("postFeatureRequired")).willReturn(false);
        given(technology.getField("otherFeatureRequired")).willReturn(false);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        ComponentState features = mock(ComponentState.class);
        FieldComponent shiftFeature = mock(FieldComponent.class);
        ComponentState postFeature = mock(ComponentState.class);
        ComponentState otherFeature = mock(ComponentState.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("featuresLayout")).willReturn(features);
        given(state.getComponentByReference("shiftFeaturesList")).willReturn(shiftFeature);
        given(state.getComponentByReference("postBorderLayout")).willReturn(postFeature);
        given(state.getComponentByReference("otherBorderLayout")).willReturn(otherFeature);
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        genealogyService.hideComponents(state);

        // then
        verify(features, never()).setVisible(false);
        verify(shiftFeature).setRequired(true);
        verify(postFeature).setVisible(false);
        verify(otherFeature).setVisible(false);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldSetRequiredOnFeaturesList() throws Exception {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity technology = mock(Entity.class);
        given(technology.getField("shiftFeatureRequired")).willReturn(true);
        given(technology.getField("postFeatureRequired")).willReturn(true);
        given(technology.getField("otherFeatureRequired")).willReturn(true);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        ComponentState features = mock(ComponentState.class);
        FieldComponent shiftFeature = mock(FieldComponent.class);
        FieldComponent postFeature = mock(FieldComponent.class);
        FieldComponent otherFeature = mock(FieldComponent.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("featuresLayout")).willReturn(features);
        given(state.getComponentByReference("shiftFeaturesList")).willReturn(shiftFeature);
        given(state.getComponentByReference("postFeaturesList")).willReturn(postFeature);
        given(state.getComponentByReference("otherFeaturesList")).willReturn(otherFeature);
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        genealogyService.hideComponents(state);

        // then
        verify(features, never()).setVisible(false);
        verify(shiftFeature).setRequired(true);
        verify(postFeature).setRequired(true);
        verify(otherFeature).setRequired(true);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

}
