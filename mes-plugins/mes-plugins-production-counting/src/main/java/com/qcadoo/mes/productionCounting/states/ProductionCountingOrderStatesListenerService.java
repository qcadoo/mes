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

import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class ProductionCountingOrderStatesListenerService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private ProductionCountingService productionCountingService;

    public void validationOnComplete(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
            checkFinalProductionCountingForOrderCumulated(stateChangeContext);
        } else if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
            checkFinalProductionCountingForOrderForEach(stateChangeContext);
        }
    }

    public void onComplete(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();

        order.setField(OrderFields.DONE_QUANTITY,
                basicProductionCountingService.getProducedQuantityFromBasicProductionCountings(order));

        stateChangeContext.setOwner(order);
    }

    private void checkFinalProductionCountingForOrderCumulated(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();

        if (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE) && !checkIfOrderHasFinalProductionTrackings(order)) {
            stateChangeContext.addValidationError("orders.order.state.allowToClose.failureCumulated");
        }
    }

    private void checkFinalProductionCountingForOrderForEach(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        final List<Entity> technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        int productionTrackingsNumber = 0;
        for (Entity technologyOperationComponent : technologyOperationComponents) {

            if (checkIfOrderHasFinalProductionTrackings(order, technologyOperationComponent)) {
                productionTrackingsNumber++;
            }
        }

        if (order.getBooleanField(OrderFieldsPC.ALLOW_TO_CLOSE)
                && (technologyOperationComponents.size() != productionTrackingsNumber)) {
            stateChangeContext.addValidationError("orders.order.state.allowToClose.failureForEach");
        }
    }

    private boolean checkIfOrderHasFinalProductionTrackings(final Entity order) {
        return checkIfOrderHasFinalProductionTrackings(order, null);
    }

    private boolean checkIfOrderHasFinalProductionTrackings(final Entity order, final Entity technologyOperationComponent) {
        final SearchCriteriaBuilder searchCriteriaBuilder = productionCountingService.getProductionTrackingDD().find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq(ProductionTrackingFields.LAST_TRACKING, true));

        if (technologyOperationComponent != null) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent));
        }

        SearchResult searchResult = searchCriteriaBuilder.list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

    public void validationOnAbandone(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();

        if (checkIfOrderHasProductionTrackings(order)) {
            stateChangeContext.addValidationError("orders.order.state.error.draftProductionTrackingsExists");
        }
    }

    public void validationOnDecline(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();

        if (checkIfOrderHasProductionTrackings(order)) {
            stateChangeContext.addValidationError("orders.order.state.error.draftProductionTrackingsExists");
        }
    }

    private boolean checkIfOrderHasProductionTrackings(final Entity order) {
        SearchResult searchResult = order.getHasManyField(OrderFieldsPC.PRODUCTION_TRACKINGS).find()
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.DRAFT)).list();

        return (searchResult.getTotalNumberOfEntities() > 0);
    }

}
