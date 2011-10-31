package com.qcadoo.mes.productionCounting.internal.states;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionCountingChangeStateHook {

    public void changedState(final DataDefinition dataDefinition, final Entity entity) {
        if (entity == null) {
            return;
        }
        if (entity.getId() == null) {
            return;
        }
        Entity oldEntity = dataDefinition.get(entity.getId());

        if (oldEntity == null) {
            return;
        }

    }
}
