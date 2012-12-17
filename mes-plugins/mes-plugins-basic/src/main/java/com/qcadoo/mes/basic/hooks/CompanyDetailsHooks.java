/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class CompanyDetailsHooks {

    @Autowired
    private ParameterService parameterService;

    private static final String L_FORM = "form";

    public void updateRibbonState(final ViewDefinitionState view) {
        disabledRedirectToFilteredOrderProductionListButton(view);
        disabledRibbonForOwner(view);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private void disabledRedirectToFilteredOrderProductionListButton(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = form.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup productionOrderGroups = (RibbonGroup) window.getRibbon().getGroupByName("orderProduction");

        RibbonActionItem redirectToFilteredOrderProductionList = (RibbonActionItem) productionOrderGroups
                .getItemByName("redirectToFilteredOrderProductionList");

        if (company.getId() == null) {
            updateButtonState(redirectToFilteredOrderProductionList, false);
        } else {
            updateButtonState(redirectToFilteredOrderProductionList, true);
        }
    }

    private void disabledRibbonForOwner(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        boolean disabled = true;
        Entity company = form.getEntity();
        Entity parameter = parameterService.getParameter();
        Entity owner = parameter.getBelongsToField(ParameterFields.COMPANY);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup actions = (RibbonGroup) window.getRibbon().getGroupByName("actions");

        if (company.getId().equals(owner.getId())) {
            disabled = false;
        }
        for (String item : Lists.newArrayList("save", "saveBack", "saveNew", "copy", "delete")) {
            RibbonActionItem ribbonActionItem = (RibbonActionItem) actions.getItemByName(item);
            ribbonActionItem.setEnabled(disabled);
            ribbonActionItem.requestUpdate(true);
        }

    }
}
