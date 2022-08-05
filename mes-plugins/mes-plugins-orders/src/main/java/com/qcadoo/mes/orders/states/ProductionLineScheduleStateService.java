package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePPSExecutorService;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePSExecutorService;
import com.qcadoo.mes.orders.constants.DurationOfOrderCalculatedOnBasis;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleStateChangeFields;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineScheduleStateService extends BasicStateService implements ProductionLineScheduleServiceMarker {

    @Autowired
    private ProductionLineScheduleStateChangeDescriber productionLineScheduleStateChangeDescriber;

    @Autowired
    private ProductionLineScheduleServicePPSExecutorService productionLineScheduleServicePPSExecutorService;

    @Autowired
    private ProductionLineScheduleServicePSExecutorService productionLineScheduleServicePSExecutorService;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return productionLineScheduleStateChangeDescriber;
    }

    @Autowired
    private ScheduleStateService scheduleStateService;

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                               StateChangeEntityDescriber describer) {
        if (ScheduleStateStringValues.APPROVED.equals(targetState)) {
            entity.setField(ProductionLineScheduleFields.APPROVE_TIME, stateChangeEntity.getDateField(ProductionLineScheduleStateChangeFields.DATE_AND_TIME));
        }

        return entity;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        if (ScheduleStateStringValues.APPROVED.equals(targetState)) {
            updateOrderDates(entity);
        }

        return entity;
    }

    private void updateOrderDates(Entity entity) {
        String durationOfOrderCalculatedOnBasis = entity.getStringField(ProductionLineScheduleFields.DURATION_OF_ORDER_CALCULATED_ON_BASIS);
        for (Entity position : entity.getHasManyField(ProductionLineScheduleFields.POSITIONS)) {
            Entity order = position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
            Entity productionLine = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            scheduleStateService.setOrderDateFrom(order, position.getDateField(ProductionLineSchedulePositionFields.START_TIME), orderFromDB);
            scheduleStateService.setOrderDateTo(order, position.getDateField(ProductionLineSchedulePositionFields.END_TIME), orderFromDB);
            order.setField(OrderFields.PRODUCTION_LINE, productionLine);
            order.getDataDefinition().fastSave(order);
            if (DurationOfOrderCalculatedOnBasis.TIME_CONSUMING_TECHNOLOGY.getStringValue()
                    .equals(durationOfOrderCalculatedOnBasis)) {
                productionLineScheduleServicePSExecutorService.copyPS(entity,
                        order, productionLine);
            } else if (DurationOfOrderCalculatedOnBasis.PLAN_FOR_SHIFT.getStringValue()
                    .equals(durationOfOrderCalculatedOnBasis)) {
                productionLineScheduleServicePPSExecutorService.copyPPS(entity,
                        order, productionLine);
            }
        }
    }
}
