package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants.MODEL_PRODUCTION_PER_SHIFT;
import static com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields.ORDER;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechInstOperCompHooksPPS {

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

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
            Integer day = ((Long) progressForDay.getField("day")).intValue();
            if (dayNumber.compareTo(day) == -1) {
                dayNumber = day;
            } else {
                Entity order = entity.getBelongsToField("order");
                Entity productionPerShift = dataDefinitionService
                        .get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_PER_SHIFT).find()
                        .add(SearchRestrictions.belongsTo(ORDER, order)).uniqueResult();
                entity.addGlobalError("productionPerShift.progressForDay.daysIsNotInAscendingOrder");

                // productionPerShift.addGlobalError("productionPerShift.progressForDay.daysIsNotInAscendingOrder");
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
            if (progressForDay.getBooleanField("corrected") != entity.getBooleanField("hasCorrections")) {
                continue;
            }
            Integer day = ((Long) progressForDay.getField("day")).intValue();
            Entity shift = progressForDay.getBelongsToField("shift");
            if (shift == null) {
                continue;
            }
            Date startOrder = getPlannedOrCorrectedDate(order);
            Date dayOfProduction = new Date(startOrder.getTime() + day * MILLISECONDS_OF_ONE_DAY);
            Entity shiftFromDay = shiftsService.getShiftFromDate(dayOfProduction);

            if (!(shiftFromDay != null && shift.getId().equals(shiftFromDay.getId()))) {
                entity.addError(dataDefinition.getField("progressForDays"), "productionPerShift.progressForDay.shiftDoesNotWork",
                        shift.getStringField("name"), dayOfProduction.toString());
                return false;
            }
        }
        return true;
    }

    private Date getPlannedOrCorrectedDate(final Entity order) {
        if (order.getField("correctedDateFrom") != null) {
            return (Date) order.getField("correctedDateFrom");
        } else {
            return (Date) order.getField("dateFrom");
        }
    }
}
