package com.qcadoo.mes.productionCounting.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionTrackingsListListeners {

    private static final String L_GRID = "grid";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    public void correct(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {
        GridComponent grid = (GridComponent) state.getComponentByReference(L_GRID);
        List<Entity> selected = grid.getSelectedEntities();
        Entity productionTracking = selected.get(0);
        DataDefinition productionTrackingDD = getDataDefinition();
        productionTracking = productionTrackingDD.get(productionTracking.getId());

        productionTrackingService.correct(productionTracking);
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING);
    }
}
