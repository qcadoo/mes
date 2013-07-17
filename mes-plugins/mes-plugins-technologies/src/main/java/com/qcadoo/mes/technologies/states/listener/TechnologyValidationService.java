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
package com.qcadoo.mes.technologies.states.listener;

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.PRODUCT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;

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
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.tree.TechnologyTreeValidationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public class TechnologyValidationService {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyTreeValidationService technologyTreeValidationService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductQuantitiesService productQuantitiyService;

    private static final String L_TECHNOLOGIES_OPERATION_DETAILS_VALIDATE_ERROR_OUTPUT_UNITS_NOT_MATCH = "technologies.operationDetails.validate.error.OutputUnitsNotMatch";

    private static final String L_OPERATION = "operation";

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
        final EntityTree operations = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        final EntityTreeNode root = operations.getRoot();
        final EntityList productOutComps = root
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
        for (Entity productOutComp : productOutComps) {
            if (product.getId().equals(productOutComp.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId())) {
                return true;
            }
        }
        stateContext.addValidationError("technologies.technology.validate.global.error.noFinalProductInTechnologyTree");
        return false;
    }

    public boolean checkIfTechnologyHasAtLeastOneComponent(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();
        final Entity savedTechnology = technology.getDataDefinition().get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        if (operations != null && !operations.isEmpty()) {
            for (Entity operation : operations) {
                if (L_OPERATION.equals(operation.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                    return true;
                }
            }
        }
        stateContext.addValidationError("technologies.technology.validate.global.error.emptyTechnologyTree");
        return false;
    }

    // TODO DEV_TEAM when we fixed problem with referenced technology
    public boolean checkIfAllReferenceTechnologiesAreAceepted(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();
        final DataDefinition technologyDD = technology.getDataDefinition();
        final Entity savedTechnology = technologyDD.get(technology.getId());
        final EntityTree operations = savedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        for (Entity operation : operations) {
            if (L_OPERATION.equals(operation.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                continue;
            }
            final Entity referenceTechnology = operation
                    .getBelongsToField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY);
            if (referenceTechnology != null
                    && !TechnologyState.ACCEPTED.getStringValue().equals(referenceTechnology.getStringField(STATE))) {
                stateContext.addFieldValidationError(TechnologyFields.OPERATION_COMPONENTS,
                        "technologies.technology.validate.global.error.treeIsNotValid");
                stateContext.addMessage("technologies.technology.validate.global.error.unacceptedReferenceTechnology",
                        StateMessageType.FAILURE, false);
                return false;
            }
        }
        return true;
    }

    public boolean checkIfOperationsUsesSubOperationsProds(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();
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
                    || TechnologyOperationComponentFields.REFERENCETECHNOLOGY.equals(parent
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
                        .getBelongsToField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY);

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

    public boolean checkIfTreeOperationIsValid(final StateChangeContext stateContext) {
        Entity technology = stateContext.getOwner();
        if (technology == null || technology.getId() == null) {
            return true;
        }
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
        String productionInOneCycleUnit = technologyOperationComponent.getStringField("productionInOneCycleUNIT");
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
        final String productionInOneCycleUNIT = technologyOperationComponent.getStringField("productionInOneCycleUNIT");
        DataDefinition dataDefinition = technologyOperationComponent.getDataDefinition();
        if (productionInOneCycleUNIT == null) {
            technologyOperationComponent.addError(dataDefinition.getField("productionInOneCycleUNIT"),
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
                technologyOperationComponent.addError(dataDefinition.getField("productionInOneCycleUNIT"),
                        "technologies.operationDetails.validate.error.OutputUnitsNotMatch");
                return false;
            }
        }
        return true;
    }
}
