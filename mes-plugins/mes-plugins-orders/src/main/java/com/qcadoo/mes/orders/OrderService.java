/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import static com.qcadoo.mes.orders.constants.OrderFields.COMPANY;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EXTERNAL_NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.EXTERNAL_SYNCHRONIZED;
import static com.qcadoo.mes.orders.constants.OrderFields.NAME;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrderStates.DECLINED;
import static com.qcadoo.mes.orders.constants.OrdersConstants.BASIC_MODEL_PRODUCT;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_BATCH_REQUIRED;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_FORM;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_NUMBER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.TechnologyStateUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ExpressionService expressionService;

    private static final String EMPTY_NUMBER = "";

    public Entity getOrder(final Long orderId) {
        return getOrderDataDefinition().get(orderId);
    }

    private DataDefinition getOrderDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    public boolean clearOrderDatesOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(STATE, OrderStates.PENDING.getStringValue());
        entity.setField(EFFECTIVE_DATE_TO, null);
        entity.setField(EFFECTIVE_DATE_FROM, null);
        entity.setField("doneQuantity", null);
        entity.setField(EXTERNAL_NUMBER, null);
        entity.setField(EXTERNAL_SYNCHRONIZED, true);
        return true;
    }

    public void setDefaultNameUsingTechnology(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!(component instanceof FieldComponent)) {
            return;
        }

        FieldComponent productField = (FieldComponent) view.getComponentByReference(BASIC_MODEL_PRODUCT);
        FieldComponent technologyField = (FieldComponent) view.getComponentByReference("technology");
        FieldComponent name = (FieldComponent) view.getComponentByReference(NAME);

        if (technologyField.getFieldValue() == null || productField.getFieldValue() == null
                || StringUtils.hasText((String) name.getFieldValue())) {
            return;
        }

        Entity productEntity = getProductById((Long) productField.getFieldValue());
        Entity technologyEntity = getTechnologyById((Long) technologyField.getFieldValue());

        if (productEntity == null) {
            return;
        }

        Locale locale = component.getLocale();
        name.setFieldValue(makeDefaultName(productEntity, technologyEntity, locale));
    }

    public String makeDefaultName(final Entity productEntity, Entity technologyEntity, final Locale locale) {

        if (technologyEntity == null) {
            technologyEntity = getDefaultTechnology(productEntity.getId());
        }
        String technologyNumber = EMPTY_NUMBER;
        if (technologyEntity != null) {
            technologyNumber = "tech. " + technologyEntity.getStringField(FIELD_NUMBER);
        }

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(new Date());

        return translationService.translate("orders.order.name.default", locale, productEntity.getStringField(NAME),
                productEntity.getStringField(FIELD_NUMBER), technologyNumber,
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
    }

    private Entity getProductById(final Long id) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(id);
    }

    private Entity getTechnologyById(final Long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(id);
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent product = (FieldComponent) state;
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY);
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
        FormComponent form = (FormComponent) state.getComponentByReference(FIELD_FORM);
        FieldComponent orderState = (FieldComponent) state.getComponentByReference(STATE);

        orderState.setEnabled(false);

        if (form.getEntityId() != null) {
            return;
        }

        orderState.setFieldValue("01pending");
    }

    public void generateOrderNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                FIELD_FORM, FIELD_NUMBER);
    }

    public void fillDefaultTechnology(final ViewDefinitionState state) {
        FieldComponent product = (FieldComponent) state.getComponentByReference(BASIC_MODEL_PRODUCT);
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
        FieldComponent product = (FieldComponent) state.getComponentByReference(BASIC_MODEL_PRODUCT);
        FieldComponent technology = (FieldComponent) state.getComponentByReference(TECHNOLOGY);
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");
        FieldComponent plannedQuantity = (FieldComponent) state.getComponentByReference(PLANNED_QUANTITY);

        defaultTechnology.setEnabled(false);

        if (product.getFieldValue() == null || !hasAnyTechnologies((Long) product.getFieldValue())) {
            // technology.setEnabled(false);
            technology.setRequired(false);
            plannedQuantity.setRequired(false);
        } else {
            technology.setRequired(true);
            plannedQuantity.setRequired(true);
        }
    }

    public void disableFieldOrder(final ViewDefinitionState view) {
        FormComponent order = (FormComponent) view.getComponentByReference(FIELD_FORM);
        FieldComponent technology = (FieldComponent) view.getComponentByReference(TECHNOLOGY);

        boolean disabled = false;

        if (order.getEntityId() != null) {
            Entity entity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    order.getEntityId());
            if (entity == null) {
                return;
            }
            String state = entity.getStringField(STATE);
            if (("04completed".equals(state) || "05declined".equals(state) || "07abandoned".equals(state)) && order.isValid()) {
                disabled = true;
            }
        }

        order.setFormEnabled(!disabled);
        technology.setEnabled(!disabled);
    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        return compareDates(dataDefinition, entity, DATE_FROM, DATE_TO);
    }

    public boolean checkOrderPlannedQuantity(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(BASIC_MODEL_PRODUCT);
        if (product == null) {
            return true;
        }
        Object o = entity.getField(PLANNED_QUANTITY);
        if (o == null) {
            entity.addError(dataDefinition.getField(PLANNED_QUANTITY), "orders.validate.global.error.plannedQuantityError");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkOrderTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(BASIC_MODEL_PRODUCT);
        if (product == null) {
            return true;
        }
        if (entity.getField(TECHNOLOGY) == null && hasAnyTechnologies(product.getId())) {
            entity.addError(dataDefinition.getField(TECHNOLOGY), "orders.validate.global.error.technologyError");
            return false;
        }
        return true;
    }

    public void fillOrderDates(final DataDefinition dataDefinition, final Entity entity) {
        if (("03inProgress".equals(entity.getField(STATE)) || "04completed".equals(entity.getField(STATE)))
                && entity.getField(EFFECTIVE_DATE_FROM) == null) {
            entity.setField(EFFECTIVE_DATE_FROM, new Date());
        }
        if ("04completed".equals(entity.getField(STATE)) && entity.getField(EFFECTIVE_DATE_TO) == null) {
            entity.setField(EFFECTIVE_DATE_TO, new Date());
        }
    }

    public boolean checkComponentOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = null;
        if (MODEL_ORDER.equals(entity.getDataDefinition().getName())) {
            order = entity;
        } else {
            order = entity.getBelongsToField(MODEL_ORDER);
        }

        if (order == null) {
            return true;
        }

        if (order.getField(TECHNOLOGY) == null) {
            entity.addError(dataDefinition.getField(MODEL_ORDER), "orders.validate.global.error.orderMustHaveTechnology");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfOrderTechnologyHasOperations(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(MODEL_ORDER);

        if (order == null || order.getField(TECHNOLOGY) == null) {
            return true;
        }

        if (order.getBelongsToField(TECHNOLOGY).getTreeField("operationComponents").isEmpty()) {
            entity.addError(dataDefinition.getField(MODEL_ORDER), "orders.validate.global.error.orderTechnologyMustHaveOperation");
            return false;
        } else {
            return true;
        }
    }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition instructionDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        SearchCriteriaBuilder searchCriteria = instructionDD.find().setMaxResults(1).add(SearchRestrictions.eq("master", true))
                .belongsTo(BASIC_MODEL_PRODUCT, selectedProductId);

        SearchResult searchResult = searchCriteria.list();

        if (searchResult.getTotalNumberOfEntities() == 1) {
            return searchResult.getEntities().get(0);
        } else {
            return null;
        }
    }

    private boolean hasAnyTechnologies(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        SearchCriteriaBuilder searchCriteria = technologyDD.find().setMaxResults(1)
                .belongsTo(BASIC_MODEL_PRODUCT, selectedProductId);

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
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();
        Entity parameter = null;
        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }
        if ((parameter == null) || (parameter.getField("batchForDoneOrder") == null)) {
            return false;
        } else {
            return !(parameter.getField("batchForDoneOrder").toString().equals("01none"));
        }
    }

    public boolean checkRequiredBatch(final Entity order) {
        Entity technology = (Entity) order.getField(TECHNOLOGY);
        if (technology != null) {
            if (order.getHasManyField("genealogies").isEmpty()) {
                if ((Boolean) technology.getField(FIELD_BATCH_REQUIRED)) {
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
                        if ((Boolean) operationProductComponent.getField(FIELD_BATCH_REQUIRED)) {
                            return false;
                        }
                    }
                }
            }
            for (Entity genealogy : order.getHasManyField("genealogies")) {
                if ((Boolean) technology.getField(FIELD_BATCH_REQUIRED) && genealogy.getField("batch") == null) {
                    return false;
                }
                if ((Boolean) technology.getField("shiftFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("shiftFeatures");
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField("postFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("postFeatures");
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if ((Boolean) technology.getField("otherFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("otherFeatures");
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                for (Entity genealogyProductIn : genealogy.getHasManyField("productInComponents")) {
                    if ((Boolean) (genealogyProductIn.getBelongsToField("productInComponent").getField(FIELD_BATCH_REQUIRED))) {
                        List<Entity> entityList = genealogyProductIn.getHasManyField("batch");
                        if (entityList.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkIfTechnologyIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity technology = entity.getBelongsToField(TECHNOLOGY);

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity technologyEntity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technology.getId());

        if (technologyEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(TECHNOLOGY, null);
            return false;
        } else {
            return true;
        }
    }

    public void disableOrderFormForExternalItems(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(FIELD_FORM);

        if (form.getEntityId() == null) {
            return;
        }

        Entity entity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get(form.getEntityId());

        if (entity == null) {
            return;
        }

        String externalNumber = entity.getStringField(EXTERNAL_NUMBER);
        boolean externalSynchronized = (Boolean) entity.getField(EXTERNAL_SYNCHRONIZED);

        if (StringUtils.hasText(externalNumber) || !externalSynchronized) {
            state.getComponentByReference(FIELD_NUMBER).setEnabled(false);
            state.getComponentByReference(NAME).setEnabled(false);
            state.getComponentByReference(COMPANY).setEnabled(false);
            state.getComponentByReference(DATE_FROM).setEnabled(false);
            state.getComponentByReference(DATE_TO).setEnabled(false);
            state.getComponentByReference(BASIC_MODEL_PRODUCT).setEnabled(false);
            state.getComponentByReference(PLANNED_QUANTITY).setEnabled(false);
        }
    }

    public boolean checkChosenTechnologyState(final DataDefinition orderDD, final Entity order) {
        if (DECLINED.getStringValue().equals(order.getStringField(STATE))) {
            return true;
        }

        if (order.isActive()) {
            Entity technology = order.getBelongsToField(TECHNOLOGY);
            if (technology == null) {
                return true;
            }
            TechnologyState technologyState = TechnologyStateUtils.getStateFromField(technology.getStringField(STATE));

            if (TechnologyState.CHECKED != technologyState && TechnologyState.ACCEPTED != technologyState) {
                order.addError(orderDD.getField(TECHNOLOGY), "orders.validate.technology.error.wrongState.checked");
                return false;
            }
        }

        return true;
    }

}
