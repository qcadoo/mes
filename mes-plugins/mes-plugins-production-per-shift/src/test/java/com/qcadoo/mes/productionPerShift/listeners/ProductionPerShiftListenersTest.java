package com.qcadoo.mes.productionPerShift.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ProductionPerShiftListenersTest {

    private ProductionPerShiftListeners productionPerShiftListeners;

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

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionPerShiftListeners = new ProductionPerShiftListeners();

        ReflectionTestUtils.setField(productionPerShiftListeners, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(productionPerShiftListeners, "technologyService", technologyService);
    }

    @Test
    public void shouldRedirectToTheProductionPerShiftView() {
        // given
        Long id = 5l;
        given(componentState.getFieldValue()).willReturn(id);
        String url = "../page/productionPerShift/productionPerShiftView.html";

        // when
        productionPerShiftListeners.redirectToProductionPerShift(viewState, componentState, null);

        // then
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("order.id", id);

        verify(viewState).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldFillProducesFieldAfterSelection() {
        // given
        Long id = 3l;
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
        productionPerShiftListeners.fillProducedField(viewState, componentState, null);

        // then
        verify(producesInput).setFieldValue(prodName);
    }
}
