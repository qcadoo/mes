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
package com.qcadoo.mes.productionPerShift.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.ProductionLineSchedulePositionFieldsLCNFO;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePPS;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.mes.orders.listeners.ProductionLinePositionNewData;
import com.qcadoo.mes.productionPerShift.PpsTimeHelper;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PlanProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;

@Service
@Order(1)
@RunIfEnabled(ProductionPerShiftConstants.PLUGIN_IDENTIFIER)
public class ProductionLineScheduleServicePPSImpl implements ProductionLineScheduleServicePPS {

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private AutomaticPpsExecutorService automaticPpsExecutorService;

    @Autowired
    private PpsTimeHelper ppsTimeHelper;

    @Autowired
    private TechnologyService technologyService;


    public void createProductionLinePositionNewData(Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData,
                                                    Entity productionLine, Date startDate, Entity position, Entity technology, Entity previousOrder) {
        Optional<BigDecimal> norm = technologyService.getStandardPerformance(technology, productionLine);
        if (norm.isPresent()) {
            Entity changeover = lineChangeoverNormsForOrdersService.getChangeover(previousOrder, technology, productionLine);
            if (changeover != null) {
                startDate = Date.from(startDate.toInstant().plusSeconds(changeover.getIntegerField(LineChangeoverNormsFields.DURATION)));
            }

            DateTime startDateTime = new DateTime(startDate);
            startDate = shiftsService
                    .getNearestWorkingDate(startDateTime, productionLine).orElse(startDateTime).toDate();

            Optional<Date> finishDate = generateProductionPerShift(position, startDate, productionLine);

            Date finalStartDate = startDate;
            finishDate.ifPresent(fd -> {
                ProductionLinePositionNewData productionLinePositionNewData = new ProductionLinePositionNewData(finalStartDate,
                        fd, changeover);
                orderProductionLinesPositionNewData.put(productionLine.getId(), productionLinePositionNewData);
            });
        }
    }

