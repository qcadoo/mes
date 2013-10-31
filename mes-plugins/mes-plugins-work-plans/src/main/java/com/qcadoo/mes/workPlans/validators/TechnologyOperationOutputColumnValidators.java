/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.workPlans.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationComponentFieldsWP;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationOutputColumnFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationOutputColumnValidators {

    @Autowired
    private WorkPlansService workPlansService;

    public boolean validatesWith(final DataDefinition technologyOperationOutputColumnDD,
            final Entity technologyOperationOutputColumn) {
        return checkIfColumnForOutputProductsIsNotAlreadyUsed(technologyOperationOutputColumnDD, technologyOperationOutputColumn);
    }

    private boolean checkIfColumnForOutputProductsIsNotAlreadyUsed(final DataDefinition technologyOperationOutputColumnDD,
            final Entity technologyOperationOutputColumn) {
        return workPlansService.checkIfColumnIsNotUsed(technologyOperationOutputColumnDD, technologyOperationOutputColumn,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                TechnologyOperationOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS,
                TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_OUTPUT_COLUMNS);
    }

}
