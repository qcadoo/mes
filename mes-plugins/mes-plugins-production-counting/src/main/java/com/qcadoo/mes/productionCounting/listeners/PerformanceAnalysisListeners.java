package com.qcadoo.mes.productionCounting.listeners;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class PerformanceAnalysisListeners {

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    public void showDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        Map<String, String> filters = Maps.newHashMap();

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        String url = "../page/productionCounting/performanceAnalysisDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void calculateTotalTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }
}