    @Override
    public void savePosition(Entity position, ProductionLinePositionNewData productionLinePositionNewData) {
        position.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, productionLinePositionNewData.getChangeover());
        position.getDataDefinition().fastSave(position);
    }

    @Override
    public void copyPPS(Entity productionLineSchedule, Entity order, Entity productionLine) {
        DataDefinition planProductionPerShiftDD = dataDefinitionService
                .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PLAN_PRODUCTION_PER_SHIFT);
        Entity planProductionPerShift = planProductionPerShiftDD
                .find().add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .setMaxResults(1)
                .uniqueResult();
        if(planProductionPerShift != null) {
            DataDefinition productionPerShiftDD = dataDefinitionService
                    .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT);
            DataDefinition progressForDayDD = dataDefinitionService
                    .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
            DataDefinition dailyProgressDD = dataDefinitionService
                    .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_DAILY_PROGRESS);
            Entity productionPerShift =
                    productionPerShiftDD.find().add(SearchRestrictions.belongsTo(ProductionPerShiftFields.ORDER, order)).setMaxResults(1)
                            .uniqueResult();
            if (productionPerShift == null) {
                productionPerShift = productionPerShiftDD.create();
                productionPerShift.setField(ProductionPerShiftFields.ORDER, order);
                productionPerShift.setField(ProductionPerShiftFields.PLANNED_PROGRESS_TYPE, ProgressType.PLANNED.getStringValue());
                productionPerShift = productionPerShiftDD.save(productionPerShift);
            } else {
                progressForDayDD.delete(productionPerShift.getHasManyField(ProductionPerShiftFields.PROGRES_FOR_DAYS).stream()
                        .map(Entity::getId).collect(Collectors.toList()).toArray(new Long[]{}));
            }

            for (Entity planProgressForDay : planProductionPerShift.getHasManyField(PlanProductionPerShiftFields.PROGRES_FOR_DAYS)) {
                Entity progressForDay = progressForDayDD.create();
                progressForDay.setField(ProgressForDayFields.PRODUCTION_PER_SHIFT, productionPerShift);
                progressForDay.setField(ProgressForDayFields.DAY, planProgressForDay.getField(ProgressForDayFields.DAY));
                progressForDay.setField(ProgressForDayFields.DATE_OF_DAY, planProgressForDay.getField(ProgressForDayFields.DATE_OF_DAY));
                progressForDay.setField(ProgressForDayFields.ACTUAL_DATE_OF_DAY, planProgressForDay.getField(ProgressForDayFields.ACTUAL_DATE_OF_DAY));
                progressForDay.setField(ProgressForDayFields.CORRECTED, false);
                progressForDay = progressForDayDD.save(progressForDay);
                for (Entity planDailyProgress : planProgressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS)) {
                    Entity dailyProgress = dailyProgressDD.create();
                    dailyProgress.setField(DailyProgressFields.PROGRESS_FOR_DAY, progressForDay);
                    dailyProgress.setField(DailyProgressFields.SHIFT, planDailyProgress.getField(DailyProgressFields.SHIFT));
                    dailyProgress.setField(DailyProgressFields.QUANTITY, planDailyProgress.getField(DailyProgressFields.QUANTITY));
                    dailyProgress.setField(DailyProgressFields.EFFICIENCY_TIME, planDailyProgress.getField(DailyProgressFields.EFFICIENCY_TIME));
                    dailyProgressDD.save(dailyProgress);
                }
            }
        }
        planProductionPerShiftDD.delete(planProductionPerShiftDD
                .find().add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .list()
                .getEntities().stream().map(Entity::getId).collect(Collectors.toList()).toArray(new Long[]{}));
    }

    private Optional<Date> generateProductionPerShift(final Entity position, Date startDate, Entity productionLine) {
        Entity order = position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
        Entity productionLineSchedule = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE);
        DataDefinition planProductionPerShiftDD = dataDefinitionService
                .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PLAN_PRODUCTION_PER_SHIFT);
        Entity planProductionPerShift = planProductionPerShiftDD
                .find().add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.belongsTo(PlanProductionPerShiftFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .setMaxResults(1)
                .uniqueResult();
        if (planProductionPerShift == null) {
            planProductionPerShift = planProductionPerShiftDD.create();
            planProductionPerShift.setField(PlanProductionPerShiftFields.ORDER, order);
            planProductionPerShift.setField(PlanProductionPerShiftFields.PRODUCTION_LINE, productionLine);
            planProductionPerShift.setField(PlanProductionPerShiftFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule);
        }
        planProductionPerShift.setField(PlanProductionPerShiftFields.PROGRES_FOR_DAYS, null);
        planProductionPerShift = planProductionPerShiftDD.save(planProductionPerShift);

        ProgressForDaysContainer progressForDaysContainer = new ProgressForDaysContainer();
        progressForDaysContainer.setOrder(order);
        try {
            automaticPpsExecutorService.generatePlanProgressForDays(progressForDaysContainer, planProductionPerShift, startDate);
        } catch (Exception ex) {
            return Optional.empty();
        }
        if (progressForDaysContainer.isCalculationError()) {
            return Optional.empty();
        }
        List<Entity> progressForDays = progressForDaysContainer.getProgressForDays();
        Entity orderWithDates = order.getDataDefinition().create();
        orderWithDates.setField(OrderFields.START_DATE, startDate);
        orderWithDates.setField(OrderFields.PRODUCTION_LINE, productionLine);
        Date finishDate = ppsTimeHelper.calculateOrderFinishDate(orderWithDates, progressForDays);
        if (finishDate == null) {
            return Optional.empty();
        }

        planProductionPerShift.setField(PlanProductionPerShiftFields.PROGRES_FOR_DAYS, progressForDays);
        planProductionPerShiftDD.save(planProductionPerShift);
        return Optional.of(finishDate);
    }
}
