/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.products;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.genealogies.GenealogiesGenealogyProductInComponent;
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.beans.products.ProductsProduct;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.RestrictionOperator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.lookup.LookupComponentState;

@Service
public final class OrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    public boolean clearOrderDatesAndWorkersOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("state", "01pending");
        entity.setField("effectiveDateFrom", new Date());
        entity.setField("startWorker", securityService.getCurrentUserName());
        entity.setField("effectiveDateTo", null);
        entity.setField("endWorker", null);
        entity.setField("effectiveDateFrom", null);
        entity.setField("startWorker", null);
        entity.setField("doneQuantity", null);
        return true;
    }

    public void printOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/products/order." + args[0] + "?id=" + state.getFieldValue(), false);
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

    public void autocompleteGenealogy(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("products", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                createGenealogy(order, Boolean.parseBoolean(args[0]));
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
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = ((GenealogiesGenealogyProductInComponent) entity.getField("productInComponent"))
                .getProductInComponent().getProduct();
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void fillLastUsedBatchForGenealogy(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsProduct product = ((ProductsOrder) entity.getField("order")).getProduct();
        DataDefinition productInDef = dataDefinitionService.get("products", "product");
        Entity productEntity = productInDef.get(product.getId());
        productEntity.setField("lastUsedBatch", entity.getField("batch"));
        productInDef.save(productEntity);
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof LookupComponentState)) {
            return;
        }

        LookupComponentState product = (LookupComponentState) state;
        LookupComponentState technology = (LookupComponentState) viewDefinitionState.getComponentByReference("technology");
        FieldComponentState defaultTechnology = (FieldComponentState) viewDefinitionState
                .getComponentByReference("defaultTechnology");

        defaultTechnology.setFieldValue("");
        technology.setFieldValue(null);

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology(product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                technology.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    public void generateOrderNumber(final ViewDefinitionState state, final Locale locale) {
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        FieldComponentState number = (FieldComponentState) state.getComponentByReference("number");

        if (form.getEntityId() != null) {
            // form is already saved
            return;
        }

        if (StringUtils.hasText((String) number.getFieldValue())) {
            // number is already choosen
            return;
        }

        if (number.isHasError()) {
            // there is a validation message for that field
            return;
        }

        SearchResult results = dataDefinitionService.get("products", "order").find().withMaxResults(1).orderDescBy("id").list();

        long longValue = 0;

        if (results.getEntities().isEmpty()) {
            longValue++;
        } else {
            longValue = results.getEntities().get(0).getId() + 1;
        }

        String generatedNumber = String.format("%06d", longValue);

        number.setFieldValue(generatedNumber);
    }

    public void fillDefaultTechnology(final ViewDefinitionState state, final Locale locale) {
        LookupComponentState product = (LookupComponentState) state.getComponentByReference("product");
        FieldComponentState defaultTechnology = (FieldComponentState) state.getComponentByReference("defaultTechnology");

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology(product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                String defaultTechnologyValue = ExpressionUtil.getValue(defaultTechnologyEntity, "#number + ' - ' + #name",
                        locale);
                defaultTechnology.setFieldValue(defaultTechnologyValue);
            }
        }
    }

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState state, final Locale locale) {
        LookupComponentState product = (LookupComponentState) state.getComponentByReference("product");
        LookupComponentState technology = (LookupComponentState) state.getComponentByReference("technology");
        FieldComponentState defaultTechnology = (FieldComponentState) state.getComponentByReference("defaultTechnology");
        FieldComponentState plannedQuantity = (FieldComponentState) state.getComponentByReference("plannedQuantity");

        defaultTechnology.setEnabled(false);

        if (product.getFieldValue() == null || !hasAnyTechnologies(product.getFieldValue())) {
            technology.setEnabled(false);
            technology.setRequired(false);
            plannedQuantity.setRequired(false);
        } else {
            technology.setEnabled(true);
            technology.setRequired(true);
            plannedQuantity.setRequired(true);
        }
    }

    public void disableFormForDoneOrder(final ViewDefinitionState state, final Locale locale) {
        FormComponentState order = (FormComponentState) state.getComponentByReference("form");

        boolean disabled = false;

        if (order.getEntityId() != null) {
            Entity entity = dataDefinitionService.get("products", "order").get(order.getEntityId());

            if (entity != null && "03done".equals(entity.getStringField("state")) && order.isValid()) {
                disabled = true;
            }
        }

        order.setEnabledWithChildren(!disabled);
    }

    public boolean checkIfStateChangeIsCorrect(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder searchCriteria = dataDefinition.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(dataDefinition.getField("state"), "02inProgress"))
                .restrictedWith(Restrictions.idRestriction(entity.getId(), RestrictionOperator.EQ));

        SearchResult searchResult = searchCriteria.list();

        if (entity.getField("state").toString().equals("01pending") && searchResult.getTotalNumberOfEntities() > 0) {
            entity.addError(dataDefinition.getField("state"), "products.validate.global.error.illegalStateChange");
            return false;
        }
        return true;
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        return compareDates(dataDefinition, entity, "dateFrom", "dateTo");
    }

    public boolean checkOrderPlannedQuantity(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        if (product == null) {
            return true;
        }
        Object o = entity.getField("plannedQuantity");
        if (o == null) {
            entity.addError(dataDefinition.getField("plannedQuantity"), "products.validate.global.error.illegalStateChange");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkOrderTechnology(final DataDefinition dataDefinition, final Entity entity) {
        ProductsProduct product = (ProductsProduct) entity.getField("product");
        if (product == null) {
            return true;
        }
        if (entity.getField("technology") == null) {
            if (hasAnyTechnologies(product.getId())) {
                entity.addError(dataDefinition.getField("technology"), "products.validate.global.error.technologyError");
                return false;
            }
        }
        return true;
    }

    public void fillOrderDatesAndWorkers(final DataDefinition dataDefinition, final Entity entity) {
        if (("02inProgress".equals(entity.getField("state")) || "03done".equals(entity.getField("state")))
                && entity.getField("effectiveDateFrom") == null) {
            entity.setField("effectiveDateFrom", new Date());
            entity.setField("startWorker", securityService.getCurrentUserName());
        }
        if ("03done".equals(entity.getField("state")) && entity.getField("effectiveDateTo") == null) {
            entity.setField("effectiveDateTo", new Date());
            entity.setField("endWorker", securityService.getCurrentUserName());

        }
        // TODO MADY autocomplete genealogy last used/active based on parameter
        if (entity.getField("effectiveDateTo") != null) {
            entity.setField("state", "03done");
        } else if (entity.getField("effectiveDateFrom") != null) {
            entity.setField("state", "02inProgress");
        }
    }

    public boolean checkIfOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsOrder order = (ProductsOrder) entity.getField("order");

        if (order == null) {
            return true;
        }

        if (order.getTechnology() == null) {
            entity.addError(dataDefinition.getField("order"), "products.validate.global.error.orderMustHaveTechnology");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfOrderTechnologyHasOperations(final DataDefinition dataDefinition, final Entity entity) {
        // TODO masz why we get hibernate entities here?
        ProductsOrder order = (ProductsOrder) entity.getField("order");

        if (order == null || order.getTechnology() == null) {
            return true;
        }

        if (order.getTechnology().getOperationComponents().isEmpty()) {
            entity.addError(dataDefinition.getField("order"), "products.validate.global.error.orderTechnologyMustHaveOperation");
            return false;
        } else {
            return true;
        }
    }

    private void createGenealogy(final Entity order, final boolean lastUsedMode) {
        Entity mainProduct = (Entity) order.getField("product");
        Entity technology = (Entity) order.getField("technology");
        if (mainProduct == null || technology == null) {
            return;
        }
        Object mainBatch = null;
        if (lastUsedMode) {
            mainBatch = mainProduct.getField("lastUsedBatch");
        } else {
            mainBatch = mainProduct.getField("batch");
        }
        if (mainBatch == null) {
            return;
        }
        Entity genealogy = new DefaultEntity("products", "genealogy");
        genealogy.setField("order", order);
        genealogy.setField("batch", mainBatch);
        if (order.getField("plannedQuantity") != null) {
            genealogy.setField("quantity", order.getField("plannedQuantity"));
        } else if (order.getField("doneQuantity") != null) {
            genealogy.setField("quantity", order.getField("doneQuantity"));
        }
        genealogy.setField("date", new Date());
        genealogy.setField("worker", securityService.getCurrentUserName());
        DataDefinition genealogyDef = dataDefinitionService.get("products", "genealogy");
        Entity savedGenealogy = genealogyDef.save(genealogy);
        completeAttributesForGenealogy(technology, savedGenealogy);
        completeBatchForComponents(technology, savedGenealogy, lastUsedMode);

    }

    private void completeAttributesForGenealogy(final Entity technology, final Entity genealogy) {
        // TODO KRNA complete attributes
        Entity shift = new DefaultEntity("products", "genealogyShiftFeature");
        shift.setField("genealogy", genealogy);
        shift.setField("value", "");
        shift.setField("date", new Date());
        shift.setField("worker", securityService.getCurrentUserName());
        DataDefinition shiftInDef = dataDefinitionService.get("products", "genealogyShiftFeature");
        shiftInDef.save(shift);
        Entity other = new DefaultEntity("products", "genealogyOtherFeature");
        other.setField("genealogy", genealogy);
        other.setField("value", "");
        other.setField("date", new Date());
        other.setField("worker", securityService.getCurrentUserName());
        DataDefinition otherInDef = dataDefinitionService.get("products", "genealogyOtherFeature");
        otherInDef.save(other);
        Entity post = new DefaultEntity("products", "genealogyPostFeature");
        post.setField("genealogy", genealogy);
        post.setField("value", "");
        post.setField("date", new Date());
        post.setField("worker", securityService.getCurrentUserName());
        DataDefinition postInDef = dataDefinitionService.get("products", "genealogyPostFeature");
        postInDef.save(post);
    }

    private void completeBatchForComponents(final Entity technology, final Entity genealogy, final boolean lastUsedMode) {
        for (Entity operationComponent : technology.getHasManyField("operationComponents")) {
            for (Entity operationProductComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                if ((Boolean) operationProductComponent.getField("batchRequired")) {
                    Entity productIn = new DefaultEntity("products", "genealogyProductInComponent");
                    productIn.setField("genealogy", genealogy);
                    productIn.setField("productInComponent", operationProductComponent);
                    DataDefinition productInDef = dataDefinitionService.get("products", "genealogyProductInComponent");
                    Entity savedProductIn = productInDef.save(productIn);
                    Entity product = (Entity) operationProductComponent.getField("product");
                    Object batch = null;
                    if (lastUsedMode) {
                        batch = product.getField("lastUsedBatch");
                    } else {
                        batch = product.getField("batch");
                    }
                    if (batch != null) {
                        Entity productBatch = new DefaultEntity("products", "genealogyProductInBatch");
                        productBatch.setField("batch", batch);
                        productBatch.setField("productInComponent", savedProductIn);
                        productBatch.setField("date", new Date());
                        productBatch.setField("worker", securityService.getCurrentUserName());
                        DataDefinition batchDef = dataDefinitionService.get("products", "genealogyProductInBatch");
                        batchDef.save(productBatch);
                    }
                }
            }
        }
    }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get("products", "technology");

        SearchCriteriaBuilder searchCriteria = instructionDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.eq(instructionDD.getField("master"), true))
                .restrictedWith(Restrictions.belongsTo(instructionDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    private boolean hasAnyTechnologies(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get("products", "technology");

        SearchCriteriaBuilder searchCriteria = technologyDD.find().withMaxResults(1)
                .restrictedWith(Restrictions.belongsTo(technologyDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    private boolean compareDates(final DataDefinition dataDefinition, final Entity entity, final String dateFromField,
            final String dateToField) {
        Date dateFrom = (Date) entity.getField(dateFromField);
        Date dateTo = (Date) entity.getField(dateToField);

        if (dateFrom == null || dateTo == null) {
            return true;
        }

        if (dateFrom.after(dateTo)) {
            entity.addError(dataDefinition.getField(dateToField), "products.validate.global.error.datesOrder");
            return false;
        } else {
            return true;
        }
    }

}
