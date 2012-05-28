package com.qcadoo.mes.lineChangeoverNorms.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.lineChangeoverNorms.listeners.MatchingChangeoverNormsDetailsListeners;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MatchingChangeoverNormsDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MatchingChangeoverNormsDetailsListeners listeners;

    public void invisibleField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        ComponentState matchingNorm = view.getComponentByReference("matchingNorm");
        ComponentState matchingNormNotFound = view.getComponentByReference("matchingNormNotFound");
        if (form.getEntityId() == null) {
            matchingNorm.setVisible(false);
            matchingNormNotFound.setVisible(true);
        } else {
            matchingNorm.setVisible(true);
            matchingNormNotFound.setVisible(false);
        }
    }

    public void fillOrCleanFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            listeners.clearField(view);
            listeners.changeStateEditButton(view, false);
        } else {
            Entity changeover = dataDefinitionService.get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER,
                    LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS).get(form.getEntityId());
            listeners.fillField(view, changeover);
            listeners.changeStateEditButton(view, true);
        }
    }
}
