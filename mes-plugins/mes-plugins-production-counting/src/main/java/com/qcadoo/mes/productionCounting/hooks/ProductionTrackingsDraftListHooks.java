package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductionTrackingsDraftListHooks {

    public void onBeforeRender(final ViewDefinitionState view) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Map<String, String> filters = gridComponent.getFilters();
        filters.put("state", "01draft");
        gridComponent.setFilters(filters);
    }
}
