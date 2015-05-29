package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductsListListenersMFR {

    public void showCostNormsGenerator(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        String url = "../page/materialFlowResources/costNormsGenerator.html";
        view.redirectTo(url, false, true);
    }
}
