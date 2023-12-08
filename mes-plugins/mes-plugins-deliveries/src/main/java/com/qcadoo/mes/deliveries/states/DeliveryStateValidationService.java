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
package com.qcadoo.mes.deliveries.states;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.ProductSynchronizationService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class DeliveryStateValidationService {

    private static final String L_ENTITY_IS_NULL = "entity is null";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ProductSynchronizationService productSynchronizationService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    public void validationOnApproved(final StateChangeContext stateChangeContext) {
        final List<String> fieldNames = Lists.newArrayList(DeliveryFields.DELIVERY_DATE, DeliveryFields.SUPPLIER);

        checkRequired(stateChangeContext, fieldNames);
        checkOrderedProductsOrderedQuantities(stateChangeContext);
    }

    public void validationOnReceived(final StateChangeContext stateChangeContext) {
        final List<String> fieldNames = Lists.newArrayList(DeliveryFields.LOCATION);

        checkRequired(stateChangeContext, fieldNames);
        checkDeliveredProductsDeliveredQuantities(stateChangeContext);
        checkDeliveredProductsBatches(stateChangeContext);
        checkDeliveredPackages(stateChangeContext);
        checkIfStorageLocationsAndPalletNumbersAreSet(stateChangeContext);

        if (parameterService.getParameter().getBooleanField(ParameterFieldsD.POSITIVE_PURCHASE_PRICE)) {
            checkDeliveredProductsPricePerUnits(stateChangeContext);
        }

        if (pluginManager.isPluginEnabled("integration") && productSynchronizationService.shouldSynchronize(stateChangeContext)) {
            checkDeliveredProductsExternalNumbers(stateChangeContext);

            productSynchronizationService.synchronizeProducts(stateChangeContext, false);
        }
    }

    public void checkRequired(final StateChangeContext stateChangeContext, final List<String> fieldNames) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        for (String fieldName : fieldNames) {
            if (Objects.isNull(delivery.getField(fieldName))) {
                stateChangeContext.addFieldValidationError(fieldName, "deliveries.delivery.deliveryStates.fieldRequired");
            }
        }
    }

    private void checkOrderedProductsOrderedQuantities(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        Set<String> orderedProductsWithoutOrderedQuantities = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS)
                .stream().filter(this::checkOrderedProductOrderedQuantity).map(this::getOrderedProductProductNumber)
                .collect(Collectors.toSet());

        if (!orderedProductsWithoutOrderedQuantities.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.orderedProducts.orderedQuantity.isRequired", false,
                    String.join(", ", orderedProductsWithoutOrderedQuantities));
        }
    }

    private boolean checkOrderedProductOrderedQuantity(final Entity orderedProduct) {
        BigDecimal orderedQuantity = BigDecimalUtils
                .convertNullToZero(orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY));

        return (orderedQuantity.compareTo(BigDecimal.ZERO) <= 0);
    }

    private void checkDeliveredProductsDeliveredQuantities(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        if (deliveredProducts.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.deliveredProductsList.isEmpty");
        }

        Set<String> deliveredProductsWithoutDeliveredQuantities = delivery
                .getHasManyField(DeliveryFields.DELIVERED_PRODUCTS).stream().filter(this::checkDeliveredProductDeliveredQuantity)
                .map(this::getDeliveredProductProductNumber).collect(Collectors.toSet());

        if (!deliveredProductsWithoutDeliveredQuantities.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.deliveredQuantity.isRequired", false,
                    String.join(", ", deliveredProductsWithoutDeliveredQuantities));
        }
    }

    private boolean checkDeliveredProductDeliveredQuantity(final Entity deliveredProduct) {
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);

        return Objects.isNull(deliveredQuantity);
    }

    private void checkDeliveredProductsBatches(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        Set<String> deliveredProductsWithoutBatches = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS)
                .stream().filter(this::checkDeliveredProductBatch).map(this::getDeliveredProductProductNumber)
                .collect(Collectors.toSet());

        if (!deliveredProductsWithoutBatches.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.batch.isRequired", false,
                    String.join(", ", deliveredProductsWithoutBatches));
        }
    }

    private boolean checkDeliveredProductBatch(final Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);

        return (product.getBooleanField(ProductFields.BATCH_EVIDENCE) && Objects.isNull(batch));
    }

    private void checkDeliveredPackages(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        Set<String> deliveredPackagesWithBatchEvidence = delivery.getHasManyField(DeliveryFields.DELIVERED_PACKAGES)
                .stream().filter(this::checkDeliveredPackageBatchEvidence).map(this::getDeliveredPackageProductNumber)
                .collect(Collectors.toSet());

        if (!deliveredPackagesWithBatchEvidence.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredPackages.batch.isRequired", false,
                    String.join(", ", deliveredPackagesWithBatchEvidence));
        }
    }

    private boolean checkDeliveredPackageBatchEvidence(final Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        return product.getBooleanField(ProductFields.BATCH_EVIDENCE);
    }

    private void checkIfStorageLocationsAndPalletNumbersAreSet(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        Set<String> missingStorageLocations = Sets.newHashSet();
        Set<String> missingPalletNumbers = Sets.newHashSet();
        Set<String> existsMorePallets = Sets.newHashSet();

        deliveredProducts.forEach(deliveredProduct -> {
            String productNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getStringField(ProductFields.NUMBER);
            Entity storageLocation = deliveredProduct.getBelongsToField(DeliveredProductFields.STORAGE_LOCATION);
            Entity palletNumber = deliveredProduct.getBelongsToField(DeliveredProductFields.PALLET_NUMBER);

            if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
                missingStorageLocations.add(productNumber);
            } else {
                if (Objects.nonNull(storageLocation)) {
                    boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

                    if (placeStorageLocation) {
                        if (Objects.isNull(palletNumber)) {
                            missingPalletNumbers.add(productNumber);
                        } else {
                            if (!palletValidatorService.notTooManyPalletsInStorageLocationAndDeliveredProducts(deliveredProduct.getDataDefinition(), deliveredProduct)) {
                                existsMorePallets.add(productNumber);
                            }
                        }
                    }
                }
            }
        });

        if (!missingStorageLocations.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.error.storageLocationRequired", false, String.join(", ", missingStorageLocations));
        }
        if (!missingPalletNumbers.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.error.palletNumberRequired", false, String.join(", ", missingPalletNumbers));
        }
        if (!existsMorePallets.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.error.morePalletsExists", false, String.join(", ", existsMorePallets));
        }
    }

    private void checkDeliveredProductsPricePerUnits(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        Set<String> deliveredProductsWithoutPrices = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS).stream()
                .filter(this::checkDeliveredProductPurchasePrice).map(this::getDeliveredProductProductNumber)
                .collect(Collectors.toSet());

        if (!deliveredProductsWithoutPrices.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.deliveredPurchasePrice.isRequired", false,
                    String.join(", ", deliveredProductsWithoutPrices));
        }
    }

    private boolean checkDeliveredProductPurchasePrice(final Entity deliveredProduct) {
        BigDecimal pricePerUnit = deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT);

        return (Objects.isNull(pricePerUnit) || (pricePerUnit.compareTo(BigDecimal.ZERO) <= 0));
    }

    private void checkDeliveredProductsExternalNumbers(final StateChangeContext stateChangeContext) {
        checkArgument(Objects.nonNull(stateChangeContext), L_ENTITY_IS_NULL);

        final Entity delivery = stateChangeContext.getOwner();

        Set<String> deliveredProductsNotSynchronized = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS)
                .stream().filter(this::checkDeliveredProductExternalNumber).map(this::getDeliveredProductProductNumber)
                .collect(Collectors.toSet());

        if (!deliveredProductsNotSynchronized.isEmpty()) {
            stateChangeContext.addValidationError("deliveries.deliveredProducts.notSynchronized", false,
                    String.join(", ", deliveredProductsNotSynchronized));
        }
    }

    private boolean checkDeliveredProductExternalNumber(final Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        return Objects.isNull(product.getStringField(ProductFields.EXTERNAL_NUMBER));
    }

    private String getOrderedProductProductNumber(final Entity orderedProduct) {
        return orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT).getStringField(ProductFields.NUMBER);
    }

    private String getDeliveredProductProductNumber(final Entity deliveredProduct) {
        return deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getStringField(ProductFields.NUMBER);
    }

    private String getDeliveredPackageProductNumber(final Entity deliveredPackage) {
        return deliveredPackage.getBelongsToField(DeliveredPackageFields.PRODUCT).getStringField(ProductFields.NUMBER);
    }

}
