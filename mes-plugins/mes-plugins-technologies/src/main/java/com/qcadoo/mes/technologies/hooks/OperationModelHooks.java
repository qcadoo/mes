/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.1
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
package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationModelHooks {

    private static final Integer DEFAULT_QUANTITY_OF_WORKSTATIONS = 1;

    public void onView(final DataDefinition operationDD, final Entity operation) {
        fillQuantityOfWorkstations(operation);
    }

    public void onSave(final DataDefinition operationDD, final Entity operation) {
        clearField(operation);
    }

    private void clearField(final Entity operation) {
        String assignedToOperation = operation.getStringField(OperationFields.ASSIGNED_TO_OPERATION);
        if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperation)) {
            operation.setField(OperationFields.WORKSTATIONS, null);
        }
    }

    private void fillQuantityOfWorkstations(final Entity operation) {

        if (operation.getIntegerField(OperationFields.QUANTITY_OF_WORKSTATIONS) == null) {
            operation.setField(OperationFields.QUANTITY_OF_WORKSTATIONS, DEFAULT_QUANTITY_OF_WORKSTATIONS);
        }
    }

}
