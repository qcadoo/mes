package com.qcadoo.mes.plugins.crud.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.grid.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.grid.GridDefinition;
import com.qcadoo.mes.core.data.internal.search.SearchResultImpl;
import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.plugins.crud.data.ListData;

public class CrudTemplateControllerGetGridDataTest {

    private CrudController controller;

    private DataAccessService dasMock;

    private GridDefinition gridDefinition;

    private DataDefinition gridDataDefinition;

    @Before
    public void init() {
        controller = new CrudController();

        dasMock = mock(DataAccessService.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(controller, "dataAccessService", dasMock);

        ViewDefinitionService vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);

        gridDataDefinition = new DataDefinition("testEntity");
        gridDataDefinition.addField(new DataFieldDefinition("parentEntity"));

        gridDefinition = new GridDefinition("testGrid", gridDataDefinition);
        gridDefinition.setColumns(new LinkedList<ColumnDefinition>());
        gridDefinition.setParent("entityId");
        gridDefinition.setParentField("parentEntity");

        given(vdsMock.getViewDefinition("testView").getElementByName("testViewElement")).willReturn(gridDefinition);
    }

    @Test
    public void shouldReturnValidDataWhenNoOrderAndNoFilterAndNoPagingAndNoParent() {
        // given
        Map<String, String> arguments = new HashMap<String, String>();
        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity(gridDataDefinition).build();

        SearchResultImpl searchResult = new SearchResultImpl();
        searchResult.setResults(new LinkedList<Entity>());
        searchResult.setTotalNumberOfEntities(30);

        given(dasMock.find(searchCriteria)).willReturn(searchResult);
        // when
        ListData result = controller.getGridData("testView", "testViewElement", arguments);

        // then
        assertEquals(30, result.getTotalNumberOfEntities());
    }

    @Test
    public void shouldReturnValidDataWhenOrderAndFilterAndPagingAndParent() {
        // given
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("entityId", "123");
        arguments.put("maxResults", "11");
        arguments.put("firstResult", "21");
        arguments.put("sortField", "testCol");
        arguments.put("sortOrder", "desc");

        DataFieldDefinition parentField = gridDataDefinition.getField("parentEntity");
        Long parentId = new Long(123);

        // TODO mina test for filtering

        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity(gridDataDefinition)
                .restrictedWith(Restrictions.belongsTo(parentField, parentId)).withMaxResults(11).withFirstResult(21)
                .orderBy(Order.desc("testCol")).build();

        SearchResultImpl searchResult = new SearchResultImpl();
        searchResult.setResults(new LinkedList<Entity>());
        searchResult.setTotalNumberOfEntities(30);

        given(dasMock.find(searchCriteria)).willReturn(searchResult);

        // when
        ListData result = controller.getGridData("testView", "testViewElement", arguments);

        // then
        assertEquals(30, result.getTotalNumberOfEntities());
    }

}
