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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.productionCounting.SetTrackingOperationProductsComponentsService;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.hooks.helpers.AbstractPlannedQuantitiesCounter;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class TrackingOperationProductOutComponentHooks extends AbstractPlannedQuantitiesCounter {

    @Autowired
    private SetTrackingOperationProductsComponentsService setTrackingOperationProductsComponents;

    public TrackingOperationProductOutComponentHooks() {
        super(ProductionCountingQuantityRole.PRODUCED);
    }

    public void onView(final DataDefinition trackingOperationProductOutComponentDD,
            final Entity trackingOperationProductOutComponent) {
        fillPlannedQuantity(trackingOperationProductOutComponent);
    }

    private void fillPlannedQuantity(final Entity trackingOperationProductOutComponent) {
        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.PLANNED_QUANTITY,
                getPlannedQuantity(trackingOperationProductOutComponent));
    }

    public void onSave(final DataDefinition trackingOperationProductOutComponentDD,
             Entity trackingOperationProductOutComponent) {

        Entity productionTracking = trackingOperationProductOutComponent.getBelongsToField("productionTracking");
        BigDecimal usedQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);
        trackingOperationProductOutComponent = setTrackingOperationProductsComponents.fillTrackingOperationProductOutComponent(productionTracking, trackingOperationProductOutComponent, usedQuantity);

        List<Entity> setTrackingOperationProductsInComponents = trackingOperationProductOutComponent.getHasManyField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS);
        setTrackingOperationProductsInComponents.stream().forEach(entity -> {
            entity.getDataDefinition().save(entity);
        });
    }

}
