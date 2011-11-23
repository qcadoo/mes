/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class AutoGenealogyService extends OrderStateListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private GenealogyService genealogyService;

    @Autowired
    private PluginAccessor pluginAccessor;

    public void fillLastUsedBatchForProduct(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        Entity product = entity.getBelongsToField("productInComponent").getBelongsToField("productInComponent")
                .getBelongsToField("product");
        DataDefinition productInDef = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void fillLastUsedBatchForGenealogy(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        Entity product = entity.getBelongsToField("order").getBelongsToField("product");
        if (product != null) {
            DataDefinition productInDef = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_PRODUCT);
            Entity productEntity = productInDef.get(product.getId());
            productEntity.setField("lastUsedBatch", entity.getField("batch"));
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
                state.addMessage(
                        translationService.translate("qcadooView.message.entityNotFound", viewDefinitionState.getLocale()),
                        MessageType.FAILURE);
            } else {
                List<ChangeOrderStateMessage> listOfMessage = createGenealogy(order, Boolean.parseBoolean(args[0]));
                for (ChangeOrderStateMessage message : listOfMessage) {
                    state.addMessage(message.getMessage(), message.getType());
                }
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(
                        translationService.translate("qcadooView.form.entityWithoutIdentifier", viewDefinitionState.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(
                        translationService.translate("qcadooView.grid.noRowSelectedError", viewDefinitionState.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void fillLastUsedShiftFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);
        SearchResult searchResult = featureDef.find().setMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedShift", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedPostFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);
        SearchResult searchResult = featureDef.find().setMaxResults(1).list();
        if (searchResult.getEntities().size() > 0) {
            Entity currentAttribute = searchResult.getEntities().get(0);
            currentAttribute.setField("lastUsedPost", entity.getField("value"));
            featureDef.save(currentAttribute);
        }
    }

    public void fillLastUsedOtherFeature(final DataDefinition dataDefinition, final Entity entity) {
        fillUserAndDate(entity);
        DataDefinition featureDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE);
        SearchResult searchResult = featureDef.find().setMaxResults(1).list();
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

    List<ChangeOrderStateMessage> createGenealogy(final Entity order, final boolean lastUsedMode) {
        Entity mainProduct = order.getBelongsToField("product");
        Entity technology = order.getBelongsToField("technology");
        List<ChangeOrderStateMessage> listOfMessage = new ArrayList<ChangeOrderStateMessage>();
        if (mainProduct == null || technology == null) {
            listOfMessage.add(ChangeOrderStateMessage.info(translationService.translate(
                    "genealogies.message.autoGenealogy.failure.product", getLocale())));
            return listOfMessage;
        }
        Object mainBatch = null;
        if (lastUsedMode) {
            mainBatch = mainProduct.getField("lastUsedBatch");
        } else {
            mainBatch = mainProduct.getField("batch");
        }
        if (mainBatch == null) {
            listOfMessage.add(ChangeOrderStateMessage.info(translationService.translate(
                    "genealogies.message.autoGenealogy.missingMainBatch", getLocale())));
            return listOfMessage;
        }
        if (checkIfExistGenealogyWithBatch(order, mainBatch.toString())) {
            listOfMessage.add(ChangeOrderStateMessage.info(translationService.translate(
                    "genealogies.message.autoGenealogy.genealogyExist", getLocale())));
            return listOfMessage;
        }
        DataDefinition genealogyDef = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                GenealogiesConstants.MODEL_GENEALOGY);
        Entity genealogy = genealogyDef.create();
        genealogy.setField("order", order);
        genealogy.setField("batch", mainBatch);
        completeAttributesForGenealogy(technology, genealogy, lastUsedMode);
        if (pluginAccessor.getEnabledPlugin("genealogiesForComponents") != null) {
            completeBatchForComponents(technology, genealogy, lastUsedMode);
        }

        if (genealogy.isValid()) {
            genealogy = genealogyDef.save(genealogy);
        }

        if (!genealogy.isValid()) {
            if (!genealogy.getGlobalErrors().isEmpty()) {
                for (ErrorMessage error : genealogy.getGlobalErrors()) {
                    StringBuilder message = new StringBuilder(error.getMessage());
                    for (String var : error.getVars()) {
                        message.append("\n" + var);
                    }
                    listOfMessage
                            .add(ChangeOrderStateMessage.error(translationService.translate(message.toString(), getLocale())));
                }
            } else {
                listOfMessage.add(ChangeOrderStateMessage.info(translationService.translate(
                        "genealogies.message.autoGenealogy.failure", getLocale())));
            }
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } else {
            listOfMessage.add(ChangeOrderStateMessage.success(translationService.translate(
                    "genealogies.message.autoGenealogy.success", getLocale())));
        }

        return listOfMessage;
    }

    private boolean checkIfExistGenealogyWithBatch(final Entity order, final String batch) {
        SearchResult searchResult = dataDefinitionService
                .get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_GENEALOGY).find().isEq("batch", batch)
                .belongsTo("order", order.getId()).setMaxResults(1).list();

        if (searchResult.getEntities().size() > 0) {
            return true;
        }
        return false;
    }

    private void completeAttributesForGenealogy(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        SearchResult searchResult = dataDefinitionService
                .get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE).find()
                .setMaxResults(1).list();
        Entity currentAttribute = null;
        if (searchResult.getEntities().size() > 0) {
            currentAttribute = searchResult.getEntities().get(0);
        }
        if ((Boolean) technology.getField("shiftFeatureRequired")) {
            Entity shift = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                    GenealogiesConstants.MODEL_SHIFT_FEATURE).create();
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
                genealogy.addGlobalError(translationService.translate("genealogies.message.autoGenealogy.missingShift",
                        getLocale()));
            }
        }
        if ((Boolean) technology.getField("otherFeatureRequired")) {
            Entity other = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                    GenealogiesConstants.MODEL_OTHER_FEATURE).create();
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
                genealogy.addGlobalError(translationService.translate("genealogies.message.autoGenealogy.missingOther",
                        getLocale()));
            }
        }
        if ((Boolean) technology.getField("postFeatureRequired")) {
            Entity post = dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER,
                    GenealogiesConstants.MODEL_POST_FEATURE).create();
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
                genealogy.addGlobalError(translationService.translate("genealogies.message.autoGenealogy.missingPost",
                        getLocale()));
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
                if (operationProductComponent.getField("batchRequired") != null
                        && (Boolean) operationProductComponent.getField("batchRequired")) {
                    Entity productIn = dataDefinitionService.get("genealogiesForComponents", "genealogyProductInComponent")
                            .create();
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
                        Entity productBatch = dataDefinitionService.get("genealogiesForComponents", "productInBatch").create();
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
            genealogy.addGlobalError(
                    translationService.translate("genealogies.message.autoGenealogy.missingBatch",
                            LocaleContextHolder.getLocale()),
                    componentsWithoutBatch.toArray(new String[componentsWithoutBatch.size()]));
        }
    }

}
