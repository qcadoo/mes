package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductDetailsHooks {

    private static final String L_FORM = "form";

    public void updateRibbonState(final ViewDefinitionState view) {

        FormComponent operationGroupForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity operationGroup = operationGroupForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup operationGroups = (RibbonGroup) window.getRibbon().getGroupByName("conversions");

        RibbonActionItem getDefaultConversions = (RibbonActionItem) operationGroups.getItemByName("getDefaultConversions");

        if (operationGroup.getId() != null) {

            updateButtonState(getDefaultConversions, true);

        } else {

            updateButtonState(getDefaultConversions, false);

        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
