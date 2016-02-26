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
package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ActionListListeners {

    private static final String L_FORM = "form";

    public void disableActionsWhenDefault(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup actions = window.getRibbon().getGroupByName("actions");

        RibbonActionItem copyButton = actions.getItemByName("copy");
        RibbonActionItem deleteButton = actions.getItemByName("delete");

        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        List<Entity> selectedFaults = grid.getSelectedEntities();
        for (Entity selectedFault : selectedFaults) {
            if (selectedFault.getBooleanField("isDefault")) {
                copyButton.setEnabled(false);
                deleteButton.setEnabled(false);
                copyButton.requestUpdate(true);
                deleteButton.requestUpdate(true);
                return;
            }
        }

        boolean enabled = !selectedFaults.isEmpty();
        copyButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        copyButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
    }
}
