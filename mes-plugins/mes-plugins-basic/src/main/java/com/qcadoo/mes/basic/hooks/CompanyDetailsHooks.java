package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class CompanyDetailsHooks {

    private static final String L_FORM = "form";

    public void updateRibbonState(final ViewDefinitionState view) {

        FormComponent productionOrderGroup = (FormComponent) view.getComponentByReference(L_FORM);

        Entity productionOrder = productionOrderGroup.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup productionOrderGroups = (RibbonGroup) window.getRibbon().getGroupByName("orderProduction");

        RibbonActionItem redirectToFilteredOrderProductionList = (RibbonActionItem) productionOrderGroups
                .getItemByName("redirectToFilteredOrderProductionList");

        if (productionOrder.getId() == null) {
            updateButtonState(redirectToFilteredOrderProductionList, false);
        } else {
            updateButtonState(redirectToFilteredOrderProductionList, true);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
