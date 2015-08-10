package com.qcadoo.mes.cmmsMachineParts.validators;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventValidators {

    public boolean validatesWith(final DataDefinition plannedEventDD, final Entity plannedEvent) {

        if (!checkOperatorWorkTime(plannedEventDD, plannedEvent)) {
            return false;
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition plannedEventDD, final Entity plannedEvent) {
        Date startDate = plannedEvent.getDateField(PlannedEventFields.START_DATE);
        Date finishDate = plannedEvent.getDateField(PlannedEventFields.FINISH_DATE);

        if (startDate == null || finishDate == null || finishDate.after(startDate)) {
            return true;
        }
        plannedEvent.addError(plannedEventDD.getField(PlannedEventFields.FINISH_DATE),
                "cmmsMachineParts.plannedEventDetails.error.wrongDateOrder");
        return false;
    }
}
