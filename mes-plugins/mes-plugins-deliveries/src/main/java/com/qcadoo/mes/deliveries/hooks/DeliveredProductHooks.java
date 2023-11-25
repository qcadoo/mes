/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.mes.advancedGenealogy.AdvancedGenealogyService;
import com.qcadoo.mes.advancedGenealogy.constants.BatchNumberUniqueness;
import com.qcadoo.mes.advancedGenealogy.hooks.BatchModelValidators;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;

import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeliveredProductHooks {

    private static final String L_OFFER = "offer";

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
    private PalletValidatorService palletValidatorService;

    @Autowired
    private AdvancedGenealogyService advancedGenealogyService;

    @Autowired
    private BatchModelValidators batchModelValidators;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onCreate(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
    }

    public void onSave(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        deliveriesService.calculatePricePerUnit(deliveredProduct, DeliveredProductFields.DELIVERED_QUANTITY);

        updateDeliveredAndAdditionalQuantityInOrderedProduct(deliveredProductDD, deliveredProduct);

        createBatch(deliveredProduct);
        setPalletType(deliveredProduct);
    }

    private void updateDeliveredAndAdditionalQuantityInOrderedProduct(final DataDefinition deliveredProductDD,
                                                                      final Entity deliveredProduct) {
        if (Objects.nonNull(deliveredProduct.getId())) {
            Entity deliveredProductDB = deliveredProduct.getDataDefinition().get(deliveredProduct.getId());

            boolean isDeliveredProductChange = checkIfDeliveredProductChanged(deliveredProductDB, deliveredProduct);

            if (isDeliveredProductChange) {
                BigDecimal deliveredQuantity = BigDecimal.ZERO;
                BigDecimal additionalQuantity = BigDecimal.ZERO;

                Entity orderedProduct = deliveredProduct.getBelongsToField(DeliveredProductFields.ORDERED_PRODUCT);

                updateDeliveredAndAdditionalQuantityInOrderedProduct(deliveredProductDB, deliveredQuantity, additionalQuantity,
                        orderedProduct);
            }
        }

        BigDecimal deliveredQuantity = BigDecimalUtils
                .convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
        BigDecimal additionalQuantity = BigDecimalUtils
                .convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY));

        updateDeliveredAndAdditionalQuantityInOrderedProduct(deliveredProductDD, deliveredProduct, deliveredQuantity,
                additionalQuantity);
    }

    private boolean checkIfDeliveredProductChanged(final Entity deliveredProductDB, final Entity deliveredProduct) {
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

        Entity deliveredProductDBOffer = deliveredProductDB.getBelongsToField(L_OFFER);
        Entity deliveredProductOffer = deliveredProduct.getBelongsToField(L_OFFER);

        return Objects.isNull(deliveredProductDBOffer) != Objects.isNull(deliveredProductOffer)
                || Objects.nonNull(deliveredProductDBOffer)
                && !deliveredProductDBOffer.getId().equals(deliveredProductOffer.getId());
    }

    private void updateDeliveredAndAdditionalQuantityInOrderedProduct(final DataDefinition deliveredProductDD,
                                                                      final Entity deliveredProduct, BigDecimal deliveredQuantity, BigDecimal additionalQuantity) {
        Optional<Entity> maybeOrderedProduct = deliveriesService.getOrderedProductForDeliveredProduct(deliveredProduct);

        if (maybeOrderedProduct.isPresent()) {
            Entity orderedProduct = maybeOrderedProduct.get();

            deliveredProduct.setField(DeliveredProductFields.ORDERED_PRODUCT, orderedProduct);

            updateDeliveredAndAdditionalQuantityInOrderedProduct(deliveredProduct, deliveredQuantity, additionalQuantity,
                    orderedProduct);
        } else {
            maybeOrderedProduct = deliveriesService.getSuitableOrderedProductForDeliveredProduct(deliveredProduct);

            if (maybeOrderedProduct.isPresent()) {
                Entity orderedProduct = maybeOrderedProduct.get();

                deliveredProduct.setField(DeliveredProductFields.ORDERED_PRODUCT, orderedProduct);

                updateDeliveredAndAdditionalQuantityInOrderedProduct(deliveredProduct, deliveredQuantity, additionalQuantity,
                        orderedProduct);
            }
        }
    }

    private void updateDeliveredAndAdditionalQuantityInOrderedProduct(final Entity deliveredProduct, BigDecimal deliveredQuantity,
                                                                      BigDecimal additionalQuantity, Entity orderedProduct) {
        if (Objects.nonNull(orderedProduct)) {
            List<Entity> deliveredProducts = getOtherDeliveredProducts(deliveredProduct, orderedProduct);

            if (!deliveredProducts.isEmpty()) {
                BigDecimal deliveredQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY))
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                deliveredQuantity = deliveredQuantity.add(deliveredQuantityRest, numberService.getMathContext());

                BigDecimal additionalQuantityRest = deliveredProducts.stream()
                        .map(dp -> dp.getDecimalField(DeliveredProductFields.ADDITIONAL_QUANTITY))
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                additionalQuantity = additionalQuantity.add(additionalQuantityRest, numberService.getMathContext());
            }

            orderedProduct.setField(OrderedProductFields.DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(deliveredQuantity));
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_DELIVERED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(additionalQuantity));

            orderedProduct = orderedProduct.getDataDefinition().save(orderedProduct);
        }
    }

    private List<Entity> getOtherDeliveredProducts(final Entity deliveredProduct, final Entity orderedProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = orderedProduct.getHasManyField(OrderedProductFields.DELIVERED_PRODUCTS)
                .find();

        if (Objects.nonNull(deliveredProduct.getId())) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProduct.getId()));
        }

        return searchCriteriaBuilder.list().getEntities();
    }

    private void createBatch(final Entity deliveredProduct) {
        if (deliveredProduct.getBooleanField(DeliveredProductFields.ADD_BATCH)
                && (StringUtils.isNoneEmpty(deliveredProduct.getStringField(DeliveredProductFields.BATCH_NUMBER))
                || parameterService.getParameter().getBooleanField(
                ParameterFieldsD.PRODUCT_DELIVERY_BATCH_EVIDENCE))) {
            String batchNumber = deliveredProduct.getStringField(DeliveredProductFields.BATCH_NUMBER);
            Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
            Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);


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

    private void setPalletType(final Entity deliveredProduct) {
        Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);

        if (Objects.isNull(palletNumber)) {
            deliveredProduct.setField(DeliveredProductFields.PALLET_TYPE, null);
        }
    }

    public boolean onDelete(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        BigDecimal deliveredQuantity = BigDecimal.ZERO;
        BigDecimal additionalQuantity = BigDecimal.ZERO;

        updateDeliveredAndAdditionalQuantityInOrderedProduct(deliveredProductDD, deliveredProduct, deliveredQuantity,
                additionalQuantity);

        return true;
    }

    public boolean validatesWith(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        boolean isValid = checkIfDeliveredProductAlreadyExists(deliveredProductDD, deliveredProduct);

        isValid = isValid && checkIfDeliveredQuantityIsLessThanDamagedQuantity(deliveredProductDD, deliveredProduct);
        isValid = isValid && checkIfDeliveredQuantityIsLessThanOrderedQuantity(deliveredProductDD, deliveredProduct);
        isValid = isValid && palletValidatorService.validatePalletForDeliveredProduct(deliveredProduct);

        return isValid;
    }

    public boolean checkIfDeliveredProductAlreadyExists(final DataDefinition deliveredProductDD, final Entity deliveredProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = deliveriesService
                .getSearchCriteriaBuilderForDeliveredProduct(deliveredProductDD.find(), deliveredProduct);

        Long deliveredProductId = deliveredProduct.getId();

        if (Objects.nonNull(deliveredProductId)) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProductId));
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

        BigDecimal deliveredQuantity = getDeliveredQuantity(deliveredProduct);
        BigDecimal orderedQuantity = getOrderedQuantity(deliveredProduct);

        deliveredQuantity = deliveredQuantity.add(
                BigDecimalUtils.convertNullToZero(deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY)),
                numberService.getMathContext());

        if (deliveredQuantity.compareTo(orderedQuantity) > 0) {
            deliveredProduct.addError(deliveredProductDD.getField(DeliveredProductFields.DELIVERED_QUANTITY),
                    "deliveries.deliveredProduct.error.deliveredQuantity.biggerThanOrderedQuantity");

            return false;
        }

        return true;
    }

    private boolean isBiggerDeliveredQuantityAllowed() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsD.DELIVERED_BIGGER_THAN_ORDERED);
    }

    private BigDecimal getDeliveredQuantity(final Entity deliveredProduct) {
        List<Entity> deliveredProducts = getDeliveredProducts(deliveredProduct);

        BigDecimal deliveredQuantity = BigDecimal.ZERO;

        if (!deliveredProducts.isEmpty()) {
            BigDecimal deliveredQuantityRest = deliveredProducts.stream()
                    .map(dp -> dp.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            deliveredQuantity = deliveredQuantity.add(deliveredQuantityRest, numberService.getMathContext());
        }

        return deliveredQuantity;
    }

    private List<Entity> getDeliveredProducts(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        SearchCriteriaBuilder searchCriteriaBuilder = deliveriesService.getDeliveredProductDD().find()
                .add(SearchRestrictions.belongsTo(DeliveredProductFields.DELIVERY, delivery))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product));

        if (Objects.nonNull(deliveredProduct.getId())) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", deliveredProduct.getId()));
        }

        return searchCriteriaBuilder.list().getEntities();
    }

    private BigDecimal getOrderedQuantity(final Entity deliveredProduct) {
        List<Entity> orderedProducts = getOrderedProducts(deliveredProduct);

        BigDecimal orderedQuantity = BigDecimal.ZERO;

        if (!orderedProducts.isEmpty()) {
            BigDecimal orderedQuantityRest = orderedProducts.stream()
                    .map(op -> op.getDecimalField(OrderedProductFields.ORDERED_QUANTITY))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            orderedQuantity = orderedQuantity.add(orderedQuantityRest, numberService.getMathContext());
        }

        return orderedQuantity;
    }

    private List<Entity> getOrderedProducts(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        SearchCriteriaBuilder searchCriteriaBuilder = deliveriesService.getOrderedProductDD().find()
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY, delivery))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product));

        return searchCriteriaBuilder.list().getEntities();
    }

}
