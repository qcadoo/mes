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
package com.qcadoo.mes.technologies.imports.operation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.productionLines.constants.DivisionFieldsPL;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    public static final String L_PRODUCTION_IN_ONE_CYCLE_UNIT = "productionInOneCycleUNIT";

    @Autowired
    private ParameterService parameterService;

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity operation = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(operation);

        return operation;
    }

    private void setRequiredFields(final Entity operation) {
        operation.setField(OperationFields.ASSIGNED_TO_OPERATION, AssignedToOperation.WORKSTATIONS.getStringValue());
        operation.setField(OperationFields.QUANTITY_OF_WORKSTATIONS, BigDecimal.ONE);
    }

    @Override
    public void validateEntity(final Entity operation, final DataDefinition operationDD) {
        validateProductionLine(operation, operationDD);
        validateWorkstation(operation, operationDD);
        validateProductionInOneCycle(operation, operationDD);
    }

    private void validateProductionLine(final Entity operation, final DataDefinition operationDD) {
        Entity productionLine = operation.getBelongsToField(OperationFields.PRODUCTION_LINE);
        Entity division = operation.getBelongsToField(OperationFields.DIVISION);

        if (Objects.nonNull(productionLine)) {
            if (Objects.isNull(division)) {
                operation.addError(operationDD.getField(OperationFields.DIVISION), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            } else {
                List<Entity> divisionProductionLines = division.getHasManyField(DivisionFieldsPL.PRODUCTION_LINES);

                Optional<Entity> mayBeProductionLine = divisionProductionLines.stream()
                        .filter(divisionProductionLine -> divisionProductionLine.getId().equals(productionLine.getId()))
                        .findAny();

                if (!mayBeProductionLine.isPresent()) {
                    operation.addError(operationDD.getField(OperationFields.PRODUCTION_LINE),
                            L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }
            }
        }
    }

    private void validateWorkstation(final Entity operation, final DataDefinition operationDD) {
        Entity workstation = operation.getBelongsToField(OperationFields.WORKSTATION);
        Entity productionLine = operation.getBelongsToField(OperationFields.PRODUCTION_LINE);

        if (Objects.nonNull(workstation)) {
            if (Objects.isNull(productionLine)) {
                operation.addError(operationDD.getField(OperationFields.PRODUCTION_LINE),
                        L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            } else {
                Entity workstationProductionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);

                if (!workstationProductionLine.getId().equals(productionLine.getId())) {
                    operation.addError(operationDD.getField(OperationFields.WORKSTATION),
                            L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }
            }

            operation.setField(OperationFields.WORKSTATIONS, Lists.newArrayList(workstation));
        }
    }

    private void validateProductionInOneCycle(final Entity operation, final DataDefinition operationDD) {
        BigDecimal productionInOneCycle = operation.getDecimalField(L_PRODUCTION_IN_ONE_CYCLE);
        Entity product = operation.getBelongsToField(OperationFields.PRODUCT);

        if (Objects.nonNull(productionInOneCycle)) {
            String productionInOneCycleUNIT;

            if (Objects.nonNull(product)) {
                productionInOneCycleUNIT = product.getStringField(ProductFields.UNIT);
            } else {
                productionInOneCycleUNIT = parameterService.getParameter().getStringField(ParameterFields.UNIT);
            }

            operation.setField(L_PRODUCTION_IN_ONE_CYCLE_UNIT, productionInOneCycleUNIT);
        }
    }

}
