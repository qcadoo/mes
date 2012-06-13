package com.qcadoo.mes.productionPerShift.validators;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAILY_PROGRESS;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFields.HAS_CORRECTIONS;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            if (progressForDay.getBooleanField("corrected") != entity.getBooleanField("hasCorrections")) {
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

    public boolean checkShiftIfWorks(final DataDefinition dataDefinition, final Entity entity) {
        List<Entity> progressForDays = entity.getHasManyField("progressForDays");
        if (progressForDays.isEmpty()) {
            return true;
        }
        Entity order = entity.getBelongsToField("order");
        for (Entity progressForDay : progressForDays) {
            if ((progressForDay.getBooleanField(CORRECTED) && !entity.getBooleanField(HAS_CORRECTIONS))
                    || progressForDay.getField(DAY) == null) {
                continue;
            }
            Integer day = Integer.valueOf(progressForDay.getField(DAY).toString());
            List<Entity> dailyProgressList = progressForDay.getHasManyField(DAILY_PROGRESS);
            for (Entity dailyProgress : dailyProgressList) {
                Entity shift = dailyProgress.getBelongsToField("shift");
                if (shift == null) {
                    continue;
                }
                Date startOrder = getPlannedOrCorrectedDate(order);
                Date dayOfProduction = new Date(startOrder.getTime() + day * MILLISECONDS_OF_ONE_DAY);
                Entity shiftFromDay = shiftsService.getShiftFromDate(dayOfProduction);
                if (!(shiftFromDay != null && shift.getId().equals(shiftFromDay.getId()))) {
                    entity.addGlobalError("productionPerShift.progressForDay.shiftDoesNotWork", shift.getStringField("name"),
                            dayOfProduction.toString());
                    return false;
                }
            }
        }
        return true;
    }

    private Date getPlannedOrCorrectedDate(final Entity order) {
        if (order.getField(CORRECTED_DATE_FROM) == null) {
            return (Date) order.getField(DATE_FROM);
        } else {
            return (Date) order.getField(CORRECTED_DATE_FROM);
        }
    }
}
