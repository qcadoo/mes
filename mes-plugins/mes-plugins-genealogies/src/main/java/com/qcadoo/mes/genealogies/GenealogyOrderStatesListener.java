package com.qcadoo.mes.genealogies;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class GenealogyOrderStatesListener extends OrderStateListener {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    AutoGenealogyService autoGenealogyService;

    @Override
    public List<ChangeOrderStateMessage> onCompleted(final Entity newEntity) {
        checkArgument(newEntity != null, "entity is null");
        List<ChangeOrderStateMessage> listOfMessage = new ArrayList<ChangeOrderStateMessage>();
        SearchResult searchResult = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)
                .find().setMaxResults(1).list();
        Entity parameter = null;

        if (searchResult.getEntities().size() > 0) {
            parameter = searchResult.getEntities().get(0);
        }
        if (parameter != null) {
            if (parameter.getField("batchForDoneOrder").toString().equals("02active")) {
                listOfMessage = autoGenealogyService.createGenealogy(newEntity, false);
            } else if (parameter.getField("batchForDoneOrder").toString().equals("03lastUsed")) {
                listOfMessage = autoGenealogyService.createGenealogy(newEntity, true);
            }
        }
        return listOfMessage;
    }
}
