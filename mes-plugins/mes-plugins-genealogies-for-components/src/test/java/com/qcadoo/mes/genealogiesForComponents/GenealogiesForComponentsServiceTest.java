/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.1
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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.genealogies.GenealogyService;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class GenealogiesForComponentsServiceTest {

    private GenealogiesForComponentsService genealogyService;

    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        genealogyService = new GenealogiesForComponentsService();
        setField(genealogyService, "dataDefinitionService", dataDefinitionService);
        setField(genealogyService, "genealogyService", new GenealogyService());
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

    @Test
    @Ignore
    // TODO masz fix tests
    public void shouldShowProductInComponents() throws Exception {
        // given
        ComponentState products = mock(ComponentState.class);

        FieldComponent productsList = mock(FieldComponent.class);

        EntityList existingGenealogyProductInComponents = prepareExistingGenealogyProductInComponents();
        EntityTree operationProductInComponents = prepareOperationProductInComponents();

        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        given(form.isValid()).willReturn(true);
        given(form.getEntityId()).willReturn(11L);
        given(form.getEntity().getField("order").toString()).willReturn("13");

        Entity genealogyProductInComponent = new DefaultEntity(null);
        given(dataDefinitionService.get("genealogiesForComponents", "genealogyProductInComponent").create()).willReturn(
                genealogyProductInComponent);

        Entity technology = mock(Entity.class);
        given(technology.getTreeField("operationComponents")).willReturn(operationProductInComponents);

        Entity order = mock(Entity.class);
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(13L)).willReturn(
                order);
        given(order.getBelongsToField("technology")).willReturn(technology);

        Entity genealogy = mock(Entity.class);
        given(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY).get(11L))
                .willReturn(genealogy);
        given(genealogy.getHasManyField("productInComponents")).willReturn(existingGenealogyProductInComponents);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("form")).willReturn(form);
        given(state.getComponentByReference("productGridLayout")).willReturn(products);
        given(state.getComponentByReference("productInComponentsList")).willReturn(productsList);

        // when
        genealogyService.fillProductInComponents(state);

        // then
        verify(products, never()).setVisible(false);
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

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getName()).willReturn("joinField");

        DataDefinition treeDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(treeDataDefinition.find().belongsTo("joinField", 13L).orderAscBy(eq("priority")).list().getEntities()).willReturn(
                entities, subEntities);
        given(treeDataDefinition.getField("joinField")).willReturn(fieldDefinition);

        DataDefinition listDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(listDataDefinition.find().belongsTo("joinField", 1L).list().getEntities()).willReturn(productsEntities1);
        given(listDataDefinition.find().belongsTo("joinField", 3L).list().getEntities()).willReturn(productsEntities3);
        given(listDataDefinition.getField("joinField")).willReturn(fieldDefinition);

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
        given(existingListDataDefinition.find().list().getEntities()).willReturn(existingEntities);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getName()).willReturn("joinField");
        given(existingListDataDefinition.getField("joinField")).willReturn(fieldDefinition);

        EntityList existingOperationComponents = new EntityListImpl(existingListDataDefinition, "joinField", 11L);
        return existingOperationComponents;
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
