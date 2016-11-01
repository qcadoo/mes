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
package com.qcadoo.mes.productionPerShift.hooks;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.TechnologyOperationComponentFieldsPPS;
import com.qcadoo.mes.productionPerShift.dates.ProgressDatesService;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.mes.productionPerShift.services.AutomaticPpsExecutorService;
import com.qcadoo.mes.productionPerShift.services.AutomaticPpsParametersService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

@Service
public class OrderHooksPPS {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProgressDatesService progressDatesService;

    @Autowired
    private AutomaticPpsExecutorService automaticPpsExecutorService;

    @Autowired
    private AutomaticPpsParametersService automaticPpsParametersService;

    public void onUpdate(final DataDefinition orderDD, final Entity order) {
        setUpPpsDaysAndDatesFor(order);
        regenerateProductionPerShift(orderDD, order);
    }

    void setUpPpsDaysAndDatesFor(final Entity order) {
        if (startDatesHasBeenChanged(order)) {
            progressDatesService.setUpDatesFor(order);
        }
    }

    private boolean startDatesHasBeenChanged(final Entity order) {
        SearchCriteriaBuilder scb = order.getDataDefinition().find();
        scb.setProjection(id());
        scb.add(idEq(order.getId()));
        for (String dateFieldName : Sets.newHashSet(OrderFields.DATE_FROM, OrderFields.CORRECTED_DATE_FROM,
                OrderFields.EFFECTIVE_DATE_FROM)) {
            scb.add(eq(dateFieldName, order.getDateField(dateFieldName)));
        }
        return scb.setMaxResults(1).uniqueResult() == null;
    }

    public void regenerateProductionPerShift(final DataDefinition orderDD, final Entity order) {
        if (order.getId() == null) {
            return;
        }
        Entity orderFromDB = orderDD.get(order.getId());

        if (isOrderFieldsChanged(order, orderFromDB)) {
            Entity productionPerShift = dataDefinitionService
                    .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT)
                    .find().add(SearchRestrictions.belongsTo(ProductionPerShiftFields.ORDER, order)).setMaxResults(1)
                    .uniqueResult();

            if (productionPerShift != null && automaticPpsParametersService.isAutomaticPlanForShiftOn()) {
                boolean shouldBeCorrected = OrderState.of(order).compareTo(OrderState.PENDING) != 0;
                List<Entity> operationComponents = order.getBelongsToField(OrderFields.TECHNOLOGY)
                        .getHasManyField(TechnologyFields.OPERATION_COMPONENTS).stream()
                        .filter(toc -> !toc.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS).isEmpty())
                        .collect(Collectors.toList());
                for (Entity toc : operationComponents) {

                    BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
                    if (order.getBooleanField(OrderFields.FINAL_PRODUCTION_TRACKING)) {
                        plannedQuantity = basicProductionCountingService.getProducedQuantityFromBasicProductionCountings(order);
                    }
                    ProgressForDaysContainer progressForDaysContainer = new ProgressForDaysContainer();
                    progressForDaysContainer.setShouldBeCorrected(shouldBeCorrected);
                    progressForDaysContainer.setOperationComponent(toc);
                    progressForDaysContainer.setOrder(order);
                    try {
                        automaticPpsExecutorService.generateProgressForDays(progressForDaysContainer, productionPerShift);
                    } catch (Exception ex) {
                        for (ErrorMessage errorMessage : progressForDaysContainer.getErrors()) {
                            order.addGlobalError(errorMessage.getMessage(), false, errorMessage.getVars());
                        }
                        return;
                    }
                    List<Entity> progressForDays = progressForDaysContainer.getProgressForDays();
                    if (progressForDaysContainer.isCalculationError()) {
                        productionPerShift.getGlobalErrors().forEach(
                                error -> order.addGlobalError(error.getMessage(), false, error.getVars()));
                        return;
                    }

                    if (!progressForDaysContainer.isPartCalculation()) {
                        Date finishDate = automaticPpsExecutorService.calculateOrderFinishDate(order, progressForDays);

                        order.setField(OrderFields.FINISH_DATE, finishDate);

                        if (shouldBeCorrected) {
                            order.setField(OrderFields.CORRECTED_DATE_TO, finishDate);
                        } else {
                            order.setField(OrderFields.DATE_TO, finishDate);
                        }
                    }

                    if (shouldBeCorrected) {
                        progressForDays.addAll(toc.getHasManyField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS)
                                .stream()
                                .filter(progressForDay -> !progressForDay.getBooleanField(ProgressForDayFields.CORRECTED))
                                .collect(Collectors.toList()));
                    }
                    toc.setField(TechnologyOperationComponentFieldsPPS.PROGRESS_FOR_DAYS, progressForDays);
                    toc.getDataDefinition().save(toc);
                }

            }
        }
        updateOrderData(order);
    }

    /**
     * Method to additional process data - overridden by aspect
     * 
     * @param order
     */
    private void updateOrderData(Entity order) {

    }

    private boolean isOrderFieldsChanged(Entity order, Entity orderFromDB) {
        BigDecimal plannedQuantity1 = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        BigDecimal plannedQuantity2 = orderFromDB.getDecimalField(OrderFields.PLANNED_QUANTITY);

        if (plannedQuantity1 == null && plannedQuantity2 != null) {
            return true;
        }

        if (plannedQuantity2 == null && plannedQuantity1 != null) {
            return true;
        }

        if (plannedQuantity1 != null && plannedQuantity2 != null && plannedQuantity1.compareTo(plannedQuantity2) != 0) {
            return true;
        }

        Date startDate1 = order.getDateField(OrderFields.START_DATE);
        Date startDate2 = orderFromDB.getDateField(OrderFields.START_DATE);

        if (startDate1 == null && startDate2 != null) {
            return true;
        }

        if (startDate2 == null && startDate1 != null) {
            return true;
        }

        if (startDate1 != null && startDate2 != null && startDate1.compareTo(startDate2) != 0) {
            return true;
        }

        return false;
    }

}
