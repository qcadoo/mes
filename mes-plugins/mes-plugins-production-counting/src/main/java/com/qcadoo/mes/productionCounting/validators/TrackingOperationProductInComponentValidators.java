package com.qcadoo.mes.productionCounting.validators;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Component
public class TrackingOperationProductInComponentValidators {


    public boolean validateWasteUsedQuantity(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity, final Object fieldOldValue, final Object fieldNewValue) {
        BigDecimal wasteUsedQuantity = entity.getDecimalField(TrackingOperationProductInComponentFields.WASTE_USED_QUANTITY);
        boolean wasteUsed = entity.getBooleanField(TrackingOperationProductInComponentFields.WASTE_USED);
        return !(wasteUsed && wasteUsedQuantity == null);
    }

    public boolean validateIfOnlyWasteUsed(final DataDefinition dataDefinition, final Entity trackingOperationProductIn) {
        boolean wasteUsed = trackingOperationProductIn.getBooleanField(TrackingOperationProductInComponentFields.WASTE_USED);
        boolean wasteUsedOnly = trackingOperationProductIn
                .getBooleanField(TrackingOperationProductInComponentFields.WASTE_USED_ONLY);
        if (wasteUsed && !wasteUsedOnly) {
            BigDecimal usedQuantity = trackingOperationProductIn
                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
            if (usedQuantity == null || BigDecimalUtils.valueEquals(usedQuantity, BigDecimal.ZERO)) {
                trackingOperationProductIn.addError(
                        dataDefinition.getField(TrackingOperationProductInComponentFields.WASTE_USED_ONLY),
                        "productionCounting.trackingOperationProductInComponent.messages.error.wasteUsedOnly.invalid");
                return false;
            }
        }
        return true;
    }
}
