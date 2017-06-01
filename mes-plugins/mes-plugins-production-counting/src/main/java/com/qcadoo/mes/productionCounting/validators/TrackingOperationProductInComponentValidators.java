package com.qcadoo.mes.productionCounting.validators;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Component
public class TrackingOperationProductInComponentValidators {

    public boolean validateWasteUsedQuantity(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Entity entity, final Object fieldOldValue, final Object fieldNewValue) {
        BigDecimal wasteUsedQuantity = entity.getDecimalField(TrackingOperationProductInComponentFields.WASTE_USED_QUANTITY);
        boolean wasteUsed = entity.getBooleanField(TrackingOperationProductInComponentFields.WASTE_USED);
        return !(wasteUsed && wasteUsedQuantity == null);
    }

}
