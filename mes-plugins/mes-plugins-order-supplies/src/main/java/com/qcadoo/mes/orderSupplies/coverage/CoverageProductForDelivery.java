/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.coverage;

import java.math.BigDecimal;
import java.util.Date;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.model.api.Entity;

public class CoverageProductForDelivery extends CoverageProduct {

    private static final String L_PRODUCT = "product";

    private Entity delivery;

    private Entity deliveryProduct;

    private BigDecimal quantity;

    public CoverageProductForDelivery(final Date coverageDate, final Entity delivery, final Entity deliveryProduct) {
        super(coverageDate);

        this.delivery = delivery;
        this.deliveryProduct = deliveryProduct;
    }

    public Entity getDelivery() {
        return delivery;
    }

    public void setDelivery(final Entity delivery) {
        this.delivery = delivery;
    }

    public Entity getDeliveryProduct() {
        return deliveryProduct;
    }

    public void setDeliveryProduct(final Entity orderedProduct) {
        this.deliveryProduct = orderedProduct;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Entity getProduct() {
        return deliveryProduct.getBelongsToField(L_PRODUCT);
    }

    public BigDecimal getDeliveryQuantity() {
        return deliveryProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
    }

}