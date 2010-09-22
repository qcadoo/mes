package com.qcadoo.mes.crud.controller;

import org.junit.Test;

public class CrudTemplateControllerGetGridDataTest {

    @Test
    public void test() {

    }

    // private CrudController controller;
    //
    // private DataAccessService dasMock;
    //
    // private GridDefinition gridDefinition;
    //
    // private DataDefinition gridDataDefinition;
    //
    // @Before
    // public void init() {
    // controller = new CrudController();
    //
    // dasMock = mock(DataAccessService.class, RETURNS_DEEP_STUBS);
    // ReflectionTestUtils.setField(controller, "dataAccessService", dasMock);
    //
    // ViewDefinitionService vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
    // ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);
    //
    // gridDataDefinition = new DataDefinition("testEntity");
    // gridDataDefinition.addField(new DataFieldDefinition("parentEntity"));
    //
    // gridDefinition = new GridDefinition("testGrid", gridDataDefinition);
    // gridDefinition.setColumns(new LinkedList<ColumnDefinition>());
    // gridDefinition.setParent("entityId");
    // gridDefinition.setParentField("parentEntity");
    //
    // given(vdsMock.getViewDefinition("testView").getElementByName("testViewElement")).willReturn(gridDefinition);
    // }
    //
    // @Test
    // public void shouldReturnValidDataWhenNoOrderAndNoFilterAndNoPagingAndNoParent() {
    // // given
    // Map<String, String> arguments = new HashMap<String, String>();
    // SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity(gridDataDefinition).build();
    //
    // SearchResultImpl searchResult = new SearchResultImpl();
    // searchResult.setResults(new LinkedList<Entity>());
    // searchResult.setTotalNumberOfEntities(30);
    //
    // given(dasMock.find(searchCriteria)).willReturn(searchResult);
    // // when
    // ListData result = controller.getGridData("testView", "testViewElement", arguments);
    //
    // // then
    // assertEquals(30, result.getTotalNumberOfEntities());
    // }
    //
    // @Test
    // public void shouldReturnValidDataWhenOrderAndFilterAndPagingAndParent() {
    // // given
    // Map<String, String> arguments = new HashMap<String, String>();
    // arguments.put("entityId", "123");
    // arguments.put("maxResults", "11");
    // arguments.put("firstResult", "21");
    // arguments.put("sortField", "testCol");
    // arguments.put("sortOrder", "desc");
    //
    // DataFieldDefinition parentField = gridDataDefinition.getField("parentEntity");
    // Long parentId = new Long(123);
    //
    // // TODO mina test for filtering
    //
    // SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity(gridDataDefinition)
    // .restrictedWith(Restrictions.belongsTo(parentField, parentId)).withMaxResults(11).withFirstResult(21)
    // .orderBy(Order.desc("testCol")).build();
    //
    // SearchResultImpl searchResult = new SearchResultImpl();
    // searchResult.setResults(new LinkedList<Entity>());
    // searchResult.setTotalNumberOfEntities(30);
    //
    // given(dasMock.find(searchCriteria)).willReturn(searchResult);
    //
    // // when
    // ListData result = controller.getGridData("testView", "testViewElement", arguments);
    //
    // // then
    // assertEquals(30, result.getTotalNumberOfEntities());
    // }

}
