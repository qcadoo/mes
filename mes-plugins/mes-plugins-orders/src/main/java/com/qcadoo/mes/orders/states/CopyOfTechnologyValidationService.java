/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class CopyOfTechnologyValidationService {

    private static final String L_OPERATION = "operation";

    private static final String L_PRODUCTION_IN_ONE_CYCLE_UNIT = "productionInOneCycleUNIT";

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductQuantitiesService productQuantitiyService;

    public void checkIfTechnologyIsNotUsedInActiveOrder(final StateChangeContext stateContext) {
        final Entity technology = stateContext.getOwner();
        if (technologyService.isTechnologyUsedInActiveOrder(technology)) {
            stateContext.addValidationError("technologies.technology.state.error.orderInProgress");
        }
    }

    public void checkConsumingManyProductsFromOneSubOp(final StateChangeContext stateContext, Entity technology2) {
        final Entity technology = stateContext.getOwner();
        final Map<String, Set<String>> parentChildNodeNums = technologyTreeValidationService
                .checkConsumingManyProductsFromOneSubOp(technology.getTreeField(OPERATION_COMPONENTS));

        for (Map.Entry<String, Set<String>> parentChildNodeNum : parentChildNodeNums.entrySet()) {
            for (String childNodeNum : parentChildNodeNum.getValue()) {
                stateContext.addMessage("technologies.technology.validate.global.info.consumingManyProductsFromOneSubOperations",
                        StateMessageType.INFO, false, parentChildNodeNum.getKey(), childNodeNum);
            }
        }
    }

    public boolean checkTopComponentsProducesProductForTechnology(final StateChangeContext stateContext, Entity technology) {
        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final Entity product = savedTechnology.getBelongsToField(TechnologyFields.PRODUCT);
        final EntityTree operations = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        final EntityTreeNode root = operations.getRoot();
        final EntityList productOutComps = root
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
        for (Entity productOutComp : productOutComps) {
            if (product.getId().equals(productOutComp.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId())) {
                return true;
            }
        }
        stateContext.addValidationError("technologies.technology.validate.global.error.noFinalProductInTechnologyTree", false);
        return false;
    }

    public boolean checkIfTechnologyHasAtLeastOneComponent(final StateChangeContext stateContext, Entity technology) {
        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        if (operations != null && !operations.isEmpty()) {
            for (Entity operation : operations) {
                if (L_OPERATION.equals(operation.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                    return true;
                }
            }
        }
        stateContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree", false);
        return false;
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final StateChangeContext stateContext, Entity technology) {
        final DataDefinition technologyDD = technology.getDataDefinition();
        final Entity savedTechnology = technologyDD.get(technology.getId());
        final EntityTree technologyOperations = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        Set<Entity> operations = checkIfConsumesSubOpsProds(technologyOperations);

        if (!operations.isEmpty()) {
            StringBuilder levels = new StringBuilder();

            for (Entity operation : operations) {
                if (levels.length() != 0) {
                    levels.append(", ");
                }

                levels.append(operation.getStringField("nodeNumber"));
            }

            if (operations.size() == 1) {
                stateContext.addFieldValidationError(TechnologyFields.OPERATION_COMPONENTS,
                        "technologies.technology.validate.global.error.treeIsNotValid");
                stateContext.addMessage(
                        "technologies.technology.validate.global.error.operationDontConsumeSubOperationsProducts",
                        StateMessageType.FAILURE, false, levels.toString());
            } else {
                stateContext.addFieldValidationError(TechnologyFields.OPERATION_COMPONENTS,
                        "technologies.technology.validate.global.error.treeIsNotValid");
                stateContext.addMessage(
                        "technologies.technology.validate.global.error.operationDontConsumeSubOperationsProductsPlural",
                        StateMessageType.FAILURE, false, levels.toString());
            }
            return false;
        }

        return true;
    }

    private Set<Entity> checkIfConsumesSubOpsProds(final EntityTree technologyOperations) {
        Set<Entity> operations = new HashSet<Entity>();

        for (Entity technologyOperation : technologyOperations) {
            final Entity parent = technologyOperation.getBelongsToField(TechnologyOperationComponentFields.PARENT);
            if (parent == null
                    || TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY.equals(parent
                            .getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                continue;
            }
            final EntityList prodsIn = parent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

            if (L_OPERATION.equals(technologyOperation.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                final EntityList prodsOut = technologyOperation
                        .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

                if (prodsIn == null) {
                    operations.add(parent);
                    continue;
                }

                if (prodsIn.isEmpty()) {
                    operations.add(parent);
                    continue;
                }

                if (prodsOut == null) {
                    operations.add(technologyOperation);
                    continue;
                }

                if (prodsOut.isEmpty()) {
                    operations.add(technologyOperation);
                    continue;
                }

                if (!checkIfAtLeastOneCommonElement(prodsOut, prodsIn)) {
                    operations.add(technologyOperation);
                }
            } else {
                final Entity prodOut = technologyOperation
                        .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY);

                if (prodOut == null) {
                    operations.add(parent);
                    continue;
                }

                if (prodsIn == null) {
                    operations.add(technologyOperation);
                    continue;
                }

                if (prodsIn.isEmpty()) {
                    operations.add(technologyOperation);
                    continue;
                }

                if (!checkIfAtLeastOneCommonElement(Arrays.asList(prodOut), prodsIn)) {
                    operations.add(technologyOperation);
                }
            }
        }

        return operations;
    }

    private boolean checkIfAtLeastOneCommonElement(final List<Entity> prodsIn, final List<Entity> prodsOut) {
        for (Entity prodOut : prodsOut) {
            for (Entity prodIn : prodsIn) {
                if (prodIn.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId()
                        .equals(prodOut.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIfTreeOperationIsValid(final StateChangeContext stateContext, final Entity technology) {

        Entity techFromDB = technology.getDataDefinition().get(technology.getId());
        if (techFromDB == null) {
            return true;
        }
        String message = "";
        boolean isValid = true;
        for (Entity operationComponent : techFromDB.getTreeField("operationComponents")) {
            boolean valid = true;

            valid = valid && checkIfUnitMatch(operationComponent);
            valid = valid && checkIfUnitsInTechnologyMatch(operationComponent);

            if (!valid) {
                isValid = false;
                message = createMessageForValidationErrors(message, operationComponent);
            }
        }
        if (!isValid) {
            stateContext.addValidationError("technologies.technology.validate.error.OperationTreeNotValid", false, message);
        }
        return isValid;
    }

    public boolean checkOperationOutputQuantities(final StateChangeContext stateChangeContext, final Entity technologyEntity) {
        Entity technology = technologyEntity.getDataDefinition().get(technologyEntity.getId());

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

        if (!messages.isEmpty()) {
            stateChangeContext.addValidationError("technologies.technology.validate.global.error.treeIsNotValid");
            StringBuilder builder = new StringBuilder();
            for (String message : messages) {
                builder.append(message.toString());
                builder.append(", ");
            }
            stateChangeContext.addMessage("technologies.technology.validate.error.invalidQuantity", StateMessageType.FAILURE,
                    false, builder.toString());
        }
        return messages.isEmpty();
    }

    private String createMessageForValidationErrors(final String message, final Entity entity) {
        List<ErrorMessage> errors = Lists.newArrayList();
        if (!entity.getErrors().isEmpty()) {
            errors.addAll(entity.getErrors().values());
        }
        if (!entity.getGlobalErrors().isEmpty()) {
            errors.addAll(entity.getGlobalErrors());
        }

        StringBuilder errorMessages = new StringBuilder();
        errorMessages.append(message).append("\n");
        for (ErrorMessage error : errors) {

            if (!error.getMessage().equals("qcadooView.validate.global.error.custom")) {
                String translatedErrorMessage = translationService.translate(error.getMessage(), Locale.getDefault(),
                        error.getVars());
                errorMessages.append("- ").append(translatedErrorMessage);
                errorMessages.append(",\n ");
            }
        }
        String msg = errorMessages.toString();
        int length = msg.length();
        String lastSign = msg.substring(length - 3);
        if (",\n ".equals(lastSign)) {
            msg = msg.substring(0, length - 3);
        }
        return msg;
    }

    public boolean checkIfUnitMatch(final Entity technologyOperationComponent) {
        DataDefinition dataDefinition = technologyOperationComponent.getDataDefinition();
        String productionInOneCycleUnit = technologyOperationComponent.getStringField(L_PRODUCTION_IN_ONE_CYCLE_UNIT);
        String nextOperationAfterProducedQuantityUnit = technologyOperationComponent
                .getStringField("nextOperationAfterProducedQuantityUNIT");
        String nextOperationAfterProducedType = (String) technologyOperationComponent.getField("nextOperationAfterProducedType");

        if (productionInOneCycleUnit == null) {
            return true;
        }

        if ("02specified".equals(nextOperationAfterProducedType)
                && !productionInOneCycleUnit.equals(nextOperationAfterProducedQuantityUnit)) {
            technologyOperationComponent.addError(dataDefinition.getField("nextOperationAfterProducedQuantityUNIT"),
                    "technologies.operationDetails.validate.error.UnitsNotMatch");
            return false;
        }
        return true;

    }

    public boolean checkIfUnitsInTechnologyMatch(final Entity technologyOperationComponent) {
        final String productionInOneCycleUNIT = technologyOperationComponent.getStringField(L_PRODUCTION_IN_ONE_CYCLE_UNIT);
        DataDefinition dataDefinition = technologyOperationComponent.getDataDefinition();
        if (productionInOneCycleUNIT == null) {
            technologyOperationComponent.addError(dataDefinition.getField(L_PRODUCTION_IN_ONE_CYCLE_UNIT),
                    "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
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
                technologyOperationComponent.addError(dataDefinition.getField(L_PRODUCTION_IN_ONE_CYCLE_UNIT),
                        "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
                return false;
            }
        }
        return true;
    }

    public boolean checkIfTechnologyTreeIsSet(final StateChangeContext stateChangeContext, final Entity technology) {
        final EntityTree operations = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        if (operations.isEmpty()) {
            stateChangeContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree");
            return false;
        }
        return true;
    }

    private BigDecimal getProductionInOneCycle(final Entity operationComponent) {
        String entityType = operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE);
        if (TechnologyOperationComponentType.OPERATION.getStringValue().equals(entityType)) {
            return operationComponent.getDecimalField("productionInOneCycle");
        } else if (TechnologyOperationComponentType.REFERENCE_TECHNOLOGY.getStringValue().equals(entityType)) {
            Entity refOperationComp = operationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY)
                    .getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();
            return refOperationComp.getDecimalField("productionInOneCycle");
        } else {
            throw new IllegalStateException("operationComponent has illegal type, id = " + operationComponent.getId());
        }
    }
}
