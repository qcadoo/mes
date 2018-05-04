package com.qcadoo.mes.basic;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.NumberService;

@Service
public class CalculationQuantityService {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private NumberService numberService;

    public BigDecimal calculateQuantity(BigDecimal additionalQuantity, BigDecimal conversion, String unit) {
        BigDecimal quantity = additionalQuantity.divide(conversion, NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL,
                BigDecimal.ROUND_FLOOR);
        if (dictionaryService.checkIfUnitIsInteger(unit)) {
            return numberService.setScaleWithDefaultMathContext(quantity, 0);
        }
        return numberService.setScaleWithDefaultMathContext(quantity);
    }

    public BigDecimal calculateAdditionalQuantity(BigDecimal quantity, BigDecimal conversion, String unit) {
        BigDecimal additionalQuantity = quantity.multiply(conversion, numberService.getMathContext());
        if (dictionaryService.checkIfUnitIsInteger(unit)) {
            return numberService.setScaleWithDefaultMathContext(additionalQuantity, 0);
        }
        return numberService.setScaleWithDefaultMathContext(additionalQuantity);
    }

}
