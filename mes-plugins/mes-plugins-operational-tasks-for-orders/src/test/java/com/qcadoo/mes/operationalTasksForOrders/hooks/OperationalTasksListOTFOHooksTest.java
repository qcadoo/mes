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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OperationalTasksListOTFOHooksTest {

    private static final String L_GRID = "grid";

    private OperationalTasksListHooksOTFO operationalTasksListHooksOTFO;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private LookupComponent productInLookup, productOutLookup;

    @Mock
    private GridComponent grid;

    @Mock
    private DataDefinition technologyOperationComponentDD, operationProductInComponentDD, operationProductOutComponentDD;

    @Mock
    private Entity productIn, productOut, operationProductInComponent, operationProductOutComponent,
            technologyOperationComponent, operationalTask;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult technologyOperationComponentsResult, operationProductInComponentsResult,
            operationProductOutComponentsResult;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationalTasksListHooksOTFO = new OperationalTasksListHooksOTFO();

        PowerMockito.mockStatic(SearchRestrictions.class);

        ReflectionTestUtils.setField(operationalTasksListHooksOTFO, "dataDefinitionService", dataDefinitionService);

        given(view.getComponentByReference(OperationalTaskFields.PRODUCT_IN)).willReturn(productInLookup);
        given(view.getComponentByReference(OperationalTaskFields.PRODUCT_OUT)).willReturn(productOutLookup);
        given(view.getComponentByReference(L_GRID)).willReturn(grid);

        given(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)).willReturn(technologyOperationComponentDD);
        given(technologyOperationComponentDD.find()).willReturn(searchCriteriaBuilder);

        given(searchCriteriaBuilder.list()).willReturn(technologyOperationComponentsResult);

        given(productInLookup.getEntity()).willReturn(productIn);

        given(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)).willReturn(operationProductInComponentDD);
        given(operationProductInComponentDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, productIn)))
                .willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(operationProductInComponentsResult);

        given(productOutLookup.getEntity()).willReturn(productOut);

        given(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT)).willReturn(operationProductOutComponentDD);
        given(operationProductOutComponentDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OperationProductOutComponentFields.PRODUCT, productOut)))
                .willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(operationProductOutComponentsResult);

    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);

        given(entityList.iterator()).willReturn(list.iterator());

        return entityList;
    }

    @Test
    public void shouldReturnWhenProduInAndProduOutIsNull() throws Exception {
        // given
        given(productInLookup.getEntity()).willReturn(null);
        given(productOutLookup.getEntity()).willReturn(null);

        EntityList operationalTasks = mockEntityList(Lists.newArrayList(operationalTask));

        // when
        operationalTasksListHooksOTFO.addDiscriminatorRestrictionToGrid(view);

        // then
        Mockito.verify(grid, Mockito.never()).setEntities(operationalTasks);
    }

    @Ignore
    @Test
    public void shouldReturnTasksForProductInWhenProdOutIsNull() throws Exception {
        // given
        given(productOutLookup.getEntity()).willReturn(null);

        EntityList operationProductInComponents = mockEntityList(Lists.newArrayList(operationProductInComponent));

        given(operationProductInComponentsResult.getEntities()).willReturn(operationProductInComponents);

        EntityList technologyOperationComponents = mockEntityList(Lists.newArrayList(technologyOperationComponent));

        given(technologyOperationComponentsResult.getEntities()).willReturn(technologyOperationComponents);

        EntityList operationalTasks = mockEntityList(Lists.newArrayList(operationalTask));

        // when
        operationalTasksListHooksOTFO.addDiscriminatorRestrictionToGrid(view);

        // then
        Mockito.verify(grid).setEntities(operationalTasks);
    }

    @Ignore
    @Test
    public void shouldReturnTasksForProductOutWhenProdInIsNull() throws Exception {
        // given
        given(productInLookup.getEntity()).willReturn(null);

        EntityList operationProductOutComponents = mockEntityList(Lists.newArrayList(operationProductOutComponent));

        given(operationProductOutComponentsResult.getEntities()).willReturn(operationProductOutComponents);

        EntityList technologyOperationComponents = mockEntityList(Lists.newArrayList(technologyOperationComponent));

        given(technologyOperationComponentsResult.getEntities()).willReturn(technologyOperationComponents);

        EntityList operationalTasks = mockEntityList(Lists.newArrayList(operationalTask));

        // when
        operationalTasksListHooksOTFO.addDiscriminatorRestrictionToGrid(view);
        // then

        Mockito.verify(grid).setEntities(operationalTasks);
    }

}
