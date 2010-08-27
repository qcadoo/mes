package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;

public class CrudTemplateControllerGetEntityFormViewTest {

    private CRUD controller;

    private DataDefinitionService dds;

    private DataAccessService das;

    @Before
    public void setUp() {
        dds = new DataDefinitionServiceMock();
        das = mock(DataAccessService.class);
        controller = new CRUD(dds, das);
    }

    @Test
    public void shouldReturnValidViewWhenNoEntity() {
        // given

        // when
        ModelAndView modelAndView = controller.getEntityFormView("testView", null, "testType", null, null);

        // then
        assertEquals("testView", modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("entity"));
        assertEquals("testType", modelAndView.getModel().get("entityType"));

        assertNotNull(modelAndView.getModel().get("entityFieldsDefinition"));
        assertNotNull(modelAndView.getModel().get("dictionaryValues"));
    }

    @Test
    public void shouldReturnValidViewWhenExistingEntity() {
        // given
        Entity existingEntity = new Entity();
        given(das.get("testType", (long) 12)).willReturn(existingEntity);

        // when
        ModelAndView modelAndView = controller.getEntityFormView("testView", (long) 12, "testType", null, null);

        // then
        assertEquals("testView", modelAndView.getViewName());
        assertEquals(existingEntity, modelAndView.getModel().get("entity"));
        assertEquals("testType", modelAndView.getModel().get("entityType"));

        assertNotNull(modelAndView.getModel().get("entityFieldsDefinition"));
        assertNotNull(modelAndView.getModel().get("dictionaryValues"));

        verify(das).get("testType", (long) 12);

        verifyNoMoreInteractions(das);
    }

    @Test
    public void shouldInsertParentIdWhenNoEntity() {
        // given

        // when
        ModelAndView modelAndView = controller.getEntityFormView("testView", null, "testType", (long) 666, "testParentField");

        // then
        assertEquals("testView", modelAndView.getViewName());
        assertEquals("testType", modelAndView.getModel().get("entityType"));

        assertNotNull(modelAndView.getModel().get("entityFieldsDefinition"));
        assertNotNull(modelAndView.getModel().get("dictionaryValues"));

        Entity generatedEntity = (Entity) modelAndView.getModel().get("entity");
        assertNotNull(generatedEntity);
        assertNull(generatedEntity.getId());
        assertEquals(1, generatedEntity.getFields().size());
        assertEquals(new Long(666), ((Entity) generatedEntity.getField("testParentField")).getId());
    }

    // TODO mina Add test for fields definition
    // TODO mina Add test for dictionary values

    private class CRUD extends CrudTemplate {

        public CRUD(DataDefinitionService dds, DataAccessService das) {
            super(dds, das, LoggerFactory.getLogger(CrudTemplateControllerGetEntityFormViewTest.class), null);
        }
    }

}
