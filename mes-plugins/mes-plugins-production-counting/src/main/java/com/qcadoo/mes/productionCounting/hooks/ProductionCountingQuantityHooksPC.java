/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionCountingQuantityHooksPC {

    private static final String L_TRACKING_OPERATION_IN_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, t.id as id, t.usedQuantity AS usedQuantity FROM #productionCounting_productionTracking pt, #productionCounting_trackingOperationProductInComponent t WHERE t.productionTracking.id = pt.id AND pt.id = %s AND t.product.id = %s";

    public static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean onDelete(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {

        boolean canRemove = true;
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity toc = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        SearchCriteriaBuilder criteriaBuilder = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING)
                .find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, OrdersConstants.PLUGIN_IDENTIFIER,
                        OrdersConstants.MODEL_ORDER, order.getId()));

        String type = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(type) && Objects.nonNull(toc)) {
            criteriaBuilder.add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        }

        List<Entity> productionTrackings = criteriaBuilder.list().getEntities();

        for (Entity tracking : productionTrackings) {
            String state = tracking.getStringField(ProductionTrackingFields.STATE);
            if (!ProductionTrackingState.DECLINED.getStringValue().equals(state)) {
                Entity trackingInComponent = getTrackingOperationProductInComponent(tracking, product);
                if (canRemove && trackingInComponent != null
                        && trackingInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY) != null) {
                    productionCountingQuantity.addGlobalError("productionCounting.productionCountingQuantity.onDelete.error");
                    canRemove = false;
                }
            }
        }

        if (canRemove) {
            for (Entity tracking : productionTrackings) {
                String state = tracking.getStringField(ProductionTrackingFields.STATE);
                if (ProductionTrackingState.DRAFT.getStringValue().equals(state)) {
                    Entity trackingInComponent = getTrackingOperationProductInComponent(tracking, product);
                    if (Objects.nonNull(trackingInComponent) && Objects.nonNull(trackingInComponent.getLongField(L_ID))) {
                        dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT).delete(
                                trackingInComponent.getLongField(L_ID));
                    }
                }
            }
        }

        return canRemove;
    }

    private Entity getTrackingOperationProductInComponent(final Entity productionTracking, final Entity product) {
        return productionTracking.getDataDefinition()
                .find(String.format(L_TRACKING_OPERATION_IN_QUANTITY_QUERY, productionTracking.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();
    }

}
