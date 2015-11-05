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
package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class PalletNumbersListHooks {

    public static final String L_GRID = "grid";

    public static final String L_WINDOW = "window";

    public static final String L_PRINT = "print";

    public static final String L_PRINT_PALLET_NUMBERS_REPORT = "printPalletNumbersReport";

    public void onBeforeRender(final ViewDefinitionState view) {
        disableButtonsWhenNotSelected(view);
    }

    public void disableButtonsWhenNotSelected(final ViewDefinitionState view) {
        GridComponent palletNumbersGrid = (GridComponent) view.getComponentByReference(L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup printRibbonGroup = ribbon.getGroupByName(L_PRINT);

        RibbonActionItem printPalletNumbersReportRibbonActionItem = printRibbonGroup.getItemByName(L_PRINT_PALLET_NUMBERS_REPORT);

        boolean palletNumbersAreSelected = !palletNumbersGrid.getSelectedEntities().isEmpty();

        if (printPalletNumbersReportRibbonActionItem != null) {
            printPalletNumbersReportRibbonActionItem.setEnabled(palletNumbersAreSelected);

            printPalletNumbersReportRibbonActionItem.requestUpdate(true);
        }
    }

}
