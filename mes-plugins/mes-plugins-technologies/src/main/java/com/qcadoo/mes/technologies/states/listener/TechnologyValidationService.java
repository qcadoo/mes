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
package com.qcadoo.mes.technologies.states.listener;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class TechnologyValidationService {

    private static final String L_PRODUCTION_IN_ONE_CYCLE_UNIT = "productionInOneCycleUNIT";

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    public void checkIfEveryOperationHasInComponents(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            if (operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)
                    .isEmpty()) {
                stateContext.addValidationError("technologies.technology.validate.global.error.noInputComponents",
                        operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                                .getStringField(OperationFields.NUMBER),
                        operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

                return;
            }
        }
    }

    public void checkIfTechnologyIsNotUsedInActiveOrder(final StateChangeContext stateContext) {
        final Entity technology = stateContext.getOwner();

        if (technologyService.isTechnologyUsedInActiveOrder(technology)) {
            stateContext.addValidationError("technologies.technology.state.error.orderInProgress");
        }
    }

    public void checkConsumingManyProductsFromOneSubOp(final StateChangeContext stateContext) {
        final Entity technology = stateContext.getOwner();
        final Map<String, Set<String>> parentChildNodeNums = technologyTreeValidationService
                .checkConsumingManyProductsFromOneSubOp(technology.getTreeField(OPERATION_COMPONENTS));

        for (Map.Entry<String, Set<String>> parentChildNodeNum : parentChildNodeNums.entrySet()) {
            for (String childNodeNum : parentChildNodeNum.getValue()) {
                stateContext.addMessage("technologies.technology.validate.global.info.consumingManyProductsFromOneSubOperations",
                        StateMessageType.INFO, parentChildNodeNum.getKey(), childNodeNum);
            }
        }
    }

    public boolean checkTopComponentsProducesProductForTechnology(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final Entity product = savedTechnology.getBelongsToField(TechnologyFields.PRODUCT);
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        final EntityTreeNode root = operationComponents.getRoot();

        if (Objects.nonNull(root)) {
            final EntityList operationProductOutComponents = root
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                if (product.getId().equals(
                        operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId())) {
                    return true;
                }
            }
        }

        stateContext.addValidationError("technologies.technology.validate.global.error.noFinalProductInTechnologyTree");

        return false;
    }

    public boolean checkIfTechnologyHasAtLeastOneComponent(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (!operationComponents.isEmpty()) {
            return true;
        }

        stateContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree");

        return false;
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();

        final DataDefinition technologyDD = technology.getDataDefinition();
        final Entity savedTechnology = technologyDD.get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        Set<Entity> operations = checkIfConsumesSubOpsProds(operationComponents);

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
                stateContext.addMessage("technologies.technology.validate.global.error.operationDontConsumeSubOperationsProducts",
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

    private Set<Entity> checkIfConsumesSubOpsProds(final EntityTree operationComponents) {
        Set<Entity> operations = Sets.newHashSet();

        for (Entity operationComponent : operationComponents) {
            final Entity parent = operationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT);

            if (Objects.isNull(parent)) {
                continue;
            }

            final EntityList operationProductInComponents = parent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
            final EntityList operationProductOutComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            if (Objects.isNull(operationProductInComponents)) {
                operations.add(parent);

                continue;
            }

            if (operationProductInComponents.isEmpty()) {
                operations.add(parent);

                continue;
            }

            if (Objects.isNull(operationProductOutComponents)) {
                operations.add(operationComponent);

                continue;
            }

            if (operationProductOutComponents.isEmpty()) {
                operations.add(operationComponent);

                continue;
            }

            if (!checkIfAtLeastOneCommonElement(operationProductOutComponents, operationProductInComponents)) {
                operations.add(operationComponent);
            }
        }

        return operations;
    }

    private boolean checkIfAtLeastOneCommonElement(final List<Entity> operationProductInComponents,
            final List<Entity> operationProductOutComponents) {
        for (Entity operationProductOutComponent : operationProductOutComponents) {
            for (Entity operationProductInComponent : operationProductInComponents) {
                if (operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId().equals(
                        operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkIfTreeOperationIsValid(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();

        if (Objects.isNull(technology) || Objects.isNull(technology.getId())) {
            return true;
        }

        Entity techFromDB = technology.getDataDefinition().get(technology.getId());

        if (Objects.isNull(techFromDB)) {
            return true;
        }

        String message = "";

        boolean isValid = true;

        for (Entity operationComponent : techFromDB.getTreeField(OPERATION_COMPONENTS)) {
            boolean valid = true;

            valid = valid && checkIfUnitMatch(operationComponent);
            valid = valid && checkIfUnitsInTechnologyMatch(operationComponent);

            if (!valid) {
                isValid = false;

                message = createMessageForValidationErrors(message, operationComponent);
            }
        }

        if (!isValid) {
            stateContext.addValidationError("technologies.technology.validate.error.OperationTreeNotValid", message);
        }

        return isValid;
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

        if (Objects.isNull(productionInOneCycleUnit)) {
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

        if (Objects.isNull(productionInOneCycleUNIT)) {
            technologyOperationComponent.addError(dataDefinition.getField(L_PRODUCTION_IN_ONE_CYCLE_UNIT),
                    "technologies.operationDetails.validate.error.OutputUnitsNotMatch");

            return false;
        }

        if (Objects.isNull(technologyOperationComponent.getId())) {
            return true;
        }

        final Entity outputProduct = productQuantitiesService
                .getOutputProductsFromOperationComponent(technologyOperationComponent);

        if (Objects.nonNull(outputProduct)) {
            final String outputProductionUnit = outputProduct.getBelongsToField(PRODUCT).getStringField(UNIT);

            if (!productionInOneCycleUNIT.equals(outputProductionUnit)) {
                technologyOperationComponent.addError(dataDefinition.getField(L_PRODUCTION_IN_ONE_CYCLE_UNIT),
                        "technologies.operationDetails.validate.error.OutputUnitsNotMatch");

                return false;
            }
        }

        return true;
    }

    public boolean checkIfTechnologyTreeIsSet(final StateChangeContext stateChangeContext) {
        final Entity technology = stateChangeContext.getOwner();
        final EntityTree operations = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (operations.isEmpty()) {
            stateChangeContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree");

            return false;
        }

        return true;
    }

    public boolean checkTechnologyCycles(final StateChangeContext stateChangeContext) {
        final Entity technology = stateChangeContext.getOwner();

        Set<Long> usedTechnologies = Sets.newHashSet();

        usedTechnologies.add(technology.getId());

        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
        Entity operation = productStructureTreeService.findOperationForProductAndTechnology(product, technology);

        return checkCycleForSubProducts(stateChangeContext, operation, usedTechnologies);
    }

    private boolean checkCycleForSubProducts(final StateChangeContext stateChangeContext, final Entity operation,
            final Set<Long> usedTechnologies) {
        if (Objects.isNull(operation)) {
            return true;
        }

        EntityList productInComponents = operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

        for (Entity productInComp : productInComponents) {
            Set<Long> copyUsedTechnologies = new HashSet<>(usedTechnologies);

            Entity product = productInComp.getBelongsToField(OperationProductInComponentFields.PRODUCT);

            Entity subOperation = productStructureTreeService.findOperationForProductWithinChildren(product, operation);
            Entity subTechnology = productStructureTreeService.findTechnologyForProduct(product);

            subTechnology = useChangingTechnologyInCheckingCycle(stateChangeContext, product, subTechnology);

            if (Objects.nonNull(subTechnology)) {
                if (copyUsedTechnologies.contains(subTechnology.getId())) {
                    stateChangeContext.addValidationError(
                            "technologies.technologyDetails.window.productStructure.productStructureForm.duplicateProductForTechnology",
                            product.getStringField(ProductFields.NUMBER) + " " + product.getStringField(ProductFields.NAME));

                    return false;
                } else {
                    if (Objects.isNull(subOperation)) {
                        Entity operationForTechnology = productStructureTreeService.findOperationForProductAndTechnology(product,
                                subTechnology);

                        copyUsedTechnologies.add(subTechnology.getId());

                        boolean hasNotCycle = checkCycleForSubProducts(stateChangeContext, operationForTechnology,
                                copyUsedTechnologies);

                        if (!hasNotCycle) {
                            return false;
                        }
                    } else {
                        boolean hasNotCycle = checkCycleForSubProducts(stateChangeContext, subOperation, copyUsedTechnologies);

                        if (!hasNotCycle) {
                            return false;
                        }
                    }
                }
            } else if (Objects.nonNull(subOperation)) {
                boolean hasNotCycle = checkCycleForSubProducts(stateChangeContext, subOperation, copyUsedTechnologies);

                if (!hasNotCycle) {
                    return false;
                }
            }
        }

        return true;
    }

    private Entity useChangingTechnologyInCheckingCycle(final StateChangeContext stateChangeContext, final Entity product,
            final Entity subTechnology) {
        if (Objects.isNull(subTechnology) && Objects.nonNull(product)
                && stateChangeContext.getOwner().getBelongsToField(TechnologyFields.PRODUCT).getId().equals(product.getId())) {
            return stateChangeContext.getOwner();
        }

        return subTechnology;
    }

}
