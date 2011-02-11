package com.qcadoo.mes.genealogies;

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

public class GenealogyControllerTest {

    @Test
    public void shouldPrepareViewForGenealogyAttributes() throws Exception {
        // // given
        Map<String, String> arguments = ImmutableMap.of("context", "{\"window.currentAttribute.id\":\"13\"}");
        ModelAndView expectedMav = mock(ModelAndView.class);
        CrudController crudController = mock(CrudController.class);
        given(crudController.prepareView("genealogies", "currentAttribute", arguments, Locale.ENGLISH)).willReturn(expectedMav);

        GenealogyAttributeService genealogyAttributeService = mock(GenealogyAttributeService.class);
        given(genealogyAttributeService.getGenealogyAttributeId()).willReturn(13L);

        GenealogyController genealogyController = new GenealogyController();
        setField(genealogyController, "crudController", crudController);
        setField(genealogyController, "genealogyService", genealogyAttributeService);

        // // when
        ModelAndView mav = genealogyController.getGenealogyAttributesPageView(Locale.ENGLISH);

        // // then
        assertEquals(expectedMav, mav);
    }

    @Test
    public void shouldPrepareViewForComponentPdf() throws Exception {
        // given
        GenealogyController genealogyController = new GenealogyController();

        // when
        ModelAndView mav = genealogyController.genealogyForComponentPdf("v13");

        // then
        assertEquals("genealogyForComponentView", mav.getViewName());
        assertEquals("v13", mav.getModel().get("value"));
    }

    @Test
    public void shouldPrepareViewForProductPdf() throws Exception {
        // given
        GenealogyController genealogyController = new GenealogyController();

        // when
        ModelAndView mav = genealogyController.genealogyForProductPdf("v13");

        // then
        assertEquals("genealogyForProductView", mav.getViewName());
        assertEquals("v13", mav.getModel().get("value"));
    }
}
