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
public class PerformanceAnalysisListeners {

    private static final String L_GRID = "grid";

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    public void showDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent performanceAnalysisGrid = (GridComponent) view.getComponentByReference(L_GRID);

        Entity analysis = performanceAnalysisGrid.getSelectedEntities().get(0);

        StringBuilder staffNameBuilder = new StringBuilder();

        staffNameBuilder.append("[");
        staffNameBuilder.append(analysis.getStringField(PerformanceAnalysisDetailsDtoFields.STAFF_NAME));
        staffNameBuilder.append("]");

        String staffName = staffNameBuilder.toString();

        StringBuilder productionLineNumberBuilder = new StringBuilder();

        productionLineNumberBuilder.append("[");
        productionLineNumberBuilder.append(analysis.getStringField(PerformanceAnalysisDetailsDtoFields.PRODUCTION_LINE_NUMBER));
        productionLineNumberBuilder.append("]");

        String productionLineNumber = productionLineNumberBuilder.toString();

        StringBuilder shiftNameBuilder = new StringBuilder();

        shiftNameBuilder.append("[");
        shiftNameBuilder.append(analysis.getStringField(PerformanceAnalysisDetailsDtoFields.SHIFT_NAME));
        shiftNameBuilder.append("]");

        String shiftName = shiftNameBuilder.toString();

        Map<String, String> filters = Maps.newHashMap();
        filters.put("staffName", staffName);
        filters.put("productionLineNumber", productionLineNumber);
        filters.put("shiftName", shiftName);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "analysis.performanceAnalysis");

        String url = "../page/productionCounting/performanceAnalysisDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void calculateTotalTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }
}
