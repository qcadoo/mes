package com.qcadoo.mes.productionScheduling;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;

public class OrderRealizationTimeServiceImplTest {

    OrderRealizationTimeServiceImpl orderRealizationTimeServiceImpl;

    @Mock
    EntityTreeNode opComp1, opComp2;

    @Mock
    Entity op1, op2;

    @Mock
    ProductQuantitiesService productQuantitiesService;

    @Mock
    TechnologyService technologyService;

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

        List<EntityTreeNode> opComp1Children = asList(opComp2);
        when(opComp1.getChildren()).thenReturn(opComp1Children);

        Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();
        operationRuns.put(opComp1, new BigDecimal(1));
        operationRuns.put(opComp2, new BigDecimal(2));

        DataDefinition dd = mock(DataDefinition.class);

        when(dd.getName()).thenReturn("technologyOperationComponent");

        when(opComp1.getDataDefinition()).thenReturn(dd);
        when(opComp2.getDataDefinition()).thenReturn(dd);

        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "operationRunsField", operationRuns);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "productQuantitiesService", productQuantitiesService);
        ReflectionTestUtils.setField(orderRealizationTimeServiceImpl, "technologyService", technologyService);

        when(technologyService.getProductCountForOperationComponent(opComp1)).thenReturn(new BigDecimal(1));
        when(technologyService.getProductCountForOperationComponent(opComp2)).thenReturn(new BigDecimal(1));
    }

    @Test
    public void shouldReturnCorrectOperationTime() {
        // given
        boolean includeTpz = true;
        BigDecimal plannedQuantity = new BigDecimal(1);

        // when
        int time = orderRealizationTimeServiceImpl.estimateRealizationTimeForOperation(opComp1, plannedQuantity, includeTpz);

        // then
        assertEquals(6, time);
    }
}
