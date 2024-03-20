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
package com.qcadoo.mes.productionCounting.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TrackingOperationProductOutComponentHooks {

    private static final int L_ONE_BATCH = 1;

    private static final String L_ID = ".id";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    public boolean validatesWith(final DataDefinition trackingOperationProductOutComponentDD,
                                 final Entity trackingOperationProductOutComponent) {
        return validateUsedQuantity(trackingOperationProductOutComponentDD, trackingOperationProductOutComponent)
                && validateStorageLocationAndPalletNumber(trackingOperationProductOutComponentDD, trackingOperationProductOutComponent);
    }

    private boolean validateUsedQuantity(final DataDefinition trackingOperationProductOutComponentDD,
                                         final Entity trackingOperationProductOutComponent) {
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        if (orderProduct.getId().equals(product.getId())) {
            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

            List<Entity> trackingOperationProductOutComponents = productionTrackingService
                    .findTrackingOperationProductOutComponents(order, technologyOperationComponent, orderProduct);

            boolean useTracking = productionTracking.getStringField(ProductionTrackingFields.STATE).equals(
                    ProductionTrackingStateStringValues.DRAFT)
                    || productionTracking.getStringField(ProductionTrackingFields.STATE).equals(
                    ProductionTrackingStateStringValues.ACCEPTED);

            if (productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTED)) {
                useTracking = false;
            }

            if (productionTracking.getBooleanField(ProductionTrackingFields.UNDERGOING_CORRECTION)) {
                useTracking = false;
            }

            BigDecimal trackedQuantity = productionTrackingService.getTrackedQuantity(trackingOperationProductOutComponent,
                    trackingOperationProductOutComponents, useTracking);

            if (!parameterService.getParameter().getBooleanField(ParameterFieldsPC.PRODUCING_MORE_THAN_PLANNED)) {
                if (trackedQuantity.compareTo(plannedQuantity) > 0) {
                    trackingOperationProductOutComponent.addError(trackingOperationProductOutComponentDD
                                    .getField(TrackingOperationProductOutComponentFields.USED_QUANTITY),
                            "productionCounting.trackingOperationProductOutComponent.error.usedQuantityGreaterThanReported");

                    return false;
                }
            }
        }

        return true;
    }

    private boolean validateStorageLocationAndPalletNumber(final DataDefinition trackingOperationProductOutComponentDD,
                                                           final Entity trackingOperationProductOutComponent) {
        BigDecimal usedQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
        Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
        Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
        String typeOfPallet = trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_PALLET);

        if (Objects.nonNull(usedQuantity) && BigDecimal.ZERO.compareTo(usedQuantity) < 0) {
            if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
                trackingOperationProductOutComponent.addError(trackingOperationProductOutComponentDD.getField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION), "productionCounting.trackingOperationProductOutComponent.error.storageLocationRequired");

                return false;
            } else {
                if (Objects.nonNull(storageLocation)) {
                    Entity location = storageLocation.getBelongsToField(StorageLocationFields.LOCATION);
                    String storageLocationNumber = storageLocation.getStringField(StorageLocationFields.NUMBER);
                    boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

                    if (placeStorageLocation) {
                        if (Objects.isNull(palletNumber)) {
                            trackingOperationProductOutComponent.addError(trackingOperationProductOutComponentDD.getField(TrackingOperationProductOutComponentFields.PALLET_NUMBER), "productionCounting.trackingOperationProductOutComponent.error.palletNumberRequired");

                            return false;
                        } else {
                            String palletNumberNumber = palletNumber.getStringField(PalletNumberFields.NUMBER);

                            if (!palletValidatorService.validatePalletNumberAndTypeOfPallet(location, storageLocation, palletNumber, typeOfPallet, trackingOperationProductOutComponent)) {
                                return false;
                            }

                            if (palletValidatorService.checkIfExistsMorePalletsForStorageLocation(location.getId(), storageLocationNumber, palletNumberNumber)) {
                                trackingOperationProductOutComponent.addError(trackingOperationProductOutComponentDD.getField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION), "productionCounting.trackingOperationProductOutComponent.error.existsOtherPalletsAtStorageLocation");

                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public void onSave(final DataDefinition trackingOperationProductOutComponentDD,
                       final Entity trackingOperationProductOutComponent) {
        fillTrackingOperationProductInComponentsQuantities(trackingOperationProductOutComponent);
        fillOrderReportedQuantity(trackingOperationProductOutComponent);
        fillStorageLocation(trackingOperationProductOutComponent);
        clearLacks(trackingOperationProductOutComponent);
        setTypeOfPallet(trackingOperationProductOutComponent);
    }

    private void fillTrackingOperationProductInComponentsQuantities(final Entity trackingOperationProductOutComponent) {
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        if (checkIfShouldFillTrackingOperationProductInComponentsQuantities(trackingOperationProductOutComponent,
                productionTracking)) {
            BigDecimal usedQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal wastesQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);

            Entity trackingOperationProductOutComponentDto = getTrackingOperationProductOutComponentDto().get(
                    trackingOperationProductOutComponent.getId());
            BigDecimal plannedQuantity = trackingOperationProductOutComponentDto
                    .getDecimalField(TrackingOperationProductOutComponentDtoFields.PLANNED_QUANTITY);
            Entity parameter = parameterService.getParameter();
            BigDecimal quantity = BigDecimalUtils.convertNullToZero(usedQuantity);
            if (parameter.getBooleanField(ParameterFieldsPC.WASTES_CONSUME_RAW_MATERIALS)) {
                quantity = BigDecimalUtils.convertNullToZero(usedQuantity).add(
                        BigDecimalUtils.convertNullToZero(wastesQuantity), numberService.getMathContext());
            }

            BigDecimal ratio = quantity.divide(plannedQuantity, numberService.getMathContext());

            List<Entity> trackingOperationProductInComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

            trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
                int usedBatches = trackingOperationProductInComponent.getHasManyField(
                        TrackingOperationProductInComponentFields.USED_BATCHES).size();

                if (usedBatches > L_ONE_BATCH) {
                    clearUsedBatches(trackingOperationProductInComponent, trackingOperationProductOutComponent);

                    fillQuantities(trackingOperationProductInComponent, ratio);
                } else if (usedBatches == L_ONE_BATCH) {
                    fillQuantitiesInBatch(trackingOperationProductInComponent, ratio);
                } else {
                    fillQuantities(trackingOperationProductInComponent, ratio);
                }
            });
        }
    }

    private boolean checkIfShouldFillTrackingOperationProductInComponentsQuantities(
            final Entity trackingOperationProductOutComponent,
            final Entity productionTracking) {
        if (Objects.isNull(trackingOperationProductOutComponent.getId())) {
            return false;
        }

        boolean enteredFromTerminal = BooleanUtils.isTrue(trackingOperationProductOutComponent
                .getBooleanField(TrackingOperationProductOutComponentFields.ENTERED_FROM_TERMINAL));

        if (enteredFromTerminal) {
            return false;
        }

        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity parameter = parameterService.getParameter();

        boolean allowToOverrideQuantitiesFromTerminal = BooleanUtils.isTrue(parameter.getBooleanField(
                ParameterFieldsPC.ALLOW_CHANGES_TO_USED_QUANTITY_ON_TERMINAL));

        return !trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL)
                .equals(ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue()) &&
                (parameter.getBooleanField(ParameterFieldsPC.CONSUMPTION_OF_RAW_MATERIALS_BASED_ON_STANDARDS)
                        && allowToOverrideQuantitiesFromTerminal && (TypeOfProductionRecording.FOR_EACH
                        .getStringValue().equals(typeOfProductionRecording) || (TypeOfProductionRecording.CUMULATED.getStringValue()
                        .equals(typeOfProductionRecording) && product.getId().equals(orderProduct.getId()))));
    }

    private void fillQuantities(final Entity trackingOperationProductInComponent, final BigDecimal ratio) {
        Entity trackingOperationProductInComponentDto = getTrackingOperationProductInComponentDto().get(
                trackingOperationProductInComponent.getId());

        BigDecimal plannedQuantity = trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductInComponentDtoFields.PLANNED_QUANTITY);
        BigDecimal usedQuantity = plannedQuantity.multiply(ratio, numberService.getMathContext());

        Optional<BigDecimal> givenQuantity = productionTrackingService.calculateGivenQuantity(
                trackingOperationProductInComponent, usedQuantity);

        if (!givenQuantity.isPresent()) {
            trackingOperationProductInComponent.addError(
                    trackingOperationProductInComponent.getDataDefinition().getField(
                            TrackingOperationProductInComponentFields.GIVEN_QUANTITY),
                    "technologies.operationProductInComponent.validate.error.missingUnitConversion");
        }

        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                numberService.setScaleWithDefaultMathContext(usedQuantity));
        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                numberService.setScaleWithDefaultMathContext(givenQuantity.orElse(usedQuantity)));

        trackingOperationProductInComponent.getDataDefinition().save(trackingOperationProductInComponent);
    }

    private void fillQuantitiesInBatch(final Entity trackingOperationProductInComponent, final BigDecimal ratio) {
        Optional<Entity> isBatch = trackingOperationProductInComponent
                .getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES).stream().findFirst();

        if (isBatch.isPresent()) {
            Entity batch = isBatch.get();

            Entity trackingOperationProductInComponentDto = getTrackingOperationProductInComponentDto().get(
                    trackingOperationProductInComponent.getId());
            BigDecimal plannedQuantity = trackingOperationProductInComponentDto
                    .getDecimalField(TrackingOperationProductInComponentDtoFields.PLANNED_QUANTITY);
            BigDecimal usedQuantity = plannedQuantity.multiply(ratio, numberService.getMathContext());

            Optional<BigDecimal> givenQuantity = productionTrackingService.calculateGivenQuantity(
                    trackingOperationProductInComponent, usedQuantity);

            if (!givenQuantity.isPresent()) {
                trackingOperationProductInComponent.addError(
                        trackingOperationProductInComponent.getDataDefinition().getField(
                                TrackingOperationProductInComponentFields.GIVEN_QUANTITY),
                        "technologies.operationProductInComponent.validate.error.missingUnitConversion");
            }

            batch.setField(UsedBatchFields.QUANTITY, numberService.setScaleWithDefaultMathContext(usedQuantity));

            batch.getDataDefinition().save(batch);
        }
    }

    private void clearUsedBatches(Entity trackingOperationProductInComponent,
                                  final Entity trackingOperationProductOutComponent) {
        trackingOperationProductInComponent
                .setField(TrackingOperationProductInComponentFields.USED_BATCHES, Lists.newArrayList());

        trackingOperationProductInComponent = trackingOperationProductInComponent.getDataDefinition().save(
                trackingOperationProductInComponent);

        trackingOperationProductOutComponent.addGlobalMessage(
                "technologies.operationProductInComponent.info.consumptionOfRawMaterialsBasedOnStandards.typeBatch",
                trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)
                        .getStringField(ProductFields.NUMBER));
    }

    private void fillOrderReportedQuantity(final Entity trackingOperationProductOutComponent) {
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        if (orderProduct.getId().equals(product.getId())) {
            List<Entity> trackingOperationProductOutComponents = productionTrackingService
                    .findTrackingOperationProductOutComponents(order, technologyOperationComponent, orderProduct);

            boolean useTracking = productionTracking.getStringField(ProductionTrackingFields.STATE).equals(
                    ProductionTrackingStateStringValues.DRAFT)
                    || productionTracking.getStringField(ProductionTrackingFields.STATE).equals(
                    ProductionTrackingStateStringValues.ACCEPTED);

            if (productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTED)) {
                useTracking = false;
            }

            if (productionTracking.getBooleanField(ProductionTrackingFields.UNDERGOING_CORRECTION)) {
                useTracking = false;
            }

            BigDecimal trackedQuantity = productionTrackingService.getTrackedQuantity(trackingOperationProductOutComponent,
                    trackingOperationProductOutComponents, useTracking);

            Entity orderDb = order.getDataDefinition().get(order.getId());

            orderDb.setField(OrderFields.REPORTED_PRODUCTION_QUANTITY, trackedQuantity);

            orderDb.getDataDefinition().fastSave(orderDb);
        }
    }

    private void fillStorageLocation(final Entity trackingOperationProductOutComponent) {
        if (Objects.isNull(trackingOperationProductOutComponent.getId())) {
            Entity productionTracking = trackingOperationProductOutComponent
                    .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);
            Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            if (orderProduct.getId().equals(product.getId())) {
                Entity productionCountingQuantity = getProductionCountingQuantity(order, product);

                if (Objects.nonNull(productionCountingQuantity)) {
                    Entity productsInputLocation = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION);

                    if (Objects.nonNull(productsInputLocation)) {
                        Optional<Entity> mayBeStorageLocation = materialFlowResourcesService.findStorageLocationForProduct(productsInputLocation, product);

                        if (mayBeStorageLocation.isPresent()) {
                            Entity storageLocation = mayBeStorageLocation.get();

                            trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION, storageLocation);
                        }
                    }
                }
            }
        }
    }

    private void clearLacks(final Entity trackingOperationProductOutComponent) {
        if (!trackingOperationProductOutComponent.getBooleanField(TrackingOperationProductOutComponentFields.MANY_REASONS_FOR_LACKS)) {
            trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.LACKS, Lists.newArrayList());
        }
    }

    private void setTypeOfPallet(final Entity trackingOperationProductOutComponent) {
        Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);

        if (Objects.isNull(palletNumber)) {
            trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.TYPE_OF_PALLET, null);
        }
    }

    private Entity getProductionCountingQuantity(final Entity order, final Entity product) {
        return basicProductionCountingService.getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ORDER + L_ID, order.getId()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.PRODUCT + L_ID, product.getId()))
                .setMaxResults(1).uniqueResult();
    }

    private DataDefinition getTrackingOperationProductOutComponentDto() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT_DTO);
    }

    private DataDefinition getTrackingOperationProductInComponentDto() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT_DTO);
    }

}
