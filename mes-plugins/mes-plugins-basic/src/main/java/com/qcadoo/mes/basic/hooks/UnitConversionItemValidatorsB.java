package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.UnitConversionItemFields;

@Service
public class UnitConversionItemValidatorsB {

    public boolean validateUnitOnConversionWithProduct(final DataDefinition dataDefinition, final Entity unitConversionItem) {
        final Entity product = unitConversionItem.getBelongsToField(UnitConversionItemFieldsB.PRODUCT);
        final String unitFrom = unitConversionItem.getStringField(UnitConversionItemFields.UNIT_FROM);

        if (product == null || product.getStringField(ProductFields.UNIT).equals(unitFrom)) {
            return true;
        }

        final String errorMsg = "basic.product.validateError.unitFrom.doesNotMatchUnitConversionItem";
        unitConversionItem.addError(dataDefinition.getField(UnitConversionItemFields.UNIT_FROM), errorMsg);

        return false;
    }

}
