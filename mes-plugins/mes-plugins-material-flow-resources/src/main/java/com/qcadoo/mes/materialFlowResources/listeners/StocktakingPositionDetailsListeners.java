package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class StocktakingPositionDetailsListeners {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Autowired
    private NumberService numberService;

    public void fillTypeOfLoadUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.TYPE_OF_LOAD_UNIT);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity location = form.getEntity().getBelongsToField(StocktakingPositionFields.STOCKTAKING).getBelongsToField(StocktakingFields.LOCATION);
        Entity palletNumber = palletNumberLookup.getEntity();
        Long typeOfLoadUnit = null;

        if (Objects.nonNull(palletNumber)) {
            typeOfLoadUnit = materialFlowResourcesService.getTypeOfLoadUnitByPalletNumber(location.getId(), palletNumber.getStringField(PalletNumberFields.NUMBER));
        }

        typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit);
        typeOfLoadUnitLookup.requestComponentUpdateState();
    }

    public void onProductChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.PRODUCT);
        Entity product = productLookup.getEntity();

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

            FieldComponent conversionField = (FieldComponent) view.getComponentByReference(StocktakingPositionFields.CONVERSION);

            if (!StringUtils.isEmpty(additionalUnit)) {
                String conversion = numberService
                        .formatWithMinimumFractionDigits(materialFlowResourcesService.getConversion(product, unit, additionalUnit, null), 0);

                conversionField.setFieldValue(conversion);
                conversionField.setEnabled(true);
                conversionField.requestComponentUpdateState();
            }

            quantityChange(view, null, null);
        }
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.BATCH);
        batchLookup.setFieldValue(null);
        batchLookup.requestComponentUpdateState();
    }

    public void quantityChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity position = form.getEntity();

        Entity product = position.getBelongsToField(StocktakingPositionFields.PRODUCT);

        if (decimalFieldsInvalid(form) || Objects.isNull(product)) {
            return;
        }

        BigDecimal quantity = position.getDecimalField(StocktakingPositionFields.QUANTITY);

        FieldComponent additionalQuantityField = (FieldComponent) view
                .getComponentByReference(StocktakingPositionFields.QUANTITY_IN_ADDITIONAL_UNIT);
        if (Objects.nonNull(quantity)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);

            BigDecimal conversion = materialFlowResourcesService.getConversion(product, unit, additionalUnit, position.getDecimalField(StocktakingPositionFields.CONVERSION));
            BigDecimal newAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity,
                    conversion, additionalUnit);

            additionalQuantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(newAdditionalQuantity, 0));
            additionalQuantityField.requestComponentUpdateState();
        } else {
            additionalQuantityField.setFieldValue(null);
            additionalQuantityField.requestComponentUpdateState();
        }
    }

    private boolean decimalFieldsInvalid(final FormComponent formComponent) {
        String[] fieldNames = {StocktakingPositionFields.QUANTITY_IN_ADDITIONAL_UNIT, StocktakingPositionFields.CONVERSION,
                StocktakingPositionFields.QUANTITY};

        boolean valid = false;

        Entity entity = formComponent.getEntity();

        for (String fieldName : fieldNames) {
            try {
                entity.getDecimalField(fieldName);
            } catch (IllegalArgumentException e) {
                if (!StocktakingPositionFields.QUANTITY.equals(fieldName)) {
                    formComponent.findFieldComponentByName(fieldName).addMessage(
                            "qcadooView.validate.field.error.invalidNumericFormat", ComponentState.MessageType.FAILURE);
                }

                valid = true;
            }
        }

        return valid;
    }

    public void additionalQuantityChange(final ViewDefinitionState view, final ComponentState state,
                                         final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity position = form.getEntity();

        Entity product = position.getBelongsToField(StocktakingPositionFields.PRODUCT);

        if (decimalFieldsInvalid(form) || Objects.isNull(product)) {
            return;
        }

        BigDecimal additionalQuantity = position.getDecimalField(StocktakingPositionFields.QUANTITY_IN_ADDITIONAL_UNIT);

        FieldComponent quantityField = (FieldComponent) view
                .getComponentByReference(StocktakingPositionFields.QUANTITY);

        if (Objects.nonNull(additionalQuantity)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = Optional.ofNullable(product.getStringField(ProductFields.ADDITIONAL_UNIT)).orElse(
                    unit);

            BigDecimal conversion = materialFlowResourcesService.getConversion(product, unit, additionalUnit, position.getDecimalField(StocktakingPositionFields.CONVERSION));
            BigDecimal newQuantity = calculationQuantityService.calculateQuantity(additionalQuantity,
                    conversion, unit);

            quantityField.setFieldValue(numberService.formatWithMinimumFractionDigits(newQuantity, 0));
            quantityField.requestComponentUpdateState();
        } else {
            quantityField.setFieldValue(null);
            quantityField.requestComponentUpdateState();
        }
    }

}
