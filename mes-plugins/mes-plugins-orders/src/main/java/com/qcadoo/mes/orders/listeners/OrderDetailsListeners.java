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
package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.mes.orders.hooks.OrderDetailsHooks;
import com.qcadoo.mes.orders.states.client.OrderStateChangeViewClient;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeViewClient;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderDetailsListeners {

    private static final String L_PLANNED_DATE_FROM = "plannedDateFrom";

    private static final String L_PLANNED_DATE_TO = "plannedDateTo";

    private static final String L_EFFECTIVE_DATE_FROM = "effectiveDateFrom";

    private static final String L_EFFECTIVE_DATE_TO = "effectiveDateTo";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final String L_COMPLETE_STATION_AND_EMPLOYEE_IN_GENERATED_TASKS = "completeStationAndEmployeeInGeneratedTasks";

    @Autowired
    private OrderStateChangeViewClient orderStateChangeViewClient;

    @Autowired
    private TechnologyStateChangeViewClient technologyStateChangeViewClient;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailsHooks orderDetailsHooks;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TechnologyService technologyService;

    public void splitOrdersParts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();
        Set<Long> selectedEntitiesIds = Sets.newHashSet(orderId);

        Entity helper = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_HELPER).create();
        helper = helper.getDataDefinition().save(helper);
        for (Long id : selectedEntitiesIds) {
            Entity parent = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_PARENT).create();
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            parent.setField(SplitOrderParentConstants.SPLIT_ORDER_HELPER, helper);
            parent.setField(SplitOrderParentConstants.ORDER, order);
            parent.setField(SplitOrderParentConstants.NUMBER, order.getStringField(OrderFields.NUMBER));
            parent.setField(SplitOrderParentConstants.NAME, order.getStringField(OrderFields.NAME));
            parent.setField(SplitOrderParentConstants.DATE_FROM, order.getDateField(OrderFields.DATE_FROM));
            parent.setField(SplitOrderParentConstants.DATE_TO, order.getDateField(OrderFields.DATE_TO));
            parent.setField(SplitOrderParentConstants.PLANNED_QUANTITY, order.getDecimalField(OrderFields.PLANNED_QUANTITY));
            parent.setField(SplitOrderParentConstants.UNIT, order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT));
            parent = parent.getDataDefinition().save(parent);
            parent.getId();
        }


        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", helper.getId());

        String url = "../page/orders/divideOrdersDetails.html";
        view.openModal(url, parameters);
    }

    public void splitOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();
        Set<Long> selectedEntitiesIds = Sets.newHashSet(orderId);

        Entity helper = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_HELPER).create();
        helper = helper.getDataDefinition().save(helper);
        for (Long id : selectedEntitiesIds) {
            Entity parent = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_PARENT).create();
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            parent.setField(SplitOrderParentConstants.SPLIT_ORDER_HELPER, helper);
            parent.setField(SplitOrderParentConstants.ORDER, order);
            parent.setField(SplitOrderParentConstants.NUMBER, order.getStringField(OrderFields.NUMBER));
            parent.setField(SplitOrderParentConstants.NAME, order.getStringField(OrderFields.NAME));
            parent.setField(SplitOrderParentConstants.DATE_FROM, order.getDateField(OrderFields.DATE_FROM));
            parent.setField(SplitOrderParentConstants.DATE_TO, order.getDateField(OrderFields.DATE_TO));
            parent.setField(SplitOrderParentConstants.PLANNED_QUANTITY, order.getDecimalField(OrderFields.PLANNED_QUANTITY));
            parent.setField(SplitOrderParentConstants.UNIT, order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT));
            parent = parent.getDataDefinition().save(parent);
            parent.getId();
        }


        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", helper.getId());

        String url = "../page/orders/splitOrdersDetails.html";
        view.redirectTo(url, false, true, parameters);
    }


    public void clearAddress(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent addressLookup = (LookupComponent) view.getComponentByReference(OrderFields.ADDRESS);

        addressLookup.setFieldValue(null);
    }

    public void onTechnologyChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        setDefaultNameUsingTechnology(view, state, args);

        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        Entity technology = technologyLookup.getEntity();

        if (Objects.nonNull(technology)) {
            LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);
            LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(OrderFields.DIVISION);
            Entity productionLine = orderService.getProductionLine(technology);
            orderDetailsHooks.fillProductionLineForTechnology(view);
            orderDetailsHooks.fillDivision(divisionLookup, technology, productionLine);
        }
    }

    public void onProductionLineChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(OrderFields.DIVISION);

        Entity productionLine = productionLineLookup.getEntity();

        if (Objects.nonNull(productionLine)) {
            List<Entity> divisions = productionLine.getManyToManyField(ProductionLineFields.DIVISIONS);

            if (divisions.size() == 1) {
                divisionLookup.setFieldValue(divisions.get(0).getId());
                divisionLookup.requestComponentUpdateState();
            }
        }
    }

    public void setDefaultNameUsingTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(OrderFields.NAME);

        Entity product = productLookup.getEntity();
        Entity technology = technologyLookup.getEntity();

        if (Objects.isNull(product) || (Objects.nonNull(nameField.getFieldValue()) && !nameField.getFieldValue().equals(""))) {
            return;
        }

        Locale locale = state.getLocale();
        nameField.setFieldValue(orderService.makeDefaultName(product, technology, locale));
    }

    public void showTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (Objects.nonNull(orderId)) {
            Entity order = orderService.getOrder(orderId);

            Long technologyId = order.getBelongsToField(OrderFields.TECHNOLOGY).getId();

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", technologyId);

            String url = "/page/technologies/technologyDetails.html";
            view.redirectTo(url, false, true, parameters);
        }
    }

    public void showOrderParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (Objects.nonNull(orderId)) {
            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("form.id", orderId);

            String url = "/page/orders/orderAdditionalDetails.html";
            view.redirectTo(url, false, true, parameters);
        }
    }

    private void copyDate(final ViewDefinitionState view, final String fromNameField, final String toNameField) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent fromField = (FieldComponent) view.getComponentByReference(fromNameField);
        FieldComponent toField = (FieldComponent) view.getComponentByReference(toNameField);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            toField.setFieldValue(fromField.getFieldValue());

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (!fromField.getFieldValue().equals(order.getField(fromNameField))) {
            toField.setFieldValue(fromField.getFieldValue());
        }

        toField.requestComponentUpdateState();
    }

    public void copyStartDate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state.getName().equals(L_PLANNED_DATE_FROM)) {
            copyDate(view, L_PLANNED_DATE_FROM, OrderFields.DATE_FROM);
        } else if (state.getName().equals(L_EFFECTIVE_DATE_FROM)) {
            copyDate(view, OrderFields.EFFECTIVE_DATE_FROM, OrderFields.DATE_FROM);
        } else {
            copyDate(view, OrderFields.CORRECTED_DATE_FROM, OrderFields.DATE_FROM);
        }
    }

    public void copyEndDate(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state.getName().equals(L_PLANNED_DATE_TO)) {
            copyDate(view, L_PLANNED_DATE_TO, OrderFields.DATE_TO);
        } else if (state.getName().equals(L_EFFECTIVE_DATE_TO)) {
            copyDate(view, OrderFields.EFFECTIVE_DATE_TO, OrderFields.DATE_TO);
        } else {
            copyDate(view, OrderFields.CORRECTED_DATE_TO, OrderFields.DATE_TO);
        }
    }

    public void copyStartDateToDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            copyDate(view, OrderFields.DATE_FROM, L_PLANNED_DATE_FROM);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        String orderState = order.getStringField(OrderFields.STATE);

        if (OrderState.PENDING.getStringValue().equals(orderState)) {
            copyDate(view, OrderFields.DATE_FROM, L_PLANNED_DATE_FROM);
        }
        if (OrderState.IN_PROGRESS.getStringValue().equals(orderState)
                || OrderState.ABANDONED.getStringValue().equals(orderState)
                || OrderState.COMPLETED.getStringValue().equals(orderState)) {
            copyDate(view, OrderFields.DATE_FROM, L_EFFECTIVE_DATE_FROM);
        }
        if ((OrderState.ACCEPTED.getStringValue().equals(orderState))) {
            copyDate(view, OrderFields.DATE_FROM, OrderFields.CORRECTED_DATE_FROM);
        }
    }

    public void copyFinishDateToDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            copyDate(view, OrderFields.DATE_TO, L_PLANNED_DATE_TO);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        String orderState = order.getStringField(OrderFields.STATE);

        if (OrderState.PENDING.getStringValue().equals(orderState)) {
            copyDate(view, OrderFields.DATE_TO, L_PLANNED_DATE_TO);
        }
        if (OrderState.COMPLETED.getStringValue().equals(orderState) || OrderState.ABANDONED.getStringValue().equals(orderState)) {
            copyDate(view, OrderFields.DATE_TO, L_EFFECTIVE_DATE_TO);
        }
        if (OrderState.ACCEPTED.getStringValue().equals(orderState) || OrderState.IN_PROGRESS.getStringValue().equals(orderState)) {
            copyDate(view, OrderFields.DATE_TO, OrderFields.CORRECTED_DATE_TO);
        }
    }

    public void changeOrderProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(OrderFields.PRODUCT);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent defaultTechnologyField = (FieldComponent) view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY);

        Entity product = productLookup.getEntity();

        defaultTechnologyField.setFieldValue(null);
        technologyLookup.setFieldValue(null);

        if (Objects.nonNull(product)) {
            FilterValueHolder holder = technologyLookup.getFilterValue();

            holder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());

            technologyLookup.setFilterValue(holder);

            Entity defaultTechnologyEntity = technologyServiceO.getDefaultTechnology(product);

            if (Objects.nonNull(defaultTechnologyEntity)) {
                String defaultTechnologyValue = expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name",
                        view.getLocale());

                defaultTechnologyField.setFieldValue(defaultTechnologyValue);
                technologyLookup.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    public void printOrderReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        view.redirectTo("/orders/ordersOrderReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", args);

        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity order = formComponent.getPersistedEntityWithIncludedFormValues();
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            if (TechnologyStateStringValues.DRAFT.equals(technology.getStringField(TechnologyFields.STATE))) {
                technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state),
                        TechnologyStateStringValues.ACCEPTED, technology);
            } else if (TechnologyStateStringValues.CHECKED.equals(technology.getStringField(TechnologyFields.STATE))) {
                technologyServiceO.changeTechnologyStateToAccepted(technology);
            }
        }

        orderStateChangeViewClient.changeState(new ViewContextHolder(view, state), args[0]);
    }

    public void onQuantityForAdditionalUnitChange(final ViewDefinitionState view, final ComponentState componentState,
                                                  final String[] args) {
        if (!additionalUnitService.isValidDecimalFieldWithoutMsg(view,
                Lists.newArrayList(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT))
                || !additionalUnitService.isValidDecimalFieldWithoutMsg(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY))) {
            return;
        }

        additionalUnitService.setQuantityForUnit(view,
                ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity());
    }

    public void onQuantityChange(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        if (!additionalUnitService.isValidDecimalFieldWithoutMsg(view,
                Lists.newArrayList(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT))
                || !additionalUnitService.isValidDecimalFieldWithoutMsg(view, Lists.newArrayList(OrderFields.PLANNED_QUANTITY))) {
            return;
        }

        additionalUnitService.setQuantityFieldForAdditionalUnit(view,
                ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntity());
    }

    public void generateOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity order = orderForm.getEntity().getDataDefinition().get(orderForm.getEntityId());

        createOperationalTasksForOrder(order, false);

        orderForm.addMessage("orders.ordersDetails.info.operationalTasksCreated", MessageType.SUCCESS);
    }

    public void createOperationalTasksForOrder(final Entity order, boolean onAccept) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        List<Entity> technologyOperationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity technologyOperationComponent : technologyOperationComponents) {
            createOperationalTasks(order, technologyOperationComponent, onAccept);
        }
    }

    private void createOperationalTasks(final Entity order, final Entity technologyOperationComponent, boolean onAccept) {
        DataDefinition operationalTaskDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_OPERATIONAL_TASK);

        Entity operationalTask = operationalTaskDD.create();

        operationalTask.setField(OperationalTaskFields.START_DATE, order.getField(OrderFields.START_DATE));
        operationalTask.setField(OperationalTaskFields.FINISH_DATE, order.getField(OrderFields.FINISH_DATE));
        operationalTask.setField(OperationalTaskFields.TYPE, OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue());
        operationalTask.setField(OperationalTaskFields.ORDER, order);

        operationalTask.setField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);

        Entity mainOutputProductComponent = technologyService.getMainOutputProductComponent(technologyOperationComponent);
        Entity product = mainOutputProductComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);

        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))) {
            product = order.getBelongsToField(OrderFields.PRODUCT);
        }

        operationalTask.setField(OperationalTaskFields.PRODUCT, product);

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Optional<Entity> maybeDivision = technologyServiceO.extractDivision(technology, technologyOperationComponent);
        maybeDivision.ifPresent(d -> operationalTask.setField(OperationalTaskFields.DIVISION, d));
        if (onAccept && parameterService.getParameter().getBooleanField(L_COMPLETE_STATION_AND_EMPLOYEE_IN_GENERATED_TASKS)) {
            fillWorkstation(operationalTask);
        }

        operationalTaskDD.save(operationalTask);
    }

    private void fillWorkstation(Entity operationalTask) {
        if (Objects.nonNull(operationalTask.getId())) {
            return;
        }

        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> workstations = technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS);
        if (workstations.size() == 1) {
            Entity workstation = workstations.get(0);
            operationalTask.setField(OperationalTaskFields.WORKSTATION, workstation);
            if (Objects.nonNull(workstation.getBelongsToField(WorkstationFields.STAFF))
                    && technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF) == 1) {
                operationalTask.setField(OperationalTaskFields.STAFF, workstation.getBelongsToField(WorkstationFields.STAFF));
            }
        }

    }

    public void showOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Entity orderFromDB = orderForm.getEntity().getDataDefinition().get(orderId);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderNumber", applyInOperator(orderFromDB.getStringField(OrderFields.NUMBER)));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);
        parameters.put("window.showBack", true);
        parameters.put("window.fromOrderDetails", true);

        String url = "/page/orders/operationalTasksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        return "[" + value + "]";
    }

    public void showOrderPacks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("order.id", orderId);
        parameters.put("window.showBack", true);

        String url = "/page/orders/orderPacksSingleOrderList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void showOrderTechnologicalProcesses(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", orderId);
        parameters.put("window.showBack", true);

        String url = "/page/orders/orderTechnologicalProcessesSingleOrderList.html";
        view.redirectTo(url, false, true, parameters);
    }

}
