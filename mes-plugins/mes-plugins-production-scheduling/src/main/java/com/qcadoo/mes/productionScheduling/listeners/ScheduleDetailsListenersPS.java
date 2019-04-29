package com.qcadoo.mes.productionScheduling.listeners;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionScheduling.constants.OperCompTimeCalculation;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.productionScheduling.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ScheduleDetailsListenersPS {

    private static final String L_ORDERS = "orders";

    private static final String L_POSITIONS = "positions";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyService technologyService;

    @Transactional
    public void getOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent positionsGrid = (GridComponent) view.getComponentByReference(L_POSITIONS);
        DataDefinition schedulePositionDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_SCHEDULE_POSITION);
        if (!positionsGrid.getEntities().isEmpty()) {
            schedulePositionDD.delete(positionsGrid.getEntities().stream().map(Entity::getId).collect(Collectors.toList())
                    .toArray(new Long[positionsGrid.getEntities().size()]));
        }

        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(L_ORDERS);
        for (Entity order : ordersGrid.getEntities()) {
            OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = generateRealizationTime(
                    order, order.getBelongsToField(OrderFields.PRODUCTION_LINE).getId());
        }

        DataDefinition orderDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
        for (Entity order : ordersGrid.getEntities()) {
            Entity dbOrder = orderDD.get(order.getId());
            List<Entity> orderTimeCalculations = dbOrder.getHasManyField(OrderFieldsPS.ORDER_TIME_CALCULATIONS);
            if (!orderTimeCalculations.isEmpty()) {
                List<Entity> operCompTimeCalculations = orderTimeCalculations.get(0)
                        .getHasManyField(OrderTimeCalculationFields.OPER_COMP_TIME_CALCULATIONS);
                for (Entity operCompTimeCalculation : operCompTimeCalculations) {
                    createSchedulePosition((FormComponent) state, schedulePositionDD, order, operCompTimeCalculation);
                }
            }
        }
        view.addMessage("productionScheduling.info.schedulePositionsGenerated", ComponentState.MessageType.SUCCESS);
    }

    private void createSchedulePosition(FormComponent state, DataDefinition schedulePositionDD, Entity order,
            Entity operCompTimeCalculation) {
        Entity technologyOperationComponent = operCompTimeCalculation
                .getBelongsToField(OperCompTimeCalculation.TECHNOLOGY_OPERATION_COMPONENT);
        Entity schedulePosition = schedulePositionDD.create();
        schedulePosition.setField(OrdersConstants.MODEL_SCHEDULE, state.getEntity());
        schedulePosition.setField(OrdersConstants.MODEL_ORDER, order);
        schedulePosition.setField(OperCompTimeCalculation.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
        schedulePosition.setField(OperationProductOutComponentFields.PRODUCT,
                technologyService.getMainOutputProductComponent(technologyOperationComponent)
                        .getBelongsToField(OperationProductOutComponentFields.PRODUCT));
        schedulePosition.setField(OperationProductOutComponentFields.QUANTITY, 1);
        schedulePosition.setField(OperCompTimeCalculation.OPERATION_OFF_SET,
                operCompTimeCalculation.getIntegerField(OperCompTimeCalculation.OPERATION_OFF_SET));
        schedulePosition.setField(OperCompTimeCalculation.LABOR_WORK_TIME,
                operCompTimeCalculation.getIntegerField(OperCompTimeCalculation.LABOR_WORK_TIME));
        schedulePosition.setField(OperCompTimeCalculation.MACHINE_WORK_TIME,
                operCompTimeCalculation.getIntegerField(OperCompTimeCalculation.MACHINE_WORK_TIME));
        schedulePositionDD.save(schedulePosition);
    }

    @Transactional
    public OperationProductComponentWithQuantityContainer generateRealizationTime(final Entity order,
            final Long productionLineId) {
        Entity productionLine = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE)
                .get(productionLineId);

        BigDecimal quantity = orderRealizationTimeService
                .getBigDecimalFromField(order.getDecimalField(OrderFields.PLANNED_QUANTITY), LocaleContextHolder.getLocale());
        boolean includeTpz = parameterService.getParameter().getBooleanField("includeTpzPS");
        boolean includeAdditionalTime = parameterService.getParameter().getBooleanField("includeAdditionalTimePS");

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = productQuantitiesService
                .getProductComponentQuantities(technology, quantity, operationRuns);

        operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns, includeTpz, includeAdditionalTime,
                productionLine, true);

        orderRealizationTimeService.estimateMaxOperationTimeConsumptionForWorkstation(order,
                technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), quantity, includeTpz,
                includeAdditionalTime, productionLine);
        return operationProductComponentWithQuantityContainer;
    }
}
