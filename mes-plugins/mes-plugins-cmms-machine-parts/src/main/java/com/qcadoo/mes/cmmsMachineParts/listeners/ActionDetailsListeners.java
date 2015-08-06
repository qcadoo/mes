package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.ActionAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.hooks.ActionDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ActionDetailsListeners {

    @Autowired
    private ActionDetailsHooks actionDetailsHooks;

    private static final String L_FORM = "form";

    public void toggleAndClearGrids(final ViewDefinitionState view, final ComponentState state, final String args[]) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity action = form.getPersistedEntityWithIncludedFormValues();
        ActionAppliesTo appliesTo = ActionAppliesTo.from(action);
        actionDetailsHooks.toggleGridsEnable(view, appliesTo, true);
    }
}
