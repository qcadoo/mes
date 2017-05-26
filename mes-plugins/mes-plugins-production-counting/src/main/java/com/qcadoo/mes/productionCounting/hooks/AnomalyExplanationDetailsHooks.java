package com.qcadoo.mes.productionCounting.hooks;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.productionCounting.constants.AnomalyExplanationFields;
import com.qcadoo.mes.productionCounting.constants.AnomalyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class AnomalyExplanationDetailsHooks {

    @Autowired
    private UnitConversionService unitConversionService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getEntity();

        if (view.isViewAfterRedirect()) {
            initializeFormValues(view, entity);
        }

        boolean useWaste = ((CheckBoxComponent) view.getComponentByReference("useWaste")).isChecked();
        view.getComponentByReference("product").setEnabled(!useWaste);
        view.getComponentByReference("location").setEnabled(!useWaste);

        ComponentState givenUnitComponent = view.getComponentByReference("givenUnit");

        String givenUnit = entity.getStringField(AnomalyExplanationFields.GIVEN_UNIT);
        Entity selectedProduct = entity.getBelongsToField(AnomalyExplanationFields.PRODUCT);

        boolean shouldAdditionalUnitBeEnabled = true;
        if (selectedProduct != null) {
            String selectedProductAdditionalUnit = selectedProduct.getStringField(ProductFields.ADDITIONAL_UNIT);

            if (isNotBlank(selectedProductAdditionalUnit) && isNotBlank(givenUnit)
                    && selectedProductAdditionalUnit.equals(givenUnit)) {
                shouldAdditionalUnitBeEnabled = false;
            }
        }
        givenUnitComponent.setEnabled(shouldAdditionalUnitBeEnabled);

        form.setEntity(entity);
    }

    private void initializeFormValues(ViewDefinitionState view, Entity entity) {
        Entity anomaly = entity.getBelongsToField(AnomalyExplanationFields.ANOMALY);
        Entity anomalyProduct = anomaly.getBelongsToField(AnomalyFields.PRODUCT);
        String anomalyProductUnit = anomalyProduct.getStringField(ProductFields.UNIT);
        String additionalAnomalyProductUnit = anomalyProduct.getStringField(ProductFields.ADDITIONAL_UNIT);

        if (entity.getId() == null) {

            entity.setField(AnomalyExplanationFields.PRODUCT, anomalyProduct);

            String selectedProductUnit = anomalyProduct.getStringField(ProductFields.UNIT);
            String additionalSelectedProductUnit = anomalyProduct.getStringField(ProductFields.ADDITIONAL_UNIT);
            if (isNotBlank(additionalSelectedProductUnit)) {
                entity.setField(AnomalyExplanationFields.GIVEN_UNIT, additionalSelectedProductUnit);
            } else {
                entity.setField(AnomalyExplanationFields.GIVEN_UNIT, selectedProductUnit);
            }

            BigDecimal anomalyUsedQuantity = anomaly.getDecimalField(AnomalyFields.USED_QUANTITY);
            entity.setField(AnomalyExplanationFields.USED_QUANTITY, anomalyUsedQuantity);

            if (isNotBlank(additionalAnomalyProductUnit)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(anomalyProductUnit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, anomalyProduct)));
                if (unitConversions.isDefinedFor(additionalAnomalyProductUnit)) {
                    BigDecimal convertedQuantityNewValue = unitConversions.convertTo(anomalyUsedQuantity,
                            additionalAnomalyProductUnit);
                    entity.setField(AnomalyExplanationFields.GIVEN_QUANTITY, convertedQuantityNewValue);
                }
            } else {
                entity.setField(AnomalyExplanationFields.GIVEN_QUANTITY, anomalyUsedQuantity);
            }

            // TODO uncomment me when @kasi finishes his changes
//            entity.setField(AnomalyExplanationFields.LOCATION, anomaly.getBelongsToField(AnomalyFields.LOCATION));
        }

        Entity selectedProduct = entity.getBelongsToField(AnomalyExplanationFields.PRODUCT);
        ComponentState productUnit = view.getComponentByReference("productUnit");
        ComponentState usedQuantity = view.getComponentByReference("usedQuantity");
        if (selectedProduct != null) {
            productUnit.setFieldValue(selectedProduct.getStringField(ProductFields.UNIT));
            usedQuantity.setEnabled(true);
        } else {
            productUnit.setFieldValue(null);
            usedQuantity.setEnabled(false);
        }
    }

}
