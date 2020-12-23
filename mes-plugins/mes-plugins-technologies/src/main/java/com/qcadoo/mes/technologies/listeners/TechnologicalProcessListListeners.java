package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.qcadoo.mes.technologies.constants.TechnologicalProcessListFields.TECHNOLOGICAL_PROCESS_COMPONENTS;

@Service
public class TechnologicalProcessListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private LookupUtils lookupUtils;

    public void onAddExistingEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (args.length < 1) {
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity technologicalProcessList = form.getEntity();
        List<Long> technologicalProcessIds = lookupUtils.parseIds(args[0]);
        for (Long technologicalProcessId : technologicalProcessIds) {
            Entity technologicalProcess = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS)
                    .get(technologicalProcessId);
            Entity technologicalProcessComponent = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_COMPONENT)
                    .create();
            technologicalProcessComponent.setField(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS, technologicalProcess);
            technologicalProcessComponent.setField(TechnologiesConstants.MODEL_TECHNOLOGICAL_PROCESS_LIST,
                    technologicalProcessList);
            technologicalProcessComponent.setField(TechnologicalProcessFields.TJ,
                    technologicalProcess.getField(TechnologicalProcessFields.TJ));
            technologicalProcessComponent.setField(TechnologicalProcessFields.EXTENDED_TIME_FOR_SIZE_GROUP,
                    technologicalProcess.getField(TechnologicalProcessFields.EXTENDED_TIME_FOR_SIZE_GROUP));
            technologicalProcessComponent.setField(TechnologicalProcessFields.INCREASE_PERCENT,
                    technologicalProcess.getField(TechnologicalProcessFields.INCREASE_PERCENT));
            technologicalProcessComponent.setField(TechnologicalProcessFields.SIZE_GROUP,
                    technologicalProcess.getField(TechnologicalProcessFields.SIZE_GROUP));
            technologicalProcessComponent.getDataDefinition().save(technologicalProcessComponent);
        }
        technologicalProcessList = form.getPersistedEntityWithIncludedFormValues();
        GridComponent technologicalProcessComponents = (GridComponent) view
                .getComponentByReference(TECHNOLOGICAL_PROCESS_COMPONENTS);
        technologicalProcessComponents.setEntities(technologicalProcessList.getHasManyField(TECHNOLOGICAL_PROCESS_COMPONENTS));
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
