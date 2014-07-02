/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.deliveriesToMaterialFlow.states;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DocumentFieldsDTMF;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class DeliveryStateServiceMF {

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private NumberService numberService;

    public void createDocumentsForTheReceivedProducts(final StateChangeContext stateChangeContext) {
        final Entity delivery = stateChangeContext.getOwner();

        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder();
        documentBuilder.receipt(location);
        documentBuilder.setField(DocumentFieldsDTMF.DELIVERY, delivery);

        for (Entity deliveredProduct : deliveredProducts) {

            Optional<BigDecimal> quantity = Optional.fromNullable(deliveredProduct
                    .getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
            Optional<BigDecimal> damagedQuantity = Optional.fromNullable(deliveredProduct
                    .getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY));

            BigDecimal positionQuantity = quantity.get().subtract(damagedQuantity.or(BigDecimal.ZERO),
                    numberService.getMathContext());
            if (positionQuantity.compareTo(BigDecimal.ZERO) > 0) {
                documentBuilder.addPosition(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT), positionQuantity,
                        deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT), null, null, null);
            }
        }

        documentBuilder.setAccepted().build();
  }

}
