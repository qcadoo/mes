/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.operationTimeCalculations;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

public class OrderRealizationTimeServiceImplTest {

    private OrderRealizationTimeServiceImpl orderRealizationTimeServiceImpl;

    @Mock
    private EntityTreeNode opComp1, opComp2;

    @Mock
    private Entity productionLine;

    @Mock
    private Entity order;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    @Mock
    private ProductionLinesService productionLinesService;

    @Mock
    private NumberService numberService;

    private MathContext mathContext;

    private static EntityList mockEntityListIterator(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    private static EntityTree mockEntityTreeIterator(List<Entity> list) {
        EntityTree entityTree = mock(EntityTree.class);
        when(entityTree.iterator()).thenReturn(list.iterator());
        return entityTree;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderRealizationTimeServiceImpl = new OrderRealizationTimeServiceImpl();

        when(opComp1.getStringField("entityType")).thenReturn("operation");
        when(opComp2.getStringField("entityType")).thenReturn("operation");

        when(opComp1.getStringField("countRealized")).thenReturn("01all");
        when(opComp2.getStringField("countRealized")).thenReturn("01all");

        when(opComp1.getField("tj")).thenReturn(new Integer(1));
        when(opComp2.getField("tj")).thenReturn(new Integer(1));

        when(opComp1.getField("countMachine")).thenReturn(new BigDecimal(1));
        when(opComp2.getField("countMachine")).thenReturn(new BigDecimal(1));

        when(opComp1.getField("tpz")).thenReturn(new Integer(1));
        when(opComp2.getField("tpz")).thenReturn(new Integer(1));

        when(opComp1.getField("timeNextOperation")).thenReturn(new Integer(1));
        when(opComp2.getField("timeNextOperation")).thenReturn(new Integer(1));

        when(opComp1.getField("productionInOneCycle")).thenReturn(new BigDecimal(1));
        when(opComp2.getField("productionInOneCycle")).thenReturn(new BigDecimal(1));

        when(opComp2.getBelongsToField("parent")).thenReturn(opComp1);

        EntityList opComp1Children = mockEntityListIterator(asList((Entity) opComp2));
        when(opComp1.getHasManyField("children")).thenReturn(opComp1Children);
        EntityList opComp2Children = mockEntityListIterator(new LinkedList<Entity>());
        when(opComp2.getHasManyField("children")).thenReturn(opComp2Children);

        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();
        operationRuns.put(opComp1, new BigDecimal(2));
        operationRuns.put(opComp2, new BigDecimal(4));

        DataDefinition dd = mock(DataDefinition.class);

        when(dd.getName()).thenReturn("technologyOperationComponent");

        when(opComp1.getDataDefinition()).thenReturn(dd);
        when(opComp2.getDataDefinition()).thenReturn(dd);

        given(opComp1.getId()).willReturn(1L);
        given(opComp2.getId()).willReturn(2L);

        given(dd.get(1L)).willReturn(opComp1);
        given(dd.get(2L)).willReturn(opComp2);

        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "operationRunsField", operationRuns);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "numberService", numberService);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "productionLinesService", productionLinesService);

        mathContext = MathContext.DECIMAL64;
        when(numberService.getMathContext()).thenReturn(mathContext);

        when(productionLinesService.getWorkstationTypesCount(opComp1, productionLine)).thenReturn(1);
        when(productionLinesService.getWorkstationTypesCount(opComp2, productionLine)).thenReturn(1);
    }

    @Test
    public void shouldReturnCorrectOperationTimeWithShifts() {
        // given
        boolean includeTpz = true;
        boolean includeAdditionalTime = true;
        BigDecimal plannedQuantity = new BigDecimal(1);

        // when
        int time = orderRealizationTimeServiceImpl.estimateRealizationTimeForOperation(opComp1, plannedQuantity, includeTpz,
                includeAdditionalTime, productionLine);

        // then
        assertEquals(10, time);
    }

    @Test
    public void shouldShortenOperationTimeWithMoreWorkstations() {
        // given
        boolean includeTpz = true;
        boolean includeAdditionalTime = true;
        BigDecimal plannedQuantity = new BigDecimal(1);

        when(productionLinesService.getWorkstationTypesCount(opComp1, productionLine)).thenReturn(2);
        when(productionLinesService.getWorkstationTypesCount(opComp2, productionLine)).thenReturn(2);

        // when
        int time = orderRealizationTimeServiceImpl.estimateRealizationTimeForOperation(opComp1, plannedQuantity, includeTpz,
                includeAdditionalTime, productionLine);

        // then
        assertEquals(7, time);
    }

    @Test
    public void shouldReturnTimesForAllOperationsInAnOrder() {
        // given
        boolean includeTpz = true;
        boolean includeAdditionalTime = true;
        BigDecimal plannedQuantity = new BigDecimal(1);
        DataDefinition dd = mock(DataDefinition.class);
        when(dd.getName()).thenReturn("order");
        when(order.getDataDefinition()).thenReturn(dd);
        EntityTree technologyInstanceOperationComponents = mockEntityTreeIterator(asList((Entity) opComp1, (Entity) opComp2));
        when(order.getTreeField("technologyInstanceOperationComponents")).thenReturn(technologyInstanceOperationComponents);

        when(opComp1.getBelongsToField("technologyOperationComponent")).thenReturn(opComp1);
        when(opComp2.getBelongsToField("technologyOperationComponent")).thenReturn(opComp2);

        // when
        Map<Entity, Integer> operationDurations = orderRealizationTimeServiceImpl.estimateRealizationTimes(order,
                plannedQuantity, includeTpz, includeAdditionalTime, productionLine);

        // then
        assertEquals(new Integer(4), operationDurations.get(opComp1));
        assertEquals(new Integer(6), operationDurations.get(opComp2));
    }

    @Test
    public void shouldReturnTimesForAllOperationsInATechnology() {
        // given
        Entity technology = mock(Entity.class);
        boolean includeTpz = true;
        boolean includeAdditionalTime = true;
        BigDecimal plannedQuantity = new BigDecimal(1);
        DataDefinition dd = mock(DataDefinition.class);
        when(dd.getName()).thenReturn("technology");
        when(technology.getDataDefinition()).thenReturn(dd);
        EntityTree technologyInstanceOperationComponents = mockEntityTreeIterator(asList((Entity) opComp1, (Entity) opComp2));
        when(technology.getTreeField("operationComponents")).thenReturn(technologyInstanceOperationComponents);

        // when
        Map<Entity, Integer> operationDurations = orderRealizationTimeServiceImpl.estimateRealizationTimes(technology,
                plannedQuantity, includeTpz, includeAdditionalTime, productionLine);

        // then
        assertEquals(new Integer(4), operationDurations.get(opComp1));
        assertEquals(new Integer(6), operationDurations.get(opComp2));
    }
}
