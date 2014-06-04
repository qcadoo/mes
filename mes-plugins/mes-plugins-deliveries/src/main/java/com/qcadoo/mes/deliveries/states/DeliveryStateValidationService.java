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
package com.qcadoo.mes.deliveries.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryStateValidationService {

    private static final String ENTITY_IS_NULL = "entity is null";

    public void validationOnApproved(final StateChangeContext stateChangeContext) {
        final List<String> references = Lists.newArrayList(DELIVERY_DATE);

        checkRequired(references, stateChangeContext);
    }

    public void validationOnReceived(final StateChangeContext stateChangeContext) {
        checkDeliveredQuantity(stateChangeContext);
    }

    public void checkRequired(final List<String> fieldNames, final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);
        final Entity stateChangeEntity = stateChangeContext.getOwner();
        for (String fieldName : fieldNames) {
            if (stateChangeEntity.getField(fieldName) == null) {
                stateChangeContext.addFieldValidationError(fieldName, "deliveries.delivery.deliveryStates.fieldRequired");
            }
        }
    }

    public void checkDeliveredQuantity(final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);
        final Entity stateChangeEntity = stateChangeContext.getOwner();
        List<Entity> deliveredProducts = stateChangeEntity.getHasManyField(DELIVERED_PRODUCTS);
        boolean deliveredProductHasNull = false;
        if (deliveredProducts.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.deliveredProductsList.isEmpty");
        }
        StringBuffer listOfProductNumber = new StringBuffer();
        for (Entity delivProd : deliveredProducts) {
            if (delivProd.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY) == null) {
                deliveredProductHasNull = true;
                listOfProductNumber.append(delivProd.getBelongsToField(DeliveredProductFields.PRODUCT).getStringField(
                        ProductFields.NUMBER));
                listOfProductNumber.append(", ");
            }
        }
        if (deliveredProductHasNull) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.deliveredQuantity.isRequired",
                    listOfProductNumber.toString());
            stateChangeContext.addMessage("deliveries.deliveredProducts.deliveredQuantity.isRequired", StateMessageType.FAILURE,
                    false, listOfProductNumber.toString());
        }
    }

}
