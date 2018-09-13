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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionCountingQuantityHooksPC {

    private static final String L_TRACKING_OPERATION_IN_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, t.usedQuantity AS usedQuantity FROM #productionCounting_productionTracking pt, #productionCounting_trackingOperationProductInComponent t WHERE t.productionTracking.id = pt.id AND pt.id = %s AND t.product.id = %s";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean onDelete(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        List<Entity> productionTrackings = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order)).list().getEntities();
        for (Entity tracking : productionTrackings) {
            String state = tracking.getStringField(ProductionTrackingFields.STATE);
            if (!ProductionTrackingState.DECLINED.getStringValue().equals(state)) {
                Entity trackingInComponent = getTrackingOperationProductInComponent(tracking, product);
                if (trackingInComponent != null
                        && trackingInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY) != null) {
                    productionCountingQuantity.addGlobalError("productionCounting.productionCountingQuantity.onDelete.error");
                    return false;
                }
            }
        }
        return true;
    }

    private Entity getTrackingOperationProductInComponent(final Entity productionTracking, final Entity product) {
        return productionTracking.getDataDefinition()
                .find(String.format(L_TRACKING_OPERATION_IN_QUANTITY_QUERY, productionTracking.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();
    }

}
