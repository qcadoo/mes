package com.qcadoo.mes.productionScheduling.listeners;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OperationDurationDetailsInOrderListeners {

    private static final String L_FORM = "form";

    private static final String L_START_TIME = "startTime";

    private static final String L_STOP_TIME = "stopTime";

    private static final String L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED = "productionScheduling.error.fieldRequired";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    public void showCopyOfTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(order.getField(OrderFields.ORDER_TYPE))
                    && (order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE) == null)) {
                state.addMessage("order.technology.patternTechnology.not.set", MessageType.INFO);

                return;
            }

            Long technologyId = order.getBelongsToField(OrderFields.TECHNOLOGY).getId();
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", technologyId);

            String url = "../page/orders/copyOfTechnologyDetails.html";
            view.redirectTo(url, false, true, parameters);
        }
    }

    @Transactional
    public void generateRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        FieldComponent startTimeField = (FieldComponent) viewDefinitionState.getComponentByReference(L_START_TIME);

        if (!StringUtils.hasText((String) startTimeField.getFieldValue())) {
            startTimeField.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }

        FieldComponent plannedQuantityField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PLANNED_QUANTITY);
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PRODUCTION_LINE);
        FieldComponent generatedEndDateField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent includeTpzField = (FieldComponent) viewDefinitionState.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
        FieldComponent includeAdditionalTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);

        boolean isGenerated = false;

        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderForm.getEntity().getId());

        // copy of technology from order
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Validate.notNull(technology, "technology is null");
        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantityField.getFieldValue(),
                viewDefinitionState.getLocale());

        // Included in work time
        Boolean includeTpz = "1".equals(includeTpzField.getFieldValue());
        Boolean includeAdditionalTime = "1".equals(includeAdditionalTimeField.getFieldValue());

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, quantity, operationRuns);

        OperationWorkTime workTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns, includeTpz,
                includeAdditionalTime, productionLine, true);

        fillWorkTimeFields(viewDefinitionState, workTime);

        order = getActualOrderWithChanges(order);

        int maxPathTime = orderRealizationTimeService.estimateMaxOperationTimeConsumptionForWorkstation(
                technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), quantity, includeTpz,
                includeAdditionalTime, productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);

            generatedEndDateField.setFieldValue(null);
        } else {
            order.setField(OrderFieldsPS.REALIZATION_TIME, maxPathTime);

            Date startTime = order.getDateField(OrderFields.DATE_FROM);

            if (startTime == null) {
                startTimeField.addMessage("orders.validate.global.error.dateFromIsNull", MessageType.FAILURE);
            } else {
                Date stopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

                if (stopTime == null) {
                    orderForm.addMessage("productionScheduling.timenorms.isZero", MessageType.FAILURE, false);

                    generatedEndDateField.setFieldValue(null);
                } else {
                    generatedEndDateField.setFieldValue(orderRealizationTimeService.setDateToField(stopTime));

                    order.setField(OrderFieldsPS.GENERATED_END_DATE, orderRealizationTimeService.setDateToField(stopTime));

                    scheduleOrder(order.getId());

                    isGenerated = true;
                }

                orderForm.addMessage("orders.dateFrom.info.dateFromSetToFirstPossible", MessageType.INFO, false);
            }

            order.getDataDefinition().save(order);
        }

        generatedEndDateField.requestComponentUpdateState();

        if (isGenerated) {
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

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return;
        }

        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();

        Date orderStartDate = order.getDateField(OrderFields.START_DATE);
        for (Entity operation : operations) {
            Entity techOperCompTimeCalculation = operation
                    .getBelongsToField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION);

            if (techOperCompTimeCalculation == null) {
                continue;
            }

            Integer offset = techOperCompTimeCalculation.getIntegerField(TechOperCompTimeCalculationsFields.OPERATION_OFF_SET);
            Integer duration = techOperCompTimeCalculation
                    .getIntegerField(TechOperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME);

            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, null);
            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, null);

            if (offset == null || duration == null) {
                continue;
            }

            if (duration.equals(0)) {
                duration = duration + 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);
            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);
            if (dateTo == null) {
                continue;
            }

            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, dateFrom);
            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, dateTo);

            techOperCompTimeCalculation.getDataDefinition().save(techOperCompTimeCalculation);
        }
    }

    public void copyRealizationTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent generatedEndDateField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent stopTimeField = (FieldComponent) view.getComponentByReference(L_STOP_TIME);

        stopTimeField.setFieldValue(generatedEndDateField.getFieldValue());

        state.performEvent(view, "save", new String[0]);
    }

}
