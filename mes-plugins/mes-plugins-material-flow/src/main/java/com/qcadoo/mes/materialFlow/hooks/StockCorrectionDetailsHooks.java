package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Component
public class StockCorrectionDetailsHooks {

    @Autowired
    private MaterialFlowService materialFlowService;

    public void checkIfLocationHasExternalNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        checkIfLocationHasExternalNumber(view);
    }

    public void checkIfLocationHasExternalNumber(final ViewDefinitionState view) {
        materialFlowService.checkIfLocationHasExternalNumber(view, LOCATION);
    }

}
