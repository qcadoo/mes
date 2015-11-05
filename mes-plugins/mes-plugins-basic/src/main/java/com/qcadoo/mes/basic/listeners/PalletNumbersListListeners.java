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
package com.qcadoo.mes.basic.listeners;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.PalletNumbersService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class PalletNumbersListListeners {

    public static final String L_GRID = "grid";

    @Autowired
    private PalletNumbersService palletNumbersService;

    public void createPalletNumbers(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        String url = "../page/basic/palletNumberHelperDetails.html";

        view.openModal(url);
    }

    public void printPalletNumbersReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent palletNumbersGrid = (GridComponent) view.getComponentByReference(L_GRID);

        Set<Long> palletNumberIds = palletNumbersGrid.getSelectedEntitiesIds();

        List<Entity> palletNumbers = palletNumbersService.getPalletNumbers(palletNumberIds);

        if (!palletNumbers.isEmpty()) {
            Entity palletNumbersHelper = palletNumbersService.createPalletNumberHelper(palletNumbers.size(), true, palletNumbers);

            if (palletNumbersHelper != null) {
                Long labelsHelperId = palletNumbersHelper.getId();

                view.redirectTo("/basic/palletNumberHelperReport.pdf?id=" + labelsHelperId, true, false);
            }
        }
    }

}
