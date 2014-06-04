/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.costCalculation.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyDetailsHooksCC {

    private static final String L_COST_CALCULATE = "costCalculate";

    private static final String L_WINDOW = "window";

    private static final String L_FORM = "form";

    public void updateViewCostsCalculationButtonState(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup ribbonGroup = window.getRibbon().getGroupByName(L_COST_CALCULATE);
        RibbonActionItem costCalculate = ribbonGroup.getItemByName(L_COST_CALCULATE);

        Entity technology = technologyForm.getEntity();

        if ((technology.getId() == null)
                || TechnologyStateStringValues.DRAFT.equals(technology.getStringField(TechnologyFields.STATE))) {
            costCalculate.setEnabled(false);
        } else {
            costCalculate.setEnabled(true);
        }

        costCalculate.requestUpdate(true);
    }

}
