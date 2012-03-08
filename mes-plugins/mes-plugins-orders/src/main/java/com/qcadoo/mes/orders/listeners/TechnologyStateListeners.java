package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.technologies.constants.TechnologyState.CHECKED;
import static com.qcadoo.mes.technologies.constants.TechnologyState.DRAFT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.MessageHolder;
import com.qcadoo.mes.technologies.states.TechnologyStateAfterChangeNotifierService.AfterStateChangeListener;
import com.qcadoo.mes.technologies.states.TechnologyStateUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;

@Component
public class TechnologyStateListeners implements AfterStateChangeListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void wasChanged(ComponentState state, Entity technology, TechnologyState oldState) {
        Entity existsTechnology = technology.getDataDefinition().get(technology.getId());
        TechnologyState existsState = TechnologyStateUtils.getStateFromField(existsTechnology.getStringField(STATE));
        if (oldState.equals(CHECKED) && existsState.equals(DRAFT)) {
            changedStateFromCheckedToDraft(state, existsTechnology);
        }
        if (existsState.equals(TechnologyState.CHECKED)) {
            state.addMessage("orders.order.technology.info.aboutChecked", MessageType.INFO, false);
        }
    }

    private void changedStateFromCheckedToDraft(ComponentState state, final Entity technology) {
        MessageHolder message = deleteCheckedTechnologyFromOrder(technology);
        if (message != null) {
            state.addMessage(message.getMessageKey(), message.getMessageType(), false);
        }
    }

    private MessageHolder deleteCheckedTechnologyFromOrder(final Entity technology) {
        DataDefinition orderDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ORDER);
        List<Entity> ordersList = orderDD.find().add(SearchRestrictions.belongsTo(TECHNOLOGY, technology)).list().getEntities();
        StringBuilder ordersNumberList = new StringBuilder();
        for (Entity order : ordersList) {
            order.setField(TECHNOLOGY, null);
            orderDD.save(order);
            ordersNumberList.append(" {");
            ordersNumberList.append(order.getStringField(NUMBER));
            ordersNumberList.append("}");
        }
        if (ordersList.isEmpty()) {
            return null;
        }
        return MessageHolder.info("orders.order.technology.removed", ordersNumberList.toString());
    }

}
