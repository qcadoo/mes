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

import java.math.BigDecimal;
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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDatesService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");

        FieldComponent productionLine = (FieldComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);

        Entity defaultProductionLine = getDefaultProductionLine();

        if ((orderForm.getEntityId() == null) && (productionLine.getFieldValue() == null) && (defaultProductionLine != null)) {
            productionLine.setFieldValue(defaultProductionLine.getId());
            productionLine.requestComponentUpdateState();
        }
    }

    public void fillProductionLine(final DataDefinition orderDD, final Entity order) {
        if (order.getId() != null) {
            return;
        }

        if (order.getBelongsToField(OrderFields.PRODUCTION_LINE) != null) {
            return;
        }

        Entity defaultProductionLine = getDefaultProductionLine();

        if (defaultProductionLine != null) {
            order.setField(OrderFields.PRODUCTION_LINE, defaultProductionLine);
        }
    }

    private Entity getDefaultProductionLine() {
        return parameterService.getParameter().getBelongsToField(L_DEFAULT_PRODUCTION_LINE);
    }

    public void setDefaultNameUsingTechnology(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!(component instanceof FieldComponent)) {
            return;
        }

        FieldComponent productField = (FieldComponent) view.getComponentByReference(OrderFields.PRODUCT);
        FieldComponent technologyField = (FieldComponent) view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
        FieldComponent name = (FieldComponent) view.getComponentByReference(OrderFields.NAME);

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

    public String makeDefaultName(final Entity product, Entity technology, final Locale locale) {

        if (technology == null) {
            technology = technologyServiceO.getDefaultTechnology(product);
        }

        String technologyNumber = L_EMPTY_NUMBER;
        if (technology != null) {
            technologyNumber = "tech. " + technology.getStringField(TechnologyFields.NUMBER);
        }

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(new Date());

        return translationService.translate("orders.order.name.default", locale, product.getStringField(OrderFields.NAME),
                product.getStringField(ProductFields.NUMBER), technologyNumber,
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
    }

    private Entity getProductById(final Long id) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(id);
    }

    private Entity getTechnologyById(final Long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(id);
    }

    public void setAndDisableState(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        FieldComponent orderState = (FieldComponent) state.getComponentByReference(OrderFields.STATE);

        orderState.setEnabled(false);

        if (form.getEntityId() != null) {
            return;
        }

        orderState.setFieldValue(OrderState.PENDING.getStringValue());
    }

    public void generateOrderNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                "form", OrderFields.NUMBER);
    }

    public void fillDefaultTechnology(final ViewDefinitionState state) {
        LookupComponent productField = (LookupComponent) state.getComponentByReference(OrderFields.PRODUCT);
        FieldComponent defaultTechnology = (FieldComponent) state.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);

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

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState view) {
        FieldComponent product = (FieldComponent) view.getComponentByReference(OrderFields.PRODUCT);
        FieldComponent technology = (FieldComponent) view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
        FieldComponent defaultTechnology = (FieldComponent) view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);
        FieldComponent plannedQuantity = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);

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
        FormComponent orderForm = (FormComponent) view.getComponentByReference("form");

        boolean disabled = false;

        if (orderForm.getEntityId() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    orderForm.getEntityId());
            if (order == null) {
                return;
            }
            String state = order.getStringField(OrderFields.STATE);
            if (!OrderState.PENDING.getStringValue().equals(state)) {
                disabled = true;
            }
        }

        orderForm.setFormEnabled(!disabled);

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

    public boolean checkOrderPlannedQuantity(final DataDefinition orderDD, final Entity order) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        if (product == null) {
            return true;
        }
        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        if (plannedQuantity == null) {
            order.addError(orderDD.getField(OrderFields.PLANNED_QUANTITY), "orders.validate.global.error.plannedQuantityError");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkOrderTechnology(final DataDefinition orderDD, final Entity order) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        if (product == null) {
            return true;
        }
        if (order.getField(OrderFields.TECHNOLOGY) == null && hasAnyTechnologies(product.getId())) {
            order.addError(orderDD.getField(OrderFields.TECHNOLOGY), "orders.validate.global.error.technologyError");
            return false;
        }
        return true;
    }

    public boolean checkComponentOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = null;
        if (OrdersConstants.MODEL_ORDER.equals(entity.getDataDefinition().getName())) {
            order = entity;
        } else {
            order = entity.getBelongsToField(OrdersConstants.MODEL_ORDER);
        }

        if (order == null) {
            return true;
        }

        if (order.getField(OrderFields.TECHNOLOGY) == null) {
            entity.addError(dataDefinition.getField(OrdersConstants.MODEL_ORDER),
                    "orders.validate.global.error.orderMustHaveTechnology");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfOrderTechnologyHasOperations(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(OrdersConstants.MODEL_ORDER);

        if (order == null || order.getField(OrderFields.TECHNOLOGY) == null) {
            return true;
        }

        if (order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField("operationComponents").isEmpty()) {
            entity.addError(dataDefinition.getField(OrdersConstants.MODEL_ORDER),
                    "orders.validate.global.error.orderTechnologyMustHaveOperation");
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
                .belongsTo(TechnologyFields.PRODUCT, selectedProductId);

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
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology != null) {
            if (order.getHasManyField("genealogies").isEmpty()) {
                if (technology.getBooleanField("batchRequired")) {
                    return false;
                }
                if (technology.getBooleanField("shiftFeatureRequired")) {
                    return false;
                }
                if (technology.getBooleanField("postFeatureRequired")) {
                    return false;
                }
                if (technology.getBooleanField("otherFeatureRequired")) {
                    return false;
                }
                for (Entity operationComponent : technology.getTreeField("operationComponents")) {
                    for (Entity operationProductComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                        if (operationProductComponent.getBooleanField("batchRequired")) {
                            return false;
                        }
                    }
                }
            }
            for (Entity genealogy : order.getHasManyField("genealogies")) {
                if (technology.getBooleanField("batchRequired") && genealogy.getField("batch") == null) {
                    return false;
                }
                if (technology.getBooleanField("shiftFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("shiftFeatures");
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if (technology.getBooleanField("postFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("postFeatures");
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if (technology.getBooleanField("otherFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("otherFeatures");
                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                for (Entity genealogyProductIn : genealogy.getHasManyField("productInComponents")) {
                    if (genealogyProductIn.getBelongsToField("productInComponent").getBooleanField("batchRequired")) {
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
        if (OrderState.DECLINED.getStringValue().equals(order.getStringField(OrderFields.STATE))
                || OrderState.ABANDONED.getStringValue().equals(order.getStringField(OrderFields.STATE))) {
            return true;
        }
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(order.getStringField(OrderFields.ORDER_TYPE))
                && order.isActive()) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
            if (technology == null) {
                return true;
            }
            TechnologyState technologyState = TechnologyState.parseString(technology.getStringField(TechnologyFields.STATE));

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
