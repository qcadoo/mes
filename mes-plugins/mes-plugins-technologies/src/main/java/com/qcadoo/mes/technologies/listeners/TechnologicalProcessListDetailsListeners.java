package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.qcadoo.mes.technologies.constants.TechnologicalProcessListFields.TECHNOLOGICAL_PROCESS_COMPONENTS;

@Service
public class TechnologicalProcessListDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addProcesses(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", formComponent.getEntityId());

        String url = "../page/technologies/addProcesses.html";
        view.openModal(url, parameters);
    }

    public void onRemoveSelectedEntities(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent technologicalProcessComponents = (GridComponent) view
                .getComponentByReference(TECHNOLOGICAL_PROCESS_COMPONENTS);
        Set<Long> selectedComponents = technologicalProcessComponents.getSelectedEntitiesIds();
        DataDefinition dataDefinition = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT);
        dataDefinition.delete(selectedComponents.toArray(new Long[0]));
    }
}
