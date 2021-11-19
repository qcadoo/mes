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
package com.qcadoo.mes.timeNormsForOperations.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.timeNormsForOperations.constants.OperationWorkstationTimeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationWorkstationTimeModelValidators {

    public static final String TIME_NORMS_FOR_OPERATIONS_OPERATION_WORKSTATION_TIME_ONE_OF_TIME_FIELDS_IS_REQUIRED = "timeNormsForOperations.operationWorkstationTime.oneOfTimeFieldsIsRequired";

    public boolean checkIfAllTimeFieldsAreEqualZero(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getIntegerField(OperationWorkstationTimeFields.TJ) == 0
                && entity.getIntegerField(OperationWorkstationTimeFields.TPZ) == 0
                && entity.getIntegerField(OperationWorkstationTimeFields.TIME_NEXT_OPERATION) == 0) {
            entity.addError(dataDefinition.getField(OperationWorkstationTimeFields.TJ),
                    TIME_NORMS_FOR_OPERATIONS_OPERATION_WORKSTATION_TIME_ONE_OF_TIME_FIELDS_IS_REQUIRED);
            entity.addError(dataDefinition.getField(OperationWorkstationTimeFields.TPZ),
                    TIME_NORMS_FOR_OPERATIONS_OPERATION_WORKSTATION_TIME_ONE_OF_TIME_FIELDS_IS_REQUIRED);
            entity.addError(dataDefinition.getField(OperationWorkstationTimeFields.TIME_NEXT_OPERATION),
                    TIME_NORMS_FOR_OPERATIONS_OPERATION_WORKSTATION_TIME_ONE_OF_TIME_FIELDS_IS_REQUIRED);
            return false;
        }
        return true;
    }
}
