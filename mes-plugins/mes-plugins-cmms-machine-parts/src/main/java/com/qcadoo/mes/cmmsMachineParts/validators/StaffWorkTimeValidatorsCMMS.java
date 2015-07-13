package com.qcadoo.mes.cmmsMachineParts.validators;

import com.qcadoo.mes.cmmsMachineParts.constants.StaffWorkTimeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StaffWorkTimeValidatorsCMMS {
    public boolean validatesWith(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {

        if (!checkOperatorWorkTime(staffWorkTimeDD, staffWorkTime)) {
            return false;
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {
        Date dateFrom = staffWorkTime.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START);
        Date dateTo = staffWorkTime.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }
        staffWorkTime.addError(staffWorkTimeDD.getField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END),
                "productionCounting.productionTracking.productionTrackingError.effectiveExecutionTimeEndBeforeEffectiveExecutionTimeStart");
        return false;
    }

}
