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
import com.qcadoo.mes.beans.products.ProductsOrder;
import com.qcadoo.mes.beans.products.ProductsProduct;
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
                viewDefinitionState.redirectTo("/products/order." + args[0] + "?id=" + state.getFieldValue(), true);
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
