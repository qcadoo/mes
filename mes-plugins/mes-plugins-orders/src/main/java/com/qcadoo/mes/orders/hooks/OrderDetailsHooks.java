/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.criteriaModifiers.ProductionLineCriteriaModifiersO;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.mes.orders.states.OrderStateService;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderDetailsHooks {

    private static final String L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START = "commentReasonTypeDeviationsOfEffectiveStart";

    private static final String L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END = "commentReasonTypeDeviationsOfEffectiveEnd";

    private static final List<String> L_PREDEFINED_TECHNOLOGY_FIELDS = Lists.newArrayList("defaultTechnology",
            "technology", "predefinedTechnology");

    private static final String L_DONE_IN_PERCENTAGE = "doneInPercentage";

    private static final String L_DONE_IN_PERCENTAGE_UNIT = "doneInPercentageUnit";

    private static final String L_DIVISION = "division";

    private static final String L_RANGE = "range";

    private static final String L_ONE_DIVISION = "01oneDivision";

    private static final String L_ORDER_DETAILS_ACTIONS = "orderDetailsActions";

    private static final String L_REFRESH = "refresh";

    private static final String L_NAVIGATION = "navigation";

    private static final String L_BACK = "back";

    private static final String L_REASON_TYPE_OF_CHANGING_ORDER_STATE = "reasonTypeOfChangingOrderState";

    public static final String L_PARENT = "parent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderStateService orderStateService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private OrderProductQuantityHooks orderProductQuantityHooks;

    @Autowired
    private NumberService numberService;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public final void onBeforeRender(final ViewDefinitionState view) {
        generateOrderNumber(view);
        fillDefaultTechnology(view);
        fillProductionLine(view);
        disableFieldOrderForm(view);
        disableTechnologiesIfProductDoesNotAny(view);
        setAndDisableState(view);
        setOrderIdForMultiUploadField(view);
        unitService.fillProductUnitBeforeRender(view);
        changedEnabledFieldForSpecificOrderState(view);
        filterStateChangeHistory(view);
        disabledRibbonWhenOrderIsSynchronized(view);
        compareDeadlineAndEndDate(view);
        compareDeadlineAndStartDate(view);
        orderProductQuantityHooks.changeFieldsEnabledForSpecificOrderState(view);
        orderProductQuantityHooks.fillProductUnit(view);
        changeFieldsEnabledForSpecificOrderState(view);
        changePredefinedTechnologyFieldsVisibility(view);
        additionalUnitService.setAdditionalUnitField(view);
        unitService.fillProductForAdditionalUnitBeforeRender(view);
        fillOrderDescription(view);
        enableOrDisableGenerateOperationalTasksButton(view);
        enableOrDisableShowOrderTechnologicalProcesses(view);

        if (isValidDecimalField(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT))
                && isValidDecimalField(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY))) {
            setQuantities(view);
        }
        disableNumberForComponentOrder(view);
    }

    private void disableNumberForComponentOrder(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (Objects.nonNull(orderForm.getEntityId())) {
            Entity order = orderForm.getEntity().getDataDefinition().get(orderForm.getEntityId());
            if (Objects.nonNull(order.getBelongsToField(L_PARENT))) {
                FieldComponent numberComponent = (FieldComponent) view.getComponentByReference(OrderFields.NUMBER);
                numberComponent.setEnabled(false);
            }
        }
    }

    private void enableOrDisableGenerateOperationalTasksButton(final ViewDefinitionState view) {
        orderDetailsRibbonHelper.setButtonEnabled(view, "operationalTasks", "generateOperationalTasks",
                OrderDetailsRibbonHelper.CAN_NOT_GENERATE_OPERATIONAL_TASKS,
                Optional.of("orders.ribbon.message.canNotGenerateOperationalTasks"));
    }

    private void enableOrDisableShowOrderTechnologicalProcesses(final ViewDefinitionState view) {
        RibbonActionItem ribbonItem = orderDetailsRibbonHelper.getRibbonItem(view, "orderTechnologicalProcesses",
                "showOrderTechnologicalProcesses");

        Entity order = orderDetailsRibbonHelper.getOrderEntity(view);

        Optional<String> message = Optional.empty();

        boolean isEnabled = true;

        if (Objects.nonNull(order)) {
            if (parameterService.getParameter().getBooleanField(ParameterFieldsO.INCLUDE_PACKS_GENERATING_PROCESSES_FOR_ORDER)) {
                isEnabled = !order.getHasManyField(OrderFields.ORDER_PACKS).isEmpty();

                if (!isEnabled) {
                    message = Optional.of("orders.ribbon.message.canNotGenerateOrderTechnologicalProcesses");
                }
            }
        } else {
            isEnabled = false;
        }

        if (!isEnabled && message.isPresent()) {
            ribbonItem.setMessage(message.get());
        }

        ribbonItem.setEnabled(isEnabled);
        ribbonItem.requestUpdate(true);
    }

    public final void fillProductionLine(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (Objects.isNull(orderForm.getEntityId()) && view.isViewAfterRedirect()) {
            fillProductionLineForTechnology(view);
        } else if (view.isViewAfterRedirect()) {
            LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);
            FilterValueHolder filterValue = productionLineLookup.getFilterValue();

            LookupComponent technologyLookup = (LookupComponent) view
                    .getComponentByReference(OrderFields.TECHNOLOGY);
            Entity technology = technologyLookup.getEntity();
            fillProductionLineFilterValue(productionLineLookup, filterValue, technology);
        }
    }

    public void fillProductionLineForTechnology(ViewDefinitionState view) {
        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);
        FilterValueHolder filterValue = productionLineLookup.getFilterValue();

        LookupComponent technologyLookup = (LookupComponent) view
                .getComponentByReference(OrderFields.TECHNOLOGY);
        Entity technology = technologyLookup.getEntity();

        fillProductionLineFilterValue(productionLineLookup, filterValue, technology);
        Entity productionLine = orderService.getProductionLine(technology);
        fillProductionLine(productionLineLookup, productionLine);
    }

    private void fillProductionLineFilterValue(LookupComponent productionLineLookup, FilterValueHolder filterValue, Entity technology) {
        if (Objects.nonNull(technology)) {
            filterValue.put(ProductionLineCriteriaModifiersO.TECHNOLOGY_ID, technology.getId());
        } else {
            if (filterValue.has(ProductionLineCriteriaModifiersO.TECHNOLOGY_ID)) {
                filterValue.remove(ProductionLineCriteriaModifiersO.TECHNOLOGY_ID);
            }
        }
        productionLineLookup.setFilterValue(filterValue);
    }

    public void fillProductionLine(final LookupComponent productionLineLookup,
                                   final Entity defaultProductionLine) {
        if (Objects.nonNull(defaultProductionLine)) {
            productionLineLookup.setFieldValue(defaultProductionLine.getId());
        } else {
            productionLineLookup.setFieldValue(null);
        }
        productionLineLookup.requestComponentUpdateState();
    }

    public void fillDivision(final LookupComponent divisionLookup, final Entity technology, final Entity defaultProductionLine) {
        if (Objects.nonNull(technology) && PluginUtils.isEnabled(OrderService.L_PRODUCT_FLOW_THRU_DIVISION)
                && L_ONE_DIVISION.equals(technology.getField(L_RANGE))
                && Objects.nonNull(technology.getBelongsToField(L_DIVISION))) {
            divisionLookup.setFieldValue(technology.getBelongsToField(L_DIVISION).getId());
            divisionLookup.requestComponentUpdateState();
        } else if (Objects.nonNull(defaultProductionLine)) {
            List<Entity> divisions = defaultProductionLine.getManyToManyField(ProductionLineFields.DIVISIONS);

            if (divisions.size() == 1) {
                divisionLookup.setFieldValue(divisions.get(0).getId());
                divisionLookup.requestComponentUpdateState();
            }
        }
    }

    public void generateOrderNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                QcadooViewConstants.L_FORM, OrderFields.NUMBER);
    }

    public void fillDefaultTechnology(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);

        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            FilterValueHolder holder = technologyLookup.getFilterValue();

            holder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());

            technologyLookup.setFilterValue(holder);

            Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);

            if (Objects.nonNull(defaultTechnology)) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnology, "#number + ' - ' + #name",
                        view.getLocale());

                defaultTechnologyField.setFieldValue(defaultTechnologyValue);
            }
        }
    }

    public void disableFieldOrderForm(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        boolean disabled = false;

        Long orderId = orderForm.getEntityId();

        if (Objects.nonNull(orderId)) {
            Entity order = orderService.getOrder(orderId);

            if (Objects.isNull(order)) {
                return;
            }

            String state = order.getStringField(OrderFields.STATE);

            if (!OrderState.PENDING.getStringValue().equals(state)) {
                disabled = true;
            }
        }

        orderForm.setFormEnabled(!disabled);

        Entity order = orderForm.getEntity();
        Entity company = order.getBelongsToField(OrderFields.COMPANY);

        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(OrderFields.ADDRESS);

        if (Objects.isNull(company)) {
            addressLookup.setFieldValue(null);
            addressLookup.setEnabled(false);
        } else {
            addressLookup.setEnabled(true);
        }
    }

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);
        FieldComponent plannedQuantity = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);

        defaultTechnologyField.setEnabled(false);

        if (Objects.isNull(productLookup.getFieldValue()) || !hasAnyTechnologies(productLookup.getEntity())) {
            technologyLookup.setRequired(false);
            plannedQuantity.setRequired(false);
        } else {
            technologyLookup.setRequired(true);
            plannedQuantity.setRequired(true);
        }
    }

    private boolean hasAnyTechnologies(final Entity product) {
        SearchCriteriaBuilder searchCriteria = getTechnologyDD().find()
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product)).setMaxResults(1);

        SearchResult searchResult = searchCriteria.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public void setAndDisableState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(OrderFields.STATE);

        stateField.setEnabled(false);

        if (Objects.nonNull(orderForm.getEntityId())) {
            return;
        }

        stateField.setFieldValue(OrderState.PENDING.getStringValue());
    }

    public void changedEnabledFieldForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            List<String> references = Lists.newArrayList(OrderFields.CORRECTED_DATE_FROM, OrderFields.CORRECTED_DATE_TO,
                    OrderFields.REASON_TYPES_CORRECTION_DATE_FROM, OrderFields.REASON_TYPES_CORRECTION_DATE_TO,
                    OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END, OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START,
                    L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END, L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START,
                    OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM,
                    OrderFields.EFFECTIVE_DATE_FROM, OrderFields.EFFECTIVE_DATE_TO);

            changedEnabledFields(view, references, false);

            return;
        }

        final Entity order = getOrderDD().get(orderId);

        if (Objects.isNull(order)) {
            return;
        }

        String orderState = order.getStringField(OrderFields.STATE);

        if (OrderState.PENDING.getStringValue().equals(orderState)) {
            List<String> references = Lists.newArrayList(OrderFields.CORRECTED_DATE_FROM, OrderFields.CORRECTED_DATE_TO,
                    OrderFields.REASON_TYPES_CORRECTION_DATE_FROM, OrderFields.REASON_TYPES_CORRECTION_DATE_TO,
                    OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END, OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START,
                    L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END, L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START,
                    OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM,
                    OrderFields.EFFECTIVE_DATE_FROM, OrderFields.EFFECTIVE_DATE_TO);

            changedEnabledFields(view, references, false);
        }
        if (OrderState.ACCEPTED.getStringValue().equals(orderState)) {
            List<String> references = Lists.newArrayList(OrderFields.CORRECTED_DATE_FROM, OrderFields.CORRECTED_DATE_TO,
                    OrderFields.REASON_TYPES_CORRECTION_DATE_FROM, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM,
                    OrderFields.REASON_TYPES_CORRECTION_DATE_TO, OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO,
                    OrderFields.DATE_FROM, OrderFields.DATE_TO, OrderFields.EXPIRATION_DATE);

            boolean canChangeProdLineForAcceptedOrders = parameterService.getParameter()
                    .getBooleanField(ParameterFieldsO.CAN_CHANGE_PROD_LINE_FOR_ACCEPTED_ORDERS);

            if (canChangeProdLineForAcceptedOrders) {
                LookupComponent productionLineLookup = (LookupComponent) view
                        .getComponentByReference(OrderFields.PRODUCTION_LINE);
                productionLineLookup.setEnabled(true);
            }

            changedEnabledFields(view, references, true);
        }
        if (OrderState.IN_PROGRESS.getStringValue().equals(orderState)
                || OrderState.INTERRUPTED.getStringValue().equals(orderState)) {
            List<String> references = Lists.newArrayList(OrderFields.DATE_FROM, OrderFields.DATE_TO,
                    OrderFields.CORRECTED_DATE_TO, OrderFields.REASON_TYPES_CORRECTION_DATE_TO,
                    OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO, OrderFields.EFFECTIVE_DATE_FROM,
                    L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START, OrderFields.EXPIRATION_DATE);

            changedEnabledFields(view, references, true);
            changedEnabledAwesomeDynamicListComponents(view, Lists.newArrayList(OrderFields.REASON_TYPES_CORRECTION_DATE_FROM),
                    false);
            changedEnabledAwesomeDynamicListComponents(view, Lists.newArrayList(OrderFields.REASON_TYPES_CORRECTION_DATE_TO),
                    true);
            changedEnabledAwesomeDynamicListComponents(view,
                    Lists.newArrayList(OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START), true);
        }

        if (OrderState.COMPLETED.getStringValue().equals(orderState)) {
            List<String> references = Lists.newArrayList(OrderFields.EFFECTIVE_DATE_TO, OrderFields.DATE_TO,
                    OrderFields.EFFECTIVE_DATE_FROM, OrderFields.DATE_FROM, L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END,
                    L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START);

            changedEnabledFields(view, references, true);
            changedEnabledAwesomeDynamicListComponents(view, Lists.newArrayList(OrderFields.REASON_TYPES_CORRECTION_DATE_FROM),
                    false);
            changedEnabledAwesomeDynamicListComponents(view, Lists.newArrayList(OrderFields.REASON_TYPES_CORRECTION_DATE_TO),
                    false);
            changedEnabledAwesomeDynamicListComponents(view,
                    Lists.newArrayList(OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END), true);
            changedEnabledAwesomeDynamicListComponents(view,
                    Lists.newArrayList(OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START), true);
        }

        if (OrderState.ABANDONED.getStringValue().equals(orderState)) {
            List<String> references = Lists.newArrayList(OrderFields.EFFECTIVE_DATE_TO, OrderFields.DATE_TO,
                    OrderFields.EFFECTIVE_DATE_FROM, OrderFields.DATE_FROM, L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END,
                    L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START);

            changedEnabledFields(view, references, true);
            changedEnabledAwesomeDynamicListComponents(view, Lists.newArrayList(OrderFields.REASON_TYPES_CORRECTION_DATE_FROM),
                    false);
            changedEnabledAwesomeDynamicListComponents(view, Lists.newArrayList(OrderFields.REASON_TYPES_CORRECTION_DATE_TO),
                    false);
            changedEnabledAwesomeDynamicListComponents(view,
                    Lists.newArrayList(OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END), true);
            changedEnabledAwesomeDynamicListComponents(view,
                    Lists.newArrayList(OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START), true);
        }
    }

    private void changedEnabledFields(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

            fieldComponent.setEnabled(enabled);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private void changedEnabledAwesomeDynamicListComponents(final ViewDefinitionState view, final List<String> references,
                                                            final boolean enabled) {
        for (String reference : references) {
            AwesomeDynamicListComponent awesomeDynamicListComponent = (AwesomeDynamicListComponent) view
                    .getComponentByReference(reference);

            awesomeDynamicListComponent.setEnabled(enabled);
            awesomeDynamicListComponent.requestComponentUpdateState();

            for (FormComponent formComponent : awesomeDynamicListComponent.getFormComponents()) {
                FieldComponent fieldComponent = formComponent.findFieldComponentByName(L_REASON_TYPE_OF_CHANGING_ORDER_STATE);

                fieldComponent.setEnabled(enabled);
                fieldComponent.requestComponentUpdateState();
            }
        }
    }

    // FIXME replace this beforeRender hook with <criteriaModifier /> parameter in view XML.
    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                OrderStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void disabledRibbonWhenOrderIsSynchronized(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        List<RibbonGroup> ribbonGroups = ribbon.getGroups();

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (orderStateService.isSynchronized(order)) {
            return;
        }

        for (RibbonGroup ribbonGroup : ribbonGroups) {
            for (RibbonActionItem ribbonActionItem : ribbonGroup.getItems()) {
                ribbonActionItem.setEnabled(false);
                ribbonActionItem.requestUpdate(true);
            }
        }

        RibbonActionItem refreshRibbonActionItem = ribbon.getGroupByName(L_ORDER_DETAILS_ACTIONS).getItemByName(L_REFRESH);
        RibbonActionItem backRibbonActionItem = ribbon.getGroupByName(L_NAVIGATION).getItemByName(L_BACK);

        refreshRibbonActionItem.setEnabled(true);
        backRibbonActionItem.setEnabled(true);
        refreshRibbonActionItem.requestUpdate(true);
        backRibbonActionItem.requestUpdate(true);

        orderForm.setFormEnabled(false);
    }

    public void compareDeadlineAndEndDate(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(orderForm.getEntityId())) {
            return;
        }

        FieldComponent finishDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        FieldComponent deadlineDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);

        Date finishDate = DateUtils.parseDate(finishDateComponent.getFieldValue());
        Date deadlineDate = DateUtils.parseDate(deadlineDateComponent.getFieldValue());

        if (Objects.nonNull(finishDate) && Objects.nonNull(deadlineDate) && deadlineDate.before(finishDate)) {
            orderForm.addMessage("orders.validate.global.error.deadline", MessageType.INFO, false);
        }
    }

    public void compareDeadlineAndStartDate(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(orderForm.getEntityId())) {
            return;
        }

        FieldComponent startDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DATE_FROM);
        FieldComponent finishDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        FieldComponent deadlineDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);

        Date startDate = DateUtils.parseDate(startDateComponent.getFieldValue());
        Date finishDate = DateUtils.parseDate(finishDateComponent.getFieldValue());
        Date deadlineDate = DateUtils.parseDate(deadlineDateComponent.getFieldValue());

        if (Objects.nonNull(startDate) && Objects.nonNull(deadlineDate) && Objects.isNull(finishDate)
                && deadlineDate.before(startDate)) {
            orderForm.addMessage("orders.validate.global.error.deadlineBeforeStartDate", MessageType.INFO, false);
        }
    }

    private void changeFieldsEnabledForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return;
        }

        final Entity order = orderService.getOrder(orderId);

        String state = order.getStringField(OrderFields.STATE);

        if (OrderState.PENDING.getStringValue().equals(state) || OrderState.ACCEPTED.getStringValue().equals(state)
                || OrderState.IN_PROGRESS.getStringValue().equals(state)
                || OrderState.INTERRUPTED.getStringValue().equals(state)) {
            FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OrderFields.DESCRIPTION);

            descriptionField.setEnabled(true);
            descriptionField.requestComponentUpdateState();
        }

        if (OrderState.PENDING.getStringValue().equals(state) || OrderState.ACCEPTED.getStringValue().equals(state)
                || OrderState.IN_PROGRESS.getStringValue().equals(state)) {
            FieldComponent orderCategoryField = (FieldComponent) view.getComponentByReference(OrderFields.ORDER_CATEGORY);

            orderCategoryField.setEnabled(true);
            orderCategoryField.requestComponentUpdateState();
        }
    }

    private void changePredefinedTechnologyFieldsVisibility(final ViewDefinitionState view) {
        for (String reference : OrderDetailsHooks.L_PREDEFINED_TECHNOLOGY_FIELDS) {
            ComponentState componentState = view.getComponentByReference(reference);

            componentState.setVisible(true);
        }
    }

    private void setQuantities(final ViewDefinitionState view) {
        setProductQuantities(view);
        setDoneQuantity(view);
    }

    private void setProductQuantities(final ViewDefinitionState view) {
        if (!isValidDecimalField(view, Lists.newArrayList(OrderFields.DONE_QUANTITY))) {
            return;
        }

        final FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(orderForm.getEntityId())) {
            return;
        }

        Entity order = orderForm.getEntity();

        FieldComponent amountOfProductProducedField = (FieldComponent) view
                .getComponentByReference(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED);

        amountOfProductProducedField
                .setFieldValue(numberService.formatWithMinimumFractionDigits(order.getField(OrderFields.DONE_QUANTITY), 0));
        amountOfProductProducedField.requestComponentUpdateState();

        Entity product = order.getBelongsToField(BasicConstants.MODEL_PRODUCT);

        if (!isValidDecimalField(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT))
                || !isValidDecimalField(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY))) {
            return;
        }
        if (Objects.nonNull(product) && (Objects.isNull(order.getDecimalField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT))
                || !isValidQuantityForAdditionalUnit(order, product))) {
            additionalUnitService.setQuantityFieldForAdditionalUnit(view, order);
        }
    }

    private void setDoneQuantity(final ViewDefinitionState view) {
        if (!isValidDecimalField(view,
                Lists.newArrayList(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED, OrderFields.PLANNED_QUANTITY))) {
            return;
        }

        final FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(orderForm.getEntityId())) {
            return;
        }

        Entity order = orderForm.getEntity();

        FieldComponent doneInPercentage = (FieldComponent) view.getComponentByReference(L_DONE_IN_PERCENTAGE);
        FieldComponent doneInPercentageUnit = (FieldComponent) view.getComponentByReference(L_DONE_IN_PERCENTAGE_UNIT);
        FieldComponent doneQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.DONE_QUANTITY);
        FieldComponent remainingAmountOfProductToProduceField = (FieldComponent) view
                .getComponentByReference(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE);

        doneQuantityField.setFieldValue(numberService.format(order.getField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)));
        doneQuantityField.requestComponentUpdateState();

        BigDecimal remainingAmountOfProductToProduce = BigDecimalUtils
                .convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))
                .subtract(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)),
                        numberService.getMathContext());

        if (BigDecimal.ZERO.compareTo(remainingAmountOfProductToProduce) > 0) {
            remainingAmountOfProductToProduce = BigDecimal.ZERO;
        }

        remainingAmountOfProductToProduceField
                .setFieldValue(numberService.formatWithMinimumFractionDigits(remainingAmountOfProductToProduce, 0));
        remainingAmountOfProductToProduceField.requestComponentUpdateState();

        BigDecimal doneInPercentageQuantity = BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.DONE_QUANTITY))
                .multiply(new BigDecimal(100));

        if (BigDecimal.ZERO
                .compareTo(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))) == 0) {
            doneInPercentageQuantity = BigDecimal.ZERO;
        } else {
            doneInPercentageQuantity = doneInPercentageQuantity.divide(order.getDecimalField(OrderFields.PLANNED_QUANTITY),
                    MathContext.DECIMAL64);
        }

        doneInPercentage.setFieldValue(
                numberService.formatWithMinimumFractionDigits(doneInPercentageQuantity.setScale(0, RoundingMode.CEILING), 0));
        doneInPercentage.setEnabled(false);
        doneInPercentageUnit.setFieldValue("%");
    }

    private boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fields) {
        boolean isValid = true;

        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity entity = orderForm.getEntity();

        for (String field : fields) {
            try {
                entity.getDecimalField(field);
            } catch (IllegalArgumentException e) {
                FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(field);
                fieldComponent.addMessage("qcadooView.validate.field.error.invalidNumericFormat", MessageType.FAILURE);

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isValidQuantityForAdditionalUnit(final Entity order, final Entity product) {
        BigDecimal expectedVariable = additionalUnitService.getQuantityAfterConversion(order,
                additionalUnitService.getAdditionalUnit(product), order.getDecimalField(OrderFields.PLANNED_QUANTITY),
                product.getStringField(ProductFields.UNIT));
        BigDecimal currentVariable = order.getDecimalField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT);

        return expectedVariable.compareTo(currentVariable) == 0;
    }

    public void fillOrderDescription(final ViewDefinitionState view) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OrderFields.DESCRIPTION);
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent masterOrderLookup = (LookupComponent) view.getComponentByReference("masterOrder");
        FieldComponent oldTechnologyField = (FieldComponent) view.getComponentByReference("oldTechnologyId");

        Entity masterOrder = masterOrderLookup.getEntity();
        Entity technology = technologyLookup.getEntity();
        Entity product = productLookup.getEntity();
        Entity order = orderForm.getEntity();
        Long orderId = orderForm.getEntityId();
        Entity oldTechnology = null;
        Entity oldProduct = null;

        if (!((String) oldTechnologyField.getFieldValue()).isEmpty()) {
            Long oldTechnologyId = Long.parseLong((String) oldTechnologyField.getFieldValue());

            oldTechnology = getTechnologyDD().get(oldTechnologyId);
        }

        if (orderId != null) {
            oldProduct = getOrderDD().get(orderId).getBelongsToField(OrderFields.PRODUCT);
        }

        boolean technologyChanged = isTechnologyChanged(oldTechnologyField, technology, oldTechnology);
        boolean productChanged = isProductChanged(product, oldProduct);

        Entity parameter = parameterService.getParameter();

        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        boolean fillOrderDescriptionBasedOnProductDescription = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_PRODUCT_DESCRIPTION);

        String orderDescription = orderService.buildOrderDescription(masterOrder, technology, product,
                fillOrderDescriptionBasedOnTechnology, fillOrderDescriptionBasedOnProductDescription);

        String currentDescription = order.getStringField(OrderFields.DESCRIPTION);

        descriptionField.setFieldValue("");
        descriptionField.requestComponentUpdateState();

        if (technologyChanged || productChanged) {
            if (fillOrderDescriptionBasedOnTechnology || fillOrderDescriptionBasedOnProductDescription) {
                descriptionField.setFieldValue(orderDescription);
            } else {
                descriptionField.setFieldValue(currentDescription);
            }
        } else {
            if (Objects.nonNull(currentDescription) && !currentDescription.isEmpty()) {
                descriptionField.setFieldValue(currentDescription);
            } else {
                descriptionField.setFieldValue(orderDescription);
            }
        }

        descriptionField.requestComponentUpdateState();
    }

    private boolean isTechnologyChanged(FieldComponent oldTechnologyField, Entity technology, Entity oldTechnology) {
        boolean technologyChanged = false;
        if (Objects.nonNull(technology)) {
            oldTechnologyField.setFieldValue(technology.getId());
            oldTechnologyField.requestComponentUpdateState();

            if (Objects.nonNull(oldTechnology) && !oldTechnology.getId().equals(technology.getId())) {
                technologyChanged = true;
            }
        } else if (Objects.nonNull(oldTechnology)) {
            technologyChanged = true;
        }
        return technologyChanged;
    }

    private boolean isProductChanged(Entity product, Entity oldProduct) {
        boolean productChanged = false;
        if (Objects.nonNull(product)) {
            if (Objects.nonNull(oldProduct) && !oldProduct.getId().equals(product.getId())) {
                productChanged = true;
            }
        } else if (Objects.nonNull(oldProduct)) {
            productChanged = true;
        }
        return productChanged;
    }


    private void setOrderIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent orderIdForMultiUpload = (FieldComponent) view
                .getComponentByReference("orderIdForMultiUpload");
        FieldComponent qualityControlMultiUploadLocale = (FieldComponent) view
                .getComponentByReference("orderMultiUploadLocale");

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            orderIdForMultiUpload.setFieldValue("");
        } else {
            orderIdForMultiUpload.setFieldValue(orderId);
        }

        orderIdForMultiUpload.requestComponentUpdateState();
        qualityControlMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        qualityControlMultiUploadLocale.requestComponentUpdateState();
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

}
