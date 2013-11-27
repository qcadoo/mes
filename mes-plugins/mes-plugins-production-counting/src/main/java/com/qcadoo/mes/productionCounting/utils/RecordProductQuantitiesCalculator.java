package com.qcadoo.mes.productionCounting.utils;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.RecordOperationProductInComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class RecordProductQuantitiesCalculator {

    @Autowired
    private NumberService numberService;

    /**
     * Tries to calculate effective used product quantity. Notice that this method may return also null!
     * 
     * @param recordInputProductComponent
     *            entity representing record's input product component
     * @return effective used product quantity or null if partial quantities is not specified.
     */
    public BigDecimal getEffectiveUsed(final Entity recordInputProductComponent) {
        BigDecimal wasteQuantity = recordInputProductComponent
                .getDecimalField(RecordOperationProductInComponentFields.REMAINED_QUANTITY);
        BigDecimal usedQuantity = recordInputProductComponent
                .getDecimalField(RecordOperationProductInComponentFields.USED_QUANTITY);

        BigDecimal effectiveUsedQuantity = null;
        if (wasteQuantity != null && usedQuantity != null) {
            effectiveUsedQuantity = numberService.setScale(wasteQuantity.add(usedQuantity, numberService.getMathContext()));
        }

        return effectiveUsedQuantity;
    }

}
