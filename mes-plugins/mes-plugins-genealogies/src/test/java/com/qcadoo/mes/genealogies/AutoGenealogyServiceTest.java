/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GenealogyService.class, TransactionAspectSupport.class })
public class AutoGenealogyServiceTest {

    private AutoGenealogyService autoGenealogyService;

    private DataDefinitionService dataDefinitionService;

    private TranslationService translationService;

    private GenealogyService genealogyService;

    private SecurityService securityService;

    private PluginAccessor pluginAccessor;

    private Entity entity;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        translationService = mock(TranslationService.class);
        genealogyService = mock(GenealogyService.class);
        securityService = mock(SecurityService.class);
        pluginAccessor = mock(PluginAccessor.class);
        entity = mock(Entity.class);
        autoGenealogyService = new AutoGenealogyService();

        setField(autoGenealogyService, "dataDefinitionService", dataDefinitionService);
        setField(autoGenealogyService, "translationService", translationService);
        setField(autoGenealogyService, "genealogyService", genealogyService);
        setField(autoGenealogyService, "securityService", securityService);
        setField(autoGenealogyService, "pluginAccessor", pluginAccessor);
        given(pluginAccessor.getEnabledPlugin("genealogiesForComponents")).willReturn(mock(Plugin.class));
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfNoRowIsSelected() throws Exception {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);
        given(translationService.translate("qcadooView.grid.noRowSelectedError", Locale.ENGLISH)).willReturn(
                "qcadooView.grid.noRowSelectedError.pl");

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state).addMessage("qcadooView.grid.noRowSelectedError.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfFormHasNoIdentifier() throws Exception {
        // given
        FormComponent state = mock(FormComponent.class);
        given(state.getFieldValue()).willReturn(null);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);
        given(translationService.translate("qcadooView.form.entityWithoutIdentifier", Locale.ENGLISH)).willReturn(
                "qcadooView.form.entityWithoutIdentifier.pl");

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state).addMessage("qcadooView.form.entityWithoutIdentifier.pl", MessageType.FAILURE);
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

        given(translationService.translate("qcadooView.message.entityNotFound", Locale.ENGLISH)).willReturn(
                "qcadooView.message.entityNotFound.pl");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

        // then
        verify(state, times(2)).getFieldValue();
        verify(state).addMessage("qcadooView.message.entityNotFound.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldFailAutoCreateGenealogyIfProductIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(null);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        given(translationService.translate("genealogies.message.autoGenealogy.failure.product", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.failure.product");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

    }

    @Test
    public void shouldFailAutoCreateGenealogyIfTechnologyIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(null);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        given(translationService.translate("genealogies.message.autoGenealogy.failure.product", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.failure.product");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

    }

    @Test
    public void shouldFailAutoCreateGenealogyIfMainBatchIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(product.getField("number")).willReturn("test");
        given(product.getField("name")).willReturn("test");

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        given(translationService.translate("genealogies.message.autoGenealogy.missingMainBatch", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.missingMainBatch");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

    }

    @Test
    public void shouldFailAutoCreateGenealogyIfExistingGenealogyWithBatch() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(product.getField("batch")).willReturn("test");

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        List<Entity> list = new ArrayList<Entity>();
        list.add(mock(Entity.class));
        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY).find()
                        .isEq("batch", "test").belongsTo("order", order.getId()).setMaxResults(1).list().getEntities())
                .willReturn(list);

        given(translationService.translate("genealogies.message.autoGenealogy.genealogyExist", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.genealogyExist");
        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "false" });

    }

    @Test
    public void shouldFailAutoCreateGenealogyWithLastUsedBatchOtherError() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        Entity order = mock(Entity.class);
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);
        Entity product = mock(Entity.class);
        Entity technology = mock(Entity.class);
        given(order.getBelongsToField("product")).willReturn(product);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(product.getField("lastUsedBatch")).willReturn("test");
        given(technology.getField("shiftFeatureRequired")).willReturn(false);
        given(technology.getField("postFeatureRequired")).willReturn(false);
        given(technology.getField("otherFeatureRequired")).willReturn(false);

        given(translationService.translate("genealogies.message.autoGenealogy.failure", Locale.ENGLISH)).willReturn(
                "genealogies.message.autoGenealogy.failure");

        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY).create()
                        .isValid()).willReturn(true);
        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY)
                        .save(any(Entity.class)).getGlobalErrors().isEmpty()).willReturn(true);

        mockStatic(TransactionAspectSupport.class);

        TransactionStatus transactionStatus = mock(TransactionStatus.class);

        given(TransactionAspectSupport.currentTransactionStatus()).willReturn(transactionStatus);

        // when
        autoGenealogyService.autocompleteGenealogy(viewDefinitionState, state, new String[] { "true" });

    }

    @Test
    public void shouldFailAutoCreateGenealogyOnChangeOrderStatusIfNoRowIsSelected() throws Exception {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(null);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);
        given(translationService.translate("qcadooView.grid.noRowSelectedError", Locale.ENGLISH)).willReturn(
                "qcadooView.grid.noRowSelectedError");
        Entity parameter = mock(Entity.class);
        SearchResult searchResult = mock(SearchResult.class);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).list()).willReturn(searchResult);
        List<Entity> list = new ArrayList<Entity>();
        list.add(parameter);
        when(searchResult.getEntities()).thenReturn(list);
        when(parameter.getField("batchForDoneOrder")).thenReturn(false, true);
        // when
        autoGenealogyService.onCompleted(entity);

    }

    @Test
    public void shouldFailAutoCreateGenealogyOnChangeOrderStatusIfFormHasNoIdentifier() throws Exception {
        // given
        FormComponent state = mock(FormComponent.class);
        given(state.getFieldValue()).willReturn(null);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);
        given(translationService.translate("qcadooView.form.entityWithoutIdentifier", Locale.ENGLISH)).willReturn(
                "qcadooView.form.entityWithoutIdentifier");

        Entity parameter = mock(Entity.class);
        SearchResult searchResult = mock(SearchResult.class);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).list()).willReturn(searchResult);
        List<Entity> list = new ArrayList<Entity>();
        list.add(parameter);
        when(searchResult.getEntities()).thenReturn(list);
        when(parameter.getField("batchForDoneOrder")).thenReturn(false, true);
        // when
        autoGenealogyService.onCompleted(entity);

        // then
    }

    @Test
    public void shouldFailAutoCreateGenealogyOnChangeOrderStatusIfOrderIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        given(viewDefinitionState.getLocale()).willReturn(Locale.ENGLISH);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L))
                .willReturn(null);

        given(translationService.translate("qcadooView.message.entityNotFound", Locale.ENGLISH)).willReturn(
                "qcadooView.message.entityNotFound.pl");
        Entity parameter = mock(Entity.class);
        SearchResult searchResult = mock(SearchResult.class);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).list()).willReturn(searchResult);
        List<Entity> list = new ArrayList<Entity>();
        list.add(parameter);
        when(searchResult.getEntities()).thenReturn(list);
        when(parameter.getField("batchForDoneOrder")).thenReturn(false, true);
        // when
        autoGenealogyService.onCompleted(entity);

    }

    @Test
    public void shouldFailAutoCreateGenealogyOnChangeOrderStatusIfHasNotGoodStatus() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);
        Entity parameter = mock(Entity.class);
        SearchResult searchResult = mock(SearchResult.class);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).list()).willReturn(searchResult);
        List<Entity> list = new ArrayList<Entity>();
        list.add(parameter);
        // when
        when(searchResult.getEntities()).thenReturn(list);
        when(parameter.getField("batchForDoneOrder")).thenReturn(false, true);

        autoGenealogyService.onCompleted(entity);

    }

    @Test
    public void shouldFailAutoCreateGenealogyOnChangeOrderStatusIfParameterIsNull() {
        // given
        ComponentState state = mock(ComponentState.class);
        given(state.getFieldValue()).willReturn(13L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        Entity order = mock(Entity.class);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);

        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        Entity parameter = mock(Entity.class);
        SearchResult searchResult = mock(SearchResult.class);
        given(
                dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find()
                        .setMaxResults(1).list()).willReturn(searchResult);
        List<Entity> list = new ArrayList<Entity>();
        list.add(parameter);
        // when
        when(searchResult.getEntities()).thenReturn(list);
        when(parameter.getField("batchForDoneOrder")).thenReturn(false, true);
        autoGenealogyService.onCompleted(entity);

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
