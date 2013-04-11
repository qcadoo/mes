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
import java.util.List;

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
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_ORDERED_QUANTITY = "orderedQuantity";

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

    public void fillOrderedAndDeliveredQuantity(final DataDefinition deliveryDD, final Entity delivery) {
        delivery.setField(DeliveryFields.QUANTITY_OF_DELIVERED_PRODUCT, countQuantityOfDeliveredProducts(delivery));
        delivery.setField(DeliveryFields.QUANTITY_OF_ORDERED_PRODUCT, countQuantityOfOrderedProducts(delivery));
    }

    private BigDecimal countQuantityOfDeliveredProducts(final Entity delivery) {
        DataDefinition deliveredProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
        List<Entity> deliveredProducts = deliveredProductDD.find(createQueryForDeliveredProduct()).list().getEntities();
        BigDecimal quantityOfDeliveredProducts = BigDecimal.ZERO;
        if (!deliveredProducts.isEmpty()) {
            for (Entity deliveryProduct : deliveredProducts) {
                BigDecimal deliveredQuantity = deliveryProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
                if (deliveredQuantity == null) {
                    deliveredQuantity = BigDecimal.ZERO;
                }
                quantityOfDeliveredProducts = numberService.setScale(quantityOfDeliveredProducts.add(deliveredQuantity));
            }
        }
        return quantityOfDeliveredProducts;
    }

    private String createQueryForDeliveredProduct() {
        return String.format("SELECT SUM(op.orderedQuantity) AS " + L_ORDERED_QUANTITY + " FROM #deliveries_orderedProduct op ");
    }

    private BigDecimal countQuantityOfOrderedProducts(final Entity delivery) {
        List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);
        BigDecimal quantityOfOrderedProducts = BigDecimal.ZERO;
        if (!orderedProducts.isEmpty()) {
            for (Entity deliveryProduct : orderedProducts) {
                BigDecimal orderedQuantity = deliveryProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
                quantityOfOrderedProducts = numberService.setScale(quantityOfOrderedProducts.add(orderedQuantity));
            }
        }
        return quantityOfOrderedProducts;
    }
}
