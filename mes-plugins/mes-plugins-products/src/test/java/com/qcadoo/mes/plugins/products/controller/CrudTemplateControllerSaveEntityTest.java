package com.qcadoo.mes.plugins.products.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.plugins.products.data.mock.DataDefinitionServiceMock;
import com.qcadoo.mes.plugins.products.validation.ValidationResult;
import com.qcadoo.mes.plugins.products.validation.ValidationService;

public class CrudTemplateControllerSaveEntityTest {

    private CRUD controller;

    private DataDefinitionService dds;

    private DataAccessService das;

    private ValidationService vs;

    @Before
    public void init() {
        dds = new DataDefinitionServiceMock();
        das = mock(DataAccessService.class);
        vs = mock(ValidationService.class);
        controller = new CRUD();
        ReflectionTestUtils.setField(controller, "dataAccessService", das);
        ReflectionTestUtils.setField(controller, "dataDefinitionService", dds);
        ReflectionTestUtils.setField(controller, "validationUtils", vs);

    }

    @Test
    public void shouldPerformSaveWhenEntityIsValid() {
        // given
        Entity entity = new Entity();
        Entity validEntity = new Entity();

        ValidationResult vr = new ValidationResult();
        vr.setValid(true);
        vr.setValidEntity(validEntity);
        given(vs.validateEntity(eq(entity), anyList())).willReturn(vr);

        // when
        ValidationResult result = controller.saveEntity(entity, "testType", null);

        // then
        assertEquals(vr, result);
        verify(das).save("testType", validEntity);
        verifyNoMoreInteractions(das);
    }

    @Test
    public void shouldRedirectToFormIfRequiredDataIsIncompleteAndOnNewForm() {
        // given
        Entity entity = new Entity((long) 1);

        ValidationResult vr = new ValidationResult();
        vr.setValid(false);
        given(vs.validateEntity(eq(entity), anyList())).willReturn(vr);

        // when
        ValidationResult result = controller.saveEntity(entity, "testType", null);

        // then
        assertEquals(vr, result);
        assertNull(vr.getValidEntity());

        verifyNoMoreInteractions(das);
    }

    private class CRUD extends CrudTemplate {

        public CRUD() {
            super(LoggerFactory.getLogger(CrudTemplateControllerSaveEntityTest.class));
        }
    }
}