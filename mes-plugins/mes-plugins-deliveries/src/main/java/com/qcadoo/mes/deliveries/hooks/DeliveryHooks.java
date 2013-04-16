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

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
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
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    private static final String L_ORDERED_QUANTITY = "orderedQuantity";

    private static final String L_TOTAL_PRICE = "totalPrice";

    private static final String L_DELIVERED_QUANTITY = "deliveredQuantity";

    public void setInitialState(final DataDefinition assignmentToShiftDD, final Entity assignmentToShift) {
        stateChangeEntityBuilder.buildInitial(describer, assignmentToShift, DRAFT);
    }

    public void clearFieldsOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(STATE, DeliveryStateStringValues.DRAFT);
        entity.setField(EXTERNAL_NUMBER, null);
        entity.setField(EXTERNAL_SYNCHRONIZED, true);
    }

    public void setDeliveryAddressDefaultValue(final DataDefinition deliveryDD, final Entity delivery) {
        String deliveryAddress = delivery.getStringField(DELIVERY_ADDRESS);

        if (deliveryAddress == null) {
            delivery.setField(DELIVERY_ADDRESS, deliveriesService.getDeliveryAddressDefaultValue());
        }
    }

    public void setDescriptionDefaultValue(final DataDefinition deliveryDD, final Entity delivery) {
        String description = delivery.getStringField(DESCRIPTION);

        if (description == null) {
            delivery.setField(DESCRIPTION, deliveriesService.getDescriptionDefaultValue());
        }

    }

    public void fillOrderedAndDeliveredQuantityAndTotalPrice(final DataDefinition deliveryDD, final Entity delivery) {
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_QUANTITY, countQuantityOfDeliveredProducts(delivery));
        delivery.setField(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_QUANTITY, countQuantityOfOrderedProducts(delivery));
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE, countTotalPriceOfDeliveredProducts(delivery));
        delivery.setField(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE, countTotalPriceOfOrderedProducts(delivery));
    }

    private BigDecimal countQuantityOfDeliveredProducts(final Entity delivery) {
        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        String query = createQueryForQuantityOfDeliveredProduct(delivery);
        Entity deliveredProductsCumulatedQuantity = deliveredProductDD.find(query).setMaxResults(0).uniqueResult();

        BigDecimal deliveredQuantity = deliveredProductsCumulatedQuantity
                .getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
        if (deliveredQuantity == null) {
            deliveredQuantity = BigDecimal.ZERO;
        }
        return numberService.setScale(deliveredQuantity);
    }

    private BigDecimal countQuantityOfOrderedProducts(final Entity delivery) {
        DataDefinition orderedProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_ORDERED_PRODUCT);
        String query = createQueryForQuantityOfOrderedProduct(delivery);
        Entity orderedProductsCumulatedQuantity = orderedProductDD.find(query).setMaxResults(0).uniqueResult();

        BigDecimal orderedQuantity = orderedProductsCumulatedQuantity.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        if (orderedQuantity == null) {
            orderedQuantity = BigDecimal.ZERO;
        }
        return numberService.setScale(orderedQuantity);
    }

    private BigDecimal countTotalPriceOfOrderedProducts(final Entity delivery) {
        DataDefinition orderedProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_ORDERED_PRODUCT);
        String query = createQueryForTotalPriceOfOrderedProduct(delivery);
        Entity orderedProductsTotalPrice = orderedProductDD.find(query).setMaxResults(0).uniqueResult();

        BigDecimal orderedTotalPrice = orderedProductsTotalPrice.getDecimalField(OrderedProductFields.TOTAL_PRICE);
        if (orderedTotalPrice == null) {
            orderedTotalPrice = BigDecimal.ZERO;
        }
        return numberService.setScale(orderedTotalPrice);
    }

    private BigDecimal countTotalPriceOfDeliveredProducts(final Entity delivery) {
        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        String query = createQueryForTotalPriceDeliveredProduct(delivery);
        Entity deliveredProductsTotalPrice = deliveredProductDD.find(query).setMaxResults(0).uniqueResult();

        BigDecimal deliveredTotalPrice = deliveredProductsTotalPrice.getDecimalField(DeliveredProductFields.TOTAL_PRICE);
        if (deliveredTotalPrice == null) {
            deliveredTotalPrice = BigDecimal.ZERO;
        }
        return numberService.setScale(deliveredTotalPrice);
    }

    private String createQueryForQuantityOfOrderedProduct(final Entity delivery) {
        return String.format("SELECT  coalesce(SUM(op.orderedQuantity),0) AS " + L_ORDERED_QUANTITY
                + " FROM #deliveries_orderedProduct as op where op.delivery.id =" + delivery.getId());
    }

    private String createQueryForQuantityOfDeliveredProduct(final Entity delivery) {
        return String.format("SELECT  coalesce(SUM(dp.deliveredQuantity),0) AS " + L_DELIVERED_QUANTITY
                + " FROM #deliveries_deliveredProduct as dp where dp.delivery.id =" + delivery.getId());

    }

    private String createQueryForTotalPriceOfOrderedProduct(final Entity delivery) {
        return String.format("SELECT coalesce(SUM(op.totalPrice),0) AS " + L_TOTAL_PRICE
                + " FROM #deliveries_orderedProduct op where op.delivery.id =" + delivery.getId());
    }

    private String createQueryForTotalPriceDeliveredProduct(final Entity delivery) {
        return String.format("SELECT coalesce(SUM(dp.totalPrice),0) AS " + L_TOTAL_PRICE
                + " FROM #deliveries_deliveredProduct dp where dp.delivery.id =" + delivery.getId());
    }
}
