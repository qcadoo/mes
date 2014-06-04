/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionScheduling.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionLinesDetailsHooksPS {

    @Autowired
    private ParameterService parameterService;

    public void disabledWorkstationTypesTab(final ViewDefinitionState view) {
        if (!parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
            FieldComponent field = (FieldComponent) view
                    .getComponentByReference(ProductionLineFields.QUANTITY_FOR_OTHER_WORKSTATION_TYPES);
            GridComponent workstationTypeComponentGrid = (GridComponent) view.getComponentByReference("grid");
            field.setEnabled(false);
            workstationTypeComponentGrid.setEditable(false);
        }
    }

}
