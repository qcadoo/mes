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

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.ReservationService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.*;

@Service
public class DeliveredProductHooks {

    public static final String OFFER = "offer";

    public static final String OPERATION = "operation";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onCreate(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        reservationService.createDefaultReservationsForDeliveredProduct(deliveredProduct);
    }

    public void onSave(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        reservationService.deleteReservationsForDeliveredProductIfChanged(deliveredProduct);
        updateDeliveredQuantityInOrderedProduct(deliveredProduct);
    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT)
                .find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)));

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OPERATION, deliveredProduct.getBelongsToField(OPERATION)));
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OFFER, deliveredProduct.getBelongsToField(OFFER)));
        }
        Optional<Entity> maybeOrderedProduct = Optional.ofNullable(searchCriteriaBuilder.setMaxResults(1).uniqueResult());
        maybeOrderedProduct.ifPresent(orderedProduct -> {
            orderedProduct.setField(OrderedProductFields.DELIVERED_QUANTITY, BigDecimal.ZERO);
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_DELIVERED_QUANTITY, BigDecimal.ZERO);
            orderedProduct = orderedProduct.getDataDefinition().save(orderedProduct);
        });
        return true;
    }

    private void updateDeliveredQuantityInOrderedProduct(final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT)
                .find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)));

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OPERATION, deliveredProduct.getBelongsToField(OPERATION)));
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OFFER, deliveredProduct.getBelongsToField(OFFER)));
        }
        Optional<Entity> maybeOrderedProduct = Optional.ofNullable(searchCriteriaBuilder.setMaxResults(1).uniqueResult());
        maybeOrderedProduct.ifPresent(orderedProduct -> {
            orderedProduct.setField(OrderedProductFields.DELIVERED_QUANTITY, BigDecimalUtils.convertNullToZero(deliveredProduct
                    .getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY)));
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_DELIVERED_QUANTITY, BigDecimalUtils
                    .convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY)));
            orderedProduct = orderedProduct.getDataDefinition().save(orderedProduct);
        });

    }

    public void calculateDeliveredProductPricePerUnit(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        deliveriesService.calculatePricePerUnit(deliveredProduct, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public boolean validatesWith(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        return checkIfDeliveredProductAlreadyExists(deliveredProductDD, deliveredProduct)
                && checkIfDeliveredQuantityIsLessThanDamagedQuantity(deliveredProductDD, deliveredProduct)
                && checkIfDeliveredQuantityIsLessThanOrderedQuantity(deliveredProductDD, deliveredProduct)
                && validatePallet(deliveredProductDD, deliveredProduct);
    }

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = addSearchRestrictions(deliveredProductDD.find(), deliveredProduct);

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OFFER, deliveredProduct.getBelongsToField(OFFER)));
        }

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
        return scb.add(SearchRestrictions.belongsTo(DELIVERY, deliveredProduct.getBelongsToField(DELIVERY))).add(
                SearchRestrictions.belongsTo(PRODUCT, deliveredProduct.getBelongsToField(PRODUCT)));
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

    private boolean checkIfDeliveredQuantityIsLessThanOrderedQuantity(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
        if (isBiggerDeliveredQuantityAllowed()) {
            return true;
        }
        Optional<Entity> orderedProduct = getOrderedProductForDeliveredProduct(deliveredProduct);
        BigDecimal orderedQuantity = orderedProduct.isPresent() ? orderedProduct.get().getDecimalField(
                OrderedProductFields.ORDERED_QUANTITY) : BigDecimal.ZERO;
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
        if (deliveredQuantity != null && deliveredQuantity.compareTo(orderedQuantity) > 0) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.DELIVERED_QUANTITY),
                    "deliveries.deliveredProduct.error.deliveredQuantity.biggerThanOrderedQuantity");
            return false;
        }
        return true;
    }

    private Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct) {
        DataDefinition orderedProductDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_ORDERED_PRODUCT);
        SearchCriteriaBuilder searchCriteriaBuilder = orderedProductDD
                .find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)));
        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(OFFER, deliveredProduct.getBelongsToField(OFFER)));
        }
        Entity orderedProduct = searchCriteriaBuilder.setMaxResults(1).uniqueResult();
        return Optional.ofNullable(orderedProduct);

    }

    private boolean isBiggerDeliveredQuantityAllowed() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsD.DELIVERED_BIGGER_THAN_ORDERED);
    }

    @Autowired
    private PalletValidatorService palletValidatorService;

    private boolean validatePallet(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        return palletValidatorService.validatePalletForDeliveredProduct(deliveredProduct);
    }

    // private boolean validatePallet(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
    // return (!existsOtherDeliveredProductForPalletAndStorageLocation(deliveredProductDD, deliveredProduct) ||
    // !existsOtherDeliveredProductForStorageLocationAndPallet(deliveredProductDD, deliveredProduct))
    // && !existsOtherDeliveredProductForOtherPalletType(deliveredProductDD, deliveredProduct)
    //
    // // && (!existsOtherPositionForPalletAndStorageLocation(deliveredProductDD, deliveredProduct) ||
    // !existsOtherPositionForStorageLocationAndPallet(deliveredProductDD, deliveredProduct))
    // // && !existsOtherPositionForOtherPalletType(deliveredProductDD, deliveredProduct)
    //
    // && (!existsOtherResourceForPalletAndStorageLocation(deliveredProductDD, deliveredProduct) ||
    // !existsOtherResourceForStorageLocationAndPallet(deliveredProductDD, deliveredProduct))
    // && !existsOtherResourceForOtherPalletType(deliveredProductDD, deliveredProduct);
    // }
    //

}
