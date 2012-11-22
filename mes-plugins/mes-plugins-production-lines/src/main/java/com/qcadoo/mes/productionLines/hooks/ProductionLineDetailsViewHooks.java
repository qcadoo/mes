/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.SUPPORTS_ALL_TECHNOLOGIES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.TECHNOLOGIES;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionLineDetailsViewHooks {

    private static final String L_FORM = "form";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view) {
        FormComponent productionLineForm = (FormComponent) view.getComponentByReference(L_FORM);
        ComponentState supportsAllTechnologies = view.getComponentByReference(SUPPORTS_ALL_TECHNOLOGIES);

        if ((productionLineForm.getEntityId() == null) || "1".equals(supportsAllTechnologies.getFieldValue())) {
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

    public void generateProductionLineNumber(final ViewDefinitionState viewDefinitionState) {
        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE, "form", "number");
    }
}
