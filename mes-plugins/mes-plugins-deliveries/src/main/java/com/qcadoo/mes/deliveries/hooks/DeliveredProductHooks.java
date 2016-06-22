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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DAMAGED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductReservationFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveredProductHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        createDefaultReservations(deliveredProduct);
    }

    public void calculateDeliveredProductPricePerUnit(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        deliveriesService.calculatePricePerUnit(deliveredProduct, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = addSearchRestrictions(deliveredProductDD.find(), deliveredProduct);

        if (deliveredProduct.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProduct.getId()));
        }

        Entity deliveredProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (deliveredProductFromDB == null) {
            return true;
        } else {
            deliveredProduct.addError(deliveredProductDD.getField(PRODUCT),
                    "deliveries.deliveredProduct.error.productAlreadyExists");

            return false;
        }
    }

    private SearchCriteriaBuilder addSearchRestrictions(SearchCriteriaBuilder scb, Entity deliveredProduct) {
        return scb.add(SearchRestrictions.belongsTo(DELIVERY, deliveredProduct.getBelongsToField(DELIVERY)))
                .add(SearchRestrictions.belongsTo(PRODUCT, deliveredProduct.getBelongsToField(PRODUCT)));
    }

    public boolean checkIfDeliveredQuantityIsLessThanDamagedQuantity(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
        BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DAMAGED_QUANTITY);
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DELIVERED_QUANTITY);

        if ((damagedQuantity != null) && (deliveredQuantity != null) && (damagedQuantity.compareTo(deliveredQuantity) == 1)) {
            deliveredProduct.addError(deliveredProductDD.getField(DAMAGED_QUANTITY),
                    "deliveries.deliveredProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");
            deliveredProduct.addError(deliveredProductDD.getField(DELIVERED_QUANTITY),
                    "deliveries.deliveredProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");

            return false;
        }

        return true;
    }

    private void createDefaultReservations(Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        if (product != null) {
            List<Entity> deliveredProductReservations = new ArrayList<>();
            Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
            Entity orderedProduct = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS).find().add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product)).uniqueResult();
            if (orderedProduct != null) {
                EntityList reservations = orderedProduct.getHasManyField(OrderedProductFields.RESERVATIONS);
                for (Entity reservation : reservations) {
                    DataDefinition deliveredProductReservationDD = getDeliveredProductReservationDD();
                    Entity deliveredProductReservation = deliveredProductReservationDD.create();
                    deliveredProductReservation.setField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY, 0);
                    deliveredProductReservation.setField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY_UNIT,
                            reservation.getStringField(OrderedProductReservationFields.ADDITIONAL_QUANTITY_UNIT));
                    deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_PRODUCT, deliveredProduct);
                    deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_QUANTITY, 0);
                    deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_QUANTITY_UNIT,
                            reservation.getStringField(OrderedProductReservationFields.ORDERED_QUANTITY_UNIT));
                    deliveredProductReservation.setField(DeliveredProductReservationFields.LOCATION, reservation.getBelongsToField(OrderedProductReservationFields.LOCATION));

                    deliveredProductReservations.add(deliveredProductReservation);
                }
            }
            deliveredProduct.setField(DeliveredProductFields.RESERVATIONS, deliveredProductReservations);
        }
    }

    private DataDefinition getDeliveredProductReservationDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT_RESERVATION);
    }

}
