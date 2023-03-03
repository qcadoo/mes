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
package com.qcadoo.mes.operationCostCalculations;

import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.ALL;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE;

@Service
public class OperationCostCalculationTreeBuilder {

    private static final String L_OPERATION = "operation";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String L_CALCULATION_OPERATION_COMPONENTS = "calculationOperationComponents";

    private static final Logger LOG = LoggerFactory.getLogger(OperationCostCalculationTreeBuilder.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public Entity copyTechnologyTree(final Entity costCalculation, final Entity technology) {
        deleteOperationsTreeIfExists(costCalculation);

        EntityTree sourceOperationsTree = technology.getTreeField(OPERATION_COMPONENTS);
        return createTechnologyInstanceForCalculation(sourceOperationsTree, costCalculation);
    }

    private Entity createTechnologyInstanceForCalculation(final EntityTree sourceTree, final Entity parentEntity) {
        checkArgument(sourceTree != null, "source is null");
        DataDefinition calculationOperationComponentDD = dataDefinitionService.get(PLUGIN_IDENTIFIER,
                MODEL_CALCULATION_OPERATION_COMPONENT);

        // drop old operation components tree
        EntityTree oldCalculationOperationComponents = parentEntity.getTreeField(L_CALCULATION_OPERATION_COMPONENTS);
        if (oldCalculationOperationComponents != null && oldCalculationOperationComponents.getRoot() != null) {
            calculationOperationComponentDD.delete(oldCalculationOperationComponents.getRoot().getId());
        }

        Entity tree = createCalculationOperationComponent(sourceTree.getRoot(), null, calculationOperationComponentDD,
                parentEntity);

        parentEntity.setField(L_CALCULATION_OPERATION_COMPONENTS, Collections.singletonList(tree));
        return parentEntity;
    }

    private Entity createCalculationOperationComponent(final EntityTreeNode sourceTreeNode, final Entity parent,
            final DataDefinition calculationOperationComponentDD, final Entity parentEntity) {
        Entity calculationOperationComponent = calculationOperationComponentDD.create();

        calculationOperationComponent.setField(CalculationOperationComponentFields.PARENT, parent);
        calculationOperationComponent.setField(parentEntity.getDataDefinition().getName(), parentEntity);

        createOrCopyCalculationOperationComponent(sourceTreeNode, calculationOperationComponent, parentEntity);

        return calculationOperationComponent;
    }

    private void createOrCopyCalculationOperationComponent(final EntityTreeNode operationComponent,
            final Entity calculationOperationComponent, final Entity costCalculation) {
        DataDefinition sourceDD = operationComponent.getDataDefinition();

        for (String fieldName : Arrays.asList("priority", "nodeNumber", L_PRODUCTION_IN_ONE_CYCLE,
                "nextOperationAfterProducedQuantity", "operationOffSet", "effectiveOperationRealizationTime",
                "effectiveDateFrom", "effectiveDateTo")) {
            calculationOperationComponent.setField(fieldName, operationComponent.getField(fieldName));
        }

        calculationOperationComponent.setField(L_OPERATION, operationComponent.getBelongsToField(L_OPERATION));
        calculationOperationComponent.setField(
                NEXT_OPERATION_AFTER_PRODUCED_TYPE,
                operationComponent.getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE) == null ? ALL : operationComponent
                        .getField(NEXT_OPERATION_AFTER_PRODUCED_TYPE));

        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(sourceDD.getName())) {
            LOG.error("incorrect model!");
        }
        calculationOperationComponent.setField(L_TECHNOLOGY_OPERATION_COMPONENT, operationComponent);

        calculationOperationComponent.setField(L_ENTITY_TYPE, L_OPERATION);
        List<Entity> newTechnologyInstanceOperationComponents = new ArrayList<>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newTechnologyInstanceOperationComponents.add(createCalculationOperationComponent(child,
                    calculationOperationComponent, calculationOperationComponent.getDataDefinition(), costCalculation));
        }

        calculationOperationComponent.setField("children", newTechnologyInstanceOperationComponents);
    }

    private void deleteOperationsTreeIfExists(final Entity costCalculation) {
        Entity yetAnotherCostCalculation = costCalculation.getDataDefinition().get(costCalculation.getId());
        EntityTree existingOperationsTree = yetAnotherCostCalculation.getTreeField(L_CALCULATION_OPERATION_COMPONENTS);

        if (existingOperationsTree == null || existingOperationsTree.getRoot() == null) {
            return;
        }

        debug();
        EntityTreeNode existingOperationsTreeRoot = existingOperationsTree.getRoot();
        existingOperationsTreeRoot.getDataDefinition().delete(existingOperationsTreeRoot.getId());
    }

    private void debug() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("existing calculation operation components tree will be removed..");
        }
    }

}
