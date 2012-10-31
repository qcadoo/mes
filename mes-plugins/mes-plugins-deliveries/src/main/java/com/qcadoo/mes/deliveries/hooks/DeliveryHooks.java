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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DRAFT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class DeliveryHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private DeliveryStateChangeDescriber describer;

    private static final String L_FORM = "form";

    public void setInitialState(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, DRAFT);
    }

    public void clearStateFieldOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT);
    }

    public void updateRibbonState(final ViewDefinitionState view) {

        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = companyForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup suppliers = (RibbonGroup) window.getRibbon().getGroupByName("suppliers");

        RibbonActionItem redirectToFilteredDeliveriesList = (RibbonActionItem) suppliers
                .getItemByName("redirectToFilteredDeliveriesList");

        if (company.getId() == null) {
            updateButtonState(redirectToFilteredDeliveriesList, false);
        } else {
            updateButtonState(redirectToFilteredDeliveriesList, true);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }
}
