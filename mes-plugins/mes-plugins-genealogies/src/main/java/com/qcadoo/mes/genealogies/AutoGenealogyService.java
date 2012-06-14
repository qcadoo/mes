/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.genealogies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.states.messages.MessageService;
import com.qcadoo.mes.states.messages.MessagesHolder;
import com.qcadoo.mes.states.messages.MessagesHolderImpl;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.service.client.StateChangeViewClientUtil;
import com.qcadoo.mes.states.service.client.StateChangeViewClientValidationUtil;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class AutoGenealogyService {

    private static final String BATCH_MODEL = "batch";

    private static final String ORDER_MODEL = "order";

    private static final String PRODUCT_MODEL = "product";

    private static final String PRODUCT_IN_BATCH_MODEL = "productInBatch";

    private static final String GENEALOGY_PRODUCT_IN_COMPONENT_MODEL = "genealogyProductInComponent";

    private static final String NUMBER_FIELD = "number";

    private static final String NAME_FIELD = "name";

    private static final String BATCH_REQUIRED_FIELD = "batchRequired";

    private static final String OPERATION_PRODUCT_IN_COMPONENTS_FIELD = "operationProductInComponents";

    private static final String OPERATION_COMPONENTS_FIELD = "operationComponents";

    private static final String PRODUCT_IN_COMPONENTS_FIELD = "productInComponents";

    private static final String POST_FEATURES_FIELD = "postFeatures";

    private static final String POST_FIELD = "post";

    private static final String POST_FEATURE_REQUIRED_FIELD = "postFeatureRequired";

    private static final String OTHER_FEATURES_FIELD = "otherFeatures";

    private static final String OTHER_FIELD = "other";

    private static final String OTHER_FEATURE_REQUIRED_FIELD = "otherFeatureRequired";

    private static final String GENEALOGY_FIELD = "genealogy";

    private static final String SHIFT_FEATURES_FIELD = "shiftFeatures";

    private static final String SHIFT_FIELD = "shift";

    private static final String SHIFT_FEATURE_REQUIRED_FIELD = "shiftFeatureRequired";

    private static final String GENEALOGIES_FOR_COMPONENTS_PLUGIN = "genealogiesForComponents";

    private static final String DATE_FIELD = "date";

    private static final String WORKER_FIELD = "worker";

    private static final String TECHNOLOGY_FIELD = "technology";

    private static final String LAST_USED_OTHER_FIELD = "lastUsedOther";

    private static final String LAST_USED_POST_FIELD = "lastUsedPost";

    private static final String VALUE_FIELD = "value";

    private static final String LAST_USED_SHIFT_FIELD = "lastUsedShift";

    private static final String LAST_USED_BATCH_FIELD = "lastUsedBatch";

    private static final String PRODUCT_IN_COMPONENT_FIELD = "productInComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private MessageService messageService;

    @Autowired
    private StateChangeViewClientValidationUtil viewValidationUtil;

    @Autowired
    private StateChangeViewClientUtil viewUtil;

    public void fillLastUsedBatchForProduct(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        Entity product = entity.getBelongsToField(PRODUCT_IN_COMPONENT_FIELD).getBelongsToField(PRODUCT_IN_COMPONENT_FIELD)
                .getBelongsToField(PRODUCT_MODEL);
        DataDefinition productInDef = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField(LAST_USED_BATCH_FIELD, entity.getField(BATCH_MODEL));
        productInDef.save(productEntity);
    }

    public void fillLastUsedBatchForGenealogy(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        Entity product = entity.getBelongsToField(ORDER_MODEL).getBelongsToField(PRODUCT_MODEL);
        if (product == null) {
            return;
        } else {
            DataDefinition productInDef = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_PRODUCT);
            Entity productEntity = productInDef.get(product.getId());
            productEntity.setField(LAST_USED_BATCH_FIELD, entity.getField(BATCH_MODEL));
            productInDef.save(productEntity);
        }
    }

    @Transactional
    public void autocompleteGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    (Long) state.getFieldValue());
            if (order == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
            } else {
                final MessagesHolder messagesHolder = new MessagesHolderImpl(messageService);
                createGenealogy(order, messagesHolder, Boolean.parseBoolean(args[0]));
                viewValidationUtil.addValidationErrorMessages(state, order, messagesHolder);
                viewUtil.addStateMessagesToView(state, messagesHolder);
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage("qcadooView.form.entityWithoutIdentifier", MessageType.FAILURE);
            } else {
                state.addMessage("qcadooView.grid.noRowSelectedError", MessageType.FAILURE);
            }
        }
    }

    public void fillLastUsedShiftFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);
        SearchResult searchResult = featureDef.find().setMaxResults(1).list();
        if (!searchResult.getEntities().isEmpty()) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField(LAST_USED_SHIFT_FIELD, entity.getField(VALUE_FIELD));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedPostFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);
        SearchResult searchResult = featureDef.find().setMaxResults(1).list();
        if (!searchResult.getEntities().isEmpty()) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField(LAST_USED_POST_FIELD, entity.getField(VALUE_FIELD));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedOtherFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);
        SearchResult searchResult = featureDef.find().setMaxResults(1).list();
        if (!searchResult.getEntities().isEmpty()) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField(LAST_USED_OTHER_FIELD, entity.getField(VALUE_FIELD));
            featureDef.save(currentAttribute);
        }
    }

    private void fillUserAndDate(final Entity entity) {
        if (entity.getField(DATE_FIELD) == null) {
            entity.setField(DATE_FIELD, new Date());
        }
        if (entity.getField(WORKER_FIELD) == null) {
            entity.setField(WORKER_FIELD, securityService.getCurrentUserName());
        }
    }

    public void createGenealogy(final Entity order, final MessagesHolder messagesHolder, final boolean lastUsedMode) {
        Entity mainProduct = order.getBelongsToField(PRODUCT_MODEL);
        Entity technology = order.getBelongsToField(TECHNOLOGY_FIELD);
        if (mainProduct == null || technology == null) {
            messagesHolder.addMessage("genealogies.message.autoGenealogy.failure.product", StateMessageType.INFO);
            return;
        }
        Object mainBatch = null;
        if (lastUsedMode) {
            mainBatch = mainProduct.getField(LAST_USED_BATCH_FIELD);
        } else {
            mainBatch = mainProduct.getField(BATCH_MODEL);
        }
        if (mainBatch == null) {
            messagesHolder.addMessage("genealogies.message.autoGenealogy.missingMainBatch", StateMessageType.INFO);
            return;
        }
        if (checkIfExistGenealogyWithBatch(order, mainBatch.toString())) {
            messagesHolder.addMessage("genealogies.message.autoGenealogy.genealogyExist", StateMessageType.INFO);
            return;
        }
        DataDefinition genealogyDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_GENEALOGY);
        Entity genealogy = genealogyDef.create();
        genealogy.setField(ORDER_MODEL, order);
        genealogy.setField(BATCH_MODEL, mainBatch);
        completeAttributesForGenealogy(technology, genealogy, lastUsedMode);
        if (pluginManager.isPluginEnabled(GENEALOGIES_FOR_COMPONENTS_PLUGIN)) {
            completeBatchForComponents(technology, genealogy, lastUsedMode);
        }

        if (genealogy.isValid()) {
            genealogy = genealogyDef.save(genealogy);
        }

        if (genealogy.isValid()) {
            messagesHolder.addMessage("genealogies.message.autoGenealogy.success", StateMessageType.SUCCESS);
        } else {
            if (genealogy.getGlobalErrors().isEmpty()) {
                messagesHolder.addMessage("genealogies.message.autoGenealogy.failure", StateMessageType.INFO);
            } else {
                for (ErrorMessage error : genealogy.getGlobalErrors()) {
                    messagesHolder.addMessage(error.getMessage(), StateMessageType.FAILURE, error.getVars());
                }
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    private boolean checkIfExistGenealogyWithBatch(final Entity order, final String batch) {
        SearchResult searchResult = dataDefinitionService
                .get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY).find()
                .add(SearchRestrictions.eq(BATCH_MODEL, batch)).add(SearchRestrictions.belongsTo(ORDER_MODEL, order))
                .setMaxResults(1).list();

        if (!searchResult.getEntities().isEmpty()) {
            return true;
        }
        return false;
    }

    private void completeAttributesForGenealogy(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        SearchResult searchResult = dataDefinitionService
                .get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE).find()
                .setMaxResults(1).list();
        Entity currentAttribute = null;
        if (!searchResult.getEntities().isEmpty()) {
            currentAttribute = searchResult.getEntities().get(0);
        }
        if ((Boolean) technology.getField(SHIFT_FEATURE_REQUIRED_FIELD)) {
            Entity shift = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                    GenealogiesConstants.MODEL_SHIFT_FEATURE).create();
            shift.setField(GENEALOGY_FIELD, genealogy);
            if (currentAttribute == null) {
                shift.setField(VALUE_FIELD, null);
            } else if (lastUsedMode) {
                shift.setField(VALUE_FIELD, currentAttribute.getField(LAST_USED_SHIFT_FIELD));
            } else {
                shift.setField(VALUE_FIELD, currentAttribute.getField(SHIFT_FIELD));
            }
            if (shift.getField(VALUE_FIELD) == null) {
                genealogy.addGlobalError("genealogies.message.autoGenealogy.missingShift");
            } else {
                genealogy.setField(SHIFT_FEATURES_FIELD, Collections.singletonList(shift));
            }
        }
        if ((Boolean) technology.getField(OTHER_FEATURE_REQUIRED_FIELD)) {
            Entity other = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                    GenealogiesConstants.MODEL_OTHER_FEATURE).create();
            other.setField(GENEALOGY_FIELD, genealogy);
            if (currentAttribute == null) {
                other.setField(VALUE_FIELD, null);
            } else if (lastUsedMode) {
                other.setField(VALUE_FIELD, currentAttribute.getField(LAST_USED_OTHER_FIELD));
            } else {
                other.setField(VALUE_FIELD, currentAttribute.getField(OTHER_FIELD));
            }
            if (other.getField(VALUE_FIELD) == null) {
                genealogy.addGlobalError("genealogies.message.autoGenealogy.missingOther");
            } else {
                genealogy.setField(OTHER_FEATURES_FIELD, Collections.singletonList(other));
            }
        }
        if ((Boolean) technology.getField(POST_FEATURE_REQUIRED_FIELD)) {
            Entity post = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                    GenealogiesConstants.MODEL_POST_FEATURE).create();
            post.setField(GENEALOGY_FIELD, genealogy);
            if (currentAttribute == null) {
                post.setField(VALUE_FIELD, null);
            } else if (lastUsedMode) {
                post.setField(VALUE_FIELD, currentAttribute.getField(LAST_USED_POST_FIELD));
            } else {
                post.setField(VALUE_FIELD, currentAttribute.getField(POST_FIELD));
            }
            if (post.getField(VALUE_FIELD) == null) {
                genealogy.addGlobalError("genealogies.message.autoGenealogy.missingPost");
            } else {
                genealogy.setField(POST_FEATURES_FIELD, Collections.singletonList(post));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void completeBatchForComponents(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        genealogy.setField(PRODUCT_IN_COMPONENTS_FIELD, new ArrayList<Entity>());
        List<String> componentsWithoutBatch = new ArrayList<String>();
        List<Entity> operationComponents = new ArrayList<Entity>();
        technologyService.addOperationsFromSubtechnologiesToList(technology.getTreeField(OPERATION_COMPONENTS_FIELD),
                operationComponents);
        for (Entity operationComponent : operationComponents) {
            for (Entity operationProductComponent : operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS_FIELD)) {
                if (operationProductComponent.getField(BATCH_REQUIRED_FIELD) != null
                        && (Boolean) operationProductComponent.getField(BATCH_REQUIRED_FIELD)) {
                    Entity productIn = dataDefinitionService.get(GENEALOGIES_FOR_COMPONENTS_PLUGIN,
                            GENEALOGY_PRODUCT_IN_COMPONENT_MODEL).create();
                    productIn.setField(GENEALOGY_FIELD, genealogy);
                    productIn.setField(PRODUCT_IN_COMPONENT_FIELD, operationProductComponent);
                    Entity product = (Entity) operationProductComponent.getField(PRODUCT_MODEL);
                    Object batch = null;
                    if (lastUsedMode) {
                        batch = product.getField(LAST_USED_BATCH_FIELD);
                    } else {
                        batch = product.getField(BATCH_MODEL);
                    }
                    if (batch == null) {
                        String value = product.getField(NUMBER_FIELD) + "-" + product.getField(NAME_FIELD) + "; ";
                        if (!componentsWithoutBatch.contains(value)) {
                            componentsWithoutBatch.add(value);
                        }
                    } else {
                        Entity productBatch = dataDefinitionService
                                .get(GENEALOGIES_FOR_COMPONENTS_PLUGIN, PRODUCT_IN_BATCH_MODEL).create();
                        productBatch.setField(BATCH_MODEL, batch);
                        productBatch.setField(PRODUCT_IN_COMPONENT_FIELD, productIn);
                        productIn.setField(BATCH_MODEL, Collections.singletonList(productBatch));
                    }
                    ((List<Entity>) genealogy.getField(PRODUCT_IN_COMPONENTS_FIELD)).add(productIn);
                }
            }
        }
        if (!componentsWithoutBatch.isEmpty()) {
            genealogy.addGlobalError("genealogies.message.autoGenealogy.missingBatch",
                    componentsWithoutBatch.toArray(new String[componentsWithoutBatch.size()]));
        }
    }

}
