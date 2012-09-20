package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields.ORDER;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationalTaskHooksOTFO {

    public boolean checkIfOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(ORDER);
        if (order == null) {
            return true;
        }
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology == null) {
            entity.addError(dataDefinition.getField(ORDER), "operationalTasks.operationalTask.order.error.technologyIsNull");
            return false;
        }
        return true;
    }
}
