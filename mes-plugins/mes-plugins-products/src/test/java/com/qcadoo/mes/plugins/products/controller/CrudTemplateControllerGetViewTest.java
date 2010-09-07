package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FormDefinition;
import com.qcadoo.mes.core.data.definition.GridDefinition;
import com.qcadoo.mes.core.data.definition.ViewDefinition;
import com.qcadoo.mes.core.data.definition.ViewElementDefinition;
import com.qcadoo.mes.plugins.products.mock.MessageSourceMock;
import com.qcadoo.mes.plugins.products.translation.TranslationServiceImpl;

public class CrudTemplateControllerGetViewTest {

    private CrudController controller;

    private ViewDefinitionService vdsMock;

    private ViewDefinition viewDefinition;

    @Before
    public void init() {
        controller = new CrudController();

        vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);

        TranslationServiceImpl translationService = new TranslationServiceImpl();
        ReflectionTestUtils.setField(translationService, "messageSource", new MessageSourceMock());
        ReflectionTestUtils.setField(controller, "translationService", translationService);

        viewDefinition = new ViewDefinition("testView");
        ViewElementDefinition viewElementDefinition1 = new FormDefinition("testForm", new DataDefinition("testEntity1"));
        GridDefinition viewElementDefinition2 = new GridDefinition("testGrid", new DataDefinition("testEntity2"));
        viewElementDefinition2.setColumns(new LinkedList<ColumnDefinition>());
        viewDefinition.setElements(Arrays.asList(new ViewElementDefinition[] { viewElementDefinition1, viewElementDefinition2 }));

        given(vdsMock.getViewDefinition("testView")).willReturn(viewDefinition);
    }

    @Test
    public void shouldReturnValidView() {
        // given
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("entityId", "testEntityId");
        arguments.put("contextEntityId", "testContextEntityId");

        // when
        ModelAndView mav = controller.getView("testView", arguments, null);

        // then
        assertEquals("crudView", mav.getViewName());
        assertEquals(viewDefinition, mav.getModel().get("viewDefinition"));

        @SuppressWarnings("unchecked")
        Map<String, String> optionsMap = (Map<String, String>) mav.getModel().get("viewElementsOptions");
        assertEquals(2, optionsMap.size());

        // TODO mina add test for dictionary values

        assertEquals("testEntityId", mav.getModel().get("entityId"));
        assertEquals("testContextEntityId", mav.getModel().get("contextEntityId"));
    }
}
