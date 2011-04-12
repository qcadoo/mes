/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.Restriction;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.components.FieldComponentState;
import com.qcadoo.view.components.awesomeDynamicList.AwesomeDynamicListState;
import com.qcadoo.view.components.form.FormComponentState;

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
        verify(viewDefinitionState).redirectTo("../page/genealogies/orderGenealogies.html?context={\"order.id\":\"13\"}", false,
                true);
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
        verify(viewDefinitionState).redirectTo("../page/genealogies/orderGenealogy.html?context={\"form.order\":\"13\"}", false,
                true);
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
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(null);

        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);

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
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity technology = mock(Entity.class);
        given(technology.getField("shiftFeatureRequired")).willReturn(false);
        given(technology.getField("postFeatureRequired")).willReturn(false);
        given(technology.getField("otherFeatureRequired")).willReturn(false);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);

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
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity technology = mock(Entity.class);
        given(technology.getField("shiftFeatureRequired")).willReturn(true);
        given(technology.getField("postFeatureRequired")).willReturn(false);
        given(technology.getField("otherFeatureRequired")).willReturn(false);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);

        ComponentState features = mock(ComponentState.class);
        FieldComponentState shiftFeature = mock(FieldComponentState.class);
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
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity technology = mock(Entity.class);
        given(technology.getField("shiftFeatureRequired")).willReturn(true);
        given(technology.getField("postFeatureRequired")).willReturn(true);
        given(technology.getField("otherFeatureRequired")).willReturn(true);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);

        ComponentState features = mock(ComponentState.class);
        FieldComponentState shiftFeature = mock(FieldComponentState.class);
        FieldComponentState postFeature = mock(FieldComponentState.class);
        FieldComponentState otherFeature = mock(FieldComponentState.class);

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

    @Test
    public void shouldNoFillProductInComponentsIfFormIsNotValid() throws Exception {
        // given
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
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

        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(true);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(null);

        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);

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

        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(true);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        DataDefinition dataDefinition = mock(DataDefinition.class);

        EntityTree operationComponents = new EntityTreeImpl(dataDefinition, "joinField", null);

        Entity technology = mock(Entity.class);
        given(technology.getTreeField("operationComponents")).willReturn(operationComponents);

        Entity order = mock(Entity.class);
        given(order.getBelongsToField("technology")).willReturn(technology);

        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);

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
    public void shouldShowProductInComponents() throws Exception {
        // given
        ComponentState products = mock(ComponentState.class);

        AwesomeDynamicListState productsList = mock(AwesomeDynamicListState.class);

        List<Entity> expectedGenealogyProductInComponents = prepareExpectedGenealogyProductInComponents();
        EntityList existingGenealogyProductInComponents = prepareExistingGenealogyProductInComponents();
        EntityTree operationProductInComponents = prepareOperationProductInComponents();

        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(true);
        given(form.getEntityId()).willReturn(11L);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity genealogyProductInComponent = new DefaultEntity(null);
        given(dataDefinitionService.get("genealogiesForComponents", "genealogyProductInComponent").create()).willReturn(
                genealogyProductInComponent);

        Entity technology = mock(Entity.class);
        given(technology.getTreeField("operationComponents")).willReturn(operationProductInComponents);

        Entity order = mock(Entity.class);
        given(dataDefinitionService.get("orders", "order").get(13L)).willReturn(order);
        given(order.getBelongsToField("technology")).willReturn(technology);

        Entity genealogy = mock(Entity.class);
        given(dataDefinitionService.get("genealogies", "genealogy").get(11L)).willReturn(genealogy);
        given(genealogy.getHasManyField("productInComponents")).willReturn(existingGenealogyProductInComponents);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("form")).willReturn(form);
        given(state.getComponentByReference("productGridLayout")).willReturn(products);
        given(state.getComponentByReference("productInComponentsList")).willReturn(productsList);

        // when
        genealogyService.fillProductInComponents(state);

        // then
        verify(products, never()).setVisible(false);
        verify(productsList).setFieldValue(expectedGenealogyProductInComponents);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @SuppressWarnings("unchecked")
    private EntityTree prepareOperationProductInComponents() {
        List<Entity> entities = new ArrayList<Entity>();
        List<Entity> subEntities = new ArrayList<Entity>();
        List<Entity> productsEntities1 = new ArrayList<Entity>();
        List<Entity> productsEntities3 = new ArrayList<Entity>();

        productsEntities1.add(craeteOperationProductInComponent(101L, true));
        productsEntities3.add(craeteOperationProductInComponent(103L, true));
        productsEntities3.add(craeteOperationProductInComponent(104L, false));

        DataDefinition treeDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(treeDataDefinition.find().addRestriction(any(Restriction.class)).setOrderAscBy(eq("priority")).list().getEntities())
                .willReturn(entities, subEntities);

        DataDefinition listDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(listDataDefinition.find().addRestriction(any(Restriction.class)).list().getEntities()).willReturn(
                productsEntities1, productsEntities3);

        EntityTree subOperationComponents = new EntityTreeImpl(treeDataDefinition, "joinField", 13L);

        EntityList operationProductInComponents1 = new EntityListImpl(listDataDefinition, "joinField", 1L);
        EntityList operationProductInComponents3 = new EntityListImpl(listDataDefinition, "joinField", 3L);

        Entity operationComponent1 = mock(Entity.class);
        given(operationComponent1.getId()).willReturn(1L);
        given(operationComponent1.getField("entityType")).willReturn("operation");
        given(operationComponent1.getHasManyField("operationProductInComponents")).willReturn(operationProductInComponents1);
        given(operationComponent1.getBelongsToField("parent")).willReturn(null);

        Entity referenceTechnology = mock(Entity.class);
        given(referenceTechnology.getTreeField("operationComponents")).willReturn(subOperationComponents);

        Entity operationComponent2 = mock(Entity.class);
        given(operationComponent2.getId()).willReturn(2L);
        given(operationComponent2.getField("entityType")).willReturn("referenceTechnology");
        given(operationComponent2.getBelongsToField("referenceTechnology")).willReturn(referenceTechnology);
        given(operationComponent2.getBelongsToField("parent")).willReturn(operationComponent1);

        Entity operationComponent3 = mock(Entity.class);
        given(operationComponent3.getId()).willReturn(3L);
        given(operationComponent3.getField("entityType")).willReturn("operation");
        given(operationComponent3.getHasManyField("operationProductInComponents")).willReturn(operationProductInComponents3);
        given(operationComponent3.getBelongsToField("parent")).willReturn(null);

        entities.add(operationComponent1);
        entities.add(operationComponent2);
        subEntities.add(operationComponent3);

        EntityTree operationComponents = new EntityTreeImpl(treeDataDefinition, "joinField", 13L);
        return operationComponents;
    }

    private EntityList prepareExistingGenealogyProductInComponents() {
        List<Entity> existingEntities = new ArrayList<Entity>();
        existingEntities.add(craeteGenealogyProductInComponent(101L, craeteOperationProductInComponent(101L, true)));
        existingEntities.add(craeteGenealogyProductInComponent(102L, craeteOperationProductInComponent(102L, true)));

        DataDefinition existingListDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(existingListDataDefinition.find().addRestriction(any(Restriction.class)).list().getEntities()).willReturn(
                existingEntities);

        EntityList existingOperationComponents = new EntityListImpl(existingListDataDefinition, "joinField", 11L);
        return existingOperationComponents;
    }

    private List<Entity> prepareExpectedGenealogyProductInComponents() {
        List<Entity> expectedEntities = new ArrayList<Entity>();
        expectedEntities.add(craeteGenealogyProductInComponent(101L, craeteOperationProductInComponent(101L, true)));
        expectedEntities.add(craeteGenealogyProductInComponent(null, craeteOperationProductInComponent(103L, true)));
        return expectedEntities;
    }

    private Entity craeteGenealogyProductInComponent(final Long id, final Entity operationProductInComponent) {
        Entity genealogyProductInComponent = new DefaultEntity(null, id);
        genealogyProductInComponent.setField("productInComponent", operationProductInComponent);
        return genealogyProductInComponent;
    }

    private Entity craeteOperationProductInComponent(final Long id, final boolean batchRequired) {
        Entity operationProductInComponent = new DefaultEntity(null, id);
        operationProductInComponent.setField("batchRequired", batchRequired);
        return operationProductInComponent;
    }
}
