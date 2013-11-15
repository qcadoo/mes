package com.qcadoo.mes.productionPerShift.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DailyProgressValidators {

    public void checkUniqueShift(final DataDefinition dailyProgressDD, final Entity dailyProgress) {
        Entity secondDailyProgress = dailyProgressDD
                .find()
                .add(SearchRestrictions.belongsTo(DailyProgressFields.SHIFT,
                        dailyProgress.getBelongsToField(DailyProgressFields.SHIFT)))
                .add(SearchRestrictions.belongsTo(DailyProgressFields.PROGRESS_FOR_DAY,
                        dailyProgress.getBelongsToField(DailyProgressFields.PROGRESS_FOR_DAY))).setMaxResults(1).uniqueResult();
        if (secondDailyProgress != null && dailyProgress.getId() == null) {
            dailyProgress.addError(dailyProgressDD.getField(DailyProgressFields.SHIFT),
                    "productionPerShift.dailyProgress.shiftAndProgressForDay.mustBeUnique");
        }
    }
}
