/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting.states;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class PcOrderStatesListenerService {

    @Autowired
    private ProductionCountingService productionCountingService;

    public void onCompleted(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
            checkFinalProductionCountingForOrderCumulated(stateChangeContext);
        } else if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
            checkFinalProductionCountingForOrderForEach(stateChangeContext);
        }
    }

    private void checkFinalProductionCountingForOrderCumulated(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final SearchResult productionRecordingsResult = productionCountingService.getProductionRecordDD().find()
                .add(SearchRestrictions.belongsTo(ProductionRecordFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionRecordFields.LAST_RECORD, true)).list();

        if (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) && productionRecordingsResult.getTotalNumberOfEntities() == 0) {
            stateChangeContext.addMessage("orders.order.state.allowToClose.failure", StateMessageType.FAILURE);
        }
    }

    private void checkFinalProductionCountingForOrderForEach(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final List<Entity> operations = order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);
        int numberOfRecord = 0;
        for (Entity operation : operations) {
            final SearchResult productionRecordingsResult = productionCountingService.getProductionRecordDD().find()
                    .add(SearchRestrictions.belongsTo(ProductionRecordFields.ORDER, order))
                    .add(SearchRestrictions.belongsTo(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT, operation))
                    .add(SearchRestrictions.eq(ProductionRecordFields.LAST_RECORD, true)).list();
            if (productionRecordingsResult.getTotalNumberOfEntities() > 0) {
                numberOfRecord++;
            }
        }
        if (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) && operations.size() != numberOfRecord) {
            stateChangeContext.addMessage("orders.order.state.allowToClose.failure", StateMessageType.FAILURE);
        }
    }

}
