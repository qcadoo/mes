package com.qcadoo.mes.basic.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ConversionItemHooks {

    public boolean validateQuantityFrom(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getDecimalField("quantityFrom").compareTo(BigDecimal.ONE) != 0) {
            entity.addError(dataDefinition.getField("quantityFrom"),
                    "basic.conversionItem.validateError.quantityFrom.differentFromOne");
            return false;
        }
        return true;
    }

    public boolean validateUnitOnConversionWithProduct(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField("product");
        if (product == null) {
            return true;
        }
        if (product.getStringField("unit").equals(entity.getStringField("unitFrom"))) {
            return true;
        } else {
            entity.addError(dataDefinition.getField("unitFrom"),
                    "basic.conversionItem.validateError.unitFrom.doesnotMatchWithProductUnit");
            return false;
        }
    }

}
