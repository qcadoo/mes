package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class TechnologiesListListeners {
    private static final String L_GRID = "grid";

    public void changeParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        Set<Long> selectedEntities = grid.getSelectedEntitiesIds();
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("selectedEntities", selectedEntities);
        JSONObject context = new JSONObject(parameters);
        StringBuilder url = new StringBuilder("../page/technologies/changeTechnologyParameters.html");
        url.append("?context=");
        url.append(context.toString());

        view.openModal(url.toString());
    }

}
