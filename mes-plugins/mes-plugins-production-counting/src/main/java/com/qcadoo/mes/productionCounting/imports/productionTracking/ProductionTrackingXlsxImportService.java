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
package com.qcadoo.mes.productionCounting.imports.productionTracking;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.hooks.ProductionTrackingHooks;
import com.qcadoo.mes.technologies.constants.BarcodeOperationComponentFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductionTrackingXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";


    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionTrackingHooks productionTrackingHooks;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity entity = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(entity);

        return entity;
    }

    private void setRequiredFields(final Entity productionTracking) {
    }

    @Override
    public void validateEntity(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        validateOrder(productionTracking, productionTrackingDD);
        validateStaff(productionTracking, productionTrackingDD);
        validateDates(productionTracking, productionTrackingDD);
        validateProducts(productionTracking);
    }

    private void validateProducts(Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        if (order != null) {
            productionTrackingHooks.copyProducts(productionTracking);
            List<Entity> trackingOperationProductOutComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
            Optional<Entity> mainOutProduct = trackingOperationProductOutComponents.stream().
                    filter(e -> (e.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue())
                            || e.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL).equals(ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()))).findFirst();
            if (mainOutProduct.isPresent()) {
                Entity outProduct = mainOutProduct.get();
                BigDecimal quantity = numberService.setScaleWithDefaultMathContext(productionTracking.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY));

                if (quantity != null && quantity.compareTo(BigDecimal.ZERO) == 0) {
                    outProduct.addError(outProduct.getDataDefinition().getField(TrackingOperationProductOutComponentFields.USED_QUANTITY), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }

                outProduct.setField(TrackingOperationProductOutComponentFields.USED_QUANTITY, quantity);
                outProduct.setField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY, productionTracking.getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY));
                outProduct.setField(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES, productionTracking.getStringField(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES));
                fillTrackingOperationProductInComponentsQuantities(outProduct, productionTracking);
                fillGivenQuantityAndUnit(outProduct, quantity);
            }
        }
    }

    private void fillGivenQuantityAndUnit(Entity outProduct, BigDecimal quantity) {
        if (quantity != null) {
            Entity product = outProduct.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
            String unit = product.getStringField(ProductFields.UNIT);
            String givenUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            BigDecimal givenQuantity = quantity;

            if (Objects.nonNull(givenUnit) && !givenUnit.equals(unit)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                if (unitConversions.isDefinedFor(givenUnit)) {
                    givenQuantity = unitConversions.convertTo(quantity, givenUnit, BigDecimal.ROUND_FLOOR);
                } else {
                    givenUnit = unit;
                }
            } else {
                givenUnit = unit;
            }
            outProduct.setField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY, givenQuantity);
            outProduct.setField(TrackingOperationProductOutComponentFields.GIVEN_UNIT,
                    givenUnit);
        }
    }

    private void fillTrackingOperationProductInComponentsQuantities(final Entity trackingOperationProductOutComponent, Entity productionTracking) {
        if (checkIfShouldFillTrackingOperationProductInComponentsQuantities(trackingOperationProductOutComponent,
                productionTracking.getBelongsToField(ProductionTrackingFields.ORDER))) {
            BigDecimal usedQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal wastesQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);

            BigDecimal plannedQuantity = getProductPlannedQuantity(productionTracking.getBelongsToField(ProductionTrackingFields.ORDER), productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT), trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT), ProductionCountingQuantityRole.PRODUCED.getStringValue());
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
                fillQuantities(trackingOperationProductInComponent, productionTracking.getBelongsToField(ProductionTrackingFields.ORDER), productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT), ratio);
            });
        }
    }

    private void fillQuantities(final Entity trackingOperationProductInComponent, Entity order, Entity toc, final BigDecimal ratio) {
        BigDecimal plannedQuantity = getProductPlannedQuantity(order, toc, trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT), ProductionCountingQuantityRole.USED.getStringValue());
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
    }

    private boolean checkIfShouldFillTrackingOperationProductInComponentsQuantities(
            final Entity trackingOperationProductOutComponent,
            final Entity order) {
        Entity product = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
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

    private void validateOrder(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        Entity barcodeOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity order = null;
        if (barcodeOperationComponent != null) {
            order = barcodeOperationComponent.getBelongsToField(BarcodeOperationComponentFields.ORDER);
            productionTracking.setField(ProductionTrackingFields.ORDER, order);
            if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                productionTracking.setField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, barcodeOperationComponent.getBelongsToField(BarcodeOperationComponentFields.OPERATION_COMPONENT));
            }
        }
        if (order == null || !OrderStateStringValues.IN_PROGRESS.equals(order.getStringField(OrderFields.STATE))) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER), "productionCounting.productionTrackingsList.window.mainTab.productionTrackingsList.grid.error.copy");
        }
    }

    private void validateStaff(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        Entity staff = productionTracking.getBelongsToField(ProductionTrackingFields.STAFF);
        if (staff != null) {
            productionTracking.setField(ProductionTrackingFields.DIVISION, staff.getBelongsToField(StaffFields.DIVISION));
        }
    }


    private void validateDates(final Entity productionTracking, final DataDefinition productionTrackingDD) {
        Date timeRangeFrom = productionTracking.getDateField(ProductionTrackingFields.TIME_RANGE_FROM);
        Date timeRangeTo = productionTracking.getDateField(ProductionTrackingFields.TIME_RANGE_TO);

        if (timeRangeFrom != null && timeRangeTo != null && (timeRangeFrom.after(timeRangeTo) || timeRangeFrom.equals(timeRangeTo))) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.TIME_RANGE_TO), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }

        if (timeRangeFrom != null) {
            productionTracking.setField(ProductionTrackingFields.SHIFT_START_DAY, new DateTime(timeRangeFrom).withTimeAtStartOfDay().toDate());
        }
    }

    public BigDecimal getProductPlannedQuantity(Entity order, Entity toc, Entity product, String role) {
        SearchCriteriaBuilder scb = getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, role));

        if (toc != null) {
            scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        }

        return scb.list().getEntities().stream().map(e -> e.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

}
