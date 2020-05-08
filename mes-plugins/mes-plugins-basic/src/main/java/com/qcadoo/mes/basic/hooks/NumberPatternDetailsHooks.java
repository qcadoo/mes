package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.NumberPatternFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class NumberPatternDetailsHooks {

    



    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity numberPattern = form.getPersistedEntityWithIncludedFormValues();
        if (numberPattern.getBooleanField(NumberPatternFields.USED)) {
            form.setFormEnabled(false);
            GridComponent grid = (GridComponent) view.getComponentByReference("numberPatternElements");
            grid.setEditable(false);
            WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
            RibbonGroup actions = window.getRibbon().getGroupByName("actions");

            RibbonActionItem save = actions.getItemByName("save");
            save.setEnabled(false);
            save.requestUpdate(true);
            RibbonActionItem saveBack = actions.getItemByName("saveBack");
            saveBack.setEnabled(false);
            saveBack.requestUpdate(true);
            RibbonActionItem saveNew = actions.getItemByName("saveNew");
            saveNew.setEnabled(false);
            saveNew.requestUpdate(true);
            window.requestRibbonRender();
        }
    }
}
