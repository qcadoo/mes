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
package com.qcadoo.mes.warehouseMinimalState;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class WarehouseMinimalStateHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfLowerThanMinimum(long productId, BigDecimal quantity, BigDecimal minimumState) {
        return quantity.compareTo(minimumState) == -1;
    }

    public BigDecimal getOrderedQuantityForProductAndLocation(final Long warehouse, final Long product) {
        String query = "select COALESCE(sum(op.orderedQuantity),0) as  orderedQuantity from #deliveries_orderedProduct op, "
                + "#deliveries_delivery del where op.delivery.id=del.id and op.product.id=:product and del.location.id = :warehouseId "
                + "and del.state in ('01draft', '02prepared', '03duringCorrection', '05approved') and del.active=true";
        return getResourceStockDtoDD().find(query).setParameter("warehouseId", warehouse).setParameter("product", product)
                .setMaxResults(1).uniqueResult().getDecimalField("orderedQuantity");
    }

    // WARNING unused argument is used in aspect in plugin integration
    public List<Entity> getWarehouseStockWithTooSmallMinState(final Entity warehouse, final List<Entity> product) {

        String query = "select stock from #materialFlowResources_resourceStockDto as stock where stock.minimumState > 0"
                + " and stock.location_id = :warehouseId";
        return getResourceStockDtoDD().find(query).setParameter("warehouseId", warehouse.getId().intValue()).list().getEntities();
    }

    private DataDefinition getResourceStockDtoDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO);
    }
}
