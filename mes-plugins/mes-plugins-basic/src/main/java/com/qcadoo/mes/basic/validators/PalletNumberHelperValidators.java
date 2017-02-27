package com.qcadoo.mes.basic.validators;

import static com.qcadoo.mes.basic.constants.PalletNumberHelperFields.QUANTITY;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.PalletNumberHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PalletNumberHelperValidators {

    public static boolean checkQuantity(final DataDefinition palletHelperDefinition, final Entity pallet) {

        Object fieldValue = pallet.getField(PalletNumberHelperFields.QUANTITY);

        Integer quantity = 0;
        if (fieldValue != null) {
            if (fieldValue instanceof Long) {
                quantity = ((Long) fieldValue).intValue();
            }
            if (fieldValue instanceof Integer) {
                quantity = (Integer) fieldValue;
            }
        }
        if (quantity.compareTo(PalletNumberHelperFields.QUANTITY_MAX_VALUE) <= 0) {
            return true;
        }
        pallet.addError(palletHelperDefinition.getField(QUANTITY), "basic.palleteNumberHelperDetails.field.error.invalidQuantityRange");
        return false;
    }
}
