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

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductReservationFields;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveredProductReservationHooks {

    public void onView(final DataDefinition deliveredProductReservationDD, final Entity deliveredProductReservation) {
        Entity deliveredProduct = deliveredProductReservation.getBelongsToField(DeliveredProductReservationFields.DELIVERED_PRODUCT);
        if (deliveredProduct != null) {
            Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

            if (product != null) {
                String unit = product.getStringField(ProductFields.UNIT);
                String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

                if (additionalUnit == null) {
                    additionalUnit = unit;
                }

                deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_QUANTITY_UNIT, unit);
                deliveredProductReservation.setField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY_UNIT, additionalUnit);
            }
        }
    }

    public boolean validate(final DataDefinition deliveredProductReservationDD, final Entity deliveredProductReservation) {
        return true;
//        return locationUniqueToDelivery(orderedProductReservation) && locationUnique(orderedProductReservation) && sumIsNotExceeded(orderedProductReservation);
    }

//    private boolean locationUniqueToDelivery(Entity orderedProductReservation) {
//        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);
//        Entity delivery = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY);
//        Entity deliveryLocation = delivery.getBelongsToField(DeliveryFields.LOCATION);
//
//        Entity reservationLocation = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.LOCATION);
//
//        boolean locationOtherThenDelivery = deliveryLocation == null || !deliveryLocation.getId().equals(reservationLocation.getId());
//
//        if (!locationOtherThenDelivery) {
//            FieldDefinition locationField = orderedProductReservation.getDataDefinition().getField(OrderedProductReservationFields.LOCATION);
//            orderedProductReservation.addError(locationField, "deliveries.deliveredProductReservation.error.locationNotUniqueToDelivery");
//        }
//
//        return locationOtherThenDelivery;
//    }
//
//    private boolean locationUnique(Entity orderedProductReservation) {
//        Entity location = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.LOCATION);
//        if (location != null) {
//            SearchCriterion criterion;
//
//            SearchCriterion criterionLocation = SearchRestrictions.belongsTo(OrderedProductReservationFields.LOCATION, location);
//
//            if (orderedProductReservation.getId() == null) {
//                criterion = criterionLocation;
//
//            } else {
//                SearchCriterion criterionId = SearchRestrictions.idNe(orderedProductReservation.getId());
//                criterion = SearchRestrictions.and(criterionLocation, criterionId);
//            }
//
//            boolean locationUnique = orderedProductReservation.getDataDefinition().count(criterion) == 0;
//
//            if (!locationUnique) {
//                FieldDefinition locationField = orderedProductReservation.getDataDefinition().getField(OrderedProductReservationFields.LOCATION);
//                orderedProductReservation.addError(locationField, "deliveries.deliveredProductReservation.error.locationNotUnique");
//            }
//
//            return locationUnique;
//        }
//
//        return true;
//    }
//
//    private boolean sumIsNotExceeded(Entity orderedProductReservation) {
//        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);
//        BigDecimal productOrderedQuantity = orderedProduct.getDecimalField(OrderedProductReservationFields.ORDERED_QUANTITY);
//        BigDecimal reservationOrderedQuantity = orderedProductReservation.getDecimalField(OrderedProductReservationFields.ORDERED_QUANTITY);
//
//        SearchCriteriaBuilder searchCriteriaBuilder = orderedProductReservation.getDataDefinition().find();
//        SearchProjection sumOfQuantityProjection = SearchProjections.alias(SearchProjections.sum(OrderedProductReservationFields.ORDERED_QUANTITY), "sumOfQuantity");
//        searchCriteriaBuilder.setProjection(sumOfQuantityProjection);
//
//        SearchCriterion criterion;
//        SearchCriterion criterionOrderedProduct = SearchRestrictions.belongsTo(OrderedProductReservationFields.ORDERED_PRODUCT, orderedProduct);
//
//        if (orderedProductReservation.getId() == null) {
//            criterion = criterionOrderedProduct;
//
//        } else {
//            SearchCriterion criterionId = SearchRestrictions.idNe(orderedProductReservation.getId());
//            criterion = SearchRestrictions.and(criterionOrderedProduct, criterionId);
//        }
//        searchCriteriaBuilder.add(criterion);
//        searchCriteriaBuilder.addOrder(SearchOrders.asc("sumOfQuantity"));
//
//        Entity res = searchCriteriaBuilder.setMaxResults(1).uniqueResult();
//
//        BigDecimal sumOfQuantity = res == null ? BigDecimal.ZERO : res.getDecimalField("sumOfQuantity");
//
//        boolean sumIsNotExceeded = productOrderedQuantity.compareTo(reservationOrderedQuantity.add(sumOfQuantity)) >= 0;
//
//        if (!sumIsNotExceeded) {
//            FieldDefinition orderedQuantityField = orderedProductReservation.getDataDefinition().getField(OrderedProductReservationFields.ORDERED_QUANTITY);
//            orderedProductReservation.addError(orderedQuantityField, "deliveries.deliveredProductReservation.error.sumIsExceeded");
//        }
//
//        return sumIsNotExceeded;
//    }
}
