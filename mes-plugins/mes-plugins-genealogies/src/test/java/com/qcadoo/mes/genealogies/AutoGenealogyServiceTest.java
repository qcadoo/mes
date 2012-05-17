/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GenealogyService.class, TransactionAspectSupport.class, SearchRestrictions.class })
public class AutoGenealogyServiceTest {

    private AutoGenealogyService autoGenealogyService;

    private DataDefinitionService dataDefinitionService;

    private SecurityService securityService;

    private PluginManager pluginManager;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        securityService = mock(SecurityService.class);
        pluginManager = mock(PluginManager.class);

        autoGenealogyService = new AutoGenealogyService();

        setField(autoGenealogyService, "dataDefinitionService", dataDefinitionService);
        setField(autoGenealogyService, "securityService", securityService);
        setField(autoGenealogyService, "pluginManager", pluginManager);
        given(pluginManager.isPluginEnabled("genealogiesForComponents")).willReturn(true);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfNoRowIsSelected() throws Exception {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state).addMessage("qcadooView.grid.noRowSelectedError", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfFormHasNoIdentifier() throws Exception {
        // given
        FormComponent state = mock(FormComponent.class);
        given(state.getFieldValue()).willReturn(null);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state).addMessage("qcadooView.form.entityWithoutIdentifier", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfOrderIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L))
                .willReturn(null);

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
    }

    @Test
    public void shouldFillLastUsedShiftFeature() {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);

        Entity entity = new DefaultEntity(dataDefinition);

        Entity feature = mock(Entity.class);
        List<Entity> featureEntities = new ArrayList<Entity>();
        featureEntities.add(feature);

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(featureEntities);
        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedShiftFeature(dataDefinition, entity);
        // then
        verify(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities().get(0)).setField(anyString(), anyString());
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE))
                .save(any(Entity.class));
        assertNotNull(entity.getField("date"));
        assertNotNull(entity.getField("worker"));
    }

    @Test
    public void shouldFailFillLastUsedShiftFeature() {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);

        Entity entity = new DefaultEntity(dataDefinition);

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedShiftFeature(dataDefinition, entity);
        // then
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE),
                never()).save(any(Entity.class));
    }

    @Test
    public void shouldFillLastUsedOtherFeature() {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);

        Entity entity = new DefaultEntity(dataDefinition);
        Entity feature = mock(Entity.class);
        List<Entity> featureEntities = new ArrayList<Entity>();
        featureEntities.add(feature);

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(featureEntities);
        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedOtherFeature(dataDefinition, entity);
        // then
        verify(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities().get(0)).setField(anyString(), anyString());
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE))
                .save(any(Entity.class));
        assertNotNull(entity.getField("date"));
        assertNotNull(entity.getField("worker"));
    }

    @Test
    public void shouldFailFillLastUsedOtherFeature() {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);

        Entity entity = new DefaultEntity(dataDefinition);

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedOtherFeature(dataDefinition, entity);
        // then
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE),
                never()).save(any(Entity.class));
    }

    @Test
    public void shouldFillLastUsedPostFeature() {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);

        Entity entity = new DefaultEntity(dataDefinition);
        Entity feature = mock(Entity.class);
        List<Entity> featureEntities = new ArrayList<Entity>();
        featureEntities.add(feature);

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(featureEntities);
        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedPostFeature(dataDefinition, entity);
        // then
        verify(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities().get(0)).setField(anyString(), anyString());
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE))
                .save(any(Entity.class));
        assertNotNull(entity.getField("date"));
        assertNotNull(entity.getField("worker"));
    }

    @Test
    public void shouldFailFillLastUsedPostFeature() {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);

        Entity entity = new DefaultEntity(dataDefinition);

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedPostFeature(dataDefinition, entity);
        // then
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE),
                never()).save(any(Entity.class));
    }

    @Test
    public void shouldFillLastUsedBatchForProduct() {
        // given
        Entity entity = mock(Entity.class, RETURNS_DEEP_STUBS);
        given(entity.getField("date")).willReturn(null);
        given(entity.getField("worker")).willReturn(null);

        DataDefinition dataDefinition = mock(DataDefinition.class);

        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedBatchForProduct(dataDefinition, entity);

        // then
        verify(entity, times(2)).setField(anyString(), any());
        verify(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(anyLong()))
                .setField(anyString(), anyString());
        verify(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).save(any(Entity.class));
    }

    @Test
    public void shouldFillLastUsedBatchForGenealogyWithoutFillUserAndDate() {
        // given
        Entity entity = mock(Entity.class, RETURNS_DEEP_STUBS);

        DataDefinition dataDefinition = mock(DataDefinition.class);

        given(securityService.getCurrentUserName()).willReturn("newTest");

        // when
        autoGenealogyService.fillLastUsedBatchForGenealogy(dataDefinition, entity);

        // then
        verify(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(anyLong()))
                .setField(anyString(), anyString());
        verify(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).save(any(Entity.class));
    }

}
