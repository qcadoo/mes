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

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class OperationXlsxImportService extends XlsxImportService {

    public static final String L_MIN_STAFF = "minStaff";

    public static final String L_OPTIMAL_STAFF = "optimalStaff";
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
        operation.setField(OperationFields.QUANTITY_OF_WORKSTATIONS,1);
        operation.setField(L_MIN_STAFF, 1);
        operation.setField(L_OPTIMAL_STAFF, 1);
    }

    @Override
    public void validateEntity(final Entity operation, final DataDefinition operationDD) {
        validateWorkstation(operation, operationDD);
        validateProductionInOneCycle(operation, operationDD);
    }

    private void validateWorkstation(final Entity operation, final DataDefinition operationDD) {
        Entity workstation = operation.getBelongsToField(OperationFields.WORKSTATION);

        if (Objects.nonNull(workstation)) {
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
