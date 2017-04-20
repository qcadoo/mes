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
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsSearchService;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PPSReportFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.UnitConversionItemFields;
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
        DateTime dateFrom = new DateTime(goodFoodReport.getDateField(PPSReportFields.DATE_FROM));

        List<Entity> shifts = getShifts();
        Shift shiftFirst = new Shift(shifts.get(0));
        List<TimeRange> ranges = shiftFirst.findWorkTimeAt(dateFrom.toLocalDate());
        LocalTime startTime = ranges.get(0).getFrom();
        DateTime firstStartShitTime = dateFrom;
        firstStartShitTime = firstStartShitTime.withHourOfDay(startTime.getHourOfDay());
        firstStartShitTime = firstStartShitTime.withMinuteOfHour(startTime.getMinuteOfHour());

        long dateToInMills = getDateToInMills(goodFoodReport, shifts);

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
    }

    private long getDateToInMills(final Entity goodFoodReport, final List<Entity> shifts) {
        DateTime dateTo = new DateTime(goodFoodReport.getDateField(PPSReportFields.DATE_TO));
        Shift shiftEnd = new Shift(shifts.get(shifts.size() - 1));
        List<TimeRange> rangesEnd = shiftEnd.findWorkTimeAt(dateTo.toLocalDate());
        LocalTime endTime = rangesEnd.get(0).getTo();
        dateTo = dateTo.plusDays(ONE);
        dateTo = dateTo.withHourOfDay(endTime.getHourOfDay());
        dateTo = dateTo.withMinuteOfHour(endTime.getMinuteOfHour());

        Entity shiftEntity = shiftsService.getShiftFromDateWithTime(dateTo.toDate());
        if (shiftEntity != null) {
            dateTo = getShiftEndDate(new DateTime(dateTo), shiftEntity);
        }
        return dateTo.getMillis();
    }

    private DateTime getShiftEndDate(DateTime day, Entity shiftEntity) {
        Shift shift = new Shift(shiftEntity);
        DateRange range = shift.findWorkTimeAt(day.toDate()).get();
        return new DateTime(range.getTo());
    }

    public List<Entity> getShifts() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).find().list()
                .getEntities();
    }

    public List<DateTime> getDaysBetweenGivenDates(final Entity goodFoodReport) {
        List<DateTime> days = new LinkedList<DateTime>();

        DateTime dateFrom = new DateTime(goodFoodReport.getDateField(PPSReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(goodFoodReport.getDateField(PPSReportFields.DATE_TO));
        DateTime nextDay = dateFrom;

        int numberOfDays = Days.daysBetween(dateFrom.toDateMidnight(), dateTo.toDateMidnight()).getDays();

        days.add(nextDay);

        int oneDay = 1;

        while (numberOfDays != 0) {
            nextDay = nextDay.plusDays(oneDay).toDateTime();
            days.add(nextDay);
            numberOfDays--;
        }

        return days;
    }

    public int getNumberOfDaysBetweenGivenDates(final Entity goodFoodReport) {
        DateTime dateFrom = new DateTime(goodFoodReport.getField(PPSReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(goodFoodReport.getField(PPSReportFields.DATE_TO));

        return Days.daysBetween(dateFrom.toDateMidnight(), dateTo.toDateMidnight()).getDays();
    }

    public Entity getDailyProgress(final Entity productionPerShift, final Date day, final Entity shift) {
        Entity order = getOrder(productionPerShift);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity toc = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        DataDefinition progressForDayDD = dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);

        List<Entity> correctedProgressForDay = progressForDayDD.find()
                .add(SearchRestrictions.belongsTo(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, true)).list().getEntities();
        Entity progressForDay;

        if (correctedProgressForDay.isEmpty()) {
            progressForDay = progressForDayDD.find()
                    .add(SearchRestrictions.belongsTo(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                    .add(SearchRestrictions.eq(ProgressForDayFields.ACTUAL_DATE_OF_DAY, day))
                    .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, false)).setMaxResults(1).uniqueResult();
        } else {
            progressForDay = progressForDayDD.find()
                    .add(SearchRestrictions.belongsTo(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                    .add(SearchRestrictions.eq(ProgressForDayFields.ACTUAL_DATE_OF_DAY, day))
                    .add(SearchRestrictions.eq(ProgressForDayFields.CORRECTED, true)).setMaxResults(1).uniqueResult();
        }

        if (progressForDay == null) {
            return null;
        } else {
            Entity dailyProgress = progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS).find()
                    .add(SearchRestrictions.belongsTo(DailyProgressFields.SHIFT, shift)).setMaxResults(1).uniqueResult();

            return dailyProgress;
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
        if (previousOrder == null) {
            return null;
        }
        Entity fromTechnology = previousOrder.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity toTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        Entity productionLine = order.getBelongsToField(PRODUCTION_LINE);
        return changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine);
    }

}
