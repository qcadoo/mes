package com.qcadoo.mes.orders.states;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePPSExecutorService;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePSExecutorService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.hooks.OrderHooks;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ProductionLineScheduleStateService extends BasicStateService implements ProductionLineScheduleServiceMarker {

    @Autowired
    private ProductionLineScheduleStateChangeDescriber productionLineScheduleStateChangeDescriber;

    @Autowired
    private ProductionLineScheduleServicePPSExecutorService productionLineScheduleServicePPSExecutorService;

    @Autowired
    private ProductionLineScheduleServicePSExecutorService productionLineScheduleServicePSExecutorService;

    @Autowired
    private OrderHooks orderHooks;

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

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        if (ScheduleStateStringValues.APPROVED.equals(targetState)) {
            updateOrderDates(entity);
        }

        return entity;
    }

    public void setOrderDateTo(Entity order, Date endTime, Entity orderFromDB) {
        Date finishDateDB = new Date();
        if (orderFromDB.getDateField(OrderFields.FINISH_DATE) != null) {
            finishDateDB = orderFromDB.getDateField(OrderFields.FINISH_DATE);
        }
        if (!finishDateDB.equals(endTime)) {
            order.setField(OrderFields.FINISH_DATE, endTime);
            orderHooks.copyEndDate(order.getDataDefinition(), order);
        }
    }

    public void setOrderDateFrom(Entity order, Date startTime, Entity orderFromDB) {
        Date startDateDB = new Date();
        if (orderFromDB.getDateField(OrderFields.START_DATE) != null) {
            startDateDB = orderFromDB.getDateField(OrderFields.START_DATE);
        }
        if (!startDateDB.equals(startTime)) {
            order.setField(OrderFields.START_DATE, startTime);
            orderHooks.copyStartDate(order.getDataDefinition(), order);
        }
    }

    private void updateOrderDates(Entity entity) {
        String durationOfOrderCalculatedOnBasis = entity.getStringField(ProductionLineScheduleFields.DURATION_OF_ORDER_CALCULATED_ON_BASIS);
        for (Entity position : entity.getHasManyField(ProductionLineScheduleFields.POSITIONS)) {
            Entity order = position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
            Entity productionLine = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            setOrderDateFrom(order, position.getDateField(ProductionLineSchedulePositionFields.START_TIME), orderFromDB);
            setOrderDateTo(order, position.getDateField(ProductionLineSchedulePositionFields.END_TIME), orderFromDB);
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
