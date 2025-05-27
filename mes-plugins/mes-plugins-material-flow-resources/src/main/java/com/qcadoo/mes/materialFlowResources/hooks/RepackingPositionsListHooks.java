/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.RepackingPositionFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

@Service
public class RepackingPositionsListHooks {

    public void toggleButtons(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup documentPositions = window.getRibbon().getGroupByName("documentPositions");
        RibbonActionItem filterRepackingPositionsByFromResource = documentPositions.getItemByName("filterRepackingPositionsByFromResource");
        RibbonActionItem showDocumentPositionsWithFromResource = documentPositions.getItemByName("showDocumentPositionsWithFromResource");
        RibbonActionItem showDocumentPositionsWithToResource = documentPositions.getItemByName("showDocumentPositionsWithToResource");

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        if (grid.getSelectedEntitiesIds().size() != 1) {
            filterRepackingPositionsByFromResource.setEnabled(false);
            showDocumentPositionsWithFromResource.setEnabled(false);
        } else {
            filterRepackingPositionsByFromResource.setEnabled(true);
            showDocumentPositionsWithFromResource.setEnabled(true);
        }
        showDocumentPositionsWithToResource.setEnabled(grid.getSelectedEntitiesIds().size() == 1 && grid.getSelectedEntities().get(0).getStringField(RepackingPositionFields.CREATED_RESOURCE_NUMBER) != null);
        filterRepackingPositionsByFromResource.requestUpdate(true);
        showDocumentPositionsWithFromResource.requestUpdate(true);
        showDocumentPositionsWithToResource.requestUpdate(true);
    }
}
