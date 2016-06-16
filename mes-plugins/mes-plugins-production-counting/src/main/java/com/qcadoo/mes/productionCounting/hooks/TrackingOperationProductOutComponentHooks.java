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
package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.SetTrackingOperationProductsComponentsService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.hooks.helpers.AbstractPlannedQuantitiesCounter;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TrackingOperationProductOutComponentHooks extends AbstractPlannedQuantitiesCounter {

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private SetTrackingOperationProductsComponentsService setTrackingOperationProductsComponentsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionCountingService productionCountingService;

    public TrackingOperationProductOutComponentHooks() {
        super(ProductionCountingQuantityRole.PRODUCED);
    }

    public void onView(final DataDefinition trackingOperationProductOutComponentDD,
            final Entity trackingOperationProductOutComponent) {
        fillQuantities(trackingOperationProductOutComponent);
    }

    private void fillQuantities(final Entity trackingOperationProductOutComponent) {

        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity operation = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        BigDecimal plannedQuantity = getPlannedQuantity(trackingOperationProductOutComponent);
        BigDecimal producedSum = productionCountingService.getUsedQuantitySumForProduct(product, order, operation);
        BigDecimal wastesSum = productionCountingService.getWastesSumForProduct(product, order, operation);
        BigDecimal remainingQuantity = plannedQuantity.subtract(producedSum);
        if (BigDecimal.ZERO.compareTo(remainingQuantity) == 1) {
            remainingQuantity = BigDecimal.ZERO;
        }
        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.PLANNED_QUANTITY,
                plannedQuantity);
        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.WASTES_SUM, wastesSum);
        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.PRODUCED_SUM, producedSum);
        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.REMAINING_QUANTITY,
                remainingQuantity);

    }

    public void onSave(final DataDefinition trackingOperationProductOutComponentDD, Entity trackingOperationProductOutComponent) {
        fillTrackingOperationProductInComponentsQuantities(trackingOperationProductOutComponent);
        fillSetTrackingOperationProductsInComponents(trackingOperationProductOutComponent);
    }

    private void fillTrackingOperationProductInComponentsQuantities(final Entity trackingOperationProductOutComponent) {
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        if (checkIfShouldfillTrackingOperationProductInComponentsQuantities(productionTracking, product)) {
            BigDecimal usedQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal wastesQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);

            if ((usedQuantity != null) || (wastesQuantity != null)) {
                BigDecimal plannedQuantity = getPlannedQuantity(trackingOperationProductOutComponent);

                BigDecimal quantity = BigDecimalUtils.convertNullToZero(usedQuantity).add(BigDecimalUtils.convertNullToZero(wastesQuantity), numberService.getMathContext());

                BigDecimal ratio = quantity.divide(plannedQuantity, numberService.getMathContext());

                List<Entity> trackingOperationProductInComponents = productionTracking
                        .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

                trackingOperationProductInComponents.stream().forEach(trackingOperationProductInComponent -> {
                    fillQuantities(trackingOperationProductInComponent, ratio);
                });
            }
        }
    }

    private boolean checkIfShouldfillTrackingOperationProductInComponentsQuantities(final Entity productionTracking,
            final Entity product) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        return (parameterService.getParameter()
                .getBooleanField(ParameterFieldsPC.CONSUMPTION_OF_RAW_MATERIALS_BASED_ON_STANDARDS) && (TypeOfProductionRecording.FOR_EACH
                .getStringValue().equals(typeOfProductionRecording) || (TypeOfProductionRecording.CUMULATED.getStringValue()
                .equals(typeOfProductionRecording) && product.getId().equals(orderProduct.getId()))));
    }

    private void fillQuantities(Entity trackingOperationProductInComponent, final BigDecimal ratio) {
        BigDecimal plannedQuantity = trackingOperationProductInComponent
                .getDecimalField(TrackingOperationProductInComponentFields.PLANNED_QUANTITY);
        BigDecimal usedQuantity = plannedQuantity.multiply(ratio, numberService.getMathContext());

        BigDecimal givenQuantity = calculateGivenQuantity(trackingOperationProductInComponent, usedQuantity);

        if (givenQuantity == null) {
            trackingOperationProductInComponent.addError(
                    trackingOperationProductInComponent.getDataDefinition().getField(
                            TrackingOperationProductInComponentFields.GIVEN_QUANTITY),
                    "technologies.operationProductInComponent.validate.error.missingUnitConversion");
        }

        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                numberService.setScale(usedQuantity));
        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY,
                numberService.setScale(givenQuantity));

        trackingOperationProductInComponent = trackingOperationProductInComponent.getDataDefinition().save(
                trackingOperationProductInComponent);
    }

    private BigDecimal calculateGivenQuantity(final Entity trackingOperationProductInComponent, final BigDecimal usedQuantity) {
        BigDecimal givenQuantity = null;

        Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

        String givenUnit = trackingOperationProductInComponent
                .getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);

        String unit = product.getStringField(ProductFields.UNIT);

        if (givenUnit == null) {
            givenUnit = unit;
        }

        if (givenUnit.equals(unit)) {
            givenQuantity = usedQuantity;
        } else {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                            UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                givenQuantity = unitConversions.convertTo(usedQuantity, givenUnit);
            }
        }

        return givenQuantity;
    }

    private void fillSetTrackingOperationProductsInComponents(Entity trackingOperationProductOutComponent) {
        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        BigDecimal givenQuantity = trackingOperationProductOutComponent
                .getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);

        trackingOperationProductOutComponent = setTrackingOperationProductsComponentsService
                .recalculateTrackingOperationProductOutComponent(productionTracking, trackingOperationProductOutComponent,
                        givenQuantity);

        List<Entity> setTrackingOperationProductsInComponents = trackingOperationProductOutComponent
                .getHasManyField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS);

        setTrackingOperationProductsInComponents.stream().forEach(setTrackingOperationProductsInComponent -> {
            setTrackingOperationProductsInComponent.getDataDefinition().save(setTrackingOperationProductsInComponent);
        });
    }

}
