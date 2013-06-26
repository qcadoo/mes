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
package com.qcadoo.mes.productionCounting.hooks;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductionCountingQuantityHooksPC {

    private static final String L_TRACKING_OPERATION_IN_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, t.usedQuantity AS usedQuantity FROM #productionCounting_productionTracking pt, #productionCounting_trackingOperationProductInComponent t WHERE t.productionTracking.id = pt.id AND pt.id = %s AND t.product.id = %s";

    private static final String L_TRACKING_OPERATION_OUT_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, t.usedQuantity AS usedQuantity FROM #productionCounting_productionTracking pt, #productionCounting_trackingOperationProductOutComponent t WHERE t.productionTracking.id = pt.id AND pt.id = %s AND t.product.id = %s";

    @Autowired
    private NumberService numberService;

    public void onView(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        fillUsedQuantity(productionCountingQuantityDD, productionCountingQuantity);
        fillProducedQuantity(productionCountingQuantityDD, productionCountingQuantity);
    }

    private void fillUsedQuantity(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY,
                numberService.setScale(getUsedQuantity(productionCountingQuantity)));
    }

    private void fillProducedQuantity(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCED_QUANTITY,
                numberService.setScale(getProducedQuantity(productionCountingQuantity)));
    }

    private BigDecimal getUsedQuantity(final Entity productionCountingQuantity) {
        BigDecimal usedQuantity = BigDecimal.ZERO;

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        List<Entity> productionTrackings = getProductionTrackings(order);

        for (Entity productionTracking : productionTrackings) {
            BigDecimal quantity = getTrackingOperationProductInComponentQuantity(productionTracking, product);

            if (quantity != null) {
                usedQuantity = usedQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return usedQuantity;
    }

    private BigDecimal getTrackingOperationProductInComponentQuantity(final Entity productionTracking, final Entity product) {
        Entity result = productionTracking.getDataDefinition()
                .find(getTrackingOperationProductInComponentQuantityQuery(productionTracking.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();

        if (result != null) {
            return result.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
        }

        return null;
    }

    private String getTrackingOperationProductInComponentQuantityQuery(final Long productionTrackingId, final Long productId) {
        String trackingOperationProductInComponentQuantityQuery = String.format(L_TRACKING_OPERATION_IN_QUANTITY_QUERY,
                productionTrackingId, productId);

        return trackingOperationProductInComponentQuantityQuery;
    }

    private BigDecimal getProducedQuantity(final Entity productionCountingQuantity) {
        BigDecimal producedQuantity = BigDecimal.ZERO;

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        List<Entity> productionTrackings = getProductionTrackings(order);

        for (Entity productionTracking : productionTrackings) {
            BigDecimal quantity = getTrackingOperationProductOutComponentQuantity(productionTracking, product);

            if (quantity != null) {
                producedQuantity = producedQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return producedQuantity;
    }

    private BigDecimal getTrackingOperationProductOutComponentQuantity(final Entity productionTracking, final Entity product) {
        Entity result = productionTracking.getDataDefinition()
                .find(getTrackingOperationProductOutComponentQuantityQuery(productionTracking.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();

        if (result != null) {
            return result.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
        }

        return null;
    }

    private String getTrackingOperationProductOutComponentQuantityQuery(final Long productionTrackingId, final Long productId) {
        String trackingOperationProductOutComponentQuantityQuery = String.format(L_TRACKING_OPERATION_OUT_QUANTITY_QUERY,
                productionTrackingId, productId);

        return trackingOperationProductOutComponentQuantityQuery;
    }

    private List<Entity> getProductionTrackings(final Entity order) {
        return order.getHasManyField(OrderFieldsPC.PRODUCTION_TRACKINGS).find().list().getEntities();
    }

}
