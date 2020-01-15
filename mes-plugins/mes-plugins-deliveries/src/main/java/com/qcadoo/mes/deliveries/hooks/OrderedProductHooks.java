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
import java.math.RoundingMode;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.AdvancedGenealogyService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.ReservationService;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class OrderedProductHooks {

    private static final String L_OFFER = "offer";

    private static final String L_OPERATION = "operation";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AdvancedGenealogyService advancedGenealogyService;

    public void onSave(final DataDefinition orderedProductDD, final Entity orderedProduct) {
        calculateOrderedProductPricePerUnit(orderedProduct);
        calculateReservationQuantities(orderedProduct);

        reservationService.deleteReservationsForOrderedProductIfChanged(orderedProduct);

        createBatch(orderedProduct);
    }

    public void calculateOrderedProductPricePerUnit(final Entity orderedProduct) {
        deliveriesService.calculatePricePerUnit(orderedProduct, OrderedProductFields.ORDERED_QUANTITY);
    }

    public boolean checkIfOrderedProductAlreadyExists(final DataDefinition orderedProductDD, final Entity orderedProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = orderedProductDD.find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY,
                        orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT,
                        orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE,
                        orderedProduct.getBelongsToField(OrderedProductFields.ADDITIONAL_CODE)));

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OPERATION, orderedProduct.getBelongsToField(L_OPERATION)));
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OFFER, orderedProduct.getBelongsToField(L_OFFER)));
        }

        if (Objects.nonNull(orderedProduct.getId())) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", orderedProduct.getId()));
        }

        Entity orderedProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.isNull(orderedProductFromDB)) {
            return true;
        } else {
            orderedProduct.addError(orderedProductDD.getField(OrderedProductFields.PRODUCT),
                    "deliveries.orderedProduct.error.productAlreadyExists");

            return false;
        }
    }

    private void calculateReservationQuantities(final Entity orderedProduct) {
        EntityList reservations = orderedProduct.getHasManyField(OrderedProductFields.RESERVATIONS);

        if (Objects.nonNull(reservations)) {
            BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);

            for (Entity reservation : reservations) {
                BigDecimal orderedQuantity = reservation.getDecimalField(OrderedProductReservationFields.ORDERED_QUANTITY);
                BigDecimal newAdditionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

                newAdditionalQuantity = newAdditionalQuantity.setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                        RoundingMode.HALF_UP);

                reservation.setField(OrderedProductReservationFields.ADDITIONAL_QUANTITY, newAdditionalQuantity);
            }
        }
    }

    private void createBatch(final Entity orderedProduct) {
        if (Objects.isNull(orderedProduct.getId())) {
            String batchNumber = orderedProduct.getStringField(OrderedProductFields.BATCH_NUMBER);
            Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
            Entity batch = orderedProduct.getBelongsToField(OrderedProductFields.BATCH);

            if (Objects.nonNull(batchNumber) && Objects.nonNull(product) && Objects.isNull(batch)) {
                orderedProduct.setField(OrderedProductFields.BATCH,
                        advancedGenealogyService.createOrGetBatch(batchNumber, product));
            }
        }
    }

}
