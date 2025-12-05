/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies.states.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static java.util.Comparator.reverseOrder;

@Service
public class TechnologyValidationService {

    private static final String L_MM = "mm";

    private static final String L_CM = "cm";

    private static final String L_PRODUCTION_IN_ONE_CYCLE_UNIT = "productionInOneCycleUNIT";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_TREE_IS_NOT_VALID = "technologies.technology.validate.global.error.treeIsNotValid";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_OPERATION_DONT_CONSUME_SUB_OPERATIONS_PRODUCTS = "technologies.technology.validate.global.error.operationDontConsumeSubOperationsProducts";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_OPERATION_DONT_CONSUME_SUB_OPERATIONS_PRODUCTS_PLURAL = "technologies.technology.validate.global.error.operationDontConsumeSubOperationsProductsPlural";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    public boolean checkIfEveryInComponentsHasQuantities(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            List<Entity> operationProductInComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

            for (Entity operationProductInComponent : operationProductInComponents) {
                boolean differentProductsInDifferentSizes = operationProductInComponent
                        .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
                boolean variousQuantitiesInProductsBySize = operationProductInComponent
                        .getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE);

                if (!differentProductsInDifferentSizes && Objects
                        .isNull(operationProductInComponent.getDecimalField(OperationProductInComponentFields.QUANTITY))) {
                    stateChangeContext.addValidationError(
                            "technologies.technology.validate.global.error.inComponentsQuantitiesNotFilled",
                            operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                                    .getStringField(OperationFields.NUMBER),
                            operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

                    return false;
                }

                if (differentProductsInDifferentSizes && variousQuantitiesInProductsBySize) {
                    boolean quantitiesOrProductsNotSet = false;

                    if (operationProductInComponent.getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS)
                            .isEmpty()) {
                        quantitiesOrProductsNotSet = true;
                    } else if (operationProductInComponent
                            .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS).stream()
                            .anyMatch(pbs -> Objects.isNull(pbs.getDecimalField(ProductBySizeGroupFields.QUANTITY)))) {
                        quantitiesOrProductsNotSet = true;
                    }

                    if (quantitiesOrProductsNotSet) {
                        stateChangeContext.addValidationError(
                                "technologies.technology.validate.global.error.variousQuantitiesInProductsBySizeNotFilled",
                                operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                                        .getStringField(OperationFields.NUMBER),
                                operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean checkIfWasteProductsIsRightMarked(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        final EntityTreeNode root = operationComponents.getRoot();

        for (Entity operationComponent : operationComponents) {
            List<Entity> operationProductOutComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            long notWasteCount = operationProductOutComponents.stream()
                    .filter(operationProductOutComponent -> !operationProductOutComponent.getBooleanField(OperationProductOutComponentFields.WASTE)).count();

            if ((!operationComponent.getId().equals(root.getId()) && notWasteCount > 1) || notWasteCount == 0) {
                stateChangeContext.addValidationError(
                        "technologies.technology.validate.global.error.noProductsWithWasteFlagNotMarked",
                        operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                                .getStringField(OperationFields.NUMBER),
                        operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

                return false;
            }
        }

        return true;
    }

    public boolean checkIfRootOperationIsSubOrder(StateChangeContext stateChangeContext) {
        if (PluginUtils.isEnabled("techSubcontracting")) {
            Entity technology = stateChangeContext.getOwner();
            final EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
            final EntityTreeNode root = operationComponents.getRoot();
            if (root.getBooleanField("isSubcontracting")) {
                stateChangeContext.addValidationError(
                        "technologies.technology.validate.global.error.rootOperationIsSubOrder");
                return false;
            }
        }

        return false;
    }

    public void checkIfEveryOperationHasInComponents(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            boolean isValid = true;

            List<Entity> operationProductInComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

            if (operationProductInComponents.isEmpty()) {
                isValid = false;
            } else {
                for (Entity operationProductInComponent : operationProductInComponents) {
                    boolean differentProductsInDifferentSizes = operationProductInComponent
                            .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);

                    if (differentProductsInDifferentSizes) {
                        List<Entity> productBySizeGroups = operationProductInComponent
                                .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);

                        isValid = isValid && !productBySizeGroups.isEmpty();
                    } else {
                        Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

                        isValid = isValid && Objects.nonNull(product);
                    }
                }
            }

            if (!isValid) {
                stateChangeContext.addValidationError("technologies.technology.validate.global.error.noInputComponents",
                        operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                                .getStringField(OperationFields.NUMBER),
                        operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

                return;
            }
        }
    }

    public void checkIfTechnologyIsNotUsedInActiveOrder(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        if (technologyService.isTechnologyUsedInActiveOrder(technology)) {
            stateChangeContext.addValidationError("technologies.technology.state.error.orderInProgress");
        }
    }

    public void checkConsumingManyProductsFromOneSubOp(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final Map<String, Set<String>> parentChildNodeNumbers = technologyTreeValidationService
                .checkConsumingManyProductsFromOneSubOp(technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS));

        for (Map.Entry<String, Set<String>> parentChildNodeNumber : parentChildNodeNumbers.entrySet()) {
            for (String childNodeNumber : parentChildNodeNumber.getValue()) {
                stateChangeContext.addMessage(
                        "technologies.technology.validate.global.info.consumingManyProductsFromOneSubOperations",
                        StateMessageType.INFO, parentChildNodeNumber.getKey(), childNodeNumber);
            }
        }
    }

    public boolean checkTopComponentsProducesProductForTechnology(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        return checkTopComponentsProducesProductForTechnology(stateChangeContext, null, technology);
    }

    public boolean checkTopComponentsProducesProductForTechnology(final FormComponent technologyForm,
                                                                  final Entity technology) {
        return checkTopComponentsProducesProductForTechnology(null, technologyForm, technology);
    }

    private boolean checkTopComponentsProducesProductForTechnology(final StateChangeContext stateChangeContext,
                                                                   final FormComponent technologyForm,
                                                                   final Entity technology) {
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

        if (Objects.nonNull(technologyForm)) {
            technologyForm.addMessage("technologies.technology.validate.global.error.noFinalProductInTechnologyTree",
                    MessageType.FAILURE);
        } else {
            stateChangeContext.addValidationError("technologies.technology.validate.global.error.noFinalProductInTechnologyTree");
        }

        return false;
    }

    public boolean checkIfTechnologyHasAtLeastOneComponent(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (!operationComponents.isEmpty()) {
            return true;
        }

        stateChangeContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree");

        return false;
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final DataDefinition technologyDD = technology.getDataDefinition();
        final Entity savedTechnology = technologyDD.get(technology.getId());

        return checkIfOperationsUsesSubOperationsProds(stateChangeContext, null, savedTechnology);
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final FormComponent technologyForm,
                                                           final Entity technology) {
        return checkIfOperationsUsesSubOperationsProds(null, technologyForm, technology);
    }

    public boolean checkIfOperationsUsesSubOperationsWasteProds(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final DataDefinition technologyDD = technology.getDataDefinition();
        final Entity savedTechnology = technologyDD.get(technology.getId());

        return checkIfOperationsUsesSubOperationsWasteProds(stateChangeContext, savedTechnology);
    }

    public boolean checkIfOperationsUsesSubOperationsWasteProds(final StateChangeContext stateChangeContext,
                                                                final Entity technology) {
        final EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        Set<Entity> operations = checkIfConsumesSubOpsWasteProds(operationComponents);


        if (!operations.isEmpty()) {
            StringBuilder levels = new StringBuilder();

            for (Entity operation : operations) {
                if (levels.length() != 0) {
                    levels.append(", ");
                }

                levels.append(operation.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));
            }
            stateChangeContext.addFieldValidationError(TechnologyFields.OPERATION_COMPONENTS,
                    L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_TREE_IS_NOT_VALID);
            stateChangeContext.addMessage(
                    "technologies.technology.validate.global.error.operationDontConsumeSubOperationsWasteProducts",
                    StateMessageType.FAILURE, false, levels.toString());
            return false;
        }


        return true;

    }

