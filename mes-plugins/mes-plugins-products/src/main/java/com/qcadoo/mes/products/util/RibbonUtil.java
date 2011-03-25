/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.products.util;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.internal.ViewDefinitionState;
import com.qcadoo.view.internal.components.form.FormComponentState;
import com.qcadoo.view.internal.components.grid.GridComponentState;
import com.qcadoo.view.internal.components.window.WindowComponentState;
import com.qcadoo.view.internal.ribbon.RibbonActionItem;

@Service
public class RibbonUtil {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @SuppressWarnings("unchecked")
    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String entityName) {
        WindowComponentState window = (WindowComponentState) state.getComponentByReference("window");
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {

            Entity materialRequirementEntity = dataDefinitionService.get("products", entityName).get(form.getEntityId());
            List<Entity> orderComponents = (List<Entity>) materialRequirementEntity.getField("orders");

            if (orderComponents.size() == 0) {
                generateButton.setMessage("products.ribbon.message.noOrders");
                generateButton.setEnabled(false);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                boolean isAnyOrderClosed = false;
                for (Entity orderComponent : orderComponents) {
                    Entity order = orderComponent.getBelongsToField("order");
                    if (order.getField("state").equals("03done")) {
                        isAnyOrderClosed = true;
                        break;
                    }
                }
                if (isAnyOrderClosed && (Boolean) materialRequirementEntity.getField("generated")) {
                    generateButton.setMessage("products.ribbon.message.recordAlreadyGenerated");
                    generateButton.setEnabled(false);
                    deleteButton.setMessage("products.ribbon.message.recordAlreadyGenerated");
                    deleteButton.setEnabled(false);
                } else {
                    generateButton.setMessage(null);
                    generateButton.setEnabled(true);
                    deleteButton.setMessage(null);
                    deleteButton.setEnabled(true);
                }
            }
        }
        generateButton.setShouldBeUpdated(true);
        deleteButton.setShouldBeUpdated(true);
        window.requestRibbonRender();
    }

    @SuppressWarnings("unchecked")
    public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String entityName) {
        WindowComponentState window = (WindowComponentState) state.getComponentByReference("window");
        GridComponentState grid = (GridComponentState) state.getComponentByReference("grid");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (grid.getSelectedEntitiesId() == null || grid.getSelectedEntitiesId().size() == 0) {
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            boolean canDelete = true;
            for (Long entityId : grid.getSelectedEntitiesId()) {
                Entity materialRequirementEntity = dataDefinitionService.get("products", entityName).get(entityId);
                List<Entity> orderComponents = (List<Entity>) materialRequirementEntity.getField("orders");
                boolean isAnyOrderClosed = false;
                for (Entity orderComponent : orderComponents) {
                    Entity order = orderComponent.getBelongsToField("order");
                    if (order.getField("state").equals("03done")) {
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
                deleteButton.setMessage("products.ribbon.message.selectedRecordAlreadyGenerated");
                deleteButton.setEnabled(false);
            }
        }

        deleteButton.setShouldBeUpdated(true);
        window.requestRibbonRender();
    }
}
