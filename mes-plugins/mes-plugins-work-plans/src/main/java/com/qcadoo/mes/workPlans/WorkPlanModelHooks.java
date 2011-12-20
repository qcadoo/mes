package com.qcadoo.mes.workPlans;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WorkPlanModelHooks {

    public final boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);
        entity.setField("worker", null);
        return true;
    }
}
