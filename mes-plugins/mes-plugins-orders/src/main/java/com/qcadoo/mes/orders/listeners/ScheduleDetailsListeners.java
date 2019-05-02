package com.qcadoo.mes.orders.listeners;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.orders.constants.ScheduleSortOrder;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ScheduleDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void assignOperationsToWorkstations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity schedule = ((FormComponent) state).getEntity();
        Map<Long, Date> workstationsFinishDates = Maps.newHashMap();
        List<Entity> positions = sortPositions(schedule.getId());
        for (Entity position : positions) {
            List<Entity> workstations = position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);
            for (Entity workstation : workstations) {
                Date finishDate = workstationsFinishDates.get(workstation.getId());
                if (finishDate == null) {
                    finishDate = schedule.getDateField(ScheduleFields.START_TIME);
                }
                Date newFinishDate = Date.from(
                        finishDate.toInstant().plusSeconds(position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME))
                                .plusSeconds(position.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
            }
        }
    }

    private List<Entity> sortPositions(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE)
                .get(scheduleId);
        if (ScheduleSortOrder.DESCENDING.getStringValue().equals(schedule.getStringField(ScheduleFields.SORT_ORDER))) {
            return schedule.getHasManyField(ScheduleFields.POSITIONS).find()
                    .addOrder(SearchOrders.desc(SchedulePositionFields.MACHINE_WORK_TIME)).list().getEntities();
        } else {
            return schedule.getHasManyField(ScheduleFields.POSITIONS).find()
                    .addOrder(SearchOrders.asc(SchedulePositionFields.MACHINE_WORK_TIME)).list().getEntities();
        }
    }

    public void assignWorkersToOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        throw new UnsupportedOperationException();
    }
}
