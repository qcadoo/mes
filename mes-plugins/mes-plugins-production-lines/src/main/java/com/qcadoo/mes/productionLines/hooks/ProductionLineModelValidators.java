package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.QUANTITYFOROTHERWORKSTATIONTYPES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.SUPPORTSOTHERTECHNOLOGIESWORKSTATIONTYPES;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineModelValidators {

    public boolean checkIfQuantityForOthersWorkstationTypesIsNotNull(final DataDefinition productionLineDD,
            final Entity productionLine) {
        Boolean supportsOtherTechnologiesWorkstationTypes = productionLine
                .getBooleanField(SUPPORTSOTHERTECHNOLOGIESWORKSTATIONTYPES);

        Integer quantityForOtherWorkstationTypes = (Integer) productionLine.getField(QUANTITYFOROTHERWORKSTATIONTYPES);

        if (supportsOtherTechnologiesWorkstationTypes && (quantityForOtherWorkstationTypes == null)) {
            productionLine.addError(productionLineDD.getField(QUANTITYFOROTHERWORKSTATIONTYPES),
                    "productionLines.productionLine.message.quantityForOtherWorkstationTypesIsNull");

            return false;
        }

        return true;
    }
}
