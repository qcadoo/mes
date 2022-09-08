package com.qcadoo.mes.technologies.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentDtoFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologiesWithUsingOperationListListeners {

    public void changeParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> selectedEntities = grid.getSelectedEntities().stream()
                .map(e -> e.getIntegerField(TechnologyOperationComponentDtoFields.TECHNOLOGY_ID).longValue()).collect(Collectors.toSet());
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("selectedEntities", selectedEntities);
        JSONObject context = new JSONObject(parameters);
        String url = "../page/technologies/changeTechnologyParameters.html?context=" + context;

        view.openModal(url);
    }
}
