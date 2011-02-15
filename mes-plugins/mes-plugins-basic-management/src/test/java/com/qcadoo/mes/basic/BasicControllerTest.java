package com.qcadoo.mes.basic;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.crud.CrudController;

public class BasicControllerTest {

    @Test
    public void shouldPrepareViewForParameters() throws Exception {
        // // given
        Map<String, String> arguments = ImmutableMap.of("context", "{\"window.parameter.id\":\"13\"}");
        ModelAndView expectedMav = mock(ModelAndView.class);
        CrudController crudController = mock(CrudController.class);
        given(crudController.prepareView("basic", "parameter", arguments, Locale.ENGLISH)).willReturn(expectedMav);

        ParameterService parameterService = mock(ParameterService.class);
        given(parameterService.getParameterId()).willReturn(13L);

        BasicController basicController = new BasicController();
        setField(basicController, "crudController", crudController);
        setField(basicController, "parameterService", parameterService);

        // // when
        ModelAndView mav = basicController.getParameterPageView(Locale.ENGLISH);

        // // then
        assertEquals(expectedMav, mav);
    }

}
