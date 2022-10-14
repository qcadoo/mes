package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.ProductionLineSchedulePositionFieldsLCNFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionLineSchedulePositionHooksLCNFO {

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public void onSave(final DataDefinition schedulePositionDD, final Entity schedulePosition) {
        if (Objects.isNull(schedulePosition.getId())) {
            return;
        }
        changeChangeovers(schedulePositionDD, schedulePosition);
    }

    private void changeChangeovers(DataDefinition schedulePositionDD, Entity schedulePosition) {
        Entity databaseSchedulePosition = schedulePositionDD.get(schedulePosition.getId());
        Entity productionLineSchedule = schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE);
        Entity oldProductionLine = databaseSchedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE);
        Date oldStart = databaseSchedulePosition.getDateField(ProductionLineSchedulePositionFields.START_TIME);
        Entity productionLine = schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE);
        Date start = schedulePosition.getDateField(ProductionLineSchedulePositionFields.START_TIME);
        if (productionLine == null && oldProductionLine == null) {
            return;
        }
        if (productionLine != null && oldProductionLine == null || productionLine == null || !productionLine.getId().equals(oldProductionLine.getId())) {
            if (oldProductionLine != null) {
                calculateChangeoverForOldPosition(schedulePositionDD, schedulePosition, productionLineSchedule, oldProductionLine, oldStart);
            }
            if (productionLine != null) {
                calculateChangeoverForNewPosition(schedulePositionDD, schedulePosition, productionLineSchedule, productionLine, start);
            }
        } else if (oldStart.compareTo(start) != 0) {
            calculateChangeoverForOldPosition(schedulePositionDD, schedulePosition, productionLineSchedule, oldProductionLine, oldStart);
            calculateChangeoverForNewPosition(schedulePositionDD, schedulePosition, productionLineSchedule, oldProductionLine, start);
        }
    }

    private void calculateChangeoverForNewPosition(DataDefinition schedulePositionDD, Entity schedulePosition, Entity productionLineSchedule, Entity productionLine, Date start) {
        Entity order = schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
        Entity previousPosition = schedulePositionDD.find().add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE, productionLine))
                .createAlias(ProductionLineSchedulePositionFields.ORDER, ProductionLineSchedulePositionFields.ORDER, JoinType.INNER)
                .add(SearchRestrictions.ne(ProductionLineSchedulePositionFields.ORDER + ".id", order.getId()))
                .add(SearchRestrictions.le(ProductionLineSchedulePositionFields.START_TIME, start))
                .addOrder(SearchOrders.desc(ProductionLineSchedulePositionFields.START_TIME)).setMaxResults(1).uniqueResult();

        if (previousPosition != null) {
            Entity changeover = lineChangeoverNormsForOrdersService.getChangeover(previousPosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER), order.getBelongsToField(OrderFields.TECHNOLOGY), productionLine);
            schedulePosition.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, changeover);
        } else {
            schedulePosition.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, null);
        }
        Entity nextPosition = schedulePositionDD.find().add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE, productionLine))
                .createAlias(ProductionLineSchedulePositionFields.ORDER, ProductionLineSchedulePositionFields.ORDER, JoinType.INNER)
                .add(SearchRestrictions.ne(ProductionLineSchedulePositionFields.ORDER + ".id", order.getId()))
                .add(SearchRestrictions.gt(ProductionLineSchedulePositionFields.START_TIME, start))
                .addOrder(SearchOrders.asc(ProductionLineSchedulePositionFields.START_TIME)).setMaxResults(1).uniqueResult();
        if (nextPosition != null) {
            Entity nextChangeover = lineChangeoverNormsForOrdersService.getChangeover(order, nextPosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER).getBelongsToField(OrderFields.TECHNOLOGY), productionLine);
            nextPosition.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, nextChangeover);
            schedulePositionDD.save(nextPosition);
        }
    }

    private void calculateChangeoverForOldPosition(DataDefinition schedulePositionDD, Entity schedulePosition, Entity productionLineSchedule, Entity oldProductionLine, Date oldStart) {
        Entity order = schedulePosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
        Entity oldPreviousPosition = schedulePositionDD.find().add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE, oldProductionLine))
                .createAlias(ProductionLineSchedulePositionFields.ORDER, ProductionLineSchedulePositionFields.ORDER, JoinType.INNER)
                .add(SearchRestrictions.ne(ProductionLineSchedulePositionFields.ORDER + ".id", order.getId()))
                .add(SearchRestrictions.le(ProductionLineSchedulePositionFields.START_TIME, oldStart))
                .addOrder(SearchOrders.desc(ProductionLineSchedulePositionFields.START_TIME)).setMaxResults(1).uniqueResult();
        Entity oldNextPosition = schedulePositionDD.find().add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(ProductionLineSchedulePositionFields.PRODUCTION_LINE, oldProductionLine))
                .add(SearchRestrictions.gt(ProductionLineSchedulePositionFields.START_TIME, oldStart))
                .addOrder(SearchOrders.asc(ProductionLineSchedulePositionFields.START_TIME)).setMaxResults(1).uniqueResult();
        if (oldPreviousPosition != null && oldNextPosition != null) {
            Entity oldNextChangeover = lineChangeoverNormsForOrdersService.getChangeover(oldPreviousPosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER), oldNextPosition.getBelongsToField(ProductionLineSchedulePositionFields.ORDER).getBelongsToField(OrderFields.TECHNOLOGY), oldProductionLine);
            oldNextPosition.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, oldNextChangeover);
            schedulePositionDD.save(oldNextPosition);
        } else if (oldNextPosition != null) {
            oldNextPosition.setField(ProductionLineSchedulePositionFieldsLCNFO.LINE_CHANGEOVER_NORM, null);
            schedulePositionDD.save(oldNextPosition);
        }
    }

}
