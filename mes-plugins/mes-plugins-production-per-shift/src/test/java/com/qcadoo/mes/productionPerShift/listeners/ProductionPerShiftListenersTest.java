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
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ProductionPerShiftListenersTest {

    private ProductionPerShiftListeners productionPerShiftListeners;

    @Mock
    private ViewDefinitionState viewState;

    @Mock
    private ComponentState componentState;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionPerShiftListeners = new ProductionPerShiftListeners();

        ReflectionTestUtils.setField(productionPerShiftListeners, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldRedirectToTheProductionPerShiftView() {
        // given
        Long id = 5L;
        given(componentState.getFieldValue()).willReturn(id);
        String url = "../page/productionPerShift/productionPerShiftDetails.html";

        // when
        productionPerShiftListeners.redirect(viewState, id);

        // then
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", id);

        verify(viewState).redirectTo(url, false, true, parameters);
    }

}
