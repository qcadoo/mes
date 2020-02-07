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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.AdvancedGenealogyService;
import com.qcadoo.mes.advancedGenealogy.constants.BatchNumberUniqueness;
import com.qcadoo.mes.advancedGenealogy.hooks.BatchModelValidators;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.ReservationService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class DeliveredProductHooks {

    private static final String L_OFFER = "offer";

    private static final String L_OPERATION = "operation";

    private static final String L_EXPIRATION_DATE = "expirationDate";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Autowired
    private AdvancedGenealogyService advancedGenealogyService;

    @Autowired
    private BatchModelValidators batchModelValidators;

    public void onCreate(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        reservationService.createDefaultReservationsForDeliveredProduct(deliveredProduct);
    }

    public void onSave(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        reservationService.deleteReservationsForDeliveredProductIfChanged(deliveredProduct);

        updateDeliveredQuantityInOrderedProduct(deliveredProductDD, deliveredProduct);
        tryFillStorageLocation(deliveredProduct);

        createBatch(deliveredProduct);
    }

    private void tryFillStorageLocation(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.nonNull(location)
                && Objects.isNull(deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION))) {
            Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

            Optional<Entity> storageLocation = findStorageLocationForProduct(product, location);

            if (storageLocation.isPresent()) {
                deliveredProduct.setField(DeliveredProductFields.STORAGE_LOCATION, storageLocation.get());
            }
        }
    }

    public boolean onDelete(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(),
                deliveredProduct);

        Optional<Entity> maybeOrderedProduct = Optional.ofNullable(searchCriteriaBuilder.setMaxResults(1).uniqueResult());

        maybeOrderedProduct.ifPresent(orderedProduct -> {
            BigDecimal deliveredQuantity = BigDecimal.ZERO;
            BigDecimal additionalQuantity = BigDecimal.ZERO;

            List<Entity> deliveredProducts = getSearchCriteriaBuilderForDeliveredProductByGroup(deliveredProductDD.find(),
                    deliveredProduct).list().getEntities();

            if (!deliveredProducts.isEmpty()) {
                BigDecimal deliveredQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                deliveredQuantity = deliveredQuantity.add(deliveredQuantityRest, numberService.getMathContext());

                BigDecimal additionalQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                additionalQuantity = additionalQuantity.add(additionalQuantityRest, numberService.getMathContext());
            }

            orderedProduct.setField(OrderedProductFields.DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(deliveredQuantity));
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(additionalQuantity));

            orderedProduct = orderedProduct.getDataDefinition().save(orderedProduct);
        });

        return true;
    }

    private SearchCriteriaBuilder getSearchCriteriaBuilderForOrderedProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct) {
        searchCriteriaBuilder
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)));

        if (Objects.nonNull(deliveredProduct.getField(DeliveredProductFields.BATCH))) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.BATCH,
                    deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH)));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(DeliveredProductFields.BATCH));
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OFFER, deliveredProduct.getBelongsToField(L_OFFER)));
        }

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OPERATION, deliveredProduct.getBelongsToField(L_OPERATION)));
        }

        return searchCriteriaBuilder;
    }

    private void updateDeliveredQuantityInOrderedProduct(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        if (Objects.nonNull(deliveredProduct.getId())) {
            Entity deliveredProductDB = deliveredProduct.getDataDefinition().get(deliveredProduct.getId());

            boolean isDeliveredProductChange = checkIfDeliveredProductChange(deliveredProductDB, deliveredProduct);

            if (isDeliveredProductChange) {
                SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForOrderedProduct(
                        getOrderedProductDD().find(), deliveredProductDB);

                Optional<Entity> maybeOrderedProduct = Optional.ofNullable(searchCriteriaBuilder.setMaxResults(1).uniqueResult());

                maybeOrderedProduct.ifPresent(orderedProduct -> {
                    BigDecimal deliveredQuantity = BigDecimal.ZERO;
                    BigDecimal additionalQuantity = BigDecimal.ZERO;

                    List<Entity> deliveredProductsDB = getSearchCriteriaBuilderForDeliveredProductByGroup(
                            deliveredProductDD.find(), deliveredProductDB).list().getEntities();

                    if (!deliveredProductsDB.isEmpty()) {
                        BigDecimal deliveredQuantityRest = deliveredProductsDB.stream()
                                .map(dp -> dp.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        deliveredQuantity = deliveredQuantity.add(deliveredQuantityRest, numberService.getMathContext());
                        BigDecimal additionalQuantityRest = deliveredProductsDB.stream()
                                .map(dp -> dp.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        additionalQuantity = additionalQuantity.add(additionalQuantityRest, numberService.getMathContext());
                    }

                    orderedProduct.setField(OrderedProductFields.DELIVERED_QUANTITY,
                            numberService.setScaleWithDefaultMathContext(deliveredQuantity));
                    orderedProduct.setField(OrderedProductFields.ADDITIONAL_DELIVERED_QUANTITY,
                            numberService.setScaleWithDefaultMathContext(additionalQuantity));

                    orderedProduct = orderedProduct.getDataDefinition().save(orderedProduct);
                });
            }
        }

        SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(),
                deliveredProduct);

        Optional<Entity> maybeOrderedProduct = Optional.ofNullable(searchCriteriaBuilder.setMaxResults(1).uniqueResult());

        maybeOrderedProduct.ifPresent(orderedProduct -> {
            BigDecimal deliveredQuantity = BigDecimalUtils
                    .convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
            BigDecimal additionalQuantity = BigDecimalUtils
                    .convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY));

            List<Entity> deliveredProducts = getSearchCriteriaBuilderForDeliveredProductByGroup(deliveredProductDD.find(),
                    deliveredProduct).list().getEntities();

            if (!deliveredProducts.isEmpty()) {
                BigDecimal deliveredQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                deliveredQuantity = deliveredQuantity.add(deliveredQuantityRest, numberService.getMathContext());
                BigDecimal additionalQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                additionalQuantity = additionalQuantity.add(additionalQuantityRest, numberService.getMathContext());
            }

            orderedProduct.setField(OrderedProductFields.DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(deliveredQuantity));
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(additionalQuantity));

            orderedProduct = orderedProduct.getDataDefinition().save(orderedProduct);
        });
    }

    private boolean checkIfDeliveredProductChange(final Entity deliveredProductDB, final Entity deliveredProduct) {
        Entity deliveredProductDBProduct = deliveredProductDB.getBelongsToField(DeliveredProductFields.PRODUCT);
        Entity deliveredProductProduct = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        if (!deliveredProductDBProduct.getId().equals(deliveredProductProduct.getId())) {
            return true;
        }

        Entity deliveredProductDBBatch = deliveredProductDB.getBelongsToField(DeliveredProductFields.BATCH);
        Entity deliveredProductBatch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);

        if (Objects.isNull(deliveredProductDBBatch) != Objects.isNull(deliveredProductBatch)
                || Objects.nonNull(deliveredProductDBBatch)
                        && !deliveredProductDBBatch.getId().equals(deliveredProductBatch.getId())) {
            return true;
        }

        Entity deliveredProductDBAdditionalCode = deliveredProductDB.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);
        Entity deliveredProductAdditionalCode = deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);

        if (Objects.isNull(deliveredProductDBAdditionalCode) != Objects.isNull(deliveredProductAdditionalCode)
                || Objects.nonNull(deliveredProductDBAdditionalCode)
                        && !deliveredProductDBAdditionalCode.getId().equals(deliveredProductAdditionalCode.getId())) {
            return true;
        }

        Entity deliveredProductDBOffer = deliveredProductDB.getBelongsToField(L_OFFER);
        Entity deliveredProductOffer = deliveredProduct.getBelongsToField(L_OFFER);

        if (Objects.isNull(deliveredProductDBOffer) != Objects.isNull(deliveredProductOffer)
                || Objects.nonNull(deliveredProductDBOffer)
                        && !deliveredProductDBOffer.getId().equals(deliveredProductOffer.getId())) {
            return true;
        }

        return false;
    }

    private void getSearchCriteriaBuilderForDeliveredProductBasic(final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct) {
        searchCriteriaBuilder
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.DELIVERY,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY)))
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.PRODUCT,
                        deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)));

        if (Objects.nonNull(deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH))) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.BATCH,
                    deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH)));
        } else {
            searchCriteriaBuilder.add(SearchRestrictions.isNull(DeliveredProductFields.BATCH));
        }

        if (PluginUtils.isEnabled("supplyNegotiations")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OFFER, deliveredProduct.getBelongsToField(L_OFFER)));
        }
    }

    private SearchCriteriaBuilder getSearchCriteriaBuilderForDeliveredProductByGroup(
            final SearchCriteriaBuilder searchCriteriaBuilder, final Entity deliveredProduct) {
        getSearchCriteriaBuilderForDeliveredProductBasic(searchCriteriaBuilder, deliveredProduct);

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(DeliveredProductFields.ADDITIONAL_CODE,
                deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)));

        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(L_OPERATION, deliveredProduct.getBelongsToField(L_OPERATION)));
        }

        if (Objects.nonNull(deliveredProduct.getId())) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProduct.getId()));
        }

        return searchCriteriaBuilder;
    }

    public void calculateDeliveredProductPricePerUnit(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        deliveriesService.calculatePricePerUnit(deliveredProduct, DeliveredProductFields.DELIVERED_QUANTITY);
    }

    public boolean validatesWith(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        return checkIfDeliveredProductAlreadyExists(deliveredProductDD, deliveredProduct)
                && checkIfDeliveredQuantityIsLessThanDamagedQuantity(deliveredProductDD, deliveredProduct)
                && checkIfDeliveredQuantityIsLessThanOrderedQuantity(deliveredProductDD, deliveredProduct)
                && validatePallet(deliveredProduct) && notTooManyPalletsInStorageLocation(deliveredProductDD, deliveredProduct);
    }

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForDeliveredProduct(deliveredProductDD.find(),
                deliveredProduct);

        if (Objects.nonNull(deliveredProduct.getId())) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProduct.getId()));
        }

        Entity deliveredProductFromDB = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        if (Objects.isNull(deliveredProductFromDB)) {
            return true;
        } else {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.PRODUCT),
                    "deliveries.deliveredProduct.error.productAlreadyExists");

            return false;
        }
    }

    private SearchCriteriaBuilder getSearchCriteriaBuilderForDeliveredProduct(final SearchCriteriaBuilder searchCriteriaBuilder,
            final Entity deliveredProduct) {
        getSearchCriteriaBuilderForDeliveredProductBasic(searchCriteriaBuilder, deliveredProduct);

        if (PluginUtils.isEnabled("deliveriesToMaterialFlow")) {
            searchCriteriaBuilder
                    .add(SearchRestrictions.belongsTo(DeliveredProductFields.PALLET_NUMBER,
                            deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER)))
                    .add(SearchRestrictions.belongsTo(DeliveredProductFields.ADDITIONAL_CODE,
                            deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE)));

            if (Objects.nonNull(deliveredProduct.getField(L_EXPIRATION_DATE))) {
                searchCriteriaBuilder.add(SearchRestrictions.eq(L_EXPIRATION_DATE, deliveredProduct.getField(L_EXPIRATION_DATE)));
            } else {
                searchCriteriaBuilder.add(SearchRestrictions.isNull(L_EXPIRATION_DATE));
            }
        }

        return searchCriteriaBuilder;
    }

    public boolean checkIfDeliveredQuantityIsLessThanDamagedQuantity(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
        BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY);
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);

        if (Objects.nonNull(damagedQuantity) && Objects.nonNull(deliveredQuantity)
                && (damagedQuantity.compareTo(deliveredQuantity) > 0)) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.DAMAGED_QUANTITY),
                    "deliveries.deliveredProduct.error.damagedQuantity.deliveredQuantityIsTooMuch");
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.DELIVERED_QUANTITY),
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

        BigDecimal deliveredQuantity = BigDecimalUtils
                .convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));

        SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(),
                deliveredProduct);

        Optional<Entity> maybeOrderedProduct = Optional.ofNullable(searchCriteriaBuilder.setMaxResults(1).uniqueResult());

        if (maybeOrderedProduct.isPresent()) {
            List<Entity> deliveredProducts = getSearchCriteriaBuilderForDeliveredProductByGroup(deliveredProductDD.find(),
                    deliveredProduct).list().getEntities();

            if (!deliveredProducts.isEmpty()) {
                BigDecimal deliveredQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                deliveredQuantity = deliveredQuantity.add(deliveredQuantityRest, numberService.getMathContext());
            }
        }

        Optional<Entity> orderedProduct = getOrderedProductForDeliveredProduct(deliveredProduct);
        BigDecimal orderedQuantity = orderedProduct.isPresent()
                ? orderedProduct.get().getDecimalField(OrderedProductFields.ORDERED_QUANTITY)
                : BigDecimal.ZERO;

        if (Objects.nonNull(deliveredQuantity) && deliveredQuantity.compareTo(orderedQuantity) > 0) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.DELIVERED_QUANTITY),
                    "deliveries.deliveredProduct.error.deliveredQuantity.biggerThanOrderedQuantity");

            return false;
        }

        return true;
    }

    private Optional<Entity> getOrderedProductForDeliveredProduct(final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaBuilderForOrderedProduct(getOrderedProductDD().find(),
                deliveredProduct);

        Entity orderedProduct = searchCriteriaBuilder.setMaxResults(1).uniqueResult();

        return Optional.ofNullable(orderedProduct);
    }

    private boolean isBiggerDeliveredQuantityAllowed() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsD.DELIVERED_BIGGER_THAN_ORDERED);
    }

    private boolean validatePallet(final Entity deliveredProduct) {
        if (Objects.isNull(deliveredProduct.getField(DeliveredProductFields.VALIDATE_PALLET))
                || deliveredProduct.getBooleanField(DeliveredProductFields.VALIDATE_PALLET)) {
            return palletValidatorService.validatePalletForDeliveredProduct(deliveredProduct);
        }

        return true;
    }

    private boolean notTooManyPalletsInStorageLocation(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        Entity storageLocation = deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);

        final BigDecimal maxNumberOfPallets;

        if (Objects.nonNull(storageLocation) && Objects
                .nonNull(maxNumberOfPallets = storageLocation.getDecimalField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS))) {

            Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);

            if (Objects.nonNull(palletNumber)) {
                String query = "SELECT count(DISTINCT palletsInStorageLocation.palletnumber_id) AS palletsCount     "
                        + "   FROM (SELECT                                                                          "
                        + "           resource.palletnumber_id,                                                     "
                        + "           resource.storagelocation_id                                                   "
                        + "         FROM materialflowresources_resource resource                                    "
                        + "         UNION ALL SELECT                                                                "
                        + "                     deliveredproduct.palletnumber_id,                                   "
                        + "                     deliveredproduct.storagelocation_id                                 "
                        + "                   FROM deliveries_delivery delivery                                     "
                        + "                     JOIN deliveries_deliveredproduct deliveredproduct                   "
                        + "                       ON deliveredproduct.delivery_id = delivery.id                     "
                        + "                   WHERE                                                                 "
                        + "                     delivery.state not in ('06received','04declined') AND                                      "
                        + "                     deliveredproduct.id <> :deliveredProductId                          "
                        + "        ) palletsInStorageLocation                                                       "
                        + "   WHERE palletsInStorageLocation.storagelocation_id = :storageLocationId AND            "
                        + "         palletsInStorageLocation.palletnumber_id <> :palletNumberId";

                Long deliveredProductId = Optional.ofNullable(deliveredProduct.getId()).orElse(-1L);
                Long palletsCount = jdbcTemplate.queryForObject(query,
                        new MapSqlParameterSource().addValue("storageLocationId", storageLocation.getId())
                                .addValue("palletNumberId", palletNumber.getId())
                                .addValue("deliveredProductId", deliveredProductId),
                        Long.class);

                boolean valid = maxNumberOfPallets.compareTo(BigDecimal.valueOf(palletsCount)) > 0;

                if (!valid) {
                    deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.STORAGE_LOCATION),
                            "deliveries.deliveredProduct.error.storageLocationPalletLimitExceeded");
                }

                return valid;
            }
        }

        return true;
    }

    public Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION)
                .find();

        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));

        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    private void createBatch(final Entity deliveredProduct) {
        String batchNumber = deliveredProduct.getStringField(DeliveredProductFields.BATCH_NUMBER);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);

        if (Objects.nonNull(batchNumber) && Objects.nonNull(product) && Objects.nonNull(delivery)) {
            Entity supplier = delivery.getBelongsToField(DeliveryFields.SUPPLIER);

            Entity batch = advancedGenealogyService.createOrGetBatch(batchNumber, product, supplier);

            if (batch.isValid()) {
                deliveredProduct.setField(DeliveredProductFields.BATCH_NUMBER, null);
                deliveredProduct.setField(DeliveredProductFields.BATCH, batch);
            } else {
                BatchNumberUniqueness batchNumberUniqueness = batchModelValidators.getBatchNumberUniqueness();
                String errorMessage = batchModelValidators.getBatchNumberErrorMessage(batchNumberUniqueness);

                deliveredProduct.addGlobalError(errorMessage);
            }
        }
    }

    private DataDefinition getOrderedProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT);
    }

}
