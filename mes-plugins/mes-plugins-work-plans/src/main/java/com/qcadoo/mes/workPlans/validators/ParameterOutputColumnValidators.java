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
package com.qcadoo.mes.workPlans.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.workPlans.WorkPlansService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.ParameterOutputColumnFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterOutputColumnValidators {

    @Autowired
    private WorkPlansService workPlansService;

    public boolean validatesWith(final DataDefinition parameterOutputColumnDD, final Entity parameterOutputColumn) {
        return checkIfColumnForOutputProductsIsNotAlreadyUsed(parameterOutputColumnDD, parameterOutputColumn);
    }

    private boolean checkIfColumnForOutputProductsIsNotAlreadyUsed(final DataDefinition parameterOutputColumnDD,
            final Entity parameterOutputColumn) {
        return workPlansService.checkIfColumnIsNotUsed(parameterOutputColumnDD, parameterOutputColumn,
                BasicConstants.MODEL_PARAMETER, ParameterOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS,
                ParameterFieldsWP.PARAMETER_OUTPUT_COLUMNS);
    }

}
