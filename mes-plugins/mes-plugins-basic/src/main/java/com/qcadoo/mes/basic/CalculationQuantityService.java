package com.qcadoo.mes.basic;

import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalculationQuantityService {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private NumberService numberService;

    public BigDecimal calculateQuantity(BigDecimal additionalQuantity, BigDecimal conversion, String unit) {
        additionalQuantity = additionalQuantity.divide(conversion, NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                BigDecimal.ROUND_FLOOR);
        if (dictionaryService.checkIfUnitIsInteger(unit)) {
            return numberService.setScale(additionalQuantity, 0);
        }
        return additionalQuantity;
    }

    public BigDecimal calculateAdditionalQuantity(BigDecimal quantity, BigDecimal conversion, String unit) {
        quantity = quantity.multiply(conversion, numberService.getMathContext());
        if (dictionaryService.checkIfUnitIsInteger(unit)) {
            return numberService.setScale(quantity, 0);
        }
        return quantity;
    }

}
