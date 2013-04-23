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
package com.qcadoo.mes.technologies.validators;

import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Component
public class TechnologyTreeValidators {

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    public boolean checkConsumingTheSameProductFromManySubOperations(final DataDefinition technologyDD, final Entity technology) {
        Entity techFromDB = technologyDD.get(technology.getId());

        EntityTree tree = techFromDB.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        Map<String, Set<Entity>> nodesMap = technologyTreeValidationService
                .checkConsumingTheSameProductFromManySubOperations(tree);

        for (Entry<String, Set<Entity>> entry : nodesMap.entrySet()) {
            String parentNodeNumber = entry.getKey();
            for (Entity product : entry.getValue()) {
                String productName = product.getStringField(ProductFields.NAME);
                String productNumber = product.getStringField(ProductFields.NUMBER);

                technology.addGlobalError(
                        "technologies.technology.validate.global.error.subOperationsProduceTheSameProductThatIsConsumed",
                        parentNodeNumber, productName, productNumber);
            }
        }
        return nodesMap.isEmpty();
    }

    public boolean invalidateIfBelongsToAcceptedTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = null;
        String errorMessageKey = "technologies.technology.state.error.modifyBelongsToAcceptedTechnology";
        if (TechnologiesConstants.MODEL_TECHNOLOGY.equals(dataDefinition.getName())) {
            technology = entity;
            errorMessageKey = "technologies.technology.state.error.modifyAcceptedTechnology";
        } else if (TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(dataDefinition.getName())) {
            technology = entity.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        } else if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(dataDefinition.getName())
                || TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT.equals(dataDefinition.getName())) {
            final Entity operationComponent = entity.getBelongsToField(L_OPERATION_COMPONENT);
            if (operationComponent == null) {
                return true;
            }
            technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        }
        if (technologyIsAcceptedAndNotDeactivated(dataDefinition, entity, technology)) {
            entity.addGlobalError(errorMessageKey, technology.getStringField(TechnologyFields.NAME));
            return false;
        }
        return true;
    }

    private boolean technologyIsAcceptedAndNotDeactivated(final DataDefinition dataDefinition, final Entity entity,
            final Entity technology) {
        if (technology == null || technology.getId() == null) {
            return false;
        }
        final Entity existingTechnology = technology.getDataDefinition().get(technology.getId());
        if (isTechnologyIsAlreadyAccepted(technology, existingTechnology)) {
            if (checkIfDeactivated(dataDefinition, technology, existingTechnology)) {
                return false;
            }
            if (entity.getId() != null) {
                final Entity existingEntity = dataDefinition.get(entity.getId());
                if (entity.equals(existingEntity)) {
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    private boolean checkIfDeactivated(final DataDefinition dataDefinition, final Entity technology,
            final Entity existingTechnology) {
        return TechnologiesConstants.MODEL_TECHNOLOGY.equals(dataDefinition.getName())
                && technology.isActive() != existingTechnology.isActive();
    }

    private boolean isTechnologyIsAlreadyAccepted(final Entity technology, final Entity existingTechnology) {
        if (technology == null || existingTechnology == null) {
            return false;
        }
        TechnologyState technologyState = TechnologyState.parseString(technology.getStringField(STATE));
        TechnologyState existingTechnologyState = TechnologyState.parseString(existingTechnology.getStringField(STATE));

        return TechnologyState.ACCEPTED.equals(technologyState) && technologyState.equals(existingTechnologyState);
    }
}
