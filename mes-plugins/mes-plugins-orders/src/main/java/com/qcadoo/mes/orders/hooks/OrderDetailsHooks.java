/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.orders.hooks;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.OrderStateService;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_GRID = "grid";

    private static final String L_WINDOW = "window";

    private static final String L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START = "commentReasonTypeDeviationsOfEffectiveStart";

    private static final String L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_END = "commentReasonTypeDeviationsOfEffectiveEnd";

    private static final List<String> L_PREDEFINED_TECHNOLOGY_FIELDS = Lists.newArrayList("defaultTechnology",
            "technologyPrototype", "predefinedTechnology");

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

    public final void onBeforeRender(final ViewDefinitionState view) {
        fillProductionLine(view);
        generateOrderNumber(view);
        fillDefaultTechnology(view);
        disableFieldOrderForm(view);
        disableTechnologiesIfProductDoesNotAny(view);
        setAndDisableState(view);
        unitService.fillProductUnitBeforeRender(view);
        disableOrderFormForExternalItems(view);
        changedEnabledFieldForSpecificOrderState(view);
        filterStateChangeHistory(view);
        disabledRibbonWhenOrderIsSynchronized(view);
        compareDeadlineAndEndDate(view);
        compareDeadlineAndStartDate(view);
        orderProductQuantityHooks.changeFieldsEnabledForSpecificOrderState(view);
        orderProductQuantityHooks.fillProductUnit(view);
        changeFieldsEnabledForSpecificOrderState(view);
        setFieldsVisibility(view);
        checkIfLockTechnologyTree(view);
        setQuantities(view);

    }

    public void fillRecipeFilterValue(final ViewDefinitionState view) {

    }

    public final void fillProductionLine(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);

        Entity defaultProductionLine = orderService.getDefaultProductionLine();

        if ((orderForm.getEntityId() == null) && (productionLineLookup.getFieldValue() == null)
                && (defaultProductionLine != null)) {
            productionLineLookup.setFieldValue(defaultProductionLine.getId());
            productionLineLookup.requestComponentUpdateState();
        }
    }

    public void generateOrderNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                L_FORM, OrderFields.NUMBER);
    }

    public void fillDefaultTechnology(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);

        Entity product = productLookup.getEntity();

        if (product != null) {
            Entity defaultTechnology = technologyServiceO.getDefaultTechnology(product);

            if (defaultTechnology != null) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnology, "#number + ' - ' + #name",
                        view.getLocale());

                defaultTechnologyField.setFieldValue(defaultTechnologyValue);
            }
        }
    }

    public void disableFieldOrderForm(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        boolean disabled = false;

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = orderService.getOrder(orderId);

            if (order == null) {
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
        if (company == null) {
            addressLookup.setFieldValue(null);
            addressLookup.setEnabled(false);
        } else {
            addressLookup.setEnabled(true);
        }
    }

    public void disableTechnologiesIfProductDoesNotAny(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);
        FieldComponent plannedQuantity = (FieldComponent) view.getComponentByReference("plannedQuantity");

        defaultTechnologyField.setEnabled(false);

        if (productLookup.getFieldValue() == null || !hasAnyTechnologies(productLookup.getEntity())) {
            technologyLookup.setRequired(false);
            plannedQuantity.setRequired(false);
        } else {
            technologyLookup.setRequired(true);
            plannedQuantity.setRequired(true);
        }
    }

    private boolean hasAnyTechnologies(final Entity product) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        SearchCriteriaBuilder searchCriteria = technologyDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product)).setMaxResults(1);

        SearchResult searchResult = searchCriteria.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public void setAndDisableState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(OrderFields.STATE);

        stateField.setEnabled(false);

        if (orderForm.getEntityId() != null) {
            return;
        }

        stateField.setFieldValue(OrderState.PENDING.getStringValue());
    }

    private void checkIfLockTechnologyTree(final ViewDefinitionState view) {
        if (parameterService.getParameter().getBooleanField(ParameterFieldsO.LOCK_TECHNOLOGY_TREE)) {
            FieldComponent orderType = (FieldComponent) view.getComponentByReference(OrderFields.ORDER_TYPE);
            orderType.setEnabled(false);
            orderType.requestComponentUpdateState();
        }

    }

    public void changedEnabledFieldForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return;
        }

        final Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .get(orderId);

        if (order == null) {
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
                    OrderFields.DATE_FROM, OrderFields.DATE_TO);
            changedEnabledFields(view, references, true);
        }
        if (OrderState.IN_PROGRESS.getStringValue().equals(orderState)
                || OrderState.INTERRUPTED.getStringValue().equals(orderState)) {
            List<String> references = Lists.newArrayList(OrderFields.DATE_FROM, OrderFields.DATE_TO,
                    OrderFields.CORRECTED_DATE_TO, OrderFields.REASON_TYPES_CORRECTION_DATE_TO,
                    OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO, OrderFields.EFFECTIVE_DATE_FROM,
                    L_COMMENT_REASON_TYPE_DEVIATIONS_OF_EFFECTIVE_START);
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
                FieldComponent fieldComponent = (FieldComponent) formComponent
                        .findFieldComponentByName("reasonTypeOfChangingOrderState");

                fieldComponent.setEnabled(enabled);
                fieldComponent.requestComponentUpdateState();
            }
        }
    }

    public void disableOrderFormForExternalItems(final ViewDefinitionState state) {
        FormComponent orderForm = (FormComponent) state.getComponentByReference(OrdersConstants.FIELD_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (order == null) {
            return;
        }

        String externalNumber = order.getStringField(OrderFields.EXTERNAL_NUMBER);

        boolean externalSynchronized = order.getBooleanField(OrderFields.EXTERNAL_SYNCHRONIZED);

        if (StringUtils.hasText(externalNumber) || !externalSynchronized) {
            state.getComponentByReference(OrderFields.NUMBER).setEnabled(false);
            state.getComponentByReference(OrderFields.NAME).setEnabled(false);
            state.getComponentByReference(OrderFields.COMPANY).setEnabled(false);
            state.getComponentByReference(OrderFields.DEADLINE).setEnabled(false);
            state.getComponentByReference(OrderFields.PRODUCT).setEnabled(false);
            state.getComponentByReference(OrderFields.PLANNED_QUANTITY).setEnabled(false);
        }
    }

    // FIXME replace this beforeRender hook with <criteriaModifier /> parameter in view XML.
    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference(L_GRID);
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                OrderStateChangeFields.STATUS, Lists.newArrayList(StateChangeStatus.SUCCESSFUL.getStringValue()));

        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

    public void disabledRibbonWhenOrderIsSynchronized(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        List<RibbonGroup> ribbonGroups = ribbon.getGroups();

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
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

        RibbonActionItem refreshRibbonActionItem = ribbon.getGroupByName("actions").getItemByName("refresh");
        RibbonActionItem backRibbonActionItem = ribbon.getGroupByName("navigation").getItemByName("back");

        refreshRibbonActionItem.setEnabled(true);
        backRibbonActionItem.setEnabled(true);
        refreshRibbonActionItem.requestUpdate(true);
        backRibbonActionItem.requestUpdate(true);

        orderForm.setFormEnabled(false);
    }

    public void compareDeadlineAndEndDate(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (orderForm.getEntityId() == null) {
            return;
        }

        FieldComponent finishDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        FieldComponent deadlineDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);

        Date finishDate = DateUtils.parseDate(finishDateComponent.getFieldValue());
        Date deadlineDate = DateUtils.parseDate(deadlineDateComponent.getFieldValue());

        if (finishDate != null && deadlineDate != null && deadlineDate.before(finishDate)) {
            orderForm.addMessage("orders.validate.global.error.deadline", MessageType.INFO, false);
        }
    }

    public void compareDeadlineAndStartDate(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (orderForm.getEntityId() == null) {
            return;
        }

        FieldComponent startDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DATE_FROM);
        FieldComponent finishDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        FieldComponent deadlineDateComponent = (FieldComponent) view.getComponentByReference(OrderFields.DEADLINE);

        Date startDate = DateUtils.parseDate(startDateComponent.getFieldValue());
        Date finidhDate = DateUtils.parseDate(finishDateComponent.getFieldValue());
        Date deadlineDate = DateUtils.parseDate(deadlineDateComponent.getFieldValue());

        if (startDate != null && deadlineDate != null && finidhDate == null && deadlineDate.before(startDate)) {
            orderForm.addMessage("orders.validate.global.error.deadlineBeforeStartDate", MessageType.INFO, false);
        }
    }

    private void changeFieldsEnabledForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
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

    public void setFieldsVisibility(final ViewDefinitionState view) {
        FieldComponent orderType = (FieldComponent) view.getComponentByReference(OrderFields.ORDER_TYPE);

        boolean selectForPatternTechnology = OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType.getFieldValue());

        changeFieldsVisibility(view, L_PREDEFINED_TECHNOLOGY_FIELDS, selectForPatternTechnology);
    }

    public void setFieldsVisibilityAndFill(final ViewDefinitionState view) {
        FieldComponent orderType = (FieldComponent) view.getComponentByReference(OrderFields.ORDER_TYPE);

        boolean selectForPatternTechnology = OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType.getFieldValue());

        changeFieldsVisibility(view, L_PREDEFINED_TECHNOLOGY_FIELDS, selectForPatternTechnology);

        if (selectForPatternTechnology) {
            LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
            FieldComponent technology = (FieldComponent) view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
            FieldComponent defaultTechnology = (FieldComponent) view.getComponentByReference("defaultTechnology");

            Entity product = productLookup.getEntity();

            defaultTechnology.setFieldValue("");
            technology.setFieldValue(null);

            if (product != null) {
                Entity defaultTechnologyEntity = technologyServiceO.getDefaultTechnology(product);
                if (defaultTechnologyEntity != null) {
                    technology.setFieldValue(defaultTechnologyEntity.getId());
                }
            }

        }
    }

    private void changeFieldsVisibility(final ViewDefinitionState view, final List<String> references,
            final boolean selectForPatternTechnology) {
        for (String reference : references) {
            ComponentState componentState = view.getComponentByReference(reference);

            componentState.setVisible(selectForPatternTechnology);
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

        final FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (orderForm.getEntityId() == null) {
            return;
        }

        Entity order = orderForm.getEntity();

        FieldComponent amountOfProductProducedField = (FieldComponent) view
                .getComponentByReference(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED);
        FieldComponent remainingAmountOfProductToProduceField = (FieldComponent) view
                .getComponentByReference(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE);

        amountOfProductProducedField.setFieldValue(numberService.format(order.getField(OrderFields.DONE_QUANTITY)));
        amountOfProductProducedField.requestComponentUpdateState();

        BigDecimal remainingAmountOfProductToProduce = BigDecimalUtils
                .convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))
                .subtract(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.DONE_QUANTITY)),
                        numberService.getMathContext());

        if (remainingAmountOfProductToProduce.compareTo(BigDecimal.ZERO) == -1) {
            remainingAmountOfProductToProduceField.setFieldValue(numberService.format(BigDecimal.ZERO));
        } else {
            remainingAmountOfProductToProduceField.setFieldValue(numberService.format(remainingAmountOfProductToProduce));
        }

        remainingAmountOfProductToProduceField.requestComponentUpdateState();
    }

    private void setDoneQuantity(final ViewDefinitionState view) {
        if (!isValidDecimalField(view, Lists.newArrayList(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED))) {
            return;
        }

        final FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (orderForm.getEntityId() == null) {
            return;
        }

        Entity order = orderForm.getEntity();

        FieldComponent doneQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.DONE_QUANTITY);
        FieldComponent remaingingAmoutOfProductToProduceField = (FieldComponent) view
                .getComponentByReference(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE);

        doneQuantityField.setFieldValue(numberService.format(order.getField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)));
        doneQuantityField.requestComponentUpdateState();

        BigDecimal remainingAmountOfProductToProduce = BigDecimalUtils
                .convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))
                .subtract(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)),
                        numberService.getMathContext());

        if (remainingAmountOfProductToProduce.compareTo(BigDecimal.ZERO) == -1) {
            remaingingAmoutOfProductToProduceField.setFieldValue(numberService.format(BigDecimal.ZERO));
        } else {
            remaingingAmoutOfProductToProduceField.setFieldValue(numberService.format(remainingAmountOfProductToProduce));
        }

        remaingingAmoutOfProductToProduceField.requestComponentUpdateState();
    }

    private boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fileds) {
        boolean isValid = true;

        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity entity = orderForm.getEntity();

        for (String field : fileds) {
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

}
