/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productionPerShift.report;

import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PPSReportFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.UserService;

@Service
public class PPSReportXlsHelper {

    private static final Integer ONE = 1;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChangeoverNormsService changeoverNormsService;

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Autowired
    private ShiftsService shiftsService;

    public List<Entity> getProductionPerShiftForReport(final Entity goodFoodReport) {
        List<Entity> shifts = shiftsService.getShifts();

        DateTime dateFrom = new DateTime(goodFoodReport.getDateField(PPSReportFields.DATE_FROM));
        Shift shiftFirst = new Shift(shifts.get(0), dateFrom, false);
        List<TimeRange> ranges = shiftFirst.findWorkTimeAt(dateFrom.toLocalDate());
        if (ranges.isEmpty()) {
            return Lists.newArrayList();
        }
        LocalTime startTime = ranges.get(0).getFrom();
        DateTime firstStartShitTime = dateFrom;
        firstStartShitTime = firstStartShitTime.withHourOfDay(startTime.getHourOfDay());
        firstStartShitTime = firstStartShitTime.withMinuteOfHour(startTime.getMinuteOfHour());

        Optional<Long> maybeDateTo = getDateToInMills(goodFoodReport, shifts);
        if (maybeDateTo.isPresent()) {
            long dateToInMills = maybeDateTo.get();
            String sql = "select pps from #productionPerShift_productionPerShift as pps where (" + "(" + "('"
                    + firstStartShitTime.toDate().toString() + "' <= pps.order.finishDate and '"
                    + firstStartShitTime.toDate().toString() + "' >= pps.order.startDate) or " + "('"
                    + new Date(dateToInMills).toString().toString() + "' < pps.order.finishDate and '"
                    + new Date(dateToInMills).toString().toString() + "' > pps.order.startDate)" + ") or " + "("
                    + "(pps.order.startDate >= '" + firstStartShitTime.toDate().toString() + "' and pps.order.startDate <'"
                    + new Date(dateToInMills).toString().toString() + "') or " + "(pps.order.finishDate >= '"
                    + firstStartShitTime.toDate().toString() + "' and pps.order.finishDate < '"
                    + new Date(dateToInMills).toString().toString() + "') " + ")"
                    + ") and pps.order.state <> '05declined' and pps.order.state <> '07abandoned'  "
                    + "and pps.order.state <> '04completed' and pps.order.active = true";
            return dataDefinitionService
                    .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT)
                    .find(sql).list().getEntities();
        } else {
            return Lists.newArrayList();
        }
    }

    private Optional<Long> getDateToInMills(final Entity goodFoodReport, final List<Entity> shifts) {
        DateTime dateTo = new DateTime(goodFoodReport.getDateField(PPSReportFields.DATE_TO));
        Shift shiftEnd = new Shift(shifts.get(shifts.size() - 1), dateTo, false);
        List<TimeRange> rangesEnd = shiftEnd.findWorkTimeAt(dateTo.toLocalDate());
        if (rangesEnd.isEmpty()) {
            return Optional.empty();
        }
        LocalTime endTime = rangesEnd.get(0).getTo();
        dateTo = dateTo.plusDays(ONE);
        dateTo = dateTo.withHourOfDay(endTime.getHourOfDay());
        dateTo = dateTo.withMinuteOfHour(endTime.getMinuteOfHour());

        Entity shiftEntity = shiftsService.getShiftFromDateWithTime(dateTo.toDate());

        if (Objects.nonNull(shiftEntity)) {
            Optional<DateTime> mayBeShiftEndDate = getShiftEndDate(new DateTime(dateTo), shiftEntity);

            if (mayBeShiftEndDate.isPresent()) {
                dateTo = mayBeShiftEndDate.get();
            }
        }

        return Optional.of(dateTo.getMillis());
    }

    private Optional<DateTime> getShiftEndDate(final DateTime day, final Entity shiftEntity) {
        Shift shift = new Shift(shiftEntity);

        com.google.common.base.Optional<DateRange> mayBeWorkTimeAt = shift.findWorkTimeAt(day.toDate());

        if (mayBeWorkTimeAt.isPresent()) {
            DateRange range = mayBeWorkTimeAt.get();

            return Optional.of(new DateTime(range.getTo()));
        }

        return Optional.empty();
    }

    public Entity getDailyProgress(final Entity productionPerShift, final Date day, final Entity shift) {
        DataDefinition progressForDayDD = dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);

        List<Entity> correctedProgressForDay = progressForDayDD.find()
                .add(SearchRestrictions.belongsTo(ProgressForDayFields.PRODUCTION_PER_SHIFT, productionPerShift))
                .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, true)).list().getEntities();
        Entity progressForDay;

        if (correctedProgressForDay.isEmpty()) {
            progressForDay = progressForDayDD.find()
                    .add(SearchRestrictions.belongsTo(ProgressForDayFields.PRODUCTION_PER_SHIFT, productionPerShift))
                    .add(SearchRestrictions.eq(ProgressForDayFields.ACTUAL_DATE_OF_DAY, day))
                    .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, false)).setMaxResults(1).uniqueResult();
        } else {
            progressForDay = progressForDayDD.find()
                    .add(SearchRestrictions.belongsTo(ProgressForDayFields.PRODUCTION_PER_SHIFT, productionPerShift))
                    .add(SearchRestrictions.eq(ProgressForDayFields.ACTUAL_DATE_OF_DAY, day))
                    .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, true)).setMaxResults(1).uniqueResult();
        }

        if (Objects.isNull(progressForDay)) {
            return null;
        } else {
            return progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS).find()
                    .add(SearchRestrictions.belongsTo(DailyProgressFields.SHIFT, shift)).setMaxResults(1).uniqueResult();
        }
    }

    public String getDocumentAuthor(final String login) {
        return userService.extractFullName(userService.find(login));
    }

    public Entity getOrder(final Entity productionPerShift) {
        return productionPerShift.getBelongsToField(ProductionPerShiftFields.ORDER);
    }

    public Entity getProduct(final Entity productionPerShift) {
        return getOrder(productionPerShift).getBelongsToField(OrderFields.PRODUCT);
    }

    public Entity getProductionLine(final Entity productionPerShift) {
        return getOrder(productionPerShift).getBelongsToField(OrderFields.PRODUCTION_LINE);
    }

    public Entity getChangeover(final Entity order) {
        Entity previousOrder = lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order);

        if (Objects.isNull(previousOrder)) {
            return null;
        }

        Entity fromTechnology = previousOrder.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity toTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity productionLine = order.getBelongsToField(PRODUCTION_LINE);

        return changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
    }

}
