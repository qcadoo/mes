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
package com.qcadoo.mes.productionScheduling.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OperationalTaskType;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.mes.productionScheduling.ProductionSchedulingService;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OperationDurationDetailsInOrderListeners {

    private static final String L_START_TIME = "startTime";

    private static final String L_STOP_TIME = "stopTime";

    private static final String L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED = "productionScheduling.error.fieldRequired";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ProductionSchedulingService productionSchedulingService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private TechnologyService technologyService;

    @Transactional
    public void generateRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
                                        final String[] args) {
        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent startTimeField = (FieldComponent) viewDefinitionState.getComponentByReference(L_START_TIME);
        LookupComponent prodLine = (LookupComponent) viewDefinitionState.getComponentByReference(OrderFields.PRODUCTION_LINE);
        if (!StringUtils.hasText((String) startTimeField.getFieldValue())) {
            startTimeField.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }
        if (prodLine.isEmpty()) {
            prodLine.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);
            return;
        }

        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PRODUCTION_LINE);
        FieldComponent generatedEndDateField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent includeTpzField = (FieldComponent) viewDefinitionState.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
        FieldComponent includeAdditionalTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);

        boolean isGenerated = false;

        Entity productionLine = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE)
                .get((Long) productionLineLookup.getFieldValue());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .get(orderForm.getEntity().getId());

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Validate.notNull(technology, "technology is null");

        // Included in work time
        boolean includeTpz = "1".equals(includeTpzField.getFieldValue());
        boolean includeAdditionalTime = "1".equals(includeAdditionalTimeField.getFieldValue());

        operationWorkTimeService.deleteOperCompTimeCalculations(order);

        OperationWorkTime workTime = orderRealizationTimeService.estimateTotalWorkTimeForOrder(order, includeTpz,
                includeAdditionalTime, true);

        fillWorkTimeFields(viewDefinitionState, workTime);

        order = getActualOrderWithChanges(order);

        int maxPathTime = orderRealizationTimeService.estimateOperationTimeConsumption(null, order,
                technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), includeTpz,
                includeAdditionalTime, true, productionLine, Optional.empty());

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);

            generatedEndDateField.setFieldValue(null);
        } else {
            order.setField(OrderFieldsPS.REALIZATION_TIME, maxPathTime);

            Date startTime = order.getDateField(OrderFields.DATE_FROM);

            if (startTime == null) {
                startTimeField.addMessage("orders.validate.global.error.dateFromIsNull", MessageType.FAILURE);
            } else {
                if (maxPathTime == 0) {
                    orderForm.addMessage("productionScheduling.timenorms.isZero", MessageType.FAILURE, false);

                    generatedEndDateField.setFieldValue(null);
                } else {
                    productionSchedulingService.scheduleOrder(order);

                    isGenerated = true;
                }

                orderForm.addMessage("orders.dateFrom.info.dateFromSetToFirstPossible", MessageType.INFO, false);
            }
        }

        generatedEndDateField.requestComponentUpdateState();

        if (isGenerated) {
            order = getActualOrderWithChanges(order);
            Entity orderTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find().add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order)).setMaxResults(1).uniqueResult();
            order.setField(OrderFields.START_DATE, operationWorkTimeService
                    .setDateToField(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM)));
            order.setField(OrderFieldsPS.GENERATED_END_DATE, operationWorkTimeService
                    .setDateToField(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO)));
            order = order.getDataDefinition().save(order);
            orderForm.setEntity(order);
            orderForm.addMessage("productionScheduling.info.calculationGenerated", MessageType.SUCCESS);
        }
    }

    private void fillWorkTimeFields(final ViewDefinitionState view, final OperationWorkTime workTime) {
        FieldComponent laborWorkTimeField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.LABOR_WORK_TIME);
        FieldComponent machineWorkTimeField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.MACHINE_WORK_TIME);

        laborWorkTimeField.setFieldValue(workTime.getLaborWorkTime());
        machineWorkTimeField.setFieldValue(workTime.getMachineWorkTime());

        laborWorkTimeField.requestComponentUpdateState();
        machineWorkTimeField.requestComponentUpdateState();
    }

    private Entity getActualOrderWithChanges(final Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(order.getId());
    }

    public void copyRealizationTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent generatedEndDateField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent stopTimeField = (FieldComponent) view.getComponentByReference(L_STOP_TIME);

        stopTimeField.setFieldValue(generatedEndDateField.getFieldValue());

        state.performEvent(view, "save");
    }

    public void createOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return;
        }

        Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology != null) {
            List<Entity> technologyOperationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

            for (Entity technologyOperationComponent : technologyOperationComponents) {
                createOperationalTasks(order, technologyOperationComponent);
            }

            orderForm.addMessage("productionScheduling.operationDurationDetailsInOrder.info.operationalTasksCreated",
                    MessageType.SUCCESS);
        }
    }

    private void createOperationalTasks(final Entity order, final Entity technologyOperationComponent) {
        Entity techOperCompTimeCalculation = operationWorkTimeService.createOrGetOperCompTimeCalculation(order,
                technologyOperationComponent);

        DataDefinition operationTaskDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_OPERATIONAL_TASK);

        Entity operationalTask = operationTaskDD.create();

        if (techOperCompTimeCalculation != null) {
            operationalTask.setField(OperationalTaskFields.START_DATE,
                    techOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM));
            operationalTask.setField(OperationalTaskFields.FINISH_DATE,
                    techOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO));
        }

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

        operationalTask.getDataDefinition().save(operationalTask);
    }
}
