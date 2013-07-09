package com.qcadoo.mes.operationCostCalculations;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class OperationCostCalculationTreeBuilder {

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_ORDER = "order";

    private static final String L_OPERATION = "operation";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_PRODUCTION_IN_ONE_CYCLE = "productionInOneCycle";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private static final String L_CALCULATION_OPERATION_COMPONENTS = "calculationOperationComponents";

    private static final Logger LOG = LoggerFactory.getLogger(OperationCostCalculationTreeBuilder.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public Entity copyTechnologyTree(final Entity costCalculation) {
        deleteOperationsTreeIfExists(costCalculation);

        Entity operationsTreeContainer;
        if (costCalculation.getBelongsToField(L_ORDER) == null) {
            operationsTreeContainer = costCalculation.getBelongsToField(L_TECHNOLOGY);
        } else {
            operationsTreeContainer = costCalculation.getBelongsToField(L_ORDER).getBelongsToField(L_TECHNOLOGY);
        }
        EntityTree sourceOperationsTree = operationsTreeContainer.getTreeField(OPERATION_COMPONENTS);
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

        parentEntity.setField(L_CALCULATION_OPERATION_COMPONENTS, asList(tree));
        return parentEntity;
    }

    private Entity createCalculationOperationComponent(final EntityTreeNode sourceTreeNode, final Entity parent,
            final DataDefinition calculationOperationComponentDD, final Entity parentEntity) {
        Entity calculationOperationComponent = calculationOperationComponentDD.create();

        calculationOperationComponent.setField("parent", parent);
        calculationOperationComponent.setField(parentEntity.getDataDefinition().getName(), parentEntity);

        // FIXME MAKU ask ALBR about strategy for reference technology pointers.
        if (L_OPERATION.equals(sourceTreeNode.getField(L_ENTITY_TYPE))) {
            createOrCopyCalculationOperationComponent(sourceTreeNode, calculationOperationComponent, parentEntity);
        } else {
            Entity referenceTechnology = sourceTreeNode.getBelongsToField("referenceTechnology");
            createOrCopyCalculationOperationComponent(referenceTechnology.getTreeField("operationComponents").getRoot(),
                    calculationOperationComponent, parentEntity);
        }

        return calculationOperationComponent;
    }

    private void createOrCopyCalculationOperationComponent(final EntityTreeNode operationComponent,
            final Entity calculationOperationComponent, final Entity costCalculation) {
        DataDefinition sourceDD = operationComponent.getDataDefinition();

        for (String fieldName : Arrays.asList("priority", "nodeNumber", L_PRODUCTION_IN_ONE_CYCLE,
                "nextOperationAfterProducedQuantity", "operationOffSet", "effectiveOperationRealizationTime",
                "effectiveDateFrom", "effectiveDateTo", "pieceworkCost", "numberOfOperations")) {
            calculationOperationComponent.setField(fieldName, operationComponent.getField(fieldName));
        }

        calculationOperationComponent.setField(L_OPERATION, operationComponent.getBelongsToField(L_OPERATION));
        calculationOperationComponent.setField(
                "nextOperationAfterProducedType",
                operationComponent.getField("nextOperationAfterProducedType") == null ? "01all" : operationComponent
                        .getField("nextOperationAfterProducedType"));

        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(sourceDD.getName())) {
            LOG.error("incorrect model!");
        }
        calculationOperationComponent.setField(L_TECHNOLOGY_OPERATION_COMPONENT, operationComponent);

        calculationOperationComponent.setField(L_ENTITY_TYPE, L_OPERATION);
        List<Entity> newTechnologyInstanceOperationComponents = new ArrayList<Entity>();

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

        debug("existing calculation operation components tree will be removed..");
        EntityTreeNode existingOperationsTreeRoot = existingOperationsTree.getRoot();
        existingOperationsTreeRoot.getDataDefinition().delete(existingOperationsTreeRoot.getId());
    }

    private void debug(final String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }

}
