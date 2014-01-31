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
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchRestrictions;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class TechnologyOperationComponentHooksOTFOTest {

    private TechnologyOperationComponentHooksOTFO technologyOperationComponentHooksOTFO;

    @Mock
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    @Mock
    private DataDefinition technologyOperationComponentDD, operationalTaskDD;

    @Mock
    private Entity technologyOperationComponent, technologyOperationComponentFromDB, techOperCompOperationalTask,
            operationalTask;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyOperationComponentHooksOTFO = new TechnologyOperationComponentHooksOTFO();

        PowerMockito.mockStatic(SearchRestrictions.class);

        ReflectionTestUtils.setField(technologyOperationComponentHooksOTFO, "operationalTasksForOrdersService",
                operationalTasksForOrdersService);

        given(operationalTask.getDataDefinition()).willReturn(operationalTaskDD);

    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);

        given(entityList.iterator()).willReturn(list.iterator());

        return entityList;
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        Long technologyOperationComponentId = null;
        String comment = "comment";

        given(technologyOperationComponent.getId()).willReturn(technologyOperationComponentId);

        // when
        technologyOperationComponentHooksOTFO.changedDescriptionOperationTasksWhenCommentEntityChanged(
                technologyOperationComponentDD, technologyOperationComponent);

        // then
        Mockito.verify(operationalTask, Mockito.never()).setField(OperationalTaskFields.DESCRIPTION, comment);
        Mockito.verify(operationalTaskDD, Mockito.never()).save(operationalTask);
    }

    @Test
    public void shouldReturnWhenTechnologyOperationComponentCommentIsTheSame() throws Exception {
        // given
        Long technologyOperationComponentId = 1L;
        String comment = "comment";
        String technologyOperationComponentComment = "comment";

        given(technologyOperationComponent.getId()).willReturn(technologyOperationComponentId);
        given(technologyOperationComponentDD.get(technologyOperationComponentId)).willReturn(technologyOperationComponentFromDB);

        given(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT)).willReturn(comment);
        given(technologyOperationComponentFromDB.getStringField(TechnologyOperationComponentFields.COMMENT)).willReturn(
                technologyOperationComponentComment);

        // when
        technologyOperationComponentHooksOTFO.changedDescriptionOperationTasksWhenCommentEntityChanged(
                technologyOperationComponentDD, technologyOperationComponent);

        // then
        Mockito.verify(operationalTask, Mockito.never()).setField(OperationalTaskFields.DESCRIPTION, comment);
        Mockito.verify(operationalTaskDD, Mockito.never()).save(operationalTask);
    }

    @Ignore
    @Test
    public void shouldChangeOperationalTasksDescriptionWhenTechnologyOperationComponentCommentWasChanged() throws Exception {
        // given
        Long technologyOperationComponentId = 1L;
        String comment = "comment";
        String technologyOperationComponentComment = "comment2";

        given(technologyOperationComponent.getId()).willReturn(technologyOperationComponentId);
        given(technologyOperationComponentDD.get(technologyOperationComponentId)).willReturn(technologyOperationComponentFromDB);

        given(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT)).willReturn(comment);
        given(technologyOperationComponentFromDB.getStringField(TechnologyOperationComponentFields.COMMENT)).willReturn(
                technologyOperationComponentComment);

        EntityList techOperCompOperationalTasks = mockEntityList(Lists.newArrayList(techOperCompOperationalTask));

        given(
                operationalTasksForOrdersService
                        .getTechOperCompOperationalTasksForTechnologyOperationComponent(technologyOperationComponent))
                .willReturn(techOperCompOperationalTasks);

        // when
        technologyOperationComponentHooksOTFO.changedDescriptionOperationTasksWhenCommentEntityChanged(
                technologyOperationComponentDD, technologyOperationComponent);
        // then
        Mockito.verify(operationalTask).setField(OperationalTaskFields.DESCRIPTION, comment);
        Mockito.verify(operationalTaskDD).save(operationalTask);
    }

}
