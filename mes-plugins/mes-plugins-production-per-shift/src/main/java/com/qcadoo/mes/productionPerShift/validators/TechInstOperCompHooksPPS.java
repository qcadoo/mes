package com.qcadoo.mes.productionPerShift.validators;

import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechInstOperCompHooksPPS {

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
                entity.addGlobalError("productionPerShift.progressForDay.daysIsNotInAscendingOrder", day.toString());
                return false;
            }
        }
        return true;
    }

    public boolean checkShiftsIfWorks(final DataDefinition tiocDD, final Entity tioc) {
        List<Entity> progressForDays = tioc.getHasManyField("progressForDays");
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getField(DAY) == null) {
                tioc.addGlobalError("productionPerShift.progressForDay.dayIsNull");
                return false;
            }
        }
        return true;
    }

}
