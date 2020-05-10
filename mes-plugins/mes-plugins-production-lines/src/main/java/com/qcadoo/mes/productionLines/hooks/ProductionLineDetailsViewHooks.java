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
package com.qcadoo.mes.productionLines.hooks;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.NUMBER;

@Service
public class ProductionLineDetailsViewHooks {

    

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateProductionLineNumber(final ViewDefinitionState viewDefinitionState) {
        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE, QcadooViewConstants.L_FORM, NUMBER);
    }

    public void fillCriteriaModifiers(final ViewDefinitionState viewDefinitionState) {
        GridComponent workstations = (GridComponent) viewDefinitionState.getComponentByReference("workstations");
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() != null) {
            FilterValueHolder filter = workstations.getFilterValue();
            filter.put("productionLine", form.getEntityId());
            workstations.setFilterValue(filter);
        }
        workstations.reloadEntities();
    }
}
