package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.PositionDtoFields;
import com.qcadoo.mes.materialFlowResources.constants.RepackingPositionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

@Service
public class RepackingPositionsListListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    public void filterRepackingPositionsByFromResource(ViewDefinitionState view, final ComponentState state,
                                                        final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Entity repackingPosition = grid.getSelectedEntities().get(0);
        String resourceNumber = repackingPosition.getStringField(RepackingPositionFields.RESOURCE_NUMBER);
        Map<String, String> filters = Maps.newHashMap();
        filters.put(RepackingPositionFields.CREATED_RESOURCE_NUMBER, applyInOperator(resourceNumber));
        grid.setSelectedEntitiesIds(new HashSet<>());
        grid.setFilters(filters);
    }

    public void showDocumentPositionsWithFromResource(final ViewDefinitionState view, final ComponentState state,
                                                      final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity repackingPosition = grid.getSelectedEntities().get(0);
        String resourceNumber = repackingPosition.getStringField(RepackingPositionFields.RESOURCE_NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put(PositionDtoFields.RESOURCE_NUMBER, applyInOperator(resourceNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "materialFlow.documentPositions");

        String url = "../page/materialFlowResources/documentPositionsList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void showDocumentPositionsWithToResource(final ViewDefinitionState view, final ComponentState state,
                                                    final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Entity repackingPosition = grid.getSelectedEntities().get(0);
        String resourceNumber = repackingPosition.getStringField(RepackingPositionFields.CREATED_RESOURCE_NUMBER);

        Map<String, String> filters = Maps.newHashMap();
        filters.put(PositionDtoFields.RESOURCE_NUMBER, applyInOperator(resourceNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "materialFlow.documentPositions");

        String url = "../page/materialFlowResources/documentPositionsList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        return "[" + value + "]";
    }

}
