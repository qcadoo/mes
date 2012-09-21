package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductFamiliesAddProductHooks {

    public void updateRibbonState(final ViewDefinitionState view) {

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference("child");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup add = (RibbonGroup) window.getRibbon().getGroupByName("add");

        RibbonActionItem addProduct = (RibbonActionItem) add.getItemByName("addProduct");

        updateButtonState(addProduct, productLookup.getFieldValue() != null);

    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
