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

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.constants.RecordOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class RecordOperationProductOutComponentHooks {

    @Autowired
    private NumberService numberService;

    public void onView(final DataDefinition recordOperationProductOutComponentDD, final Entity recordOperationProductOutComponent) {
        fillPlannedQuantity(recordOperationProductOutComponentDD, recordOperationProductOutComponent);
    }

    private void fillPlannedQuantity(final DataDefinition recordOperationProductOutComponentDD,
            final Entity recordOperationProductOutComponent) {
        recordOperationProductOutComponent.setField(RecordOperationProductOutComponentFields.PLANNED_QUANTITY,
                numberService.setScale(getPlannedQuantity(recordOperationProductOutComponent)));
    }

    private BigDecimal getPlannedQuantity(final Entity recordOperationProductOutComponent) {
        BigDecimal plannedQuantity = BigDecimal.ZERO;

        Entity productionRecord = recordOperationProductOutComponent
                .getBelongsToField(RecordOperationProductOutComponentFields.PRODUCTION_RECORD);
        Entity product = recordOperationProductOutComponent.getBelongsToField(RecordOperationProductOutComponentFields.PRODUCT);

        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        Entity technologyOperationComponent = productionRecord
                .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> productionCountingQuantities = Lists.newArrayList();

        if (technologyOperationComponent == null) {
            productionCountingQuantities = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.isNotNull(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT))
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).list().getEntities();
        } else {
            Entity operationProductOutComponent = getOperationProductOutComponent(technologyOperationComponent, product);

            if (operationProductOutComponent != null) {
                productionCountingQuantities = order
                        .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                        .find()
                        .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT,
                                operationProductOutComponent))
                        .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).list()
                        .getEntities();
            }
        }

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            BigDecimal productionCountingQuantityPlannedQuantity = productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

            if (productionCountingQuantityPlannedQuantity != null) {
                plannedQuantity = plannedQuantity.add(productionCountingQuantityPlannedQuantity, numberService.getMathContext());
            }
        }

        return plannedQuantity;
    }

    private Entity getOperationProductOutComponent(final Entity technologyOperationComponent, final Entity product) {
        return technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)
                .find().add(SearchRestrictions.belongsTo(OperationProductOutComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

}
