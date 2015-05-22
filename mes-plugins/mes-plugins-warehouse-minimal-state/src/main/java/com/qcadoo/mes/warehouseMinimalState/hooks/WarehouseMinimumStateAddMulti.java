package com.qcadoo.mes.warehouseMinimalState.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import org.springframework.stereotype.Service;

@Service public class WarehouseMinimumStateAddMulti {

    private static final String LFORM = "form";

    private static final String L_WINDOW = "window";

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent location = (LookupComponent) view.getComponentByReference("location");
        location.setRequired(true);
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        FormComponent form = (FormComponent) view.getComponentByReference(LFORM);
        RibbonActionItem addMultiButton = window.getRibbon().getGroupByName("action").getItemByName("createMultiMinimalStates");
        addMultiButton.setMessage("warehouseMinimalState.warehouseMinimumStateAddMulti.createMultiMinimalStates.button.message");
        addMultiButton.requestUpdate(true);
        window.requestRibbonRender();
    }
}
