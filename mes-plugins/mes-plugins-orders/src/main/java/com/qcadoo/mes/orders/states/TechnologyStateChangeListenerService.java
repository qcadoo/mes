package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechnologyStateChangeListenerService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void deleteCheckedTechnologyFromOrder(final StateChangeContext stateChangeContext) {
        final Entity technology = stateChangeContext.getOwner();
        final DataDefinition orderDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ORDER);
        final List<Entity> ordersList = orderDD.find().add(SearchRestrictions.belongsTo(TECHNOLOGY, technology)).list()
                .getEntities();

        StringBuilder ordersNumberList = new StringBuilder();
        for (Entity order : ordersList) {
            if (ordersNumberList.length() != 0) {
                ordersNumberList.append(", ");
            }
            order.setField(TECHNOLOGY, null);
            orderDD.save(order);
            ordersNumberList.append("{");
            ordersNumberList.append(order.getStringField(NUMBER));
            ordersNumberList.append("}");
        }
        if (!ordersList.isEmpty()) {
            stateChangeContext.addMessage("orders.order.technology.removed", StateMessageType.INFO, ordersNumberList.toString());
        }
    }

}
