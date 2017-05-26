package com.qcadoo.mes.productionCounting.hooks;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.productionCounting.constants.AnomalyExplanationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

@Service
public class AnomalyExplanationHooks {

    @Autowired
    private UnitConversionService unitConversionService;

    public boolean validateAnomalyExplanation(final DataDefinition anomalyExplanationDD, final Entity entity) {
        boolean validationResult = true;
        boolean useWaste = entity.getBooleanField(AnomalyExplanationFields.USE_WASTE);
        Entity selectedProduct = entity.getBelongsToField(AnomalyExplanationFields.PRODUCT);
        String givenUnit = entity.getStringField(AnomalyExplanationFields.GIVEN_UNIT);
        if (!useWaste && selectedProduct == null && isBlank(entity.getStringField(AnomalyExplanationFields.DESCRIPTION))) {
            String key = "productionCounting.anomalyExplanation.error.eitherRequired";
            entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.PRODUCT), key);
            entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.DESCRIPTION), key);
            validationResult = false;
        }


        if (useWaste || selectedProduct != null) {
            String key = "productionCounting.anomalyExplanation.error.required";
            if (entity.getDecimalField(AnomalyExplanationFields.USED_QUANTITY) == null) {
                entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.USED_QUANTITY), key);
                validationResult = false;
            }
            if (entity.getDecimalField(AnomalyExplanationFields.GIVEN_QUANTITY) == null) {
                entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.GIVEN_QUANTITY), key);
                validationResult = false;
            }
            if (isBlank(givenUnit)) {
                entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.GIVEN_QUANTITY),
                        "productionCounting.anomalyExplanation.error.unit.required");
                validationResult = false;
            }
        }

        if (selectedProduct != null) {
            String selectedProductUnit = selectedProduct.getStringField(ProductFields.UNIT);
            if (!StringUtils.equals(givenUnit, selectedProductUnit)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(selectedProductUnit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, selectedProduct)));
                if (!unitConversions.isDefinedFor(givenUnit)) {
                    String key = "productionCounting.anomalyExplanation.error.noConversionFound";
                    entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.GIVEN_QUANTITY), key);
                    validationResult = false;
                }
            }

            if (entity.getBelongsToField(AnomalyExplanationFields.LOCATION) == null) {
                entity.addError(anomalyExplanationDD.getField(AnomalyExplanationFields.LOCATION),
                        "productionCounting.anomalyExplanation.error.required");
                validationResult = false;
            }
        }

        return validationResult;
    }

}
