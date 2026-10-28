/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.service.ReservationsService;
import com.qcadoo.mes.materialFlowResources.service.ResourceReservationsService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class PositionModelHooks {

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private ResourceReservationsService resourceReservationsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition positionDD, final Entity position) {
        if (position.getId() != null) {
            reservationsService.updateReservationFromDocumentPosition(position);
        }
        if (position.isCopied() && !validateAvailableQuantity(position.getBelongsToField(PositionFields.DOCUMENT), position)) {
            position.addError(positionDD.getField(PositionFields.QUANTITY), "materialFlowResources.stocktaking.document.quantity.notEnoughResources", position.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER));
        }
    }

    private boolean validateAvailableQuantity(Entity document, Entity position) {
        String type = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.isOutbound(type)) {
            Entity location = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            boolean enabled = location.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION);
            if (enabled) {
                BigDecimal availableQuantity = getAvailableQuantityForProductAndLocation(
                        position.getBelongsToField(PositionFields.PRODUCT), location);
                BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
                return availableQuantity != null && quantity.compareTo(availableQuantity) <= 0;
            }
        }
        return true;
    }

    private BigDecimal getAvailableQuantityForProductAndLocation(Entity product, Entity location) {
        Entity resourceStockDto = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO)
                .find().add(SearchRestrictions.eq(ResourceStockDtoFields.PRODUCT_ID, product.getId().intValue()))
                .add(SearchRestrictions.eq(ResourceStockDtoFields.LOCATION_ID, location.getId().intValue())).setMaxResults(1)
                .uniqueResult();
        if (Objects.isNull(resourceStockDto)) {
            return BigDecimal.ZERO;
        }
        return BigDecimalUtils.convertNullToZero(resourceStockDto.getDecimalField(ResourceStockDtoFields.AVAILABLE_QUANTITY));
    }

    public void onCopy(final DataDefinition positionDD, final Entity position) {
        position.setField(PositionFields.RESOURCE, null);
        position.setCopied(true);
    }

    public void onCreate(final DataDefinition positionDD, final Entity position) {
        reservationsService.createReservationFromDocumentPosition(position);
    }

    public boolean onDelete(final DataDefinition positionDD, final Entity position) {
        resourceReservationsService.updateResourceQuantites(position,
                position.getDecimalField(ReservationFields.QUANTITY).negate());
        return true;
    }

}
