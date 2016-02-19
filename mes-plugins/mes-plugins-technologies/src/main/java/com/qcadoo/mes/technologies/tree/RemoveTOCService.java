package com.qcadoo.mes.technologies.tree;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class RemoveTOCService {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Transactional
    public boolean removeOnlySelectedOperation(final Entity tocToDelete, final ViewDefinitionState view) {
        final Entity parent = tocToDelete.getBelongsToField(TechnologyOperationComponentFields.PARENT);
        final List<Entity> operationsToRewrite = tocToDelete.getHasManyField(TechnologyOperationComponentFields.CHILDREN);

        if (parent == null) {
            if (operationsToRewrite.size() > 1) {
                view.addMessage("technologies.technologyDetails.window.treeTab.technologyTree.error.cannotDeleteRoot",
                        ComponentState.MessageType.FAILURE);
                return false;
            } else if (operationsToRewrite.size() == 1) {
                return createNewRoot(operationsToRewrite.get(0), tocToDelete, view);
            } else {
                deleteOldToc(tocToDelete);
                return true;
            }
        }
        final Optional<Entity> mainOutProductComponentToDelete = technologyService.tryGetMainOutputProductComponent(tocToDelete);
        final List<Entity> originalInProducts = tocToDelete
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
        List<Entity> newInProducts = Lists.newArrayList();

        for (Entity toc : operationsToRewrite) {
            Optional<Entity> mainOutProductComponent = technologyService.tryGetMainOutputProductComponent(toc);
            if (mainOutProductComponent.isPresent()) {
                Optional<Entity> maybeInProductComponent = getInProductComponentFromProductComponent(originalInProducts,
                        mainOutProductComponent.get());

                if (maybeInProductComponent.isPresent()) {
                    Entity inProductComponent = maybeInProductComponent.get();
                    newInProducts.add(inProductComponent);
                }
            }
            setNewParent(toc, parent);
        }
        if (!rewriteInProductComponents(parent, newInProducts, mainOutProductComponentToDelete, view)) {

            view.addMessage("technologies.technologyDetails.window.treeTab.technologyTree.error.cannotRewriteProducts",
                    ComponentState.MessageType.FAILURE);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        deleteOldToc(tocToDelete);

        return true;
    }

    private boolean rewriteInProductComponents(Entity toc, List<Entity> productComponentsToAdd, Optional<Entity> productToDelete,
            ViewDefinitionState view) {
        List<Entity> originalInProducts = Lists
                .newArrayList(toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS));
        List<String> addedProducts = Lists.newArrayList();
        if (productToDelete.isPresent()) {
            Optional<Entity> originalProductToDelete = getInProductComponentFromProductComponent(originalInProducts,
                    productToDelete.get());
            if (originalProductToDelete.isPresent()) {
                originalInProducts.remove(originalProductToDelete.get());
            }
        }

        for (Entity productComponentToAdd : productComponentsToAdd) {
            Optional<Entity> maybeOriginalInProduct = getInProductComponentFromProductComponent(originalInProducts,
                    productComponentToAdd);
            if (maybeOriginalInProduct.isPresent()) {
                Entity originalInProduct = maybeOriginalInProduct.get();
                sumQuantities(originalInProduct, productComponentToAdd);
            } else {
                setNewOperationComponent(productComponentToAdd, toc);
                originalInProducts.add(productComponentToAdd);
                addedProducts.add(productComponentToAdd.getBelongsToField(OperationProductInComponentFields.PRODUCT)
                        .getStringField(ProductFields.NUMBER));
            }
        }

        toc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS, originalInProducts);

        Entity savedToc = toc.getDataDefinition().save(toc);
        if (savedToc.isValid() && !addedProducts.isEmpty()) {
            view.addMessage("technologies.technologyDetails.window.treeTab.technologyTree.success.productsRewrote",
                    ComponentState.MessageType.INFO, addedProducts.stream().collect(Collectors.joining(", ")),
                    toc.getBelongsToField(TechnologyOperationComponentFields.OPERATION).getStringField(OperationFields.NUMBER));
        }
        return savedToc.isValid();
    }

    private void sumQuantities(Entity originalInProduct, Entity productComponentToAdd) {

        BigDecimal originalQuantity = originalInProduct.getDecimalField(OperationProductInComponentFields.QUANTITY);
        BigDecimal quantityToAdd = productComponentToAdd.getDecimalField(OperationProductInComponentFields.QUANTITY);
        String givenUnit = originalInProduct.getStringField(OperationProductInComponentFields.GIVEN_UNIT);
        Entity product = originalInProduct.getBelongsToField(OperationProductInComponentFields.PRODUCT);
        String baseUnit = product.getStringField(ProductFields.UNIT);

        BigDecimal quantity = originalQuantity.add(quantityToAdd);
        originalInProduct.setField(OperationProductInComponentFields.QUANTITY, quantity);

        if (baseUnit.equals(givenUnit)) {
            originalInProduct.setField(OperationProductInComponentFields.GIVEN_QUANTITY, quantity);
        } else {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(baseUnit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
            if (unitConversions.isDefinedFor(givenUnit)) {
                BigDecimal convertedQuantity = unitConversions.convertTo(quantity, givenUnit);
                originalInProduct.setField(OperationProductInComponentFields.GIVEN_QUANTITY, convertedQuantity);
            } else {
                originalInProduct.addError(
                        originalInProduct.getDataDefinition().getField(OperationProductInComponentFields.GIVEN_QUANTITY),
                        "technologies.operationProductInComponent.validate.error.missingUnitConversion");
                originalInProduct.setField(OperationProductInComponentFields.GIVEN_QUANTITY, null);
            }
        }
    }

    private boolean createNewRoot(Entity newRoot, final Entity tocToDelete, final ViewDefinitionState view) {

        if (setNewParent(newRoot, null)) {
            deleteOldToc(tocToDelete);
        } else {
            view.addMessage("technologies.technologyDetails.window.treeTab.technologyTree.error.cannotCreateNewRoot",
                    ComponentState.MessageType.FAILURE);
            return false;
        }
        return true;
    }

    private void deleteOldToc(final Entity tocToDelete) {
        getTocDataDefinition().delete(tocToDelete.getId());
    }

    private DataDefinition getTocDataDefinition() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private boolean setNewParent(Entity toc, final Entity newParent) {
        toc.setField(TechnologyOperationComponentFields.PARENT, newParent);
        Entity saved = getTocDataDefinition().save(toc);
        return saved.isValid();
    }

    private void setNewOperationComponent(Entity inProduct, final Entity newToc) {
        inProduct.setField(OperationProductInComponentFields.OPERATION_COMPONENT, newToc);
        inProduct.getDataDefinition().save(inProduct);
    }

    private Optional<Entity> getInProductComponentFromProductComponent(final List<Entity> originalInProducts,
            final Entity productComponent) {
        Long productId = productComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId();
        return originalInProducts.stream().filter(inProduct -> inProduct
                .getBelongsToField(OperationProductInComponentFields.PRODUCT).getId().compareTo(productId) == 0).findAny();
    }
}
