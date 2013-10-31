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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OperationalTasksListOTFOHooksTest {

    private OperationalTasksListOTFOHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity prodInEntity, prodOutEntity, prodIn1, prodIn2, prodOut1, techOperComp, tioc, task1, task2;

    @Mock
    EntityList tasks, tiocs, operations, prodsIn, prodsOut;

    @Mock
    private LookupComponent productIn, productOut;

    @Mock
    private DataDefinition tasksDD, tiocDD, prodInDD, prodOutDD;

    @Mock
    private SearchCriteriaBuilder tiocBuilder, tasksBuilder, prodInBuilder, prodOutBuilder;

    @Mock
    private SearchResult tiocResult, tasksResult, prodInResult, prodOutResult;

    @Mock
    private GridComponent gridComponent;

    @Before
    public void init() {
        hooks = new OperationalTasksListOTFOHooks();

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(hooks, "dataDefinitionService", dataDefinitionService);
        PowerMockito.mockStatic(SearchRestrictions.class);

        when(view.getComponentByReference(OperationalTasksFields.PRODUCT_IN)).thenReturn(productIn);
        when(view.getComponentByReference(OperationalTasksFields.PRODUCT_OUT)).thenReturn(productOut);
        when(view.getComponentByReference("grid")).thenReturn(gridComponent);
        // QUERY FOR TIOC
        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).thenReturn(tiocDD);
        when(tiocDD.find()).thenReturn(tiocBuilder);
        SearchCriterion tiocCriterion = SearchRestrictions.belongsTo(
                TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT, techOperComp);
        when(tiocBuilder.add(tiocCriterion)).thenReturn(tiocBuilder);
        when(tiocBuilder.list()).thenReturn(tiocResult);

        // QUERY FOR PROD_IN
        when(productIn.getEntity()).thenReturn(prodInEntity);

        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)).thenReturn(prodInDD);
        when(prodInDD.find()).thenReturn(prodInBuilder);
        SearchCriterion prodInCriterion = SearchRestrictions.belongsTo("product", prodInEntity);
        when(prodInBuilder.add(prodInCriterion)).thenReturn(prodInBuilder);
        when(prodInBuilder.list()).thenReturn(prodInResult);

        // QUERY FOR PROD_OUT
        when(productOut.getEntity()).thenReturn(prodOutEntity);

        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT)).thenReturn(prodOutDD);
        when(prodOutDD.find()).thenReturn(prodOutBuilder);
        SearchCriterion prodOutCriterion = SearchRestrictions.belongsTo("product", prodOutEntity);
        when(prodOutBuilder.add(prodOutCriterion)).thenReturn(prodOutBuilder);
        when(prodOutBuilder.list()).thenReturn(prodOutResult);

    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Ignore
    @Test
    public void shouldReturnWhenProduInAndProduOutIsNull() throws Exception {
        // given
        when(productIn.getEntity()).thenReturn(null);
        when(productOut.getEntity()).thenReturn(null);

        // when
        hooks.addDiscriminatorRestrictionToGrid(view);
        // then
    }

    @Ignore
    @Test
    public void shouldReturnTasksForProductInWhenProdOutIsNull() throws Exception {
        // given
        when(productOut.getEntity()).thenReturn(null);

        EntityList productsInLists = mockEntityList(Lists.newArrayList(prodIn1, prodIn2));
        when(prodInResult.getEntities()).thenReturn(productsInLists);

        EntityList tiocsLists = mockEntityList(Lists.newArrayList(tioc));
        when(tiocResult.getEntities()).thenReturn(tiocsLists);

        List<Entity> tasks = Lists.newArrayList(task2);
        EntityList tasksList = mockEntityList(tasks);
        when(tasksResult.getEntities()).thenReturn(tasksList);
        // when
        hooks.addDiscriminatorRestrictionToGrid(view);
        // then

        Mockito.verify(gridComponent).setEntities(tasks);

    }

    @Ignore
    @Test
    public void shouldReturnTasksForProductOutWhenProdInIsNull() throws Exception {
        // given
        when(productIn.getEntity()).thenReturn(null);

        EntityList productsOutLists = mockEntityList(Lists.newArrayList(prodOut1));
        when(prodOutResult.getEntities()).thenReturn(productsOutLists);

        EntityList tiocsLists = mockEntityList(Lists.newArrayList(tioc));
        when(tiocResult.getEntities()).thenReturn(tiocsLists);

        List<Entity> tasks = Lists.newArrayList(task1, task2);
        EntityList tasksList = mockEntityList(tasks);
        when(tasksResult.getEntities()).thenReturn(tasksList);
        // when
        hooks.addDiscriminatorRestrictionToGrid(view);
        // then

        Mockito.verify(gridComponent).setEntities(tasks);

    }

}
