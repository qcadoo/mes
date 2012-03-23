/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.states.ChangeOrderStateMessage;
import com.qcadoo.mes.orders.states.OrderStateListener;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionCountingOrderStatesListener extends OrderStateListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public List<ChangeOrderStateMessage> onCompleted(final Entity newEntity) {
        checkArgument(newEntity != null, "entity is null");
        List<ChangeOrderStateMessage> listOfMessage = Lists.newArrayList();

        Entity order = newEntity.getDataDefinition().get(newEntity.getId());
        ChangeOrderStateMessage message = null;
        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        if (CUMULATED.getStringValue().equals(typeOfProductionRecording)) {
            message = checkFinalProductionCountingForOrderCumulated(order);
        } else if (FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            message = checkFinalProductionCountingForOrderForEach(order);
        }
        if (message != null) {
            listOfMessage.add(message);
        }
        return listOfMessage;
    }

    private ChangeOrderStateMessage checkFinalProductionCountingForOrderCumulated(final Entity order) {
        Boolean allowToClose = (Boolean) order.getField("allowToClose");
        List<Entity> productionRecordings = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.eq("lastRecord", true)).list()
                .getEntities();

        if (allowToClose != null && allowToClose && productionRecordings.isEmpty()) {
            return ChangeOrderStateMessage.error("orders.order.state.allowToClose.failure");
        }
        return null;
    }

    private ChangeOrderStateMessage checkFinalProductionCountingForOrderForEach(final Entity order) {
        Boolean allowToClose = (Boolean) order.getField("allowToClose");
        List<Entity> operations = order.getTreeField("orderOperationComponents");
        Integer numberOfRecord = 0;
        for (Entity operation : operations) {
            List<Entity> productionRecordings = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                    .add(SearchRestrictions.belongsTo("order", order))
                    .add(SearchRestrictions.belongsTo("orderOperationComponent", operation))
                    .add(SearchRestrictions.eq("lastRecord", true)).list().getEntities();
            if (!productionRecordings.isEmpty()) {
                numberOfRecord++;
            }
        }
        if (allowToClose != null && allowToClose && operations.size() != numberOfRecord) {
            return ChangeOrderStateMessage.error("orders.order.state.allowToClose.failure");
        }
        return null;
    }

}
