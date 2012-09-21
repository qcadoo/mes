package com.qcadoo.mes.costNormsForMaterials.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyDetailsHooksCNFM {

    public void updateViewCostsButtonState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup materials = (RibbonGroup) window.getRibbon().getGroupByName("materials");
        RibbonActionItem viewCosts = (RibbonActionItem) materials.getItemByName("viewCosts");
        if (orderForm.getEntityId() == null) {
            viewCosts.setEnabled(false);
        } else {
            viewCosts.setEnabled(true);
        }
        viewCosts.requestUpdate(true);

    }
}
