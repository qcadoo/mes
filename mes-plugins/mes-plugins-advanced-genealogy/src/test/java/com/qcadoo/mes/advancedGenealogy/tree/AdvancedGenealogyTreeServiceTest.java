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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;

public class AdvancedGenealogyTreeServiceTest {

    AdvancedGenealogyTreeService treeService;

    @Mock
    private Entity batch1, batch2;

    @Mock
    private Entity product1, product2;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private SearchResult searchResult;

    @Mock
    private Entity batch1Tr;

    @Mock
    private Entity parent1, parent2;

    private String productName1 = "productName1";

    private String productNumber1 = "productNumber1";

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        treeService = new AdvancedGenealogyTreeService();

        ReflectionTestUtils.setField(treeService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(treeService, "pluginAccessor", pluginAccessor);

        when(pluginAccessor.getPlugin("advancedGenealogyForOrders")).thenReturn(null);

        when(dataDefinitionService.get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_BATCH))
                .thenReturn(dataDefinition);

        when(batch1.getId()).thenReturn(1L);
        when(batch2.getId()).thenReturn(2L);

        EntityList batch1Trs = mockEntityList(asList(batch1Tr));
        when(batch1.getHasManyField("trackingRecords")).thenReturn(batch1Trs);
        when(batch1Tr.getStringField("entityType")).thenReturn(TrackingRecordType.SIMPLE);
        when(batch1Tr.getStringField("state")).thenReturn(TrackingRecordState.DRAFT.getStringValue());
        Entity batch1TrUsedBatch1 = mock(Entity.class);
        when(batch1TrUsedBatch1.getBelongsToField("batch")).thenReturn(batch2);
        EntityList batch1TrUsedBatches = mockEntityList(asList(batch1TrUsedBatch1));
        when(batch1Tr.getHasManyField("usedBatchesSimple")).thenReturn(batch1TrUsedBatches);

        EntityList batch2Trs = mockEntityList(new LinkedList<Entity>());
        when(batch2.getHasManyField("trackingRecords")).thenReturn(batch2Trs);

        SearchCriteriaBuilder searchCriteriaBuilder = Mockito.mock(SearchCriteriaBuilder.class);
        when(dataDefinition.find()).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(asList(batch1));

        String number1 = "QWD33";
        String number2 = "POS22";
        String productName2 = "productName2";
        String productNumber2 = "productNumber2";
        when(batch1.getField("parent")).thenReturn(parent1);
        when(batch2.getField("parent")).thenReturn(parent2);
        when(batch1.getBelongsToField("product")).thenReturn(product1);
        when(batch2.getBelongsToField("product")).thenReturn(product2);
        when(product1.getStringField("name")).thenReturn(productName1);
        when(product1.getStringField("number")).thenReturn(productNumber1);
        when(product2.getStringField("name")).thenReturn(productName2);
        when(product2.getStringField("number")).thenReturn(productNumber2);
        when(batch1.getField("number")).thenReturn(number1);
        when(batch2.getField("number")).thenReturn(number2);
    }

    @Test
    public void shouldReturnOnlyTheRootIfThereAreNoRelatedBatchesForProducedFromTree() {
        // given
        Entity batch = mock(Entity.class);
        when(batch.getBelongsToField("product")).thenReturn(product1);
        when(product1.getStringField("name")).thenReturn(productName1);
        when(product1.getStringField("number")).thenReturn(productNumber1);
        EntityList trackingRecords = mockEntityList(new LinkedList<Entity>());
        when(batch.getHasManyField("trackingRecords")).thenReturn(trackingRecords);

        // when
        List<Entity> tree = treeService.getProducedFromTree(batch, true, false);

        // then
        assertEquals(1, tree.size());
        assertEquals(batch, tree.get(0));
    }

    @Test
    public void shouldReturnOnlyTheRootIfThereAreNoRelatedBatchesForUsedToProduceTree() {
        // given
        Entity batch = mock(Entity.class);
        when(batch.getBelongsToField("product")).thenReturn(product1);
        when(product1.getStringField("name")).thenReturn(productName1);
        when(product1.getStringField("number")).thenReturn(productNumber1);

        when(searchResult.getEntities()).thenReturn(new LinkedList<Entity>());

        // when
        List<Entity> tree = treeService.getUsedToProduceTree(batch, true, false);

        // then
        assertEquals(1, tree.size());
        assertEquals(batch, tree.get(0));
    }

    @Test
    public void shouldReturnCorrectProducedFromTree() {
        // given

        // when
        List<Entity> tree = treeService.getProducedFromTree(batch1, true, false);

        // then
        assertEquals(2, tree.size());
        assertEquals(batch1, tree.get(0));
        assertEquals(batch2, tree.get(1));
    }

    @Test
    public void shouldReturnCorrectUsedToProduceTree() {
        // given

        // when
        List<Entity> tree = treeService.getUsedToProduceTree(batch2, true, false);

        // then
        assertEquals(2, tree.size());
        assertEquals(batch2, tree.get(0));
        assertEquals(batch1, tree.get(1));
    }

    @Test
    public void shouldRespectIncludeDraftParameter() {
        // given

        // when
        List<Entity> tree = treeService.getProducedFromTree(batch1, false, false);

        // then
        assertEquals(1, tree.size());
        assertEquals(batch1, tree.get(0));
    }

    @Test
    @Ignore
    public void shouldReturnCorrectProducedFromTreeForOrders() {
        // given
        Plugin plugin = mock(Plugin.class);
        when(pluginAccessor.getPlugin("advancedGenealogyForOrders")).thenReturn(plugin);
        when(batch1Tr.getStringField("entityType")).thenReturn(TrackingRecordType.FOR_ORDER);

        Entity genProdInComp = mock(Entity.class);
        EntityList genProdInComps = mockEntityList(asList(genProdInComp));

        Entity prodInBatch = mock(Entity.class);
        when(prodInBatch.getBelongsToField("batch")).thenReturn(batch2);

        EntityList prodInBatches = mockEntityList(asList(prodInBatch));
        when(genProdInComp.getHasManyField("productInBatches")).thenReturn(prodInBatches);
        when(batch1Tr.getHasManyField("genealogyProductInComponents")).thenReturn(genProdInComps);

        // when
        List<Entity> tree = treeService.getProducedFromTree(batch1, true, false);

        // then
        assertEquals(2, tree.size());
        assertEquals(batch1, tree.get(0));
        assertEquals(batch2, tree.get(1));
    }

    @Test
    @Ignore
    public void shouldReturnCorrectUsedToProduceTreeForOrders() {
        // given
        Plugin plugin = mock(Plugin.class);
        when(pluginAccessor.getPlugin("advancedGenealogyForOrders")).thenReturn(plugin);
        when(batch1Tr.getStringField("entityType")).thenReturn(TrackingRecordType.FOR_ORDER);

        Entity genProdInComp = mock(Entity.class);
        EntityList genProdInComps = mockEntityList(asList(genProdInComp));

        Entity prodInBatch = mock(Entity.class);
        when(prodInBatch.getBelongsToField("batch")).thenReturn(batch2);

        EntityList prodInBatches = mockEntityList(asList(prodInBatch));
        when(genProdInComp.getHasManyField("productInBatches")).thenReturn(prodInBatches);
        when(batch1Tr.getHasManyField("genealogyProductInComponents")).thenReturn(genProdInComps);

        // when
        List<Entity> tree = treeService.getUsedToProduceTree(batch2, true, false);

        // then
        assertEquals(2, tree.size());
        assertEquals(batch2, tree.get(0));
        assertEquals(batch1, tree.get(1));
    }
}
