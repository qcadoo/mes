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
package com.qcadoo.mes.timeNormsForOperations.validators;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyValidatorsServiceTNFO {

    private static final String L_TECHNOLOGIES_OPERATION_DETAILS_VALIDATE_ERROR_OUTPUT_UNITS_NOT_MATCH = "technologies.operationDetails.validate.error.OutputUnitsNotMatch";

    @Autowired
    private ProductQuantitiesService productQuantitiyService;

   

    public boolean checkIfUnitMatch(final DataDefinition dataDefinition, final Entity technologyOperationComponent) {
        String productionInOneCycleUnit = technologyOperationComponent.getStringField(PRODUCTION_IN_ONE_CYCLE_UNIT);
        String nextOperationAfterProducedQuantityUnit = technologyOperationComponent
                .getStringField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT);
        String nextOperationAfterProducedType = (String) technologyOperationComponent
                .getField(TechnologyOperCompFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE);

        if (productionInOneCycleUnit == null) {
            return true;
        }

        if ("02specified".equals(nextOperationAfterProducedType)
                && !productionInOneCycleUnit.equals(nextOperationAfterProducedQuantityUnit)) {
            technologyOperationComponent.addError(dataDefinition.getField(NEXT_OPERATION_AFTER_PRODUCED_QUANTITY_UNIT),
                    "technologies.operationDetails.validate.error.UnitsNotMatch");
            return false;
        }
        return true;

    }

    public boolean checkIfUnitsInTechnologyMatch(final DataDefinition dataDefinition, final Entity technologyOperationComponent) {
        final String productionInOneCycleUNIT = technologyOperationComponent.getStringField(PRODUCTION_IN_ONE_CYCLE_UNIT);
        if (productionInOneCycleUNIT == null) {
            technologyOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                    L_TECHNOLOGIES_OPERATION_DETAILS_VALIDATE_ERROR_OUTPUT_UNITS_NOT_MATCH);
            return false;
        }

        if (technologyOperationComponent.getId() == null) {
            return true;
        }

        final Entity outputProduct = productQuantitiyService
                .getOutputProductsFromOperationComponent(technologyOperationComponent);
        if (outputProduct != null) {
            final String outputProductionUnit = outputProduct.getBelongsToField(PRODUCT).getStringField(UNIT);
            if (!productionInOneCycleUNIT.equals(outputProductionUnit)) {
                technologyOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                        L_TECHNOLOGIES_OPERATION_DETAILS_VALIDATE_ERROR_OUTPUT_UNITS_NOT_MATCH);
                return false;
            }
        }
        return true;
    }

    public boolean checkIfUnitsInInstanceTechnologyMatch(final DataDefinition dataDefinition,
            final Entity technologyInstanceOperationComponent) {
        final String productionInOneCycleUNIT = technologyInstanceOperationComponent.getStringField(PRODUCTION_IN_ONE_CYCLE_UNIT);
        if (productionInOneCycleUNIT == null) {
            technologyInstanceOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                    L_TECHNOLOGIES_OPERATION_DETAILS_VALIDATE_ERROR_OUTPUT_UNITS_NOT_MATCH);
            return false;
        }

        if (technologyInstanceOperationComponent.getId() == null) {
            return true;
        }

        final Entity technologyOperationComponent = technologyInstanceOperationComponent
                .getBelongsToField("technologyOperationComponent");
        final Entity outputProduct = productQuantitiyService
                .getOutputProductsFromOperationComponent(technologyOperationComponent);
        if (outputProduct != null) {
            final String outputProductionUnit = outputProduct.getBelongsToField(PRODUCT).getStringField(UNIT);
            if (!productionInOneCycleUNIT.equals(outputProductionUnit)) {
                technologyInstanceOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                        L_TECHNOLOGIES_OPERATION_DETAILS_VALIDATE_ERROR_OUTPUT_UNITS_NOT_MATCH);
                return false;
            }
        }
        return true;
    }
}
