/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productionPerShift.services;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeFields;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.domain.DailyProgressKey;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyProgressService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Creates map of daily progresses with accepted production tracking records only, and fills keys with quantity produced in
     * that record
     * 
     * @param order
     * @param operationComponent
     *            technologyOperationComponent
     * @return
     */
    public Map<DailyProgressKey, Entity> getDailyProgressesWithTrackingRecords(final Entity order, final Entity operationComponent) {

        Map<DailyProgressKey, Entity> dailyProgresses = Maps.newHashMap();
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        List<Entity> mainOutProductComponents = getMainOutProductComponentsForOrderAndProduct(order, product, operationComponent);
        for (Entity outProduct : mainOutProductComponents) {
            Entity trackingRecord = outProduct.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            Long shiftId = trackingRecord.getBelongsToField(ProductionTrackingFields.SHIFT).getId();
            Date startDate = trackingRecord.getDateField(ProductionTrackingFields.SHIFT_START_DAY);
            Optional<Entity> dailyProgress = findDailyProgress(operationComponent, shiftId, startDate);
            DailyProgressKey key = new DailyProgressKey(
                    outProduct.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY), shiftId, startDate);
            if (dailyProgress.isPresent()) {
                dailyProgresses.put(key, dailyProgress.get());
            }
        }
        return dailyProgresses;
    }

    private List<Entity> getMainOutProductComponentsForOrderAndProduct(final Entity order, final Entity product,
            final Entity operationComponent) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT)
                .find()
                .createAlias(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING,
                        TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING, JoinType.INNER)
                .add(SearchRestrictions.eq(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING + ".order.id",
                        order.getId()));
        if (operationComponent != null) {
            scb.add(SearchRestrictions.eq(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING
                    + ".technologyOperationComponent.id", operationComponent.getId()));
        }
        // scb.add(SearchRestrictions.eq(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING + ".state",
        // ProductionTrackingState.ACCEPTED.getStringValue()));

        scb.add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, product));
        List<Entity> outProducts = scb.list().getEntities().stream()
                .filter(outProduct -> isTrackingRecordAcceptedOrChangeInProgress(outProduct)).collect(Collectors.toList());
        return outProducts;
    }

    private boolean isTrackingRecordAcceptedOrChangeInProgress(final Entity outProduct) {
        Entity productionTracking = outProduct.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        return ProductionTrackingState.ACCEPTED.getStringValue().equals(
                productionTracking.getStringField(ProductionTrackingFields.STATE))
                || stateChangeInProgress(productionTracking);
    }

    private boolean stateChangeInProgress(final Entity productionTracking) {
        List<Entity> stateChanges = productionTracking.getHasManyField(ProductionTrackingFields.STATE_CHANGES);
        for (Entity stateChange : stateChanges) {
            if (stateChange.getStringField(ProductionTrackingStateChangeFields.STATUS).equals("01inProgress")) {
                return true;
            }
        }
        return false;
    }

    public Optional<Entity> findDailyProgress(final Entity operationComponent, final Long shiftId, final Date startDate) {
        List<Entity> dailyProgress = dataDefinitionService
                .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_DAILY_PROGRESS)
                .find()
                .createAlias(DailyProgressFields.SHIFT, DailyProgressFields.SHIFT, JoinType.INNER)
                .createAlias(DailyProgressFields.PROGRESS_FOR_DAY, DailyProgressFields.PROGRESS_FOR_DAY, JoinType.INNER)
                .add(SearchRestrictions.eq(DailyProgressFields.SHIFT + ".id", shiftId))
                .add(SearchRestrictions.eq(DailyProgressFields.PROGRESS_FOR_DAY + "."
                        + ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT + ".id", operationComponent.getId()))
                .add(SearchRestrictions.eq(DailyProgressFields.PROGRESS_FOR_DAY + "." + ProgressForDayFields.ACTUAL_DATE_OF_DAY,
                        startDate)).list().getEntities();
        for (Entity dp : dailyProgress) {
            if (dp.getBelongsToField(DailyProgressFields.PROGRESS_FOR_DAY).getBooleanField(ProgressForDayFields.CORRECTED)) {
                return Optional.ofNullable(dp);
            }
        }
        return dailyProgress.isEmpty() ? Optional.empty() : Optional.ofNullable(dailyProgress.get(0));
    }
}
