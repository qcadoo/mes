package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.QUANTITYFOROTHERWORKSTATIONTYPES;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineModelValidators {

    public boolean checkIfQuantityForOthersWorkstationTypesIsGreaterThanZero(final DataDefinition productionLineDD,
            final Entity productionLine) {
        Integer quantityForOtherWorkstationTypes = (Integer) productionLine.getField(QUANTITYFOROTHERWORKSTATIONTYPES);

        if ((quantityForOtherWorkstationTypes != null) && (quantityForOtherWorkstationTypes <= 0)) {
            productionLine.addError(productionLineDD.getField(QUANTITYFOROTHERWORKSTATIONTYPES),
                    "productionLines.productionLine.message.quantityForOtherWorkstationTypesIsLowerThanOne");

            return false;
        }

        return true;
    }
}
