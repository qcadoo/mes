/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.operationCostCalculations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

public class OperationsCostCalculationServiceTest {

    private OperationsCostCalculationService operationCostCalculationService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity costCalculation, techOperComp;

    @Mock
    private NumberService numberService;

    @Mock
    private DataDefinition dataDefinition, calculationOperationComponentDD, techOperCompDD;

    @Mock
    private ProductQuantitiesService productQuantitiesService;

    private BigDecimal expectedTotalOperationCost;

    private Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

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
        productComponentQuantities.put(techOperComp, new BigDecimal(3));

        when(numberService.setScale(new BigDecimal(15))).thenReturn(expectedTotalOperationCost);

    }

    @Ignore
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
