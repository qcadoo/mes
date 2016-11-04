package com.qcadoo.mes.productionPerShift.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.DateTimeRange;
import com.qcadoo.mes.productionPerShift.PpsTimeHelper;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PpsAlgorithm;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.domain.ProgressForDaysContainer;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class AutomaticPpsExecutorService {

    @Autowired
    private List<AutomaticPpsService> ppsAlgorithmServcies;

    @Autowired
    private AutomaticPpsParametersService parametersService;

    @Autowired
    private PpsTimeHelper ppsTimeHelper;

    public void generateProgressForDays(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift) {
        PpsAlgorithm algorithm = parametersService.getPpsAlgorithm();
        if (PpsAlgorithm.STANDARD_TECHNOLOGY == algorithm) {
            callStandardAlgorithm(progressForDaysContainer, productionPerShift, algorithm);
        } else if (PpsAlgorithm.STANDARD_TECHNOLOGY_AND_AMOUNT_OF_CHANGE == algorithm) {
            callStandardAlgorithm(progressForDaysContainer, productionPerShift, algorithm);
        } else if (PpsAlgorithm.USER == algorithm) {
            callUserAlgorithm(progressForDaysContainer, productionPerShift);
        }
    }

    private void callUserAlgorithm(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift) {
        for (AutomaticPpsService service : ppsAlgorithmServcies) {
            if (serviceEnabled(service) && isNotStandardAlgorithm(service)) {
                service.generateProgressForDays(progressForDaysContainer, productionPerShift);
            }
        }
    }

    private void callStandardAlgorithm(ProgressForDaysContainer progressForDaysContainer, Entity productionPerShift,
            PpsAlgorithm algorithm) {
        for (AutomaticPpsService service : ppsAlgorithmServcies) {
            if (serviceEnabled(service)) {
                String aClass = service.getClass().getSimpleName();
                if (algorithm.getAlgorithmClass().equalsIgnoreCase(aClass)) {
                    service.generateProgressForDays(progressForDaysContainer, productionPerShift);
                }
            }
        }
    }

    private boolean isNotStandardAlgorithm(AutomaticPpsService service) {
        if (!PpsAlgorithm.STANDARD_TECHNOLOGY.getAlgorithmClass().equals(service.getClass().getSimpleName())
                && !PpsAlgorithm.STANDARD_TECHNOLOGY_AND_AMOUNT_OF_CHANGE.getAlgorithmClass().equals(
                        service.getClass().getSimpleName())) {
            return true;
        }
        return false;
    }

    private <M extends Object & AutomaticPpsService> boolean serviceEnabled(M service) {
        RunIfEnabled runIfEnabled = service.getClass().getAnnotation(RunIfEnabled.class);
        if (runIfEnabled == null) {
            return true;
        }
        for (String pluginIdentifier : runIfEnabled.value()) {
            if (!PluginUtils.isEnabled(pluginIdentifier)) {
                return false;
            }
        }
        return true;
    }

    public Date calculateOrderFinishDate(final Entity order, final List<Entity> progressForDays) {
        if (!progressForDays.isEmpty()) {
            Entity progressForDay = Iterables.getLast(progressForDays);

            if (progressForDay != null) {
                Date dateOfDay = progressForDay.getDateField(ProgressForDayFields.DATE_OF_DAY);

                List<Entity> dailyProgresses = progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS);

                if (!dailyProgresses.isEmpty()) {
                    Entity dailyProgress = Iterables.getLast(dailyProgresses);

                    if (dailyProgress != null) {
                        Entity shift = dailyProgress.getBelongsToField(DailyProgressFields.SHIFT);
                        BigDecimal quantity = dailyProgress.getDecimalField(DailyProgressFields.QUANTITY);

                        if (dailyProgress.getIntegerField(DailyProgressFields.EFFICIENCY_TIME) == null) {
                            return order.getDateField(OrderFields.FINISH_DATE);
                        }

                        int time = dailyProgress.getIntegerField(DailyProgressFields.EFFICIENCY_TIME);

                        Date endDate = findFinishDate(shift, dateOfDay, time, order);

                        return endDate;
                    }
                }
            }
        }
        return order.getDateField(OrderFields.FINISH_DATE);
    }

    public Date findFinishDate(final Entity shiftEntity, Date dateOfDay, int time, final Entity order) {
        DateTime endDate = null;
        DateTime dateOfDayDT = new DateTime(dateOfDay, DateTimeZone.getDefault());
        DateTime orderStartDate = new DateTime(order.getDateField(OrderFields.START_DATE), DateTimeZone.getDefault());
        Shift shift = new Shift(shiftEntity);
        List<TimeRange> shiftWorkTime = Lists.newArrayList();
        List<DateTimeRange> shiftWorkDateTime = Lists.newArrayList();
        if (shift.worksAt(dateOfDay.getDay() == 0 ? 7 : dateOfDay.getDay())) {
            shiftWorkTime = shift.findWorkTimeAt(new LocalDate(dateOfDay));
        }
        for (TimeRange range : shiftWorkTime) {
            DateTimeRange dateTimeRange = new DateTimeRange(dateOfDayDT, range);
            DateTimeRange trimmedRange = dateTimeRange.trimBefore(orderStartDate);
            if (trimmedRange != null) {
                shiftWorkDateTime.add(trimmedRange);
            }
        }

        shiftWorkDateTime = ppsTimeHelper.manageExceptions(shiftWorkDateTime, shift.getEntity(), dateOfDay);

        for (DateTimeRange range : shiftWorkDateTime) {
            if (range.durationInMins() >= time && time > 0) {
                endDate = range.getFrom().plusMinutes(time);
                time = 0;
            } else {
                endDate = range.getTo();
                time -= range.durationInMins();
            }
        }
        return endDate != null ? endDate.toDate() : null;
    }
}
