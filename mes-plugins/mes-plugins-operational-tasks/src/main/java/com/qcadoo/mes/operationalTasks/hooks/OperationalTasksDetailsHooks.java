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
package com.qcadoo.mes.operationalTasks.hooks;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTasksDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void beforeRender(final ViewDefinitionState view) {
        generateOperationalTasksNumber(view);
        filterWorkstationLookup(view);
    }

    private void generateOperationalTasksNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK, L_FORM, OperationalTaskFields.NUMBER);
    }

    private void filterWorkstationLookup(final ViewDefinitionState view) {
        LookupComponent productionLineLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.PRODUCTION_LINE);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFields.WORKSTATION);

        Entity productionLine = productionLineLookup.getEntity();

        FilterValueHolder filterValueHolder = workstationLookup.getFilterValue();


        if (Objects.isNull(productionLine)) {
            filterValueHolder.remove(OperationalTaskFields.PRODUCTION_LINE);
        } else {
            Long productionLineId = productionLine.getId();
            filterValueHolder.put(OperationalTaskFields.PRODUCTION_LINE, productionLineId);
        }

        workstationLookup.setFilterValue(filterValueHolder);
    }

}
