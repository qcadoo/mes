/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.advancedGenealogy.tree;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeViewListeners.FormValidationException;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AdvancedGenealogyTreeViewListenersTest {

    private AdvancedGenealogyTreeViewListeners viewListeners;

    private Locale locale;

    private ComponentState batchLookup;

    private ViewDefinitionState view;

    private ComponentState state;

    private DataDefinition dataDefinition;

    private AdvancedGenealogyTreeService advancedGenealogyTreeService;

    private Entity formEntity;

    @Before
    public void init() {
        viewListeners = new AdvancedGenealogyTreeViewListeners();

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        dataDefinition = mock(DataDefinition.class);

        locale = Locale.getDefault();

        when(dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH))
                .thenReturn(dataDefinition);

        advancedGenealogyTreeService = mock(AdvancedGenealogyTreeService.class);

        ReflectionTestUtils.setField(viewListeners, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(viewListeners, "advancedGenealogyTreeService", advancedGenealogyTreeService);

        view = mock(ViewDefinitionState.class);
        state = mock(ComponentState.class);
        FormComponent formComponent = mock(FormComponent.class);
        formEntity = mock(Entity.class);
        batchLookup = mock(ComponentState.class);

        when(view.getComponentByReference(QcadooViewConstants.L_FORM)).thenReturn(formComponent);
        when(formComponent.getEntity()).thenReturn(formEntity);

        when(view.getComponentByReference("batchLookup")).thenReturn(batchLookup);

        when(state.getLocale()).thenReturn(locale);
    }

    @Test(expected = FormValidationException.class)
    public void shouldStopGeneratingTreeAndAddCertainMessageIfNoBatchIsSelected() {
        // given
        when(batchLookup.getFieldValue()).thenReturn(null);

        // when
        viewListeners.generateFormEntity(view, state);

        // then
    }

    @Test(expected = FormValidationException.class)
    public void shouldStopGeneratingTreeAndAddCertainMessageIfNoTreeTypeIsSelected() {
        // given
        Entity batch = mock(Entity.class);
        ComponentState includeDraftComponent = mock(ComponentState.class);
        ComponentState treeTypeComponent = mock(ComponentState.class);

        when(batchLookup.getFieldValue()).thenReturn(1L);
        when(dataDefinition.get(1L)).thenReturn(batch);
        when(view.getComponentByReference("includeDrafts")).thenReturn(includeDraftComponent);
        when(includeDraftComponent.getFieldValue()).thenReturn("0");
        when(view.getComponentByReference("treeType")).thenReturn(treeTypeComponent);
        when(treeTypeComponent.getFieldValue()).thenReturn(null);

        // when
        viewListeners.generateFormEntity(view, state);

        // then
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGenerateProducedFromTree() {
        // given
        Entity batch = mock(Entity.class);
        when(batch.getId()).thenReturn(1L);
        when(batch.getField("parent")).thenReturn(null);
        when(batch.getField("priority")).thenReturn(1);
        when(batch.getField("entityType")).thenReturn("batch");

        ComponentState includeDraftComponent = mock(ComponentState.class);
        ComponentState treeTypeComponent = mock(ComponentState.class);

        when(batchLookup.getFieldValue()).thenReturn(1L);
        when(dataDefinition.get(1L)).thenReturn(batch);
        when(view.getComponentByReference("includeDrafts")).thenReturn(includeDraftComponent);
        String includeDraftsString = "0";
        when(includeDraftComponent.getFieldValue()).thenReturn(includeDraftsString);
        boolean includeDrafts = "1".equals(includeDraftsString);
        when(view.getComponentByReference("treeType")).thenReturn(treeTypeComponent);
        when(treeTypeComponent.getFieldValue()).thenReturn("01producedFrom");

        List<Entity> tree = mock(List.class);
        when(advancedGenealogyTreeService.getProducedFromTree(batch, includeDrafts, true)).thenReturn(tree);

        Iterator<Entity> treeIterator = mock(Iterator.class);
        when(treeIterator.hasNext()).thenReturn(true, false);
        when(treeIterator.next()).thenReturn(batch);
        when(tree.iterator()).thenReturn(treeIterator);

        ComponentState genealogyTree = mock(ComponentState.class);
        when(view.getComponentByReference("genealogyTree")).thenReturn(genealogyTree);

        // when
        viewListeners.generateFormEntity(view, state);

        // then
        verify(advancedGenealogyTreeService).getProducedFromTree(batch, includeDrafts, true);
        verify(formEntity).setField("producedBatch", batch);
        verify(formEntity).setField(eq("genealogyTree"), any(EntityTree.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGenerateUsedToProduceTree() {
        // given
        Entity batch = mock(Entity.class);
        when(batch.getId()).thenReturn(1L);
        when(batch.getField("parent")).thenReturn(null);
        when(batch.getField("priority")).thenReturn(1);
        when(batch.getField("entityType")).thenReturn("batch");

        ComponentState includeDraftComponent = mock(ComponentState.class);
        ComponentState treeTypeComponent = mock(ComponentState.class);

        when(batchLookup.getFieldValue()).thenReturn(1L);
        when(dataDefinition.get(1L)).thenReturn(batch);
        when(view.getComponentByReference("includeDrafts")).thenReturn(includeDraftComponent);
        String includeDraftsString = "0";
        when(includeDraftComponent.getFieldValue()).thenReturn(includeDraftsString);
        boolean includeDrafts = "1".equals(includeDraftsString);
        when(view.getComponentByReference("treeType")).thenReturn(treeTypeComponent);
        when(treeTypeComponent.getFieldValue()).thenReturn("02usedToProduce");

        List<Entity> tree = mock(List.class);
        when(advancedGenealogyTreeService.getUsedToProduceTree(batch, includeDrafts, true)).thenReturn(tree);

        Iterator<Entity> treeIterator = mock(Iterator.class);
        when(treeIterator.hasNext()).thenReturn(true, false);
        when(treeIterator.next()).thenReturn(batch);
        when(tree.iterator()).thenReturn(treeIterator);

        ComponentState genealogyTree = mock(ComponentState.class);
        when(view.getComponentByReference("genealogyTree")).thenReturn(genealogyTree);

        // when
        viewListeners.generateFormEntity(view, state);

        // then
        verify(advancedGenealogyTreeService).getUsedToProduceTree(batch, includeDrafts, true);
        verify(formEntity).setField("producedBatch", batch);
        verify(formEntity).setField(eq("genealogyTree"), any(EntityTree.class));
    }
}
