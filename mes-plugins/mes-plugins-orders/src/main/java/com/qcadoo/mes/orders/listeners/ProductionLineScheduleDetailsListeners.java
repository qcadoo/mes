package com.qcadoo.mes.orders.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.mes.orders.states.ProductionLineScheduleServiceMarker;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionLineScheduleDetailsListeners {

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ProductionLineScheduleServiceMarker.class, view, args);
    }

    @Transactional
    public void generatePlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(ProductionLineScheduleFields.ORDERS);
        List<Entity> orders = ordersGrid.getEntities();
        FormComponent formComponent = (FormComponent) state;
        Entity schedule = formComponent.getEntity();
        DataDefinition productionLineSchedulePositionDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE_POSITION);
        List<Entity> positions = Lists.newArrayList();
        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            if (technology == null) {
                continue;
            }
            Entity schedulePosition = createSchedulePosition(schedule, productionLineSchedulePositionDD, order);
            positions.add(schedulePosition);
        }

        schedule.setField(ProductionLineScheduleFields.POSITIONS, positions);
        schedule = schedule.getDataDefinition().save(schedule);
        formComponent.setEntity(schedule);
        view.addMessage("orders.info.productionLineSchedulePositionsGenerated", ComponentState.MessageType.SUCCESS);
    }

    private Entity createSchedulePosition(Entity schedule, DataDefinition productionLineSchedulePositionDD, Entity order) {
        Entity schedulePosition = productionLineSchedulePositionDD.create();
        schedulePosition.setField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE, schedule);
        schedulePosition.setField(ProductionLineSchedulePositionFields.ORDER, order);
        schedulePosition.setField(ProductionLineSchedulePositionFields.PRODUCTION_LINE, order.getBelongsToField(OrderFields.PRODUCTION_LINE));
        return schedulePosition;
    }
}
