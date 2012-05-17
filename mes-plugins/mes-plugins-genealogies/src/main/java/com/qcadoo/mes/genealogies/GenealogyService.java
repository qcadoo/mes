/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.genealogies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public final class GenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/genealogies/orderGenealogiesList.html?context={\"order.id\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void newGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/genealogies/orderGenealogyDetails.html?context={\"form.order\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void hideComponents(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        ComponentState featuresLayout = state.getComponentByReference("featuresLayout");
        ComponentState shiftList = state.getComponentByReference("shiftBorderLayout");
        FieldComponent shiftFeaturesList = (FieldComponent) state.getComponentByReference("shiftFeaturesList");
        ComponentState postList = state.getComponentByReference("postBorderLayout");
        FieldComponent postFeaturesList = (FieldComponent) state.getComponentByReference("postFeaturesList");
        ComponentState otherList = state.getComponentByReference("otherBorderLayout");
        FieldComponent otherFeaturesList = (FieldComponent) state.getComponentByReference("otherFeaturesList");

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                Long.valueOf(form.getEntity().getField("order").toString()));
        Entity technology = order.getBelongsToField("technology");

        if (technology == null) {
            featuresLayout.setVisible(false);
        } else {
            boolean shiftFeatureRequired = (Boolean) technology.getField("shiftFeatureRequired");
            boolean postFeatureRequired = (Boolean) technology.getField("postFeatureRequired");
            boolean otherFeatureRequired = (Boolean) technology.getField("otherFeatureRequired");

            if (shiftFeatureRequired) {
                shiftFeaturesList.setRequired(true);
            } else {
                shiftList.setVisible(false);

            }

            if (postFeatureRequired) {
                postFeaturesList.setRequired(true);
            } else {
                postList.setVisible(false);
            }

            if (otherFeatureRequired) {
                otherFeaturesList.setRequired(true);
            } else {
                otherList.setVisible(false);
            }

            if (!(otherFeatureRequired || shiftFeatureRequired || postFeatureRequired)) {
                featuresLayout.setVisible(false);
            }
        }
    }
}
