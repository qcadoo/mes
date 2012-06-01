package com.qcadoo.mes.productionPerShift.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.mockito.Mock;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ProductionPerShiftDetailsHooksTest {

    @Mock
    private ViewDefinitionState viewState;

    @Mock
    private ComponentState componentState, lookup, producesInput;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition ddTIOC;

    @Mock
    private Entity tioc, toc, prodComp, prod;

    @Mock
    private TechnologyService technologyService;

    // @Test
    public void shouldFillProducesFieldAfterSelection() {
        // given
        Long id = 3L;
        String prodName = "asdf";

        given(viewState.getComponentByReference("productionPerShiftOperation")).willReturn(lookup);
        given(viewState.getComponentByReference("produces")).willReturn(producesInput);

        given(lookup.getFieldValue()).willReturn(id);
        given(dataDefinitionService.get("technologies", "technologyInstanceOperationComponent")).willReturn(ddTIOC);
        given(ddTIOC.get(id)).willReturn(tioc);

        given(tioc.getBelongsToField("technologyOperationComponent")).willReturn(toc);
        given(technologyService.getMainOutputProductComponent(toc)).willReturn(prodComp);
        given(prodComp.getBelongsToField("product")).willReturn(prod);
        given(prod.getStringField("name")).willReturn(prodName);

        // when
        // productionPerShiftListeners.fillProducedField(viewState, componentState, null);

        // then
        verify(producesInput).setFieldValue(prodName);
    }
}
