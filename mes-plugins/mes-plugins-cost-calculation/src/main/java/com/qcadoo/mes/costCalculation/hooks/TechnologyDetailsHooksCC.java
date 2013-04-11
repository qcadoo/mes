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
package com.qcadoo.mes.costCalculation.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyDetailsHooksCC {

    public void updateViewCostsCalculationButtonState(final ViewDefinitionState view) {
        FormComponent technologyForm = (FormComponent) view.getComponentByReference("form");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup materials = (RibbonGroup) window.getRibbon().getGroupByName("costCalculate");
        RibbonActionItem costCalculate = (RibbonActionItem) materials.getItemByName("costCalculate");

        Entity technology = technologyForm.getEntity();

        if (technology.getId() == null || TechnologyStateStringValues.DRAFT.equals(technology.getStringField(STATE))) {
            costCalculate.setEnabled(false);
        } else {
            costCalculate.setEnabled(true);
        }

        costCalculate.requestUpdate(true);
    }

}
