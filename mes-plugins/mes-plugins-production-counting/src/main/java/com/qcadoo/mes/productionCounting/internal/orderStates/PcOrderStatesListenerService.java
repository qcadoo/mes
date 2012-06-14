/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.internal.orderStates;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class PcOrderStatesListenerService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCompleted(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        if (CUMULATED.getStringValue().equals(typeOfProductionRecording)) {
            checkFinalProductionCountingForOrderCumulated(stateChangeContext);
        } else if (FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            checkFinalProductionCountingForOrderForEach(stateChangeContext);
        }
    }

    private void checkFinalProductionCountingForOrderCumulated(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final SearchResult productionRecordingsResult = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.eq("lastRecord", true)).list();

        if (order.getBooleanField("allowToClose") && productionRecordingsResult.getTotalNumberOfEntities() == 0) {
            stateChangeContext.addMessage("orders.order.state.allowToClose.failure", StateMessageType.FAILURE);
        }
    }

    private void checkFinalProductionCountingForOrderForEach(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final List<Entity> operations = order.getTreeField("technologyInstanceOperationComponents");
        int numberOfRecord = 0;
        for (Entity operation : operations) {
            final SearchResult productionRecordingsResult = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                    .add(SearchRestrictions.belongsTo("order", order))
                    .add(SearchRestrictions.belongsTo("technologyInstanceOperationComponent", operation))
                    .add(SearchRestrictions.eq("lastRecord", true)).list();
            if (productionRecordingsResult.getTotalNumberOfEntities() > 0) {
                numberOfRecord++;
            }
        }
        if (order.getBooleanField("allowToClose") && operations.size() != numberOfRecord) {
            stateChangeContext.addMessage("orders.order.state.allowToClose.failure", StateMessageType.FAILURE);
        }
    }

}
