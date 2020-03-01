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

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class OrderedProductReservationHooks {

    private static final String L_SUM_OF_QUANTITY = "sumOfQuantity";

    public void onView(final DataDefinition orderedProductReservationDD, final Entity orderedProductReservation) {
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);

        if (Objects.nonNull(orderedProduct)) {
            Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);

            if (Objects.nonNull(product)) {
                String unit = product.getStringField(ProductFields.UNIT);
                String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

                if (Objects.isNull(additionalUnit)) {
                    additionalUnit = unit;
                }

                orderedProductReservation.setField(OrderedProductReservationFields.ORDERED_QUANTITY_UNIT, unit);
                orderedProductReservation.setField(OrderedProductReservationFields.ADDITIONAL_QUANTITY_UNIT, additionalUnit);
            }
        }
    }

    public boolean validate(final DataDefinition orderedProductReservationDD, final Entity orderedProductReservation) {
        return locationUniqueToDelivery(orderedProductReservation) && locationUnique(orderedProductReservation)
                && sumIsNotExceeded(orderedProductReservation);
    }

    private boolean locationUniqueToDelivery(final Entity orderedProductReservation) {
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);
        Entity delivery = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY);
        Entity deliveryLocation = delivery.getBelongsToField(DeliveryFields.LOCATION);

        Entity reservationLocation = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.LOCATION);

        boolean locationOtherThenDelivery = Objects.isNull(deliveryLocation)
                || !deliveryLocation.getId().equals(reservationLocation.getId());

        if (!locationOtherThenDelivery) {
            FieldDefinition locationField = orderedProductReservation.getDataDefinition()
                    .getField(OrderedProductReservationFields.LOCATION);
            orderedProductReservation.addError(locationField,
                    "deliveries.deliveredProductReservation.error.locationNotUniqueToDelivery");
        }

        return locationOtherThenDelivery;
    }

    private boolean locationUnique(final Entity orderedProductReservation) {
        Entity location = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.LOCATION);
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);

        if (Objects.nonNull(location)) {
            SearchCriterion criterion;

            SearchCriterion criterionLocation = SearchRestrictions.belongsTo(OrderedProductReservationFields.LOCATION, location);
            SearchCriterion criterionOrderedProduct = SearchRestrictions
                    .belongsTo(OrderedProductReservationFields.ORDERED_PRODUCT, orderedProduct);

            Long orderedProductReservationId = orderedProductReservation.getId();

            if (Objects.isNull(orderedProductReservationId)) {
                criterion = SearchRestrictions.and(criterionLocation, criterionOrderedProduct);
            } else {
                SearchCriterion criterionId = SearchRestrictions.idNe(orderedProductReservationId);

                criterion = SearchRestrictions.and(criterionLocation, criterionOrderedProduct, criterionId);
            }

            boolean locationUnique = orderedProductReservation.getDataDefinition().count(criterion) == 0;

            if (!locationUnique) {
                FieldDefinition locationField = orderedProductReservation.getDataDefinition()
                        .getField(OrderedProductReservationFields.LOCATION);
                orderedProductReservation.addError(locationField,
                        "deliveries.deliveredProductReservation.error.locationNotUnique");
            }

            return locationUnique;
        }

        return true;
    }

    private boolean sumIsNotExceeded(final Entity orderedProductReservation) {
        Entity orderedProduct = orderedProductReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT);
        BigDecimal productOrderedQuantity = orderedProduct.getDecimalField(OrderedProductReservationFields.ORDERED_QUANTITY);
        BigDecimal reservationOrderedQuantity = orderedProductReservation
                .getDecimalField(OrderedProductReservationFields.ORDERED_QUANTITY);

        SearchCriteriaBuilder searchCriteriaBuilder = orderedProductReservation.getDataDefinition().find();
        SearchProjection sumOfQuantityProjection = SearchProjections
                .alias(SearchProjections.sum(OrderedProductReservationFields.ORDERED_QUANTITY), L_SUM_OF_QUANTITY);
        searchCriteriaBuilder
                .setProjection(SearchProjections.list().add(sumOfQuantityProjection).add(SearchProjections.rowCount()));

        SearchCriterion criterion;
        SearchCriterion criterionOrderedProduct = SearchRestrictions.belongsTo(OrderedProductReservationFields.ORDERED_PRODUCT,
                orderedProduct);

        Long orderedProductReservationId = orderedProductReservation.getId();

        if (Objects.isNull(orderedProductReservationId)) {
            criterion = criterionOrderedProduct;
        } else {
            SearchCriterion criterionId = SearchRestrictions.idNe(orderedProductReservationId);
            criterion = SearchRestrictions.and(criterionOrderedProduct, criterionId);
        }

        searchCriteriaBuilder.add(criterion);
        searchCriteriaBuilder.addOrder(SearchOrders.asc(L_SUM_OF_QUANTITY));

        SearchResult resList = searchCriteriaBuilder.setMaxResults(1).list();

        BigDecimal sumOfQuantity = resList.getTotalNumberOfEntities() == 0 ? BigDecimal.ZERO
                : resList.getEntities().get(0).getDecimalField(L_SUM_OF_QUANTITY);
        sumOfQuantity = BigDecimalUtils.convertNullToZero(sumOfQuantity);

        boolean sumIsNotExceeded = productOrderedQuantity.compareTo(reservationOrderedQuantity.add(sumOfQuantity)) >= 0;

        if (!sumIsNotExceeded) {
            FieldDefinition orderedQuantityField = orderedProductReservation.getDataDefinition()
                    .getField(OrderedProductReservationFields.ORDERED_QUANTITY);

            orderedProductReservation.addError(orderedQuantityField,
                    "deliveries.deliveredProductReservation.error.sumIsExceeded");
        }

        return sumIsNotExceeded;
    }

}
