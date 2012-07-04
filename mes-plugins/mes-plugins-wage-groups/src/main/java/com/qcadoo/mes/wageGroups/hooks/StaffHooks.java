package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.DETERMINED_INDIVIDUAL;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.INDIVIDUAL_LABOR_COST;
import static com.qcadoo.mes.wageGroups.constants.StaffFieldsWG.WAGE_GROUP;
import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.LABOR_HOURLY_COST;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffHooks {

    public void saveLaborHourlyCost(final DataDefinition dataDefinition, final Entity entity) {
        boolean individual = entity.getBooleanField(DETERMINED_INDIVIDUAL);
        if (individual) {
            entity.setField("laborHourlyCost", entity.getField(INDIVIDUAL_LABOR_COST));
        } else {
            Entity wageGroup = entity.getBelongsToField(WAGE_GROUP);
            if (wageGroup == null) {
                return;
            }
            entity.setField("laborHourlyCost", wageGroup.getField(LABOR_HOURLY_COST));
        }
    }
}
