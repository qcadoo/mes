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
import com.qcadoo.mes.productionCounting.constants.RecordOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.RecordOperationProductOutComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductionCountingQuantityHooksPC {

    private static final String L_RECORD_OPERATION_IN_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, r.usedQuantity AS usedQuantity FROM #productionCounting_productionRecord pr, #productionCounting_recordOperationProductInComponent r WHERE r.productionRecord.id = pr.id AND pr.id = %s AND r.product.id = %s";

    private static final String L_RECORD_OPERATION_OUT_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, r.usedQuantity AS usedQuantity FROM #productionCounting_productionRecord pr, #productionCounting_recordOperationProductOutComponent r WHERE r.productionRecord.id = pr.id AND pr.id = %s AND r.product.id = %s";

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

        List<Entity> productionRecords = getProductionRecords(order);

        for (Entity productionRecord : productionRecords) {
            BigDecimal quantity = getRecordOperationProductInComponentQuantity(productionRecord, product);

            if (quantity != null) {
                usedQuantity = usedQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return usedQuantity;
    }

    private BigDecimal getRecordOperationProductInComponentQuantity(final Entity productionRecord, final Entity product) {
        Entity result = productionRecord.getDataDefinition()
                .find(getRecordOperationProductInComponentQuantityQuery(productionRecord.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();

        if (result != null) {
            return result.getDecimalField(RecordOperationProductInComponentFields.USED_QUANTITY);
        }

        return null;
    }

    private String getRecordOperationProductInComponentQuantityQuery(final Long productionRecordId, final Long productId) {
        String recordOperationProductInComponentQuantityQuery = String.format(L_RECORD_OPERATION_IN_QUANTITY_QUERY,
                productionRecordId, productId);

        return recordOperationProductInComponentQuantityQuery;
    }

    private BigDecimal getProducedQuantity(final Entity productionCountingQuantity) {
        BigDecimal producedQuantity = BigDecimal.ZERO;

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        List<Entity> productionRecords = getProductionRecords(order);

        for (Entity productionRecord : productionRecords) {
            BigDecimal quantity = getRecordOperationProductOutComponentQuantity(productionRecord, product);

            if (quantity != null) {
                producedQuantity = producedQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return producedQuantity;
    }

    private BigDecimal getRecordOperationProductOutComponentQuantity(final Entity productionRecord, final Entity product) {
        Entity result = productionRecord.getDataDefinition()
                .find(getRecordOperationProductOutComponentQuantityQuery(productionRecord.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();

        if (result != null) {
            return result.getDecimalField(RecordOperationProductOutComponentFields.USED_QUANTITY);
        }

        return null;
    }

    private String getRecordOperationProductOutComponentQuantityQuery(final Long productionRecordId, final Long productId) {
        String recordOperationProductOutComponentQuantityQuery = String.format(L_RECORD_OPERATION_OUT_QUANTITY_QUERY,
                productionRecordId, productId);

        return recordOperationProductOutComponentQuantityQuery;
    }

    private List<Entity> getProductionRecords(final Entity order) {
        return order.getHasManyField(OrderFieldsPC.PRODUCTION_RECORDS).find().list().getEntities();
    }

}
