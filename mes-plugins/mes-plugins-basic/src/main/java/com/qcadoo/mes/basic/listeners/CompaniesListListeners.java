package com.qcadoo.mes.basic.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class CompaniesListListeners {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TranslationService translationService;

    public void checkIfIsOwner(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity parameter = parameterService.getParameter();
        Entity owner = parameter.getBelongsToField(ParameterFields.COMPANY);
        boolean enabled = true;
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        List<Entity> companies = grid.getSelectedEntities();
        for (Entity company : companies) {
            if (company.getId().equals(owner.getId())) {
                enabled = false;
            }
        }
        disabledButton(view, enabled);
    }

    private void disabledButton(final ViewDefinitionState view, final boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup actions = (RibbonGroup) window.getRibbon().getGroupByName("actions");
        RibbonActionItem delete = actions.getItemByName("delete");
        delete.setEnabled(enabled);
        if (enabled) {
            delete.setMessage("");
        } else {
            delete.setMessage(translationService.translate("basic.company.isOwner", view.getLocale()));
        }
        delete.requestUpdate(true);
    }
}
