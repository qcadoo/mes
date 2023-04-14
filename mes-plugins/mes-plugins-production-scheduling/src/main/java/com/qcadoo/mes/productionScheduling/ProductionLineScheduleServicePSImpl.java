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
package com.qcadoo.mes.productionScheduling;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.ProductionLineSchedulePositionFieldsLCNFO;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.operationTimeCalculations.constants.PlanOrderTimeCalculationFields;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePS;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.mes.orders.listeners.ProductionLinePositionNewData;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Order(1)
@RunIfEnabled(ProductionSchedulingConstants.PLUGIN_IDENTIFIER)
public class ProductionLineScheduleServicePSImpl implements ProductionLineScheduleServicePS {

    @Autowired
    private ProductionSchedulingService productionSchedulingService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void createProductionLinePositionNewData(
            Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData,
            Entity productionLine, Date finishDate, Entity position, Entity technology, Entity previousOrder) {
        Entity productionLineSchedule = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE);
        Entity order = position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);

        operationWorkTimeService.deletePlanOperCompTimeCalculations(productionLineSchedule, order, productionLine);

        int maxPathTime = orderRealizationTimeService.estimateOperationTimeConsumption(productionLineSchedule, order,
                technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(),
                true, true, true, productionLine, Optional.empty());

        if (maxPathTime != 0 && maxPathTime <= OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            Entity changeover = lineChangeoverNormsForOrdersService.getChangeover(previousOrder, technology, productionLine);
            if (changeover != null) {
                finishDate = Date.from(finishDate.toInstant().plusSeconds(changeover.getIntegerField(LineChangeoverNormsFields.DURATION)));
            }

            Entity orderTimeCalculation = productionSchedulingService.scheduleOperationsInOrder(productionLineSchedule, order, finishDate, productionLine);

            ProductionLinePositionNewData productionLinePositionNewData = new ProductionLinePositionNewData(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM),
                    orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO), changeover);
            orderProductionLinesPositionNewData.put(productionLine.getId(), productionLinePositionNewData);
        }
    }

    @Override
    public void savePosition(Entity position, ProductionLinePositionNewData productionLinePositionNewData) {
        position.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, productionLinePositionNewData.getChangeover());
        position.getDataDefinition().fastSave(position);
    }

    @Override
    public void copyPS(Entity productionLineSchedule, Entity order, Entity productionLine) {
        DataDefinition planOrderTimeCalculationDD = dataDefinitionService
                .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_PLAN_ORDER_TIME_CALCULATION);
        Entity planOrderTimeCalculation = planOrderTimeCalculationDD
                .find()
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE, productionLine))
                .setMaxResults(1).uniqueResult();
        if (planOrderTimeCalculation != null) {
            DataDefinition operCompTimeCalculationDD = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION);
            operationWorkTimeService.deleteOperCompTimeCalculations(order);
            Entity orderTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find().add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order)).setMaxResults(1).uniqueResult();
            if (Objects.isNull(orderTimeCalculation)) {
                orderTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION).create();
                orderTimeCalculation.setField(OrderTimeCalculationFields.ORDER, order);
            }
            orderTimeCalculation.setField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM, planOrderTimeCalculation.getField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM));
            orderTimeCalculation.setField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO, planOrderTimeCalculation.getField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO));
            orderTimeCalculation = orderTimeCalculation.getDataDefinition().save(orderTimeCalculation);
            for (Entity planOperCompTimeCalculation : planOrderTimeCalculation.getHasManyField(OrderTimeCalculationFields.OPER_COMP_TIME_CALCULATIONS)) {
                Entity operCompTimeCalculation = operCompTimeCalculationDD.create();
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.ORDER_TIME_CALCULATION, orderTimeCalculation);
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.OPERATION_OFF_SET, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.OPERATION_OFF_SET));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.LABOR_WORK_TIME, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.MACHINE_WORK_TIME, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME));
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.DURATION, planOperCompTimeCalculation.getField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME));
                operCompTimeCalculationDD.save(operCompTimeCalculation);
            }
        }
        planOrderTimeCalculationDD.delete(planOrderTimeCalculationDD
                .find().add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .list()
                .getEntities().stream().map(Entity::getId).collect(Collectors.toList()).toArray(new Long[]{}));
    }
}
