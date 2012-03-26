/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.costNormsForOperation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

public class OperationsCostCalculationServiceTest {

    private OperationsCostCalculationService operationCostCalculationService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity costCalculation, costCalculationFromDD, technology, calculationOperationComponent, techOperComp, order;

    @Mock
    private EntityTree entityTree;

    @Mock
    private NumberService numberService;

    @Mock
    private DataDefinition dataDefinition, calculationOperationComponentDD, operationComponentDD, techOperCompDD;

    @Mock
    private EntityTreeNode calculationOpComp;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    private BigDecimal expectedTotalOperationCost, numberOfOperation, quantity, pieceworkCost;

    private Long costCalculationId, techOperCompId;

    private Map<Entity, BigDecimal> quantities = new HashMap<Entity, BigDecimal>();

    private Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

    private EntityTree treeFromTechnology;

    private List<EntityTreeNode> children = new LinkedList<EntityTreeNode>();

    private static EntityTree mockEntityTreeIterator(List<Entity> list) {
        EntityTree entityTree = mock(EntityTree.class);
        when(entityTree.iterator()).thenReturn(list.iterator());
        return entityTree;
    }

    @Before
    public void init() {
        operationCostCalculationService = new OperationsCostCalculationServiceImpl();
        initMocks(this);

        setField(operationCostCalculationService, "dataDefinitionService", dataDefinitionService);
        setField(operationCostCalculationService, "productQuantitiesService", productQuantitiesService);
        setField(operationCostCalculationService, "numberService", numberService);
        setField(operationCostCalculationService, "productComponentQuantities", productComponentQuantities);

        when(costCalculation.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.getName()).thenReturn("costCalculation");
        when(dataDefinitionService.get("costNormsForOperation", "calculationOperationComponent")).thenReturn(
                calculationOperationComponentDD);
        when(dataDefinitionService.get("technologies", "technologyOperationComponent")).thenReturn(techOperCompDD);

        expectedTotalOperationCost = mock(BigDecimal.class);
        numberOfOperation = new BigDecimal(2);
        pieceworkCost = BigDecimal.TEN;
        quantity = new BigDecimal(3);
        costCalculationId = 1L;
        quantities = new HashMap<Entity, BigDecimal>();
        techOperCompId = 1L;
        productComponentQuantities.put(techOperComp, new BigDecimal(3));

        treeFromTechnology = mockEntityTreeIterator(Arrays.asList((Entity) calculationOpComp));

        when(numberService.setScale(new BigDecimal(15))).thenReturn(expectedTotalOperationCost);

    }

    @Test
    public void shouldCalculateOperationCostForPieceworkCorrectlyFromTechnology() throws Exception {
        // given

        when(costCalculation.getStringField("calculateOperationCostsMode")).thenReturn("02piecework");

        when(costCalculation.getId()).thenReturn(costCalculationId);
        when(dataDefinition.get(costCalculationId)).thenReturn(costCalculationFromDD);
        when(costCalculationFromDD.getTreeField("calculationOperationComponents")).thenReturn(entityTree);
        when(entityTree.getRoot()).thenReturn(calculationOpComp);
        when(costCalculation.getBelongsToField("technology")).thenReturn(technology);

        when(technology.getTreeField("operationComponents")).thenReturn(treeFromTechnology);
        when(calculationOperationComponentDD.create()).thenReturn(calculationOperationComponent);
        when(treeFromTechnology.getRoot()).thenReturn(calculationOpComp);
        when(calculationOpComp.getField("entityType")).thenReturn("operation");
        when(calculationOpComp.getDataDefinition()).thenReturn(operationComponentDD);
        when(operationComponentDD.getName()).thenReturn("technologyOperationComponent");

        when(dataDefinition.save(costCalculation)).thenReturn(costCalculation);

        when(costCalculation.getField("quantity")).thenReturn(quantity);

        when(
                productQuantitiesService.getProductComponentQuantities(Mockito.eq(technology), Mockito.eq(quantity),
                        Mockito.anyMap())).thenReturn(quantities);
        quantities.put(calculationOpComp, BigDecimal.ONE);

        when(calculationOpComp.getChildren()).thenReturn(children);
        when(calculationOpComp.getBelongsToField("technologyOperationComponent")).thenReturn(techOperComp);

        when(techOperComp.getId()).thenReturn(techOperCompId);
        when(techOperCompDD.get(techOperCompId)).thenReturn(techOperComp);

        when(calculationOpComp.getField("numberOfOperations")).thenReturn(numberOfOperation);
        when(calculationOpComp.getField("pieceworkCost")).thenReturn(pieceworkCost);
        when(numberService.getMathContext()).thenReturn(MathContext.DECIMAL64);

        when(operationComponentDD.save(calculationOpComp)).thenReturn(calculationOpComp);
        when(calculationOpComp.isValid()).thenReturn(true);
        BigDecimal quantity = new BigDecimal(15);
        when(numberService.setScale(quantity)).thenReturn(quantity);
        // when
        operationCostCalculationService.calculateOperationsCost(costCalculation);

        // then
        verify(costCalculation).setField("totalPieceworkCosts", quantity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGetIncorrectTypeOfSource() throws Exception {
        // given
        Entity wrongEntity = mock(Entity.class);
        DataDefinition wrongDataDefinition = mock(DataDefinition.class);
        when(wrongEntity.getDataDefinition()).thenReturn(wrongDataDefinition);
        when(wrongDataDefinition.getName()).thenReturn("incorrectModel");

        // when
        operationCostCalculationService.calculateOperationsCost(wrongEntity);
    }

}
