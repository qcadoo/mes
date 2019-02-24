package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ProductionTrackingsDraftListHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference("grid");
        Map<String, String> filters = gridComponent.getFilters();
        filters.put("state", "01draft");
        gridComponent.setFilters(filters);
    }
}
