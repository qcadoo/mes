package com.qcadoo.mes.genealogies;

import static org.mockito.Mockito.mock;

import java.util.Locale;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.crud.CrudController;

public class GenealogyControllerTest {

    @Test
    public void shouldPrepareViewForGenealogyAttributes() throws Exception {
        // given
        CrudController crudController = mock(CrudController.class);
        GenealogyAttributeService genealogyAttributeService = mock(GenealogyAttributeService.class);
        GenealogyController genealogyController = new GenealogyController();
        ReflectionTestUtils.setField(genealogyController, "crudController", crudController);
        ReflectionTestUtils.setField(genealogyController, "genealogyAttributeService", genealogyAttributeService);

        // when
        genealogyController.getGenealogyAttributesPageView(Locale.ENGLISH);

        // then
    }
}
