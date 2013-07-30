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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_ADDRESS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.EXTERNAL_NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.EXTERNAL_SYNCHRONIZED;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DRAFT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.deliveries.util.DeliveryPricesAndQuantities;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class DeliveryHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private DeliveryStateChangeDescriber describer;

    @Autowired
    private NumberService numberService;

    public void onCreate(final DataDefinition deliveryDD, final Entity delivery) {
        setInitialState(delivery);
        setDeliveryAddressDefaultValue(delivery);
        setDescriptionDefaultValue(delivery);
    }

    public void onCopy(final DataDefinition deliveryDD, final Entity delivery) {
        setInitialState(delivery);
        clearFieldsOnCopy(delivery);
    }

    public void onView(final DataDefinition deliveryDD, final Entity delivery) {
        fillOrderedAndDeliveredCumulatedQuantityAndCumulatedTotalPrice(delivery);
    }

    private void setInitialState(final Entity delivery) {
        stateChangeEntityBuilder.buildInitial(describer, delivery, DRAFT);
    }

    private void clearFieldsOnCopy(final Entity delivery) {
        delivery.setField(STATE, DeliveryStateStringValues.DRAFT);
        delivery.setField(EXTERNAL_NUMBER, null);
        delivery.setField(EXTERNAL_SYNCHRONIZED, true);
    }

    private void setDeliveryAddressDefaultValue(final Entity delivery) {
        String deliveryAddress = delivery.getStringField(DELIVERY_ADDRESS);
        if (deliveryAddress == null) {
            delivery.setField(DELIVERY_ADDRESS, deliveriesService.getDeliveryAddressDefaultValue());
        }
    }

    private void setDescriptionDefaultValue(final Entity delivery) {
        String description = delivery.getStringField(DESCRIPTION);
        if (description == null) {
            delivery.setField(DESCRIPTION, deliveriesService.getDescriptionDefaultValue());
        }
    }

    private void fillOrderedAndDeliveredCumulatedQuantityAndCumulatedTotalPrice(final Entity delivery) {
        DeliveryPricesAndQuantities pricesAndQntts = new DeliveryPricesAndQuantities(delivery, numberService);

        delivery.setField(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_QUANTITY,
                numberService.format(pricesAndQntts.getOrderedCumulatedQuantity()));
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_QUANTITY,
                numberService.format(pricesAndQntts.getDeliveredCumulatedQuantity()));
        delivery.setField(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE,
                numberService.format(pricesAndQntts.getOrderedTotalPrice()));
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE,
                numberService.format(pricesAndQntts.getDeliveredTotalPrice()));
    }

}
