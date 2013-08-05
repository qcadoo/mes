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
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class TechOperCompHooksOTFOTest {

    private TechOperCompHooksOTFO hooksOTFO;

    @Mock
    private Entity entity, tioc, task;

    @Mock
    private DataDefinition dataDefinition, operationalTasksDD;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private SearchCriteriaBuilder builder;

    @Mock
    private SearchResult result;

    @Before
    public void init() {
        hooksOTFO = new TechOperCompHooksOTFO();
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SearchRestrictions.class);
        ReflectionTestUtils.setField(hooksOTFO, "dataDefinitionService", dataDefinitionService);

        when(
                dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                        OperationalTasksConstants.MODEL_OPERATIONAL_TASK)).thenReturn(operationalTasksDD);
        when(operationalTasksDD.find()).thenReturn(builder);

    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        when(entity.getId()).thenReturn(null);

        // when
        hooksOTFO.changedDescriptionOperationTasksWhenCommentEntityChanged(dataDefinition, entity);
    }

    @Test
    public void shouldReturnWhenOperationCommentIsThisSame() throws Exception {
        // given
        Long entityId = 1L;
        String entityComment = "comment";
        String tiocComment = "comment";
        when(entity.getId()).thenReturn(entityId);
        when(dataDefinition.get(entityId)).thenReturn(tioc);
        when(entity.getStringField("comment")).thenReturn(entityComment);
        when(tioc.getStringField("comment")).thenReturn(tiocComment);

        // when
        hooksOTFO.changedDescriptionOperationTasksWhenCommentEntityChanged(dataDefinition, entity);
    }

    @Ignore
    @Test
    public void shouldChangedOperationaTasksDescriptionWhenOperationCommentWasChanged() throws Exception {
        // given
        Long entityId = 1L;
        String entityComment = "comment";
        String tiocComment = "comment2";
        when(entity.getId()).thenReturn(entityId);
        when(dataDefinition.get(entityId)).thenReturn(tioc);
        when(entity.getStringField("comment")).thenReturn(entityComment);
        when(tioc.getStringField("comment")).thenReturn(tiocComment);

        EntityList tasks = mockEntityList(Lists.newArrayList(task));

        when(result.getEntities()).thenReturn(tasks);
        // when
        hooksOTFO.changedDescriptionOperationTasksWhenCommentEntityChanged(dataDefinition, entity);
        // then
        Mockito.verify(task).setField(OperationalTasksFields.DESCRIPTION, entityComment);
    }
}
