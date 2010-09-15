package com.qcadoo.mes.crud.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.validation.ValidationResults;
import com.qcadoo.mes.crud.controller.CrudController;
import com.qcadoo.mes.crud.mock.MessageSourceMock;
import com.qcadoo.mes.crud.translation.TranslationServiceImpl;

public class CrudTemplateControllerSaveEntityTest {

    private CrudController controller;

    private DataAccessService dasMock;

    private ViewDefinitionService vdsMock;

    private DataDefinition dataDefinition;

    @Before
    public void init() {
        controller = new CrudController();
        dasMock = mock(DataAccessService.class);
        ReflectionTestUtils.setField(controller, "dataAccessService", dasMock);

        vdsMock = mock(ViewDefinitionService.class, RETURNS_DEEP_STUBS);
        ReflectionTestUtils.setField(controller, "viewDefinitionService", vdsMock);

        TranslationServiceImpl translationService = new TranslationServiceImpl();
        ReflectionTestUtils.setField(translationService, "messageSource", new MessageSourceMock());
        ReflectionTestUtils.setField(controller, "translationService", translationService);

        DataFieldDefinition f1 = mock(DataFieldDefinition.class);
        given(f1.getName()).willReturn("testField1");
        given(f1.getValue("testField1Val")).willReturn("testField1Ok");

        dataDefinition = new DataDefinition("testEntityType");
        dataDefinition.addField(f1);

        given(vdsMock.getViewDefinition("testView").getElementByName("testViewElement").getDataDefinition()).willReturn(
                dataDefinition);
    }

    @Test
    public void shouldPerformSaveEntity() {
        // given
        Entity entity = new Entity((long) 11);
        entity.setField("testField1", "testField1Val");

        ValidationResults valRes = new ValidationResults();
        valRes.setEntity(entity);

        given(dasMock.save(dataDefinition, entity)).willReturn(valRes);

        // when
        ValidationResults vr = controller.saveEntity("testView", "testViewElement", entity, null);

        // then
        assertEquals(new Long(11), vr.getEntity().getId());
        assertEquals(1, vr.getEntity().getFields().size());
        assertEquals("testField1Ok", vr.getEntity().getField("testField1"));
    }
}