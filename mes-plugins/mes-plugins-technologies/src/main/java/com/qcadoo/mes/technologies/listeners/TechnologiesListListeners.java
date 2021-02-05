package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TechnologiesListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setAsDefault(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            trySetAsDefault(view);
        } catch (EntityRuntimeException exc) {
            view.addMessage("technologies.setAsDefault.error.errorWhenSetAsDefault",
                    ComponentState.MessageType.FAILURE, exc.getEntity().getStringField(TechnologyFields.NUMBER));
        }
    }

    @Transactional
    public void trySetAsDefault(ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> selectedEntities = grid.getSelectedEntitiesIds();
        selectedEntities.forEach(techId -> {
            Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(techId);

            technology.setField(TechnologyFields.MASTER, Boolean.TRUE);
            technology = technology.getDataDefinition().save(technology);
            if (!technology.isValid()) {
                throw new EntityRuntimeException(technology);
            }
        });
    }

    public void changeParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
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
