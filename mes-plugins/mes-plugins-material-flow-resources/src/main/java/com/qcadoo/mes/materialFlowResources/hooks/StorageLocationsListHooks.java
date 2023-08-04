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
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StorageLocationsListHooks {

    public static final String L_PRINT = "print";

    public static final String L_PRINT_STORAGE_LOCATION_NUMBERS_REPORT = "printStorageLocationNumbersReport";

    public void onBeforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        GridComponent storageLocationsGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup printRibbonGroup = window.getRibbon().getGroupByName(L_PRINT);

        RibbonActionItem printStorageLocationNumbersReportRibbonActionItem = printRibbonGroup.getItemByName(L_PRINT_STORAGE_LOCATION_NUMBERS_REPORT);

        boolean palletNumbersAreSelected = !storageLocationsGrid.getSelectedEntities().isEmpty();

        if (Objects.nonNull(printStorageLocationNumbersReportRibbonActionItem)) {
            printStorageLocationNumbersReportRibbonActionItem.setEnabled(palletNumbersAreSelected);

            printStorageLocationNumbersReportRibbonActionItem.requestUpdate(true);
        }
    }

}
