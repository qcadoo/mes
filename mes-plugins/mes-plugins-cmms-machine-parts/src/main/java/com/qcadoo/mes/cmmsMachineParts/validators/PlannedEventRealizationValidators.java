package com.qcadoo.mes.cmmsMachineParts.validators;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventRealizationValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity plannedEventRealization) {

        if (!checkOperatorWorkTime(dataDefinition, plannedEventRealization)) {
            return false;
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition dataDefinition, final Entity plannedEventRealization) {
        Date startDate = plannedEventRealization.getDateField(PlannedEventRealizationFields.START_DATE);
        Date finishDate = plannedEventRealization.getDateField(PlannedEventRealizationFields.FINISH_DATE);

        if (startDate == null || finishDate == null || finishDate.after(startDate)) {
            return true;
        }
        plannedEventRealization.addError(dataDefinition.getField(PlannedEventRealizationFields.FINISH_DATE),
                "cmmsMachineParts.plannedEventRealizationDetails.error.wrongDateOrder");
        return false;
    }
}
