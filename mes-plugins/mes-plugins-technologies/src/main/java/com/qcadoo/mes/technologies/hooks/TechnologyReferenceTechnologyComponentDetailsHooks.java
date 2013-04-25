package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyReferenceTechnologyComponentDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_REFERENCE_MODE = "referenceMode";

    public void hideReferenceMode(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            ComponentState referenceModeComponent = viewDefinitionState.getComponentByReference(L_REFERENCE_MODE);
            referenceModeComponent.setFieldValue("01reference");
            referenceModeComponent.setVisible(false);
        }
    }

    public void disabledSaveBackButton(final ViewDefinitionState viewDefinitionState) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonGroup actionsGroup = (RibbonGroup) window.getRibbon().getGroupByName("actions");
        RibbonActionItem saveBack = (RibbonActionItem) actionsGroup.getItemByName("saveBack");

        LookupComponent technology = (LookupComponent) viewDefinitionState.getComponentByReference("technology");
        if (technology.getEntity() == null) {
            saveBack.setEnabled(false);
        } else {
            saveBack.setEnabled(true);
        }
        saveBack.requestUpdate(true);
    }
}
