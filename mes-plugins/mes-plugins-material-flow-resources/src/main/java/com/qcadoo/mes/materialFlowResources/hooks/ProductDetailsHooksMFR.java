package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductDetailsHooksMFR {

    public void setEnabledForStorageLocationHistory(final ViewDefinitionState view) {

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName("storageLocationHistory");
        RibbonActionItem history = group.getItemByName("showHistory");
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        history.setEnabled(form.getEntityId() != null);
        history.requestUpdate(true);
    }

}
