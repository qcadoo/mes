/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
            delete.setMessage(null);
        } else {
            delete.setMessage(translationService.translate("basic.company.isOwner", view.getLocale()));
        }
        delete.requestUpdate(true);
    }
}
