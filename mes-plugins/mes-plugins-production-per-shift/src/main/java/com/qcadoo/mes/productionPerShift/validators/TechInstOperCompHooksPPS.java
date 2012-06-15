package com.qcadoo.mes.productionPerShift.validators;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAILY_PROGRESS;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFields.HAS_CORRECTIONS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechInstOperCompHooksPPS {

    @Autowired
    private ShiftsService shiftsService;

    private static final Long MILLISECONDS_OF_ONE_DAY = 86400000L;

    public boolean checkGrowingNumberOfDays(final DataDefinition dataDefinition, final Entity entity) {
        List<Entity> progressForDays = entity.getHasManyField("progressForDays");
        if (progressForDays.isEmpty()) {
            return true;
        }
        Integer dayNumber = Integer.valueOf(0);
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getBooleanField("corrected") != entity.getBooleanField("hasCorrections")
                    || progressForDay.getField("day") == null) {
                continue;
            }
            Integer day = Integer.valueOf(progressForDay.getField("day").toString());
            if (day != null && dayNumber.compareTo(day) == -1) {
                dayNumber = day;
            } else {
                entity.addGlobalError("productionPerShift.progressForDay.daysIsNotInAscendingOrder");
                return false;
            }
        }
        return true;
    }

    public boolean checkShiftsIfWorks(final DataDefinition dataDefinition, final Entity tioc) {
        List<Entity> progressForDays = tioc.getHasManyField("progressForDays");
        if (progressForDays.isEmpty()) {
            return true;
        }
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getField(DAY) == null) {
                tioc.addGlobalError("productionPerShift.progressForDay.dayIsNull");
                return false;
            }
            if ((progressForDay.getBooleanField(CORRECTED) && !tioc.getBooleanField(HAS_CORRECTIONS))) {
                continue;
            }
            List<Entity> dailyProgressList = progressForDay.getHasManyField(DAILY_PROGRESS);
            for (Entity dailyProgress : dailyProgressList) {
                Entity shift = dailyProgress.getBelongsToField("shift");
                if (shift == null) {
                    continue;
                }
                if (!checkIfShiftWorks(progressForDays, progressForDay, tioc, shift)) {
                    tioc.addGlobalError(
                            "productionPerShift.progressForDay.shiftDoesNotWork",
                            shift.getStringField("name"),
                            new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(
                                    getDateAfterStartOrderForProgress(tioc.getBelongsToField("order"), progressForDay))
                                    .toString());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkIfShiftWorks(final List<Entity> progressForDays, final Entity progressForDay, final Entity tioc,
            final Entity shift) {
        boolean works = true;
        if (progressForDay.equals(progressForDays.get(0))) {
            Entity shiftFromDay = shiftsService.getShiftFromDateWithTime(getDateAfterStartOrderForProgress(
                    tioc.getBelongsToField("order"), progressForDay));
            if (!(shiftFromDay != null && shift.getId().equals(shiftFromDay.getId()))) {
                works = false;
            }
        } else {
            works = shiftsService.checkIfShiftWorkAtDate(
                    getDateAfterStartOrderForProgress(tioc.getBelongsToField("order"), progressForDay), shift);
        }
        return works;
    }

    private Date getDateAfterStartOrderForProgress(final Entity order, final Entity progressForDay) {
        Integer day = Integer.valueOf(progressForDay.getField(DAY).toString());
        Date startOrder = getPlannedOrCorrectedDate(order);
        return new Date(startOrder.getTime() + day * MILLISECONDS_OF_ONE_DAY);
    }

    private Date getPlannedOrCorrectedDate(final Entity order) {
        if (order.getField(CORRECTED_DATE_FROM) == null) {
            return (Date) order.getField(DATE_FROM);
        } else {
            return (Date) order.getField(CORRECTED_DATE_FROM);
        }
    }
}
