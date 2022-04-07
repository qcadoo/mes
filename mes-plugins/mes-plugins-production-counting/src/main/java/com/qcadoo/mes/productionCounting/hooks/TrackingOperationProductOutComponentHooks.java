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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentDtoFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentDtoFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrackingOperationProductOutComponentHooks {

    public static final int L_ONE_BATCH = 1;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    public void onSave(final DataDefinition trackingOperationProductOutComponentDD, Entity trackingOperationProductOutComponent) {
        fillTrackingOperationProductInComponentsQuantities(trackingOperationProductOutComponent);
        fillOrderReportedQuantity(trackingOperationProductOutComponent);
    }

    public boolean validatesWith(final DataDefinition trackingOperationProductOutComponentDD,
                                 final Entity trackingOperationProductOutComponent) {
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        if (orderProduct.getId().equals(product.getId())) {
            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

            List<Entity> trackings = productionTrackingService
                    .findTrackingOperationProductOutComponents(order, toc, orderProduct);
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
                    trackings, useTracking);

            if (!parameterService.getParameter().getBooleanField("producingMoreThanPlanned")) {
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

    private void fillOrderReportedQuantity(final Entity trackingOperationProductOutComponent) {

        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
        if (orderProduct.getId().equals(product.getId())) {

            List<Entity> trackings = productionTrackingService
                    .findTrackingOperationProductOutComponents(order, toc, orderProduct);
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
                    trackings, useTracking);

            Entity orderDb = order.getDataDefinition().get(order.getId());
            orderDb.setField(OrderFields.REPORTED_PRODUCTION_QUANTITY, trackedQuantity);
            orderDb.getDataDefinition().fastSave(orderDb);

        }
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

            if ((usedQuantity != null) || (wastesQuantity != null)) {
                Entity trackingOperationProductOutComponentDto = dataDefinitionService.get(
                        ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT_DTO).get(
                        trackingOperationProductOutComponent.getId());
                BigDecimal plannedQuantity = trackingOperationProductOutComponentDto
                        .getDecimalField(TrackingOperationProductOutComponentDtoFields.PLANNED_QUANTITY);

                BigDecimal quantity = BigDecimalUtils.convertNullToZero(usedQuantity).add(
                        BigDecimalUtils.convertNullToZero(wastesQuantity), numberService.getMathContext());

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
    }

    private void clearUsedBatches(Entity trackingOperationProductInComponent, Entity trackingOperationProductOutComponent) {
        trackingOperationProductInComponent
                .setField(TrackingOperationProductInComponentFields.USED_BATCHES, Lists.newArrayList());
        trackingOperationProductInComponent = trackingOperationProductInComponent.getDataDefinition().save(
                trackingOperationProductInComponent);
        trackingOperationProductOutComponent.addGlobalMessage(
                "technologies.operationProductInComponent.info.consumptionOfRawMaterialsBasedOnStandards.typeBatch",
                trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT)
                        .getStringField(ProductFields.NUMBER));
    }

    private boolean checkIfShouldFillTrackingOperationProductInComponentsQuantities(Entity trackingOperationProductOutComponent,
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

        boolean allowToOverrideQuantitiesFromTerminal = BooleanUtils.isTrue(parameterService.getParameter().getBooleanField(
                ParameterFieldsPC.ALLOW_CHANGES_TO_USED_QUANTITY_ON_TERMINAL));

        return !trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL)
                .equals(ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue()) &&
                (parameterService.getParameter()
                        .getBooleanField(ParameterFieldsPC.CONSUMPTION_OF_RAW_MATERIALS_BASED_ON_STANDARDS)
                        && allowToOverrideQuantitiesFromTerminal && (TypeOfProductionRecording.FOR_EACH
                        .getStringValue().equals(typeOfProductionRecording) || (TypeOfProductionRecording.CUMULATED.getStringValue()
                        .equals(typeOfProductionRecording) && product.getId().equals(orderProduct.getId()))));
    }

    private void fillQuantities(Entity trackingOperationProductInComponent, final BigDecimal ratio) {
        Entity trackingOperationProductInComponentDto = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT_DTO).get(
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
            Entity trackingOperationProductInComponentDto = dataDefinitionService.get(
                    ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT_DTO).get(
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

}
