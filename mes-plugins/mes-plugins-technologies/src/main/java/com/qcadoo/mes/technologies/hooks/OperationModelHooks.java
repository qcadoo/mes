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
package com.qcadoo.mes.technologies.hooks;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class OperationModelHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;


    public void onSave(final DataDefinition operationDD, final Entity operation) {
        clearField(operationDD, operation);
        createOperationOutput(operation);
    }

    private void createOperationOutput(final Entity operation) {
        if (operation.getBooleanField(OperationFields.CREATE_OPERATION_OUTPUT)
                && Objects.isNull(operation.getBelongsToField(OperationFields.PRODUCT))) {
            Entity product = createProductForOperation(operation);
            if (!product.isValid()) {
                ErrorMessage errorMessage = product.getError(ProductFields.NUMBER);
                if (Objects.nonNull(errorMessage)
                        && errorMessage.getMessage().equals("qcadooView.validate.field.error.duplicated")) {
                    operation.addGlobalError("technologies.operation.createOperationOutput.failed.productDuplicated",
                            product.getStringField(ProductFields.NUMBER));
                }
                operation.addGlobalError("technologies.operation.createOperationOutput.failed");
            } else {
                operation.setField(OperationFields.PRODUCT, product.getId());
            }
        }
    }

    private Entity createProductForOperation(final Entity operation) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).create();
        product.setField(ProductFields.NUMBER, operation.getStringField(OperationFields.NUMBER));
        product.setField(ProductFields.NAME, operation.getStringField(OperationFields.NAME));
        product.setField(ProductFields.GLOBAL_TYPE_OF_MATERIAL, GlobalTypeOfMaterial.INTERMEDIATE.getStringValue());
        product.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        product.setField(ProductFields.UNIT, parameterService.getParameter().getStringField(ParameterFields.UNIT));
        return product.getDataDefinition().save(product);

    }

    private void clearField(DataDefinition operationDD, final Entity operation) {
        String assignedToOperation = operation.getStringField(OperationFields.ASSIGNED_TO_OPERATION);
        if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperation)) {
            operation.setField(OperationFields.WORKSTATIONS, null);
        }
        Long operationId = operation.getId();

        if (Objects.nonNull(operationId)) {
            Entity operationDB = operationDD.get(operationId);
            if (operation.getBelongsToField(OperationFields.DIVISION) != null
                    && operationDB.getBelongsToField(OperationFields.DIVISION) == null
                    || operation.getBelongsToField(OperationFields.DIVISION) == null
                    && operationDB.getBelongsToField(OperationFields.DIVISION) != null
                    || operation.getBelongsToField(OperationFields.DIVISION) != null
                    && !operation.getBelongsToField(OperationFields.DIVISION).equals(operationDB.getBelongsToField(OperationFields.DIVISION))) {
                operation.setField(OperationFields.WORKSTATIONS, null);
            }
        }
    }

}
