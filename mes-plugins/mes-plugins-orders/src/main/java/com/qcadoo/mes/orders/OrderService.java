/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.orders;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public final class OrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ExpressionService expressionService;

    public boolean clearOrderDatesAndWorkersOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("state", "01pending");
        entity.setField("effectiveDateTo", null);
        entity.setField("endWorker", null);
        entity.setField("effectiveDateFrom", null);
        entity.setField("startWorker", null);
        entity.setField("doneQuantity", null);
        return true;
    }

    public void printOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity order = dataDefinitionService.get("orders", "order").get((Long) state.getFieldValue());
            if (order == null) {
                state.addMessage(translationService.translate("core.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/orders/order." + args[0] + "?id=" + state.getFieldValue(), true, false);
            }

        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");

        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            dateTo.setFieldValue(dateFrom.getFieldValue());
        }
    }

    public void setDefaultNameUsingProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        FieldComponent product = (FieldComponent) state;
        FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference("name");

        if (!StringUtils.hasText((String) name.getFieldValue())) {
            Entity entity = dataDefinitionService.get("basic", "product").get((Long) product.getFieldValue());
            name.setFieldValue(translationService.translate("orders.order.name.default", state.getLocale(),
                    entity.getStringField("number")));
        }
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        FieldComponent product = (FieldComponent) state;
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference("defaultTechnology");

        defaultTechnology.setFieldValue("");
        technology.setFieldValue(null);

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology((Long) product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                technology.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    public void setAndDisableState(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent orderState = (FieldComponent) state.getComponentByReference("state");

        orderState.setEnabled(false);

        if (form.getEntityId() != null) {
            return;
        }

        orderState.setFieldValue("01pending");
    }

    public void activateOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state.getFieldValue() != null) {

            // TODO mady
            DataDefinition orderDataDefinition = dataDefinitionService.get("orders", "order");
            Entity order = orderDataDefinition.get((Long) state.getFieldValue());

            if (state instanceof FormComponent) {
                state.performEvent(viewDefinitionState, "save", new String[0]);

                if (!state.isHasError()) {

                    FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference("state");

                    if (Boolean.parseBoolean(args[0])) {
                        orderState.setFieldValue("02inProgress");
                    } else {

                        if (checkAutogenealogyRequired() && !checkRequiredBatch(order)) {
                            state.addMessage(
                                    translationService.translate("genealogies.message.batchNotFound", state.getLocale()),
                                    MessageType.FAILURE);
                            return;
                        }
                        if (isQualityControlAutoCheckEnabled() && !checkIfAllQualityControlsAreClosed(order)) {
                            state.addMessage(
                                    translationService.translate("qualityControls.qualityControls.not.closed", state.getLocale()),
                                    MessageType.FAILURE);
                            return;
                        }

                        orderState.setFieldValue("03done");
                    }

                    state.performEvent(viewDefinitionState, "save", new String[0]);
                }
            } else if (state instanceof GridComponent) {
                if (Boolean.parseBoolean(args[0])) {
                    order.setField("state", "02inProgress");
                } else {
                    if (checkAutogenealogyRequired() && !checkRequiredBatch(order)) {
                        state.addMessage(translationService.translate("genealogies.message.batchNotFound", state.getLocale()),
                                MessageType.INFO);
                        return;
                    }

                    if (isQualityControlAutoCheckEnabled() && !checkIfAllQualityControlsAreClosed(order)) {
                        state.addMessage(
                                translationService.translate("qualityControls.qualityControls.not.closed", state.getLocale()),
                                MessageType.FAILURE);
                        return;
                    }

                    order.setField("state", "03done");
                }

                orderDataDefinition.save(order);

                state.performEvent(viewDefinitionState, "refresh", new String[0]);
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("core.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    private boolean checkIfAllQualityControlsAreClosed(final Entity order) {

        Object controlTypeField = order.getBelongsToField("technology").getField("qualityControlType");

        if (controlTypeField != null) {
            DataDefinition qualityControlDD = null;

            qualityControlDD = dataDefinitionService.get("qualityControls", "qualityControl");

            if (qualityControlDD != null) {
                SearchCriteriaBuilder searchCriteria = qualityControlDD.find()
                        .addRestriction(Restrictions.belongsTo(qualityControlDD.getField("order"), order.getId()))
                        .addRestriction(Restrictions.eq("closed", false));

                SearchResult searchResult = searchCriteria.list();

                return (searchResult.getTotalNumberOfEntities() <= 0);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isQualityControlAutoCheckEnabled() {
        SearchResult searchResult = dataDefinitionService.get("basic", "parameter").find().setMaxResults(1).list();

        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }

        if (parameter != null) {
            return (Boolean) parameter.getField("checkDoneOrderForQuality");
        } else {
            return false;
        }
    }

    public void generateOrderNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, "orders", "order", "form", "number");
    }

    public void fillDefaultTechnology(final ViewDefinitionState state) {
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology((Long) product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name",
                        state.getLocale());
                defaultTechnology.setFieldValue(defaultTechnologyValue);
            }
        }
    }

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState state) {
        FieldComponent product = (FieldComponent) state.getComponentByReference("product");
        FieldComponent technology = (FieldComponent) state.getComponentByReference("technology");
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");
        FieldComponent plannedQuantity = (FieldComponent) state.getComponentByReference("plannedQuantity");

        defaultTechnology.setEnabled(false);

        if (product.getFieldValue() == null || !hasAnyTechnologies((Long) product.getFieldValue())) {
            technology.setEnabled(false);
            technology.setRequired(false);
            plannedQuantity.setRequired(false);
        } else {
            technology.setRequired(true);
            plannedQuantity.setRequired(true);
        }
    }

    public void disableFormForDoneOrder(final ViewDefinitionState state) {
        FormComponent order = (FormComponent) state.getComponentByReference("form");
        FieldComponent technology = (FieldComponent) state.getComponentByReference("technology");

        boolean disabled = false;

        if (order.getEntityId() != null) {
            Entity entity = dataDefinitionService.get("orders", "order").get(order.getEntityId());

            if (entity != null && "03done".equals(entity.getStringField("state")) && order.isValid()) {
                disabled = true;
            }
        }

        order.setFormEnabled(!disabled);
        technology.setEnabled(!disabled);
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        return compareDates(dataDefinition, entity, "dateFrom", "dateTo");
    }

    public boolean checkOrderPlannedQuantity(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField("product");
        if (product == null) {
            return true;
        }
        Object o = entity.getField("plannedQuantity");
        if (o == null) {
            entity.addError(dataDefinition.getField("plannedQuantity"), "orders.validate.global.error.plannedQuantityError");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkOrderTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField("product");
        if (product == null) {
            return true;
        }
        if (entity.getField("technology") == null && hasAnyTechnologies(product.getId())) {
            entity.addError(dataDefinition.getField("technology"), "orders.validate.global.error.technologyError");
            return false;
        }
        return true;
    }

    public void fillOrderDatesAndWorkers(final DataDefinition dataDefinition, final Entity entity) {
        if (securityService.getCurrentUserName() == null) {
            return;
        }
        if (("02inProgress".equals(entity.getField("state")) || "03done".equals(entity.getField("state")))
                && entity.getField("effectiveDateFrom") == null) {
            entity.setField("effectiveDateFrom", new Date());
            entity.setField("startWorker", securityService.getCurrentUserName());
        }
        if ("03done".equals(entity.getField("state")) && entity.getField("effectiveDateTo") == null) {
            entity.setField("effectiveDateTo", new Date());
            entity.setField("endWorker", securityService.getCurrentUserName());
        }
    }

    public boolean checkIfOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField("order");

        if (order == null) {
            return true;
        }

        if (order.getField("technology") == null) {
            entity.addError(dataDefinition.getField("order"), "orders.validate.global.error.orderMustHaveTechnology");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfOrderTechnologyHasOperations(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField("order");

        if (order == null || order.getField("technology") == null) {
            return true;
        }

        if (order.getBelongsToField("technology").getTreeField("operationComponents").isEmpty()) {
            entity.addError(dataDefinition.getField("order"), "orders.validate.global.error.orderTechnologyMustHaveOperation");
            return false;
        } else {
            return true;
        }
    }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get("technologies", "technology");

        SearchCriteriaBuilder searchCriteria = instructionDD.find().setMaxResults(1)
                .addRestriction(Restrictions.eq(instructionDD.getField("master"), true))
                .addRestriction(Restrictions.belongsTo(instructionDD.getField("product"), selectedProductId));

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    private boolean hasAnyTechnologies(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get("technologies", "technology");

        SearchCriteriaBuilder searchCriteria = technologyDD.find().setMaxResults(1)
                .addRestriction(Restrictions.belongsTo(technologyDD.getField("product"), selectedProductId));

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
            entity.addError(dataDefinition.getField(dateToField), "orders.validate.global.error.datesOrder");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkAutogenealogyRequired() {
        SearchResult searchResult = dataDefinitionService.get("basic", "parameter").find().setMaxResults(1).list();
        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }
        if (parameter != null) {
            return !(parameter.getField("batchForDoneOrder").toString().equals("01none"));
        } else {
            return false;
        }
    }

    public boolean checkRequiredBatch(final Entity order) {
        Entity technology = (Entity) order.getField("technology");
        if (technology != null) {
            if (order.getHasManyField("genealogies").size() == 0) {
                if ((Boolean) technology.getField("batchRequired")) {
                    return false;
                }
                if ((Boolean) technology.getField("shiftFeatureRequired")) {
                    return false;
                }
                if ((Boolean) technology.getField("postFeatureRequired")) {
                    return false;
                }
                if ((Boolean) technology.getField("otherFeatureRequired")) {
                    return false;
                }
                for (Entity operationComponent : technology.getTreeField("operationComponents")) {
                    for (Entity operationProductComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                        if ((Boolean) operationProductComponent.getField("batchRequired")) {
                            return false;
                        }
                    }
                }
            }
            for (Entity genealogy : order.getHasManyField("genealogies")) {
                if ((Boolean) technology.getField("batchRequired") && genealogy.getField("batch") == null) {
                    return false;
                }
                if ((Boolean) technology.getField("shiftFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("shiftFeatures");
                    if (entityList.size() == 0) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField("postFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("postFeatures");
                    if (entityList.size() == 0) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField("otherFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("otherFeatures");
                    if (entityList.size() == 0) {
                        return false;
                    }
                }
                for (Entity genealogyProductIn : genealogy.getHasManyField("productInComponents")) {
                    if ((Boolean) (genealogyProductIn.getBelongsToField("productInComponent").getField("batchRequired"))) {
                        List<Entity> entityList = genealogyProductIn.getHasManyField("batch");
                        if (entityList.size() == 0) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkIfTechnologyIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = entity.getBelongsToField("technology");

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity technologyEntity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technology.getId());

        if (technologyEntity == null) {
            entity.addGlobalError("core.message.belongsToNotFound");
            entity.setField("technology", null);
            return false;
        } else {
            return true;
        }
    }

}
