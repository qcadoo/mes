package com.qcadoo.mes.productionCounting.listeners;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionCounting.constants.PerformanceAnalysisDetailsDtoFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionAnalysisListeners {

    private static final String L_GRID = "grid";

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    public void calculateTotalQuantities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        System.out.println("Ohh yeah!");
    }
}
