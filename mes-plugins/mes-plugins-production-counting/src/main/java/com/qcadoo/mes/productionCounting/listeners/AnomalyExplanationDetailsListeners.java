package com.qcadoo.mes.productionCounting.listeners;

import static com.google.common.collect.Iterables.toArray;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigDecimal;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.productionCounting.constants.AnomalyExplanationFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class AnomalyExplanationDetailsListeners {

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onRemoveSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent anomalyExplanationsGrid = (GridComponent) view.getComponentByReference("anomalyExplanations");
        DataDefinition dataDefinition = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_ANOMALY_EXPLANATION);
        dataDefinition.delete(toArray(anomalyExplanationsGrid.getSelectedEntitiesIds(), Long.class));
        FormComponent documentForm = (FormComponent) view.getComponentByReference("form");
        documentForm.performEvent(view, "reset");
    }

    public void onUseWasteChange(final ViewDefinitionState view, final ComponentState useWaste, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();
        if (anomalyExplanation.getBooleanField(AnomalyExplanationFields.USE_WASTE)) {
            anomalyExplanation.setField(AnomalyExplanationFields.PRODUCT, null);

            ComponentState productUnit = view.getComponentByReference("productUnit");
            productUnit.setFieldValue(null);

            anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_QUANTITY, null);
            anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_UNIT, null);
            view.getComponentByReference("usedQuantity").setEnabled(false);

            anomalyExplanation.setField(AnomalyExplanationFields.LOCATION, null);
        }
        anomalyExplanation.setField(AnomalyExplanationFields.USED_QUANTITY, null);
        form.setEntity(anomalyExplanation);
    }

    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();
        Entity selectedProduct = anomalyExplanation.getBelongsToField(AnomalyExplanationFields.PRODUCT);

        ComponentState productUnit = view.getComponentByReference("productUnit");
        ComponentState usedQuantity = view.getComponentByReference("usedQuantity");
        if (selectedProduct != null) {
            String selectedProductUnit = selectedProduct.getStringField(ProductFields.UNIT);
            productUnit.setFieldValue(selectedProductUnit);
            usedQuantity.setEnabled(true);

            String selectedProductAdditionalUnit = selectedProduct.getStringField(ProductFields.ADDITIONAL_UNIT);
            if (isNotBlank(selectedProductAdditionalUnit)) {
                anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_UNIT, selectedProductAdditionalUnit);
            } else {
                anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_UNIT, selectedProductUnit);
            }

        } else {
            productUnit.setFieldValue(null);
            anomalyExplanation.setField(AnomalyExplanationFields.USED_QUANTITY, null);
            anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_QUANTITY, null);
            anomalyExplanation.setField(AnomalyExplanationFields.GIVEN_UNIT, null);
            usedQuantity.setEnabled(false);
        }
        form.setEntity(anomalyExplanation);
        usedQuantity.performEvent(view, "onInputChange", ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public void copyGivenToUsedWhenUseWasteChecked(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();

        if(anomalyExplanation.getBooleanField(AnomalyExplanationFields.USE_WASTE)){
            anomalyExplanation.setField(AnomalyExplanationFields.USED_QUANTITY,
                    anomalyExplanation.getDecimalField(AnomalyExplanationFields.GIVEN_QUANTITY));
        }
        form.setEntity(anomalyExplanation);
    }

    public void calculateQuantity(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        new CalculationHelper(view, unitConversionService).genericCalculateMethod(
                h -> h.anomalyExplanation.getStringField(AnomalyExplanationFields.GIVEN_UNIT),
                AnomalyExplanationFields.GIVEN_QUANTITY,
                h -> view.getComponentByReference("productUnit").getFieldValue().toString(),
                AnomalyExplanationFields.USED_QUANTITY);
    }

    public void calculateQuantityToGiven(final ViewDefinitionState view, final ComponentState cs, final String[] args) {
        new CalculationHelper(view, unitConversionService).genericCalculateMethod(
                h -> view.getComponentByReference("productUnit").getFieldValue().toString(),
                AnomalyExplanationFields.USED_QUANTITY,
                h -> h.anomalyExplanation.getStringField(AnomalyExplanationFields.GIVEN_UNIT),
                AnomalyExplanationFields.GIVEN_QUANTITY);
    }

    private static class CalculationHelper {

        final ViewDefinitionState view;

        final FormComponent form;

        final Entity anomalyExplanation;

        final Entity selectedProduct;

        final UnitConversionService unitConversionService;

        private CalculationHelper(final ViewDefinitionState view, UnitConversionService unitConversionService) {
            this.view = view;
            this.unitConversionService = unitConversionService;
            form = (FormComponent) view.getComponentByReference("form");
            anomalyExplanation = form.getPersistedEntityWithIncludedFormValues();
            selectedProduct = anomalyExplanation.getBelongsToField(AnomalyExplanationFields.PRODUCT);
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
                if (isBlank(thisUnit) || otherUnit.equals(thisUnit)) {
                    otherQuantityNewValue = thisQuantity;
                } else {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(thisUnit,
                            searchCriteriaBuilder -> searchCriteriaBuilder
                                    .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, selectedProduct)));
                    if (unitConversions.isDefinedFor(otherUnit)) {
                        otherQuantityNewValue = unitConversions.convertTo(thisQuantity, otherUnit);
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
