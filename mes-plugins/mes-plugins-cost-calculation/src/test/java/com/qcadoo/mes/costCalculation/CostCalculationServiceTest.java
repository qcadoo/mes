package com.qcadoo.mes.costCalculation;

import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.model.api.Entity;

public class CostCalculationServiceTest {

    private CostCalculationService costCalc;

    private Entity costCalculation;

    private Entity technology;

    private Entity order;

    private Map hashMap;

    @Before
    public void init() {
        costCalc = new CostCalculationServiceImpl();

        costCalculation = mock(Entity.class);
        order = mock(Entity.class);
        technology = mock(Entity.class);
        hashMap = mock(Map.class);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnExceptionWhenEntityIsNull() {
        // when
        costCalc.calculateTotalCost(technology, order, hashMap);

    }

    @Test
    public void shouldReturnCorrectValue() throws Exception {
        // given

        // then

        // when

    }
}
