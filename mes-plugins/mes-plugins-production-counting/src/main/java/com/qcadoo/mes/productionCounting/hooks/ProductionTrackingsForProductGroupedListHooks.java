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
package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductionTrackingsForProductGroupedListHooks {

    public static final String L_GRID = "grid";

    private static final String L_WINDOW = "window";

    private static final String L_SHOW_PRODUCTION_TRACKINGS = "showProductionTrackings";

    private static final String L_SHOW_PRODUCTION_TRACKINGS_FOR_PRODUCT = "showProductionTrackingsForProduct";

    public void updateButtonsState(final ViewDefinitionState view) {
        GridComponent productionTrackingsForProductGroupedGrid = (GridComponent) view.getComponentByReference(L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup showProductionTrackingsRibbonGroup = ribbon.getGroupByName(L_SHOW_PRODUCTION_TRACKINGS);

        RibbonActionItem showProductionTrackingsForProductRibbonActionItem = showProductionTrackingsRibbonGroup
                .getItemByName(L_SHOW_PRODUCTION_TRACKINGS_FOR_PRODUCT);

        boolean isSelected = !productionTrackingsForProductGroupedGrid.getSelectedEntities().isEmpty();

        showProductionTrackingsForProductRibbonActionItem.setEnabled(isSelected);

        showProductionTrackingsForProductRibbonActionItem.requestUpdate(true);
    }

}
