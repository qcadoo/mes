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
package com.qcadoo.mes.timeNormsForOperations;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields;
import com.qcadoo.model.api.Entity;

@Service
public class NormService {

    @Autowired
    private TechnologyService technologyService;

    public List<String> checkOperationOutputQuantities(final Entity technology) {
        List<String> messages = Lists.newArrayList();

        List<Entity> operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            BigDecimal timeNormsQuantity = getProductionInOneCycle(operationComponent);

            BigDecimal currentQuantity;

            try {
                currentQuantity = technologyService.getProductCountForOperationComponent(operationComponent);
            } catch (IllegalStateException e) {
                continue;
            }

            if (timeNormsQuantity == null || timeNormsQuantity.compareTo(currentQuantity) != 0) {
                String nodeNumber = operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);

                if (nodeNumber == null) {
                    Entity operation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                    if (operation != null) {
                        String name = operation.getStringField(OperationFields.NAME);

                        if (name != null) {
                            messages.add(name);
                        }
                    }
                } else {
                    messages.add(nodeNumber);
                }
            }
        }

        return messages;
    }

    private BigDecimal getProductionInOneCycle(final Entity operationComponent) {
        String entityType = operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE);
        if (TechnologyOperationComponentType.OPERATION.getStringValue().equals(entityType)) {
            return operationComponent.getDecimalField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE);
        } else if (TechnologyOperationComponentType.REFERENCE_TECHNOLOGY.getStringValue().equals(entityType)) {
            Entity refOperationComp = operationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY)
                    .getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();
            return refOperationComp.getDecimalField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE);
        } else {
            throw new IllegalStateException("operationComponent has illegal type, id = " + operationComponent.getId());
        }
    }

}
