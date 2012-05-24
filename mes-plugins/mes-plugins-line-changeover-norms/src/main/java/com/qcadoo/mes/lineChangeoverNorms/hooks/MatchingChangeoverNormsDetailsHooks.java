package com.qcadoo.mes.lineChangeoverNorms.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MatchingChangeoverNormsDetailsHooks {

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
}
