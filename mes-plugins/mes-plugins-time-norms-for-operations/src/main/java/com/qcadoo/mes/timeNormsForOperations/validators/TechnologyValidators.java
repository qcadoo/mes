/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.ACCEPTED;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.CHECKED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_MACHINE_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_REALIZED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyValidators {

    @Autowired
    private NormService normService;

    @Autowired
    private ProductQuantitiesServiceImpl productQuantitiyService;

    public boolean checkOperationOutputQuantities(final DataDefinition dd, final Entity tech) {
        if (!(ACCEPTED.getStringValue().equals(tech.getStringField(STATE)) || CHECKED.getStringValue().equals(
                tech.getStringField(STATE)))) {
            return true;
        }

        // FIXME mici, why would I need this? Without it operationComponents are null
        Entity technology = tech.getDataDefinition().get(tech.getId());

        Map<String, String> messages = normService.checkOperationOutputQuantities(technology);

        for (Entry<String, String> message : messages.entrySet()) {
            tech.addGlobalError(message.getKey(), message.getValue());
        }

        return messages.isEmpty();
    }

    public boolean checkIfAllOperationComponenthHaveTJSet(final DataDefinition dataDefinition, final Entity technology) {
        if (!(ACCEPTED.getStringValue().equals(technology.getStringField(STATE)) || CHECKED.getStringValue().equals(
                technology.getStringField(STATE)))) {
            return true;
        }
        MultiFieldErrorHandler errors = new MultiFieldErrorHandler();
        Entity savedTechnology = dataDefinition.get(technology.getId());
        EntityTree operationComponents = savedTechnology.getTreeField("operationComponents");

        for (Entity operationComponent : operationComponents) {
            if ("operation".equals(operationComponent.getField("entityType")) && !checkIfTJSet(operationComponent)) {
                StringBuilder fieldName = new StringBuilder();
                fieldName.append(operationComponent.getStringField("nodeNumber")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("number")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("name"));
                errors.addToErrorMessage(fieldName.toString());
            }
        }

        return errors.getMessages("technologies.technology.validate.global.error.noTJSpecified", technology);
    }

    private boolean checkIfTJSet(final Entity operationComponent) {
        if (operationComponent.getField("tj") == null) {
            return false;
        }

        return true;
    }

    private static class MultiFieldErrorHandler {

        private boolean hasError = false;

        private final StringBuilder errorString = new StringBuilder();

        public void addToErrorMessage(final String field) {
            errorString.append(" ").append(field).append(";");
            hasError = true;
        }

        public boolean getMessages(final String error, final Entity entity) {
            if (hasError) {
                entity.addGlobalError(error, errorString.toString());
                return false;
            }
            return true;
        }
    }

    public boolean checkIfUnitMatch(final DataDefinition dataDefinition, final Entity technologyOperationComponent) {
        String productionUnit = technologyOperationComponent.getStringField(PRODUCTION_IN_ONE_CYCLE_UNIT);
        String machineUnit = technologyOperationComponent.getStringField(COUNT_MACHINE_UNIT);
        String countRealized = (String) technologyOperationComponent.getField(COUNT_REALIZED);

        if (productionUnit == null) {
            return true;
        }
        if (machineUnit == null) {
            if ("02specified".equals(countRealized)) {
                if (!productionUnit.equals(machineUnit)) {
                    technologyOperationComponent.addError(dataDefinition.getField(COUNT_MACHINE_UNIT),
                            "technologies.operationDetails.validate.error.UnitsNotMatch");
                    return false;
                }
            }
        }
        if (productionUnit != null && machineUnit != null) {
            if ("02specified".equals(countRealized)) {
                if (!productionUnit.equals(machineUnit)) {
                    technologyOperationComponent.addError(dataDefinition.getField(COUNT_MACHINE_UNIT),
                            "technologies.operationDetails.validate.error.UnitsNotMatch");
                    return false;
                }
            }
        }
        return true;

    }

    public boolean checkIfUnitsInTechnologyMatch(final DataDefinition dataDefinition, final Entity technologyOperationComponent) {
        Entity outputProduct = productQuantitiyService.getOutputProductsFromOperataionComponent(technologyOperationComponent);
        String productionInOneCycleUNIT = technologyOperationComponent.getStringField(PRODUCTION_IN_ONE_CYCLE_UNIT);

        if (productionInOneCycleUNIT == null) {
            technologyOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                    "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
            return false;
        }
        if (outputProduct != null && productionInOneCycleUNIT != null) {
            String outputProductionUnit = outputProduct.getBelongsToField(PRODUCT).getStringField(UNIT);

            if (!productionInOneCycleUNIT.equals(outputProductionUnit)) {
                technologyOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                        "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
                return false;
            }
        }
        return true;
    }

    public boolean checkIfUnitsInInstanceTechnologyMatch(final DataDefinition dataDefinition,
            final Entity technologyInstanceOperationComponent) {

        String productionInOneCycleUNIT = technologyInstanceOperationComponent.getStringField(PRODUCTION_IN_ONE_CYCLE_UNIT);
        Entity technologyOperationComponent = technologyInstanceOperationComponent
                .getBelongsToField("technologyOperationComponent");
        Entity outputProduct = productQuantitiyService.getOutputProductsFromOperataionComponent(technologyOperationComponent);
        String outputProductionUnit = outputProduct.getBelongsToField(PRODUCT).getStringField(UNIT);

        if (productionInOneCycleUNIT == null) {
            technologyInstanceOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                    "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
            return false;
        }
        if (outputProduct != null && productionInOneCycleUNIT != null) {
            if (!productionInOneCycleUNIT.equals(outputProductionUnit)) {
                technologyInstanceOperationComponent.addError(dataDefinition.getField(PRODUCTION_IN_ONE_CYCLE_UNIT),
                        "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
                return false;
            }
        }
        return true;
    }
}
