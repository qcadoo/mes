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
package com.qcadoo.mes.orders;

import static com.qcadoo.mes.orders.constants.OrderFields.DEFAULT_TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrderFields.NAME;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.BASIC_MODEL_PRODUCT;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_BATCH_REQUIRED;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_FORM;
import static com.qcadoo.mes.orders.constants.OrdersConstants.FIELD_NUMBER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.states.constants.OrderState.DECLINED;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDatesService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderService {

    private static final String L_DEFAULT_PRODUCTION_LINE = "defaultProductionLine";

    private static final String L_EMPTY_NUMBER = "";

    private static final Set<String> ORDER_STARTED_STATES = Collections.unmodifiableSet(Sets.newHashSet(
            OrderState.IN_PROGRESS.getStringValue(), OrderState.COMPLETED.getStringValue(),
            OrderState.INTERRUPTED.getStringValue()));

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrderDatesService orderDatesService;

    public Entity getOrder(final Long orderId) {
        return getOrderDataDefinition().get(orderId);
    }

    private DataDefinition getOrderDataDefinition() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    public boolean isOrderStarted(final String orderState) {
        return ORDER_STARTED_STATES.contains(orderState);
    }

    public final void fillProductionLine(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        fillProductionLine(view);
    }

    public final void fillProductionLine(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(FIELD_FORM);

        FieldComponent productionLine = (FieldComponent) view.getComponentByReference(PRODUCTION_LINE);

        if ((orderForm.getEntityId() == null) && (productionLine.getFieldValue() == null) && getDefaultProductionLine() != null) {
            productionLine.setFieldValue(getDefaultProductionLine().getId());
            productionLine.requestComponentUpdateState();
        }
    }

    public void fillProductionLine(final DataDefinition orderDD, final Entity order) {
        if (order.getId() != null) {
            return;
        }

        if (order.getBelongsToField(PRODUCTION_LINE) != null) {
            return;
        }

        Entity defaultProductionLine = getDefaultProductionLine();

        if (defaultProductionLine != null) {
            order.setField(PRODUCTION_LINE, defaultProductionLine);
        }
    }

    private Entity getDefaultProductionLine() {
        return parameterService.getParameter().getBelongsToField(L_DEFAULT_PRODUCTION_LINE);
    }

    public void setDefaultNameUsingTechnology(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!(component instanceof FieldComponent)) {
            return;
        }

        FieldComponent productField = (FieldComponent) view.getComponentByReference(BASIC_MODEL_PRODUCT);
        FieldComponent technologyField = (FieldComponent) view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
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
            technologyEntity = technologyServiceO.getDefaultTechnology(productEntity);
        }

        String technologyNumber = L_EMPTY_NUMBER;
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

    public void setAndDisableState(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(FIELD_FORM);
        FieldComponent orderState = (FieldComponent) state.getComponentByReference(STATE);

        orderState.setEnabled(false);

        if (form.getEntityId() != null) {
            return;
        }

        orderState.setFieldValue(OrderState.PENDING.getStringValue());
    }

    public void generateOrderNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                FIELD_FORM, FIELD_NUMBER);
    }

    public void fillDefaultTechnology(final ViewDefinitionState state) {
        LookupComponent productField = (LookupComponent) state.getComponentByReference(BASIC_MODEL_PRODUCT);
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference("defaultTechnology");

        Entity product = productField.getEntity();
        if (product != null) {
            Entity defaultTechnologyEntity = technologyServiceO.getDefaultTechnology(product);
            if (defaultTechnologyEntity != null) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name",
                        state.getLocale());
                defaultTechnology.setFieldValue(defaultTechnologyValue);
            }
        }
    }

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState state) {
        FieldComponent product = (FieldComponent) state.getComponentByReference(BASIC_MODEL_PRODUCT);
        FieldComponent technology = (FieldComponent) state.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference(DEFAULT_TECHNOLOGY);
        FieldComponent plannedQuantity = (FieldComponent) state.getComponentByReference(PLANNED_QUANTITY);

        defaultTechnology.setEnabled(false);

        if (product.getFieldValue() == null || !hasAnyTechnologies((Long) product.getFieldValue())) {
            technology.setRequired(false);
            plannedQuantity.setRequired(false);
        } else {
            technology.setRequired(true);
            plannedQuantity.setRequired(true);
        }
    }

    public void disableFieldOrderForm(final ViewDefinitionState view) {
        FormComponent order = (FormComponent) view.getComponentByReference(FIELD_FORM);

        boolean disabled = false;

        if (order.getEntityId() != null) {
            Entity entity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    order.getEntityId());
            if (entity == null) {
                return;
            }
            String state = entity.getStringField(STATE);
            if (!OrderState.PENDING.getStringValue().equals(state)) {
                disabled = true;
            }
        }

        order.setFormEnabled(!disabled);

    }

    public boolean checkOrderDates(final DataDefinition dataDefinition, final Entity order) {
        DateRange orderDateRange = orderDatesService.getCalculatedDates(order);
        Date dateFrom = orderDateRange.getFrom();
        Date dateTo = orderDateRange.getTo();

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }
        order.addError(dataDefinition.getField(OrderFields.FINISH_DATE), "orders.validate.global.error.datesOrder");

        return false;
    }

    public void checkOrderDates(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        DateRange orderDateRange = orderDatesService.getCalculatedDates(order);
        Date dateFrom = orderDateRange.getFrom();
        Date dateTo = orderDateRange.getTo();

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return;
        }
        stateChangeContext.addValidationError("orders.validate.global.error.datesOrder.overdue");

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

    private boolean hasAnyTechnologies(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        // TODO DEV_TEAM change this criteria to projection using count(*). This will enable us to avoid unnecessary mapping.
        SearchCriteriaBuilder searchCriteria = technologyDD.find().setMaxResults(1)
                .belongsTo(BASIC_MODEL_PRODUCT, selectedProductId);

        SearchResult searchResult = searchCriteria.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public boolean checkAutogenealogyRequired() {
        Entity parameter = parameterService.getParameter();
        if (parameter.getField("batchForDoneOrder") == null) {
            return false;
        } else {
            return !"01none".equals(parameter.getStringField("batchForDoneOrder"));
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
        Entity technology = entity.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);

        if (technology == null || technology.getId() == null) {
            return true;
        }

        Entity technologyEntity = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technology.getId());

        if (technologyEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(OrderFields.TECHNOLOGY_PROTOTYPE, null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkChosenTechnologyState(final DataDefinition orderDD, final Entity order) {
        if (DECLINED.getStringValue().equals(order.getStringField(STATE))) {
            return true;
        }

        if (order.isActive()) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
            if (technology == null) {
                return true;
            }
            TechnologyState technologyState = TechnologyState.parseString(technology.getStringField(STATE));

            if (TechnologyState.CHECKED != technologyState && TechnologyState.ACCEPTED != technologyState) {
                order.addError(orderDD.getField(OrderFields.TECHNOLOGY_PROTOTYPE),
                        "orders.validate.technology.error.wrongState.checked");
                return false;
            }
        }

        return true;
    }

    public void changeFieldState(final ViewDefinitionState view, final String booleanFieldComponentName,
            final String fieldComponentName) {
        FieldComponent booleanFieldComponent = (FieldComponent) view.getComponentByReference(booleanFieldComponentName);

        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldComponentName);

        if ("1".equals(booleanFieldComponent.getFieldValue())) {
            fieldComponent.setEnabled(true);
            fieldComponent.requestComponentUpdateState();
        } else {
            fieldComponent.setEnabled(false);
            fieldComponent.requestComponentUpdateState();
        }
    }

}
