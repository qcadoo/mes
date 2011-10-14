package com.qcadoo.mes.genealogies;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class GenealogyOrderStatesListener extends OrderStateListener {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Override
    public List<ChangeOrderStateMessage> onCompleted(final Entity newEntity) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                newEntity.getId());
        // if (order == null) {
        // state.addMessage(
        // translationService.translate("qcadooView.message.entityNotFound", viewDefinitionState.getLocale()),
        // MessageType.FAILURE);
        // } else {
        // boolean inProgressState = Boolean.parseBoolean(args[0]);
        // if (!inProgressState) {
        // SearchResult searchResult = dataDefinitionService
        // .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).find().setMaxResults(1).list();
        // Entity parameter = null;
        // if (searchResult.getEntities().size() > 0) {
        // parameter = searchResult.getEntities().get(0);
        // }
        // if (parameter != null) {
        // if (parameter.getField("batchForDoneOrder").toString().equals("02active")) {
        // createGenealogy(state, order, false);
        // } else if (parameter.getField("batchForDoneOrder").toString().equals("03lastUsed")) {
        // createGenealogy(state, order, true);
        // }
        // }
        // }
        // }
        return null;
    }
}