    private Set<Entity> checkIfConsumesSubOpsWasteProds(EntityTree operationComponents) {
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

            if (checkIfAtLeastOneWasteElement(operationProductOutComponents, operationProductInComponents)) {
                operations.add(operationComponent);
            }

        }

        return operations;

    }

    private boolean checkIfOperationsUsesSubOperationsProds(final StateChangeContext stateChangeContext,
                                                            final FormComponent technologyForm,
                                                            final Entity technology) {
        final EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        Set<Entity> operations = checkIfConsumesSubOpsProds(operationComponents);

        if (!operations.isEmpty()) {
            StringBuilder levels = new StringBuilder();

            for (Entity operation : operations) {
                if (levels.length() != 0) {
                    levels.append(", ");
                }

                levels.append(operation.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));
            }

            if (operations.size() == 1) {
                if (Objects.nonNull(technologyForm)) {
                    technologyForm.addMessage(L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_TREE_IS_NOT_VALID,
                            MessageType.FAILURE);
                    technologyForm.addMessage(
                            L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_OPERATION_DONT_CONSUME_SUB_OPERATIONS_PRODUCTS,
                            MessageType.FAILURE, false, levels.toString());
                } else {
                    stateChangeContext.addFieldValidationError(TechnologyFields.OPERATION_COMPONENTS,
                            L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_TREE_IS_NOT_VALID);
                    stateChangeContext.addMessage(
                            L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_OPERATION_DONT_CONSUME_SUB_OPERATIONS_PRODUCTS,
                            StateMessageType.FAILURE, false, levels.toString());
                }
            } else {
                if (Objects.nonNull(technologyForm)) {
                    technologyForm.addMessage(L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_TREE_IS_NOT_VALID,
                            MessageType.FAILURE);
                    technologyForm.addMessage(
                            L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_OPERATION_DONT_CONSUME_SUB_OPERATIONS_PRODUCTS_PLURAL,
                            MessageType.FAILURE, false, levels.toString());
                } else {
                    stateChangeContext.addFieldValidationError(TechnologyFields.OPERATION_COMPONENTS,
                            L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_TREE_IS_NOT_VALID);
                    stateChangeContext.addMessage(
                            L_TECHNOLOGIES_TECHNOLOGY_VALIDATE_GLOBAL_ERROR_OPERATION_DONT_CONSUME_SUB_OPERATIONS_PRODUCTS_PLURAL,
                            StateMessageType.FAILURE, false, levels.toString());
                }
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

    private boolean checkIfAtLeastOneCommonElement(final List<Entity> operationProductOutComponents,
                                                   final List<Entity> operationProductInComponents) {
        for (Entity operationProductOutComponent : operationProductOutComponents) {
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

                if (Objects.nonNull(product) && product.getId().equals(
                        operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkIfAtLeastOneWasteElement(final List<Entity> operationProductOutComponents,
                                                  final List<Entity> operationProductInComponents) {
        for (Entity operationProductOutComponent : operationProductOutComponents) {
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

                if (Objects.nonNull(product) && product.getId().equals(
                        operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId()) && operationProductOutComponent.getBooleanField(OperationProductOutComponentFields.WASTE)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkIfTreeOperationIsValid(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        if (Objects.isNull(technology) || Objects.isNull(technology.getId())) {
            return true;
        }

        Entity techFromDB = technology.getDataDefinition().get(technology.getId());

        if (Objects.isNull(techFromDB)) {
            return true;
        }

        String message = "";

        boolean isValid = true;

        for (Entity operationComponent : techFromDB.getTreeField(TechnologyFields.OPERATION_COMPONENTS)) {
            boolean valid = true;

            valid = valid && checkIfUnitMatch(operationComponent);
            valid = valid && checkIfUnitsInTechnologyMatch(operationComponent);

            if (!valid) {
                isValid = false;

                message = createMessageForValidationErrors(message, operationComponent);
            }
        }

        if (!isValid) {
            stateChangeContext.addValidationError("technologies.technology.validate.error.OperationTreeNotValid", message);
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

        boolean addedError = false;

        for (ErrorMessage error : errors) {
            if (!error.getMessage().equals("qcadooView.validate.global.error.custom")) {
                if (addedError) {
                    errorMessages.append(",\n ");
                } else {
                    addedError = true;
                }

                String translatedErrorMessage = translationService.translate(error.getMessage(), LocaleContextHolder.getLocale(),
                        error.getVars());

                errorMessages.append("- ").append(translatedErrorMessage);
            }
        }

        return errorMessages.toString();
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
        DataDefinition dataDefinition = technologyOperationComponent.getDataDefinition();

        String productionInOneCycleUNIT = technologyOperationComponent.getStringField(L_PRODUCTION_IN_ONE_CYCLE_UNIT);

        if (Objects.isNull(productionInOneCycleUNIT)) {
            technologyOperationComponent.addError(dataDefinition.getField(L_PRODUCTION_IN_ONE_CYCLE_UNIT),
                    "technologies.operationDetails.validate.error.OutputUnitsNotMatch",
                    technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

            return false;
        }

        if (Objects.isNull(technologyOperationComponent.getId())) {
            return true;
        }

        //metoda rzuca wyjątkiem - przez co zawiesza się przejście stanu
        //fixem jest oifowanie wyżej
        final Entity outputProduct = technologyService
                .getMainOutputProductComponent(technologyOperationComponent);

        if (Objects.nonNull(outputProduct)) {
            final String outputProductionUnit = outputProduct.getBelongsToField(TechnologyFields.PRODUCT).getStringField(UNIT);

            if (!productionInOneCycleUNIT.equals(outputProductionUnit)) {
                technologyOperationComponent.addError(dataDefinition.getField(L_PRODUCTION_IN_ONE_CYCLE_UNIT),
                        "technologies.operationDetails.validate.error.OutputUnitsNotMatch",
                        technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER));

                return false;
            }
        }

        return true;
    }

    public boolean checkIfTechnologyTreeIsSet(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        return checkIfTechnologyTreeIsSet(stateChangeContext, null, technology);
    }

    public boolean checkIfTechnologyTreeIsSet(final FormComponent technologyForm, final Entity technology) {
        return checkIfTechnologyTreeIsSet(null, technologyForm, technology);
    }

    public boolean checkIfTechnologyTreeIsSet(final StateChangeContext stateChangeContext,
                                              final FormComponent technologyForm,
                                              final Entity technology) {
        final EntityTree operations = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (operations.isEmpty()) {
            if (Objects.nonNull(technologyForm)) {
                technologyForm.addMessage("technologies.technology.validate.global.error.emptyTechnologyTree",
                        MessageType.FAILURE);
            } else {
                stateChangeContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree");
            }

            return false;
        }

        return true;
    }

    public boolean checkTechnologyCycles(final StateChangeContext stateChangeContext, String targetState) {
        Entity technology = stateChangeContext.getOwner();

        Set<Long> usedTechnologies = Sets.newHashSet();

        usedTechnologies.add(technology.getId());

        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
        Entity operation = productStructureTreeService.findOperationForProductAndTechnology(product, technology);

        return checkCycleForSubProducts(stateChangeContext, operation, usedTechnologies, targetState);
    }

    private boolean checkCycleForSubProducts(final StateChangeContext stateChangeContext, final Entity operation,
                                             final Set<Long> usedTechnologies, String targetState) {
        if (Objects.isNull(operation)) {
            return true;
        }

        Entity parameter = parameterService.getParameter();

        boolean checkDuplicateCyclesThroughoutProductStructure = parameter.getBooleanField(ParameterFieldsT.CHECK_DUPLICATE_CYCLES_THROUGHOUT_PRODUCT_STRUCTURE);

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
                } else if (checkDuplicateCyclesThroughoutProductStructure || TechnologyStateStringValues.ACCEPTED.equals(targetState)) {
                    if (Objects.isNull(subOperation)) {
                        Entity operationForTechnology = productStructureTreeService.findOperationForProductAndTechnology(product,
                                subTechnology);

                        copyUsedTechnologies.add(subTechnology.getId());

                        boolean hasNotCycle = checkCycleForSubProducts(stateChangeContext, operationForTechnology,
                                copyUsedTechnologies, targetState);

                        if (!hasNotCycle) {
                            return false;
                        }
                    } else {
                        boolean hasNotCycle = checkCycleForSubProducts(stateChangeContext, subOperation, copyUsedTechnologies, targetState);

                        if (!hasNotCycle) {
                            return false;
                        }
                    }
                }
            } else if (Objects.nonNull(subOperation)) {
                boolean hasNotCycle = checkCycleForSubProducts(stateChangeContext, subOperation, copyUsedTechnologies, targetState);

                if (!hasNotCycle) {
                    return false;
                }
            }
        }

        return true;
    }

    private Entity useChangingTechnologyInCheckingCycle(final StateChangeContext stateChangeContext,
                                                        final Entity product,
                                                        final Entity subTechnology) {
        Entity technology = stateChangeContext.getOwner();

        if (Objects.isNull(subTechnology) && Objects.nonNull(product)) {
            if (technology.getBelongsToField(TechnologyFields.PRODUCT).getId().equals(product.getId())) {
                return technology;
            }
        }

        return subTechnology;
    }

    public boolean checkDifferentProductsInDifferentSizes(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final Entity product = savedTechnology.getBelongsToField(TechnologyFields.PRODUCT);

        if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))) {
            final EntityTree operationComponents = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

            for (Entity operationComponent : operationComponents) {
                final List<Entity> operationProductInComponents = operationComponent
                        .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

                for (Entity operationProductInComponent : operationProductInComponents) {
                    boolean differentProductsInDifferentSizes = operationProductInComponent
                            .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);

                    if (differentProductsInDifferentSizes) {
                        stateChangeContext.addValidationError(
                                "technologies.technology.validate.global.error.differentProductsInDifferentSizes");

                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void checkIfInputProductPricesSet(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        List<Entity> products = technologyService.getComponentsWithProductWithSizes(technology.getId());

        for (Entity product : products) {
            if (Objects.nonNull(product)
                    && (Objects.isNull(product.getDecimalField("nominalCost"))
                    || BigDecimal.ZERO.compareTo(product.getDecimalField("nominalCost")) == 0)
                    && (Objects.isNull(product.getDecimalField("lastPurchaseCost"))
                    || BigDecimal.ZERO.compareTo(product.getDecimalField("lastPurchaseCost")) == 0)) {
                stateChangeContext.addMessage("technologies.technology.validate.info.inputProductPricesNotSet",
                        StateMessageType.INFO, product.getStringField(ProductFields.NUMBER), product.getStringField(ProductFields.NAME));
            }

        }
    }

    public void checkDimensionControlOfProducts(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();

        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

        if (Objects.nonNull(product)) {
            Entity parameter = parameterService.getParameter();

            List<Entity> dimensionControlAttributes = parameter.getHasManyField(ParameterFieldsT.DIMENSION_CONTROL_ATTRIBUTES);
            List<Entity> productAttributeValues = product.getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES);

            List<Entity> filteredProductAttributeValues = filterProductAttributeValues(productAttributeValues, dimensionControlAttributes);

            if (!filteredProductAttributeValues.isEmpty()) {
                List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

                for (Entity technologyOperationComponent : operationComponents.stream().sorted(Comparator.comparing(technologyOperationComponent ->
                        technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER), reverseOrder())).collect(Collectors.toList())) {
                    String nodeNumber = technologyOperationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);
                    List<Entity> workstations = technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS);

                    int wrongWorkstations = 0;

                    for (Entity workstation : workstations) {
                        for (Entity productAttributeValue : filteredProductAttributeValues) {
                            if (checkDimensionControlOfProductsWithWorkstation(stateChangeContext, null, nodeNumber, productAttributeValue, workstation)) {
                                wrongWorkstations++;

                                break;
                            }
                        }
                    }

                    if (stateChangeContext.getStatus().equals(StateChangeStatus.FAILURE)) {
                        break;
                    } else {
                        if (wrongWorkstations > 0) {
                            if (wrongWorkstations == workstations.size()) {
                                stateChangeContext.addValidationError("technologies.technology.validate.global.error.dimensionControlAllWorkstations", nodeNumber);
                            } else {
                                stateChangeContext.addValidationError("technologies.technology.validate.global.error.dimensionControlAtLeastOneWorkstation", nodeNumber);
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

    public List<Entity> filterProductAttributeValues(final List<Entity> productAttributeValues,
                                                     final List<Entity> dimensionControlAttributes) {
        return productAttributeValues.stream().filter(productAttributeValue ->
                checkDimensionControlAttributesWithAttribute(dimensionControlAttributes, productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE))).collect(Collectors.toList());
    }

    private boolean checkDimensionControlAttributesWithAttribute(final List<Entity> dimensionControlAttributes,
                                                                 final Entity attribute) {
        return dimensionControlAttributes.stream()
                .anyMatch(dimensionControlAttribute ->
                        dimensionControlAttribute.getBelongsToField(DimensionControlAttributeFields.ATTRIBUTE).getId().equals(attribute.getId()));
    }

    public boolean checkDimensionControlOfProductsWithWorkstation(final StateChangeContext stateChangeContext,
                                                                  final Entity order,
                                                                  final String nodeNumber,
                                                                  final Entity productAttributeValue,
                                                                  final Entity workstation) {
        boolean isWrong = false;

        String value = productAttributeValue.getStringField(ProductAttributeValueFields.VALUE);
        Entity attribute = productAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE);
        String unit = attribute.getStringField(AttributeFields.UNIT);

        String number = workstation.getStringField(WorkstationFields.NUMBER);
        BigDecimal minimumDimension = workstation.getDecimalField(WorkstationFields.MINIMUM_DIMENSION);
        BigDecimal maximumDimension = workstation.getDecimalField(WorkstationFields.MAXIMUM_DIMENSION);
        String minimumDimensionUnit = workstation.getStringField(WorkstationFields.MINIMUM_DIMENSION_UNIT);
        String maximumDimensionUnit = workstation.getStringField(WorkstationFields.MAXIMUM_DIMENSION_UNIT);

        if (Objects.nonNull(value) && Objects.nonNull(unit) && (Objects.nonNull(minimumDimension) || Objects.nonNull(maximumDimension))) {
            Either<Exception, com.google.common.base.Optional<BigDecimal>> eitherDimension = BigDecimalUtils.tryParseAndIgnoreSeparator(value, LocaleContextHolder.getLocale());

            if (eitherDimension.isRight() && eitherDimension.getRight().isPresent()) {
                if (Objects.nonNull(minimumDimension)) {
                    BigDecimal dimension = eitherDimension.getRight().get();

                    if (!unit.equals(minimumDimensionUnit)) {
                        PossibleUnitConversions possibleUnitConversions = unitConversionService.getPossibleConversions(unit, L_CM);

                        if (possibleUnitConversions.isDefinedFor(minimumDimensionUnit) && (possibleUnitConversions.isDefinedFor(L_MM) || L_MM.equals(unit))) {
                            dimension = convertToMM(dimension, unit);
                            minimumDimension = convertToMM(minimumDimension, minimumDimensionUnit);
                        } else {
                            if (stateChangeContext != null) {
                                stateChangeContext.addValidationError("technologies.technology.validate.global.error.dimensionControlIncompatibleUnits", nodeNumber, number);
                            } else if (order != null) {
                                order.addGlobalError("technologies.technology.validate.global.error.dimensionControlIncompatibleUnits", nodeNumber, number);
                            }
                            return true;
                        }
                    }

                    if (Objects.nonNull(dimension) && Objects.nonNull(minimumDimension)
                            && dimension.compareTo(minimumDimension) < 0) {
                        isWrong = true;
                    }
                }
                if (Objects.nonNull(maximumDimension)) {
                    BigDecimal dimension = eitherDimension.getRight().get();

                    if (!unit.equals(maximumDimensionUnit)) {
                        PossibleUnitConversions possibleUnitConversions = unitConversionService.getPossibleConversions(unit, L_CM);

                        if (possibleUnitConversions.isDefinedFor(maximumDimensionUnit) && (possibleUnitConversions.isDefinedFor(L_MM) || L_MM.equals(unit))) {
                            dimension = convertToMM(dimension, unit);
                            maximumDimension = convertToMM(maximumDimension, maximumDimensionUnit);
                        } else {
                            if (stateChangeContext != null) {
                                stateChangeContext.addValidationError("technologies.technology.validate.global.error.dimensionControlIncompatibleUnits", nodeNumber, number);
                            } else if (order != null) {
                                order.addGlobalError("technologies.technology.validate.global.error.dimensionControlIncompatibleUnits", nodeNumber, number);
                            }

                            return true;
                        }
                    }

                    if (Objects.nonNull(dimension) && Objects.nonNull(maximumDimension)
                            && dimension.compareTo(maximumDimension) > 0) {
                        isWrong = true;
                    }
                }
            }
        }

        return isWrong;
    }

    private BigDecimal convertToMM(final BigDecimal dimension, final String unit) {
        if (L_MM.equals(unit)) {
            return dimension;
        } else {
            PossibleUnitConversions possibleUnitConversions = unitConversionService.getPossibleConversions(unit, L_CM);

            if (possibleUnitConversions.isDefinedFor(L_MM)) {
                return possibleUnitConversions.convertTo(dimension, L_MM);
            }
        }

        return null;
    }

}
