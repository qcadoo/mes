package com.qcadoo.mes.operationalTasks.validators;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationalTaskValidators {

    public boolean compareDate(final DataDefinition dataDefinition, final Entity entity) {
        Date startDate = (Date) entity.getField(OperationalTasksFields.START_DATE);
        Date finishDate = (Date) entity.getField(OperationalTasksFields.FINISH_DATE);
        if (startDate.compareTo(finishDate) == 1) {
            entity.addError(dataDefinition.getField(OperationalTasksFields.START_DATE),
                    "operationalTasks.operationalTask.finishDateIsEarlier");
            entity.addError(dataDefinition.getField(OperationalTasksFields.FINISH_DATE),
                    "operationalTasks.operationalTask.finishDateIsEarlier");
            return false;
        }
        return true;
    }
}
