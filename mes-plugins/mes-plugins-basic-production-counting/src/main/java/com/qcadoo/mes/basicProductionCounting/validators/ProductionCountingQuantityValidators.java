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
package com.qcadoo.mes.basicProductionCounting.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionCountingQuantityValidators {

    private static final String L_FOR_EACH = "03forEach";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    public boolean validatesWith(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        return checkIfTechnologyOperationComponentIsNotNull(productionCountingQuantityDD, productionCountingQuantity);
    }

    private boolean checkIfTechnologyOperationComponentIsNotNull(final DataDefinition productionCountingQuantityDD,
            final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT) == null) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

            if (order != null) {
                String typeOfProductionRecording = order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

                if (L_FOR_EACH.equals(typeOfProductionRecording)) {
                    productionCountingQuantity
                            .addError(productionCountingQuantityDD
                                    .getField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                                    "basicProductionCounting.productionCountingQuantity.technologyOperationComponent.error.fieldRequired");

                    return false;
                }
            }
        }

        return true;
    }

}
