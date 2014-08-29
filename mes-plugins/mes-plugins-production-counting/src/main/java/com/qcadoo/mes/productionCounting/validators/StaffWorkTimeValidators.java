package com.qcadoo.mes.productionCounting.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.StaffWorkTimeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class StaffWorkTimeValidators {

    public boolean validatesWith(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {
        Entity productionTracking = staffWorkTime.getBelongsToField(StaffWorkTimeFields.PRODUCTION_RECORD);

        Entity existingStaffWorkTime = productionTracking
                .getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES)
                .find()
                .add(SearchRestrictions.belongsTo(StaffWorkTimeFields.WORKER,
                        staffWorkTime.getBelongsToField(StaffWorkTimeFields.WORKER))).setMaxResults(1).uniqueResult();
        if (existingStaffWorkTime != null && !existingStaffWorkTime.equals(staffWorkTime)) {
            staffWorkTime.addError(staffWorkTime.getDataDefinition().getField(StaffWorkTimeFields.WORKER),
                    "productionCounting.productionTracking.productionTrackingError.duplicateWorker");
            return false;
        }
        return true;
    }
}
