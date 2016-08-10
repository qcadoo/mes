/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.DataDefinitionService;
import org.springframework.stereotype.Service;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DocumentsListListeners {

    private static final String L_GRID = "grid";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void printDispositionOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
//        Entity documentPositionParameters = parameterService.getParameter().getBelongsToField("documentPositionParameters");
//        boolean acceptanceOfDocumentBeforePrinting = documentPositionParameters.getBooleanField("acceptanceOfDocumentBeforePrinting");
//        if (acceptanceOfDocumentBeforePrinting) {
//            view.performEvent(view, "createResourcesForDocuments");
//        }
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        Set<Long> selectedEntitiesIds = grid.getSelectedEntitiesIds();

        view.redirectTo("/materialFlowResources/dispositionOrder." + args[0] + "?id=" + selectedEntitiesIds.stream().map(String::valueOf).collect(Collectors.joining(",")), true, false);
    }
}
