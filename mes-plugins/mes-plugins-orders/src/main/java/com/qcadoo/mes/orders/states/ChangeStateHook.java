package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class ChangeStateHook {

    @Autowired
    private OrderStatesService orderStatesService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void checkState(Entity newEntity) {
        // get old entity from DB
        Entity oldEntity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                newEntity.getId());

        // sprawdzanie czy ten save to rzeczywiscie zmiana stanu
        if (oldEntity != null && oldEntity.getField("state").equals(newEntity.getField("state"))) {
            return;
        }

        orderStatesService.performChangeState(newEntity, oldEntity);
    }
}
