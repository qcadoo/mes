package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleStateChangeFields;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineScheduleStateService extends BasicStateService implements ProductionLineScheduleServiceMarker {

    @Autowired
    private ProductionLineScheduleStateChangeDescriber productionLineScheduleStateChangeDescriber;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return productionLineScheduleStateChangeDescriber;
    }

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                               StateChangeEntityDescriber describer) {
        if (ScheduleStateStringValues.APPROVED.equals(targetState)) {
            entity.setField(ProductionLineScheduleFields.APPROVE_TIME, stateChangeEntity.getDateField(ProductionLineScheduleStateChangeFields.DATE_AND_TIME));
        }

        return entity;
    }


}
