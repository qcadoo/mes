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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class OperationHooksOTFOTest {

    private OperationHooksOTFO operationHooksOTFO;

    @Mock
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    @Mock
    private DataDefinition operationDD, operationalTaskDD;

    @Mock
    private Entity operation, operationFromDB, technologyOperationComponent, operationalTask;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationHooksOTFO = new OperationHooksOTFO();

        ReflectionTestUtils.setField(operationHooksOTFO, "operationalTasksForOrdersService", operationalTasksForOrdersService);

        given(operationalTask.getDataDefinition()).willReturn(operationalTaskDD);
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);

        given(entityList.iterator()).willReturn(list.iterator());

        return entityList;
    }

    @Test
    public void shouldReturnIfEntityIdIsNull() throws Exception {
        // given
        Long operationId = null;
        String name = "name";

        given(operation.getId()).willReturn(operationId);

        // when
        operationHooksOTFO.changedNameInOperationalTasksWhenChanged(operationDD, operation);

        // then
        Mockito.verify(operationalTask, Mockito.never()).setField(OperationalTaskFields.NAME, name);
        Mockito.verify(operationalTaskDD, Mockito.never()).save(operationalTask);
    }

    @Test
    public void shouldReturnWhenEntityNameIsTheSame() throws Exception {
        // given
        Long operationId = 1L;
        String name = "name";
        String operationName = "name";

        given(operation.getId()).willReturn(operationId);
        given(operationDD.get(operationId)).willReturn(operationFromDB);

        given(operation.getStringField(OperationFields.NAME)).willReturn(name);
        given(operationFromDB.getStringField(OperationFields.NAME)).willReturn(operationName);

        // when
        operationHooksOTFO.changedNameInOperationalTasksWhenChanged(operationDD, operation);

        // then
        Mockito.verify(operationalTask, Mockito.never()).setField(OperationalTaskFields.NAME, name);
        Mockito.verify(operationalTaskDD, Mockito.never()).save(operationalTask);
    }

    @Ignore
    @Test
    public void shouldChangeOperationalTaskNameWhenOperationNameWasChanged() throws Exception {
        // given
        Long operationId = 1L;
        String name = "name";
        String operationName = "name2";

        given(operation.getId()).willReturn(operationId);
        given(operationDD.get(operationId)).willReturn(operationFromDB);

        given(operation.getStringField(OperationFields.NAME)).willReturn(name);
        given(operationFromDB.getStringField(OperationFields.NAME)).willReturn(operationName);

        EntityList technologyOperationComponents = mockEntityList(Lists.newArrayList(technologyOperationComponent));

        given(operationalTasksForOrdersService.getTechnologyOperationComponentsForOperation(operation)).willReturn(
                technologyOperationComponents);

        // when
        operationHooksOTFO.changedNameInOperationalTasksWhenChanged(operationDD, operation);

        // then
        Mockito.verify(operationalTask).setField(OperationalTaskFields.NAME, name);
        Mockito.verify(operationalTaskDD).save(operationalTask);
    }

}
