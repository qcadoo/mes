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
import com.qcadoo.mes.orders.constants.OrderFields;
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
public class OrderHooksOTFOTest {

    private OrderHooksOTFO hooksOTFO;

    @Mock
    private Entity entity, order, prodLine, orderProdLine, task1;

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
        hooksOTFO = new OrderHooksOTFO();
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SearchRestrictions.class);
        ReflectionTestUtils.setField(hooksOTFO, "dataDefinitionService", dataDefinitionService);

        when(
                dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                        OperationalTasksConstants.MODEL_OPERATIONAL_TASK)).thenReturn(operationalTasksDD);
        when(operationalTasksDD.find()).thenReturn(builder);
        SearchCriterion criterion = SearchRestrictions.belongsTo("order", order);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.list()).thenReturn(result);
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        Mockito.when(order.getId()).thenReturn(null);

        // when
        hooksOTFO.changedProductionLine(dataDefinition, order);
        // then
    }

    @Test
    public void shouldReturnWhenProductionLineIsThisSame() throws Exception {
        // given
        Long orderId = 1L;
        when(entity.getId()).thenReturn(orderId);
        when(dataDefinition.get(orderId)).thenReturn(order);
        when(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(orderProdLine);
        when(entity.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(orderProdLine);

        // when
        hooksOTFO.changedProductionLine(dataDefinition, entity);
        // then
    }

    @Test
    public void shouldChangedProdLineWhenProdLineIsNullify() throws Exception {
        // given
        Long orderId = 1L;
        when(entity.getId()).thenReturn(orderId);
        when(dataDefinition.get(orderId)).thenReturn(order);
        when(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(orderProdLine);
        when(entity.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(null);
        EntityList tasks = mockEntityList(Lists.newArrayList(task1));

        when(result.getEntities()).thenReturn(tasks);
        // when
        hooksOTFO.changedProductionLine(dataDefinition, entity);
        // then

        Mockito.verify(task1).setField(OrderFields.PRODUCTION_LINE, null);
    }

    @Test
    public void shouldChangedProdLineWhenProdLineWasNull() throws Exception {
        // given
        Long orderId = 1L;
        when(entity.getId()).thenReturn(orderId);
        when(dataDefinition.get(orderId)).thenReturn(order);
        when(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(null);
        when(entity.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(prodLine);
        EntityList tasks = mockEntityList(Lists.newArrayList(task1));

        when(result.getEntities()).thenReturn(tasks);
        // when
        hooksOTFO.changedProductionLine(dataDefinition, entity);
        // then

        Mockito.verify(task1).setField(OrderFields.PRODUCTION_LINE, prodLine);
    }

    @Test
    public void shouldChangedProdLineWhenProdLineIsChanging() throws Exception {
        // given
        Long orderId = 1L;
        when(entity.getId()).thenReturn(orderId);
        when(dataDefinition.get(orderId)).thenReturn(order);
        when(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(orderProdLine);
        when(entity.getBelongsToField(OrderFields.PRODUCTION_LINE)).thenReturn(prodLine);
        EntityList tasks = mockEntityList(Lists.newArrayList(task1));

        when(result.getEntities()).thenReturn(tasks);
        // when
        hooksOTFO.changedProductionLine(dataDefinition, entity);
        // then

        Mockito.verify(task1).setField(OrderFields.PRODUCTION_LINE, prodLine);
    }
}
