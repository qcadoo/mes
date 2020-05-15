package com.qcadoo.mes.productionCounting.listeners;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.ProductUnitsConversionService;
import com.qcadoo.mes.productionCounting.constants.AnomalyExplanationFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.google.common.collect.Iterables.toArray;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class AnomalyExplanationDetailsListeners {

    @Autowired
    private ProductUnitsConversionService productUnitsConversionService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onRemoveSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent anomalyExplanationsGrid = (GridComponent) view.getComponentByReference("anomalyExplanations");
        DataDefinition dataDefinition = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_ANOMALY_EXPLANATION);
        dataDefinition.delete(toArray(anomalyExplanationsGrid.getSelectedEntitiesIds(), Long.class));
        FormComponent documentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        documentForm.performEvent(view, "reset");
    }

    public void onUseWasteChange(final ViewDefinitionState view, final ComponentState useWaste, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();
        if (anomalyExplanation.getBooleanField(AnomalyExplanationFields.USE_WASTE)) {
            anomalyExplanation.setField(AnomalyExplanationFields.PRODUCT, null);

            anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_QUANTITY, null);
            anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_UNIT, null);
            view.getComponentByReference("usedQuantity").setEnabled(false);

            anomalyExplanation.setField(AnomalyExplanationFields.LOCATION, null);
        }
        anomalyExplanation.setField(AnomalyExplanationFields.USED_QUANTITY, null);
        view.getComponentByReference("productUnit").setFieldValue(null);
        form.setEntity(anomalyExplanation);
    }

    public void selectedProductChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();
        Entity selectedProduct = anomalyExplanation.getBelongsToField(AnomalyExplanationFields.PRODUCT);

        ComponentState productUnit = view.getComponentByReference("productUnit");
        ComponentState usedQuantity = view.getComponentByReference("usedQuantity");
        ComponentState givenUnitComponent = view.getComponentByReference("givenUnit");
        if (selectedProduct != null) {
            String selectedProductUnit = selectedProduct.getStringField(ProductFields.UNIT);
            productUnit.setFieldValue(selectedProductUnit);
            usedQuantity.setEnabled(true);

            String selectedProductAdditionalUnit = selectedProduct.getStringField(ProductFields.ADDITIONAL_UNIT);
            if (isNotBlank(selectedProductAdditionalUnit)) {
                givenUnitComponent.setFieldValue(selectedProductAdditionalUnit);
            } else {
                givenUnitComponent.setFieldValue(selectedProductUnit);
            }

        } else {
            productUnit.setFieldValue(null);
            view.getComponentByReference("usedQuantity").setFieldValue(null);
            view.getComponentByReference("givenQuantity").setFieldValue(null);
            givenUnitComponent.setFieldValue(null);
            usedQuantity.setEnabled(false);
        }
        usedQuantity.performEvent(view, "onInputChange", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public void copyGivenToUsedWhenUseWasteChecked(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();

        if(anomalyExplanation.getBooleanField(AnomalyExplanationFields.USE_WASTE)){
            anomalyExplanation.setField(AnomalyExplanationFields.USED_QUANTITY,
                    anomalyExplanation.getDecimalField(AnomalyExplanationFields.GIVEN_QUANTITY));
        }
        form.setEntity(anomalyExplanation);
    }

    public void givenUnitChange(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        if (((CheckBoxComponent) view.getComponentByReference("useWaste")).isChecked()) {
            view.getComponentByReference("productUnit").setFieldValue(cs.getFieldValue());
        } else {
            calculateQuantity(view, cs, args);
        }
    }

    public void calculateQuantity(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        new CalculationHelper(view, productUnitsConversionService).genericCalculateMethod(
                h -> h.anomalyExplanation.getStringField(AnomalyExplanationFields.GIVEN_UNIT),
                AnomalyExplanationFields.GIVEN_QUANTITY,
                h -> view.getComponentByReference("productUnit").getFieldValue().toString(),
                AnomalyExplanationFields.USED_QUANTITY);
    }

    public void calculateQuantityToGiven(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        new CalculationHelper(view, productUnitsConversionService).genericCalculateMethod(
                h -> view.getComponentByReference("productUnit").getFieldValue().toString(),
                AnomalyExplanationFields.USED_QUANTITY,
                h -> h.anomalyExplanation.getStringField(AnomalyExplanationFields.GIVEN_UNIT),
                AnomalyExplanationFields.GIVEN_QUANTITY);
    }

    private static class CalculationHelper {

        private final ViewDefinitionState view;

        private final FormComponent form;

        private final Entity anomalyExplanation;

        private final Entity selectedProduct;

        private final ProductUnitsConversionService productUnitsConversionService;

        private CalculationHelper(final ViewDefinitionState view, ProductUnitsConversionService productUnitsConversionService) {
            this.view = view;
            form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();
            selectedProduct = anomalyExplanation.getBelongsToField(AnomalyExplanationFields.PRODUCT);
            this.productUnitsConversionService = productUnitsConversionService;
        }

        private void genericCalculateMethod(Function<CalculationHelper, String> thisUnitExtractor, String thisQuantityFieldName,
                Function<CalculationHelper, String> otherUnitExtractor, String otherQuantityFieldName) {

            FieldComponent thisQuantityField = (FieldComponent) view.getComponentByReference(thisQuantityFieldName);

            if (selectedProduct == null || thisQuantityField.getFieldValue() == null) {
                return;
            }

            String thisUnit = thisUnitExtractor.apply(this);
            String otherUnit = otherUnitExtractor.apply(this);

            Either<Exception, Optional<BigDecimal>> maybeQuantity = BigDecimalUtils
                    .tryParse((String) thisQuantityField.getFieldValue(), view.getLocale());

            if (maybeQuantity.isRight() && maybeQuantity.getRight().isPresent()) {
                BigDecimal otherQuantityNewValue = null;
                BigDecimal thisQuantity = maybeQuantity.getRight().get();
                if (isBlank(thisUnit) || isBlank(otherUnit)) {
                    otherQuantityNewValue = thisQuantity;
                } else {
                    java.util.Optional<BigDecimal> convertedValue = productUnitsConversionService.forProduct(selectedProduct)
                            .from(thisUnit).to(otherUnit).convertValue(thisQuantity);

                    if (convertedValue.isPresent()) {
                        otherQuantityNewValue = convertedValue.get();
                    } else {
                        String messageKey = "productionCounting.anomalyExplanation.error.noConversionFound";
                        anomalyExplanation.addError(
                                anomalyExplanation.getDataDefinition().getField(AnomalyExplanationFields.GIVEN_QUANTITY),
                                messageKey);
                    }
                }
                if (otherQuantityNewValue != null) {
                    anomalyExplanation.setField(otherQuantityFieldName, otherQuantityNewValue);
                }
            }
            form.setEntity(anomalyExplanation);
        }
    }

}
