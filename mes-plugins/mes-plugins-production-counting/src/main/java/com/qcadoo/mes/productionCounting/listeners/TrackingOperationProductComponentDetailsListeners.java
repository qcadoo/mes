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
package com.qcadoo.mes.productionCounting.listeners;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper.L_WAREHOUSE;

@Service
public class TrackingOperationProductComponentDetailsListeners {

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    private static final Set<String> UNIT_COMPONENT_REFERENCES = Sets.newHashSet("plannedQuantityUNIT", "usedQuantityUNIT", "givenUnit", "producedSumUNIT", "wastesSumUNIT", "remainingQuantityUNIT", "wastesQuantityUNIT");

    private static final String L_PRODUCT = "product";

    private static final String L_NAME = "name";

    private static final String L_NUMBER = "number";

    private static final String L_ID = ".id";

    public void onBeforeRender(final ViewDefinitionState view) {
        Entity product = getFormEntity(view).getBelongsToField(L_PRODUCT);

        fillUnits(view, product);
        fillFieldFromProduct(view, product);
    }

    private Entity getFormEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        return form.getPersistedEntityWithIncludedFormValues();
    }

    private void fillUnits(final ViewDefinitionState view, final Entity productEntity) {
        String unit = productEntity.getStringField(ProductFields.UNIT);
        String additionalUnit = productEntity.getStringField(ProductFields.ADDITIONAL_UNIT);

        for (String componentReferenceName : UNIT_COMPONENT_REFERENCES) {
            FieldComponent unitField = (FieldComponent) view.getComponentByReference(componentReferenceName);

            if (Objects.nonNull(unitField) && StringUtils.isEmpty((String) unitField.getFieldValue())) {
                unitField.setFieldValue(unit);
                unitField.requestComponentUpdateState();
            }
        }

        if (!StringUtils.isEmpty(additionalUnit)) {
            FieldComponent givenUnitField = (FieldComponent) view.getComponentByReference(TrackingOperationProductInComponentFields.GIVEN_UNIT);

            givenUnitField.setFieldValue(additionalUnit);
            givenUnitField.setEnabled(false);
            givenUnitField.requestComponentUpdateState();
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view, final Entity productEntity) {
        view.getComponentByReference(L_NUMBER).setFieldValue(productEntity.getField(L_NUMBER));
        view.getComponentByReference(L_NAME).setFieldValue(productEntity.getField(L_NAME));
    }

    public void givenQuantityChanged(final ViewDefinitionState view, final ComponentState componentState,
                                     final String[] args) {
        calculateQuantity(view, componentState, args);
    }

    public void givenQuantityChangedIn(final ViewDefinitionState view, final ComponentState componentState,
                                       final String[] args) {
        calculateQuantity(view, componentState, args);
    }

    public void calculateQuantity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent trackingOperationProductComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity trackingOperationProductComponent = trackingOperationProductComponentForm.getPersistedEntityWithIncludedFormValues();

        String givenUnit = trackingOperationProductComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);
        Entity product = trackingOperationProductComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

        FieldComponent givenQuantityField = (FieldComponent) view.getComponentByReference(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);

        if (Objects.isNull(product) || Objects.isNull(givenUnit) || givenUnit.isEmpty() || Objects.isNull(givenQuantityField.getFieldValue())) {
            return;
        }

        Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParse((String) givenQuantityField.getFieldValue(), view.getLocale());

        if (maybeQuantity.isRight()) {
            if (maybeQuantity.getRight().isPresent()) {
                BigDecimal givenQuantity = maybeQuantity.getRight().get();
                String baseUnit = product.getStringField(ProductFields.UNIT);

                if (baseUnit.equals(givenUnit)) {
                    trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, givenQuantity);
                } else {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(givenUnit, searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                    if (unitConversions.isDefinedFor(baseUnit)) {
                        BigDecimal convertedQuantity = unitConversions.convertTo(givenQuantity, baseUnit);

                        trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, convertedQuantity);
                    } else {
                        trackingOperationProductComponent.addError(trackingOperationProductComponent.getDataDefinition().getField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY), "technologies.operationProductInComponent.validate.error.missingUnitConversion");

                        trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, null);
                    }
                }
            } else {
                trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, null);
            }
        } else {
            trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY, null);
        }

        trackingOperationProductComponentForm.setEntity(trackingOperationProductComponent);
    }

    public void calculateQuantityToGiven(final ViewDefinitionState view, final ComponentState state,
                                         final String[] args) {
        FormComponent trackingOperationProductComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity trackingOperationProductComponent = trackingOperationProductComponentForm.getPersistedEntityWithIncludedFormValues();

        Entity product = trackingOperationProductComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

        if (Objects.isNull(product)) {
            return;
        }

        String unit = product.getStringField(ProductFields.UNIT);

        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(TrackingOperationProductInComponentFields.USED_QUANTITY);

        if (StringUtils.isEmpty(unit) || Objects.isNull(quantityField.getFieldValue())) {
            return;
        }

        Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils.tryParse((String) quantityField.getFieldValue(), view.getLocale());

        if (maybeQuantity.isRight()) {
            if (maybeQuantity.getRight().isPresent()) {
                BigDecimal quantity = maybeQuantity.getRight().get();
                String givenUnit = trackingOperationProductComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT);

                if (givenUnit.equals(unit)) {
                    trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, quantity);
                } else {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit, searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                    if (unitConversions.isDefinedFor(givenUnit)) {
                        BigDecimal convertedQuantity = unitConversions.convertTo(quantity, givenUnit, BigDecimal.ROUND_FLOOR);

                        trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, convertedQuantity);
                    } else {
                        trackingOperationProductComponent.addError(trackingOperationProductComponent.getDataDefinition().getField(TrackingOperationProductInComponentFields.USED_QUANTITY), "technologies.operationProductInComponent.validate.error.missingUnitConversion");

                        trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, null);
                    }
                }
            } else {
                trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, null);
            }
        } else {
            trackingOperationProductComponent.setField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY, null);
        }

        trackingOperationProductComponentForm.setEntity(trackingOperationProductComponent);
    }

    public void onManyReasonsForLacks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent wastesQuantity = (FieldComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);
        FieldComponent causeOfWastes = (FieldComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES);

        CheckBoxComponent manyReasonsForLacks = (CheckBoxComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.MANY_REASONS_FOR_LACKS);

        if (manyReasonsForLacks.isChecked()) {
            wastesQuantity.setEnabled(false);
            causeOfWastes.setEnabled(false);
            wastesQuantity.setFieldValue(null);
            causeOfWastes.setFieldValue(null);
            causeOfWastes.requestComponentUpdateState();
            wastesQuantity.requestComponentUpdateState();
        } else {
            wastesQuantity.setFieldValue(null);
            wastesQuantity.setEnabled(true);
            causeOfWastes.setEnabled(true);
            causeOfWastes.requestComponentUpdateState();
            wastesQuantity.requestComponentUpdateState();
        }
    }

    public void onRemoveLack(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent trackingOperationProductComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        trackingOperationProductComponentForm.performEvent(view, "reset");
    }

    public void fillTypeOfLoadUnitField(final ViewDefinitionState view, final ComponentState state,
                                        final String[] args) {
        FormComponent trackingOperationProductOutComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.TYPE_OF_LOAD_UNIT);

        Entity trackingOperationProductOutComponent = trackingOperationProductOutComponentForm.getPersistedEntityWithIncludedFormValues();

        Entity productionTracking = trackingOperationProductOutComponent
                .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        Entity productionCountingQuantity = getProductionCountingQuantity(order, product);

        if (Objects.nonNull(productionCountingQuantity)) {
            Entity warehouse = getWarehouse(productionCountingQuantity);

            if (Objects.nonNull(warehouse)) {
                Long typeOfLoadUnit = null;
                Entity palletNumber = palletNumberLookup.getEntity();
                if (Objects.nonNull(palletNumber)) {
                    typeOfLoadUnit = getTypeLoadUnit(productionTracking, warehouse, palletNumber);
                }
                typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit);
                typeOfLoadUnitLookup.requestComponentUpdateState();
            }
        }
    }

    private Entity getWarehouse(Entity productionCountingQuantity) {
        if (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
            return productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.WASTE_RECEPTION_WAREHOUSE);
        } else if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
            return productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION);
        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue()
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                && L_WAREHOUSE
                .equals(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.PRODUCTION_FLOW))) {
            return productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION);
        }
        return null;
    }

    private Long getTypeLoadUnit(Entity productionTracking, Entity warehouse, Entity palletNumber) {
        Long typeOfLoadUnit = materialFlowResourcesService.getTypeOfLoadUnitByPalletNumber(warehouse.getId(), palletNumber.getStringField(PalletNumberFields.NUMBER));
        if (typeOfLoadUnit == null) {
            for (Entity topoc : productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS)) {
                Entity topocPalletNumber = topoc.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
                if (topocPalletNumber != null && topocPalletNumber.getId().equals(palletNumber.getId())) {
                    Entity topocTypeOfLoadUnit = topoc.getBelongsToField(TrackingOperationProductOutComponentFields.TYPE_OF_LOAD_UNIT);
                    if (topocTypeOfLoadUnit != null) {
                        typeOfLoadUnit = topocTypeOfLoadUnit.getId();
                    }
                    break;
                }
            }
        }

        return typeOfLoadUnit;
    }

    private Entity getProductionCountingQuantity(final Entity order, final Entity product) {
        return basicProductionCountingService.getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ORDER + L_ID, order.getId()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.PRODUCT + L_ID, product.getId()))
                .setMaxResults(1).uniqueResult();
    }

}
