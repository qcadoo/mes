/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.GROUPS;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.SUPPORTSALLTECHNOLOGIES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.TECHNOLOGIES;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionLineDetailsViewHooks {

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view) {
        ComponentState supportsAllTechnologies = view.getComponentByReference(SUPPORTSALLTECHNOLOGIES);

        if ("1".equals(supportsAllTechnologies.getFieldValue())) {
            view.getComponentByReference(TECHNOLOGIES).setEnabled(false);
            view.getComponentByReference(GROUPS).setEnabled(false);
        } else {
            view.getComponentByReference(TECHNOLOGIES).setEnabled(true);
            view.getComponentByReference(GROUPS).setEnabled(true);
        }
    }

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        disableSupportedTechnologiesGrids(view);
    }
}
