package com.qcadoo.mes.genealogies;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

@Service
public class AutoGenealogyService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private GenealogyService genealogyService;

    @Transactional(propagation = REQUIRES_NEW)
    public void generateGenalogyOnChangeOrderStatusForDone(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                boolean inProgressState = Boolean.parseBoolean(args[0]);
                if (!inProgressState) {
                    SearchResult searchResult = dataDefinitionService.get("basic", "parameter").find().withMaxResults(1).list();
                    Entity parameter = null;
                    if (searchResult.getEntities().size() > 0) {
                        parameter = searchResult.getEntities().get(0);
                    }
                    if (parameter != null) {
                        if (parameter.getField("batchForDoneOrder").toString().equals("01active")) {
                            createGenealogy(state, order, false);
                        } else {
                            createGenealogy(state, order, true);
                        }
                    }
                }
            }
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void fillLastUsedBatchForProduct(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        Entity product = entity.getBelongsToField("productInComponent").getBelongsToField("productInComponent")
                .getBelongsToField("product");
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void fillLastUsedBatchForGenealogy(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        Entity product = entity.getBelongsToField("order").getBelongsToField("product");
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    @Transactional
    public void autocompleteGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                createGenealogy(state, order, Boolean.parseBoolean(args[0]));
            }
        } else {
            if (state instanceof FormComponentState) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void fillLastUsedShiftFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get("genealogies", "currentAttribute");
        SearchResult searchResult = featureDef.find().withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedShift", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedPostFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get("genealogies", "currentAttribute");
        SearchResult searchResult = featureDef.find().withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedPost", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedOtherFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get("genealogies", "currentAttribute");
        SearchResult searchResult = featureDef.find().withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedOther", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    private void fillUserAndDate(final Entity entity) {
        if (entity.getField("date") == null) {
            entity.setField("date", new Date());
        }
        if (entity.getField("worker") == null) {
            entity.setField("worker", securityService.getCurrentUserName());
        }
    }

    private void createGenealogy(final ComponentState state, final Entity order, final boolean lastUsedMode) {
        Entity mainProduct = order.getBelongsToField("product");
        Entity technology = order.getBelongsToField("technology");
        if (mainProduct == null || technology == null) {
            state.addMessage(
                    translationService.translate("genealogies.message.autoGenealogy.failure.product", state.getLocale()),
                    MessageType.INFO);
            return;
        }
        Object mainBatch = null;
        if (lastUsedMode) {
            mainBatch = mainProduct.getField("lastUsedBatch");
        } else {
            mainBatch = mainProduct.getField("batch");
        }
        if (mainBatch == null) {
            state.addMessage(
                    translationService.translate("genealogies.message.autoGenealogy.missingMainBatch", state.getLocale())
                            + mainProduct.getField("number") + "-" + mainProduct.getField("name"), MessageType.INFO, false);
            return;
        }
        if (checkIfExistGenealogyWithBatch(order, mainBatch.toString())) {
            state.addMessage(translationService.translate("genealogies.message.autoGenealogy.genealogyExist", state.getLocale())
                    + " " + mainBatch, MessageType.INFO);
            return;
        }
        Entity genealogy = new DefaultEntity("genealogies", "genealogy");
        genealogy.setField("order", order);
        genealogy.setField("batch", mainBatch);
        DataDefinition genealogyDef = dataDefinitionService.get("genealogies", "genealogy");
        completeAttributesForGenealogy(technology, genealogy, lastUsedMode);
        completeBatchForComponents(technology, genealogy, lastUsedMode);

        if (genealogy.isValid()) {
            genealogy = genealogyDef.save(genealogy);
        }

        if (!genealogy.isValid()) {
            if (!genealogy.getGlobalErrors().isEmpty()) {
                Set<String> errors = new HashSet<String>();
                for (ErrorMessage error : genealogy.getGlobalErrors()) {
                    if (!errors.contains(error.getMessage())) {
                        StringBuilder message = new StringBuilder(translationService.translate(error.getMessage(),
                                state.getLocale()));
                        for (String var : error.getVars()) {
                            message.append("\n" + var);
                        }
                        state.addMessage(message.toString(), MessageType.INFO, false);
                        errors.add(error.getMessage());
                    }
                }
            } else {
                state.addMessage(translationService.translate("genealogies.message.autoGenealogy.failure", state.getLocale()),
                        MessageType.INFO);
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } else {
            state.addMessage(translationService.translate("genealogies.message.autoGenealogy.success", state.getLocale()),
                    MessageType.SUCCESS);
        }
    }

    private boolean checkIfExistGenealogyWithBatch(final Entity order, final String batch) {
        SearchResult searchResult = dataDefinitionService.get("genealogies", "genealogy").find()
                .restrictedWith(Restrictions.eq("batch", batch)).restrictedWith(Restrictions.eq("order.id", order.getId()))
                .withMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            return true;
        }
        return false;
    }

    private void completeAttributesForGenealogy(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        SearchResult searchResult = dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list();
        Entity currentAttribute = null;
        if (searchResult.getEntities().size() > 0) {
            currentAttribute = searchResult.getEntities().get(0);
        }
        if ((Boolean) technology.getField("shiftFeatureRequired")) {
            Entity shift = new DefaultEntity("genealogies", "shiftFeature");
            shift.setField("genealogy", genealogy);
            if (currentAttribute == null) {
                shift.setField("value", null);
            } else if (lastUsedMode) {
                shift.setField("value", currentAttribute.getField("lastUsedShift"));
            } else {
                shift.setField("value", currentAttribute.getField("shift"));
            }
            if (shift.getField("value") != null) {
                genealogy.setField("shiftFeatures", Collections.singletonList(shift));
            } else {
                genealogy.addGlobalError("genealogies.message.autoGenealogy.missingShift");
            }
        }
        if ((Boolean) technology.getField("otherFeatureRequired")) {
            Entity other = new DefaultEntity("genealogies", "otherFeature");
            other.setField("genealogy", genealogy);
            if (currentAttribute == null) {
                other.setField("value", null);
            } else if (lastUsedMode) {
                other.setField("value", currentAttribute.getField("lastUsedOther"));
            } else {
                other.setField("value", currentAttribute.getField("other"));
            }
            if (other.getField("value") != null) {
                genealogy.setField("otherFeatures", Collections.singletonList(other));
            } else {
                genealogy.addGlobalError("genealogies.message.autoGenealogy.missingOther");
            }
        }
        if ((Boolean) technology.getField("postFeatureRequired")) {
            Entity post = new DefaultEntity("genealogies", "postFeature");
            post.setField("genealogy", genealogy);
            if (currentAttribute == null) {
                post.setField("value", null);
            } else if (lastUsedMode) {
                post.setField("value", currentAttribute.getField("lastUsedPost"));
            } else {
                post.setField("value", currentAttribute.getField("post"));
            }
            if (post.getField("value") != null) {
                genealogy.setField("postFeatures", Collections.singletonList(post));
            } else {
                genealogy.addGlobalError("genealogies.message.autoGenealogy.missingPost");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void completeBatchForComponents(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        genealogy.setField("productInComponents", new ArrayList<Entity>());
        List<String> componentsWithoutBatch = new ArrayList<String>();
        List<Entity> operationComponents = new ArrayList<Entity>();
        genealogyService.addOperationsFromSubtechnologiesToList(technology.getTreeField("operationComponents"),
                operationComponents);
        for (Entity operationComponent : operationComponents) {
            for (Entity operationProductComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                if ((Boolean) operationProductComponent.getField("batchRequired")) {
                    Entity productIn = new DefaultEntity("genealogies", "genealogyProductInComponent");
                    productIn.setField("genealogy", genealogy);
                    productIn.setField("productInComponent", operationProductComponent);

                    Entity product = (Entity) operationProductComponent.getField("product");
                    Object batch = null;
                    if (lastUsedMode) {
                        batch = product.getField("lastUsedBatch");
                    } else {
                        batch = product.getField("batch");
                    }

                    if (batch != null) {
                        Entity productBatch = new DefaultEntity("genealogies", "productInBatch");
                        productBatch.setField("batch", batch);
                        productBatch.setField("productInComponent", productIn);
                        productIn.setField("batch", Collections.singletonList(productBatch));
                    } else {
                        String value = product.getField("number") + "-" + product.getField("name") + "; ";
                        if (!componentsWithoutBatch.contains(value)) {
                            componentsWithoutBatch.add(value);
                        }
                    }

                    ((List<Entity>) genealogy.getField("productInComponents")).add(productIn);
                }
            }
        }
        if (componentsWithoutBatch.size() > 0) {
            genealogy.addGlobalError("genealogies.message.autoGenealogy.missingBatch",
                    componentsWithoutBatch.toArray(new String[componentsWithoutBatch.size()]));
        }
    }
}
