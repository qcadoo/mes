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
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OperationHooksOTFOTest {

    private OperationHooksOTFO hooksOTFO;

    @Mock
    private DataDefinition dataDefinition, techInstOperCompDD, operationalTasksDD;

    @Mock
    private Entity entity, operation, tioc1, task1;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private SearchCriteriaBuilder builder1, builder2;

    @Mock
    private SearchResult result1, result2;

    @Before
    public void init() {
        hooksOTFO = new OperationHooksOTFO();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(hooksOTFO, "dataDefinitionService", dataDefinitionService);

        PowerMockito.mockStatic(SearchRestrictions.class);
        when(
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).thenReturn(techInstOperCompDD);
        when(techInstOperCompDD.find()).thenReturn(builder1);
        SearchCriterion criterion = SearchRestrictions.belongsTo(TechnologyInstanceOperCompFields.OPERATION, operation);
        when(builder1.add(criterion)).thenReturn(builder1);
        when(builder1.list()).thenReturn(result1);

        when(
                dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                        OperationalTasksConstants.MODEL_OPERATIONAL_TASK)).thenReturn(operationalTasksDD);
        when(operationalTasksDD.find()).thenReturn(builder2);
        SearchCriterion criterion2 = SearchRestrictions.belongsTo("technologyInstanceOperationComponent", tioc1);
        when(builder2.add(criterion2)).thenReturn(builder2);
        when(builder2.list()).thenReturn(result2);

    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldReturnIfEntityIdIsNull() throws Exception {
        // given
        when(entity.getId()).thenReturn(null);
        // when
        hooksOTFO.changedNameOperationTasksWhenEntityNameChanged(dataDefinition, entity);

    }

    @Test
    public void shouldReturnWhenEntityNameIsEqualsOperationName() throws Exception {
        // given
        Long entityId = 1L;
        String entityName = "name";
        String operationName = "name";
        when(entity.getId()).thenReturn(entityId);
        when(dataDefinition.get(entityId)).thenReturn(operation);
        when(entity.getStringField("name")).thenReturn(entityName);
        when(operation.getStringField("name")).thenReturn(operationName);
        // when
        hooksOTFO.changedNameOperationTasksWhenEntityNameChanged(dataDefinition, entity);

        // then
    }

    @Test
    public void shouldSetOperationNameToOperationTaskName() throws Exception {
        // given
        Long entityId = 1L;
        String entityName = "name";
        String operationName = "name2";
        when(entity.getId()).thenReturn(entityId);
        when(dataDefinition.get(entityId)).thenReturn(operation);
        when(entity.getStringField("name")).thenReturn(entityName);
        when(operation.getStringField("name")).thenReturn(operationName);

        EntityList tiocs = mockEntityList(Lists.newArrayList(tioc1));
        EntityList tasks = mockEntityList(Lists.newArrayList(task1));

        when(result1.getEntities()).thenReturn(tiocs);
        when(result2.getEntities()).thenReturn(tasks);
        // when
        hooksOTFO.changedNameOperationTasksWhenEntityNameChanged(dataDefinition, entity);

        // then
        Mockito.verify(task1).setField("name", "name");
    }

}
