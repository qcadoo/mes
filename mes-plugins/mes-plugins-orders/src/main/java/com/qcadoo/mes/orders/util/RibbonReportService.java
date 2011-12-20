/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
package com.qcadoo.mes.orders.util;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class RibbonReportService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @SuppressWarnings("unchecked")
    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference("window");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {

            Entity materialRequirementEntity = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());
            Collection<Entity> orderComponents = (Collection<Entity>) materialRequirementEntity.getField("orders");

            if (orderComponents.size() == 0) {
                generateButton.setMessage("orders.ribbon.message.noOrders");
                generateButton.setEnabled(false);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                boolean isAnyOrderClosed = false;
                Entity order = null;
                for (Entity orderComponent : orderComponents) {
                    if (OrdersConstants.MODEL_ORDER.equals(orderComponent.getDataDefinition().getName())) {
                        order = orderComponent;
                    } else {
                        order = orderComponent.getBelongsToField("order");
                    }
                    if (order.getField("state").equals("04completed")) {
                        isAnyOrderClosed = true;
                        break;
                    }
                }
                if (isAnyOrderClosed && (Boolean) materialRequirementEntity.getField("generated")) {
                    generateButton.setMessage("orders.ribbon.message.recordAlreadyGenerated");
                    generateButton.setEnabled(false);
                    deleteButton.setMessage("orders.ribbon.message.recordAlreadyGenerated");
                    deleteButton.setEnabled(false);
                } else {
                    generateButton.setMessage(null);
                    generateButton.setEnabled(true);
                    deleteButton.setMessage(null);
                    deleteButton.setEnabled(true);
                }
            }
        }
        generateButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }

    @SuppressWarnings("unchecked")
    public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference("window");
        GridComponent grid = (GridComponent) state.getComponentByReference("grid");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            boolean canDelete = true;
            for (Long entityId : grid.getSelectedEntitiesIds()) {
                Entity materialRequirementEntity = dataDefinitionService.get(plugin, entityName).get(entityId);
                List<Entity> orderComponents = (List<Entity>) materialRequirementEntity.getField("orders");
                boolean isAnyOrderClosed = false;
                for (Entity orderComponent : orderComponents) {
                    Entity order = orderComponent.getBelongsToField("order");
                    if (order.getField("state").equals("04completed")) {
                        isAnyOrderClosed = true;
                        break;
                    }
                }
                if (isAnyOrderClosed && (Boolean) materialRequirementEntity.getField("generated")) {
                    canDelete = false;
                    break;
                }
            }
            if (canDelete) {
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                deleteButton.setMessage("orders.ribbon.message.selectedRecordAlreadyGenerated");
                deleteButton.setEnabled(false);
            }
        }

        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }
}
