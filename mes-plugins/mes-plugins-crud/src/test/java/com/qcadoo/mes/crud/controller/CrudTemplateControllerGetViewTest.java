package com.qcadoo.mes.crud.controller;

import org.junit.Before;
import org.junit.Test;

public class CrudTemplateControllerGetViewTest {

    @Before
    public void init() {
        // controller = new CrudController();
        //
        // vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
        // ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);
        //
        // TranslationServiceImpl translationService = new TranslationServiceImpl();
        // ReflectionTestUtils.setField(translationService, "messageSource", new MessageSourceMock());
        // ReflectionTestUtils.setField(controller, "translationService", translationService);
        //
        // viewDefinition = new ViewDefinition("testView", "tests");
        // ComponentDefinition viewElementDefinition1 = new FormDefinition("testForm", new DataDefinition("testEntity1"));
        // GridDefinition viewElementDefinition2 = new GridDefinition("testGrid", new DataDefinition("testEntity2"));
        // viewElementDefinition2.setColumns(new LinkedList<ColumnDefinition>());
        // viewDefinition.setElements(Arrays.asList(new ComponentDefinition[] { viewElementDefinition1, viewElementDefinition2
        // }));
        //
        // given(vdsMock.getViewDefinition("testView")).willReturn(viewDefinition);
    }

    @Test
    public void test() {

    }

    // private CrudController controller;
    //
    // private ViewDefinitionService vdsMock;
    //
    // private ViewDefinition viewDefinition;
    //
    // @Before
    // public void init() {
    // controller = new CrudController();
    //
    // vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
    // ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);
    //
    // TranslationServiceImpl translationService = new TranslationServiceImpl();
    // ReflectionTestUtils.setField(translationService, "messageSource", new MessageSourceMock());
    // ReflectionTestUtils.setField(controller, "translationService", translationService);
    //
    // viewDefinition = new ViewDefinition("testView");
    // ComponentDefinition viewElementDefinition1 = new FormDefinition("testForm", new DataDefinition("testEntity1"));
    // GridDefinition viewElementDefinition2 = new GridDefinition("testGrid", new DataDefinition("testEntity2"));
    // viewElementDefinition2.setColumns(new LinkedList<ColumnDefinition>());
    // viewDefinition.setElements(Arrays.asList(new ComponentDefinition[] { viewElementDefinition1, viewElementDefinition2 }));
    //
    // given(vdsMock.getViewDefinition("testView")).willReturn(viewDefinition);
    // }
    //
    // @Test
    // public void shouldReturnValidView() {
    // // given
    // Map<String, String> arguments = new HashMap<String, String>();
    // arguments.put("entityId", "testEntityId");
    // arguments.put("contextEntityId", "testContextEntityId");
    //
    // // when
    // ModelAndView mav = controller.getView("testView", arguments, null);
    //
    // // then
    // assertEquals("crudView", mav.getViewName());
    // assertEquals(viewDefinition, mav.getModel().get("viewDefinition"));
    //
    // @SuppressWarnings("unchecked")
    // Map<String, String> optionsMap = (Map<String, String>) mav.getModel().get("viewElementsOptions");
    // assertEquals(2, optionsMap.size());
    //
    // // TODO mina add test for dictionary values
    //
    // assertEquals("testEntityId", mav.getModel().get("entityId"));
    // assertEquals("testContextEntityId", mav.getModel().get("contextEntityId"));
    // }
}
