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
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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
        final SearchResult result = productionCountingService.getProductionTrackingDD().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionTrackingFields.LAST_TRACKING, true)).list();

        if (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) && result.getTotalNumberOfEntities() == 0) {
            stateChangeContext.addMessage("orders.order.state.allowToClose.failure", StateMessageType.FAILURE);
        }
    }

    private void checkFinalProductionCountingForOrderForEach(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        final List<Entity> technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        int trackingsNumber = 0;
        for (Entity technologyOperationComponent : technologyOperationComponents) {
            final SearchResult result = productionCountingService
                    .getProductionTrackingDD()
                    .find()
                    .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                    .add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                            technologyOperationComponent))
                    .add(SearchRestrictions.eq(ProductionTrackingFields.LAST_TRACKING, true)).list();
            if (result.getTotalNumberOfEntities() > 0) {
                trackingsNumber++;
            }
        }
        if (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) && technologyOperationComponents.size() != trackingsNumber) {
            stateChangeContext.addMessage("orders.order.state.allowToClose.failure", StateMessageType.FAILURE);
        }
    }

}
