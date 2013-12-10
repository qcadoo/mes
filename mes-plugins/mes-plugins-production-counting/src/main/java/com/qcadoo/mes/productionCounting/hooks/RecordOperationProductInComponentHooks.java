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

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.*;
import static com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields.PRODUCTION_RECORD;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class RecordOperationProductInComponentHooks {

    @Autowired
    private NumberService numberService;

    public void fillPlannedQuantity(final DataDefinition recordOperationProductInComponentDD,
            final Entity recordOperationProductInComponent) {
        recordOperationProductInComponent.setField(PLANNED_QUANTITY,
                numberService.setScale(getPlannedQuantity(recordOperationProductInComponent)));
    }

    private BigDecimal getPlannedQuantity(final Entity recordOperationProductInComponent) {
        BigDecimal plannedQuantity = BigDecimal.ZERO;

        Entity productionRecord = recordOperationProductInComponent.getBelongsToField(PRODUCTION_RECORD);
        Entity product = recordOperationProductInComponent.getBelongsToField(PRODUCT);

        Entity order = productionRecord.getBelongsToField(ORDER);
        Entity technologyInstanceOperationComponent = productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

        List<Entity> productionCountingQuantities = Lists.newArrayList();

        if (technologyInstanceOperationComponent == null) {
            productionCountingQuantities = order.getHasManyField(PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.isNotNull(OPERATION_PRODUCT_IN_COMPONENT))
                    .add(SearchRestrictions.belongsTo(PRODUCT, product)).list().getEntities();
        } else {
            Entity technologyOperationComponent = technologyInstanceOperationComponent
                    .getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);

            Entity operationProductInComponent = getOperationProductInComponent(technologyOperationComponent, product);

            if (operationProductInComponent != null) {
                productionCountingQuantities = order.getHasManyField(PRODUCTION_COUNTING_QUANTITIES).find()
                        .add(SearchRestrictions.belongsTo(OPERATION_PRODUCT_IN_COMPONENT, operationProductInComponent))
                        .add(SearchRestrictions.belongsTo(PRODUCT, product)).list().getEntities();
            }
        }

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            BigDecimal productionCountingQuantityPlannedQuantity = productionCountingQuantity.getDecimalField(PLANNED_QUANTITY);

            if (productionCountingQuantityPlannedQuantity != null) {
                plannedQuantity = plannedQuantity.add(productionCountingQuantityPlannedQuantity, numberService.getMathContext());
            }
        }

        return plannedQuantity;
    }

    private Entity getOperationProductInComponent(final Entity technologyOperationComponent, final Entity product) {
        return technologyOperationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(PRODUCT, product)).setMaxResults(1).uniqueResult();
    }

}
