package com.qcadoo.mes.deliveries.helpers;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.model.api.DictionaryService;
import com.qcadoo.model.api.NumberService;

@Component
public final class DeliveryPositionCalculationHelper {

    @Autowired
    private DictionaryService dictionaryService;


    @Autowired
    private NumberService numberService;

    private BigDecimal calculateTheOtherQuantity(Function<BigDecimal, BigDecimal> operation, BigDecimal conversion, String unit) {
        if (dictionaryService.checkIfUnitIsInteger(unit)) {
            return numberService.setScale(operation.apply(conversion), 0);
        }
        return operation.apply(conversion).setScale(NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL, BigDecimal.ROUND_FLOOR);
    }

    public BigDecimal calculateQuantity(BigDecimal additionalQuantity, BigDecimal conversion, String unit) {
        return calculateTheOtherQuantity(withDefaultMathContext(additionalQuantity::divide), conversion, unit);
    }

    public BigDecimal calculateAdditionalQuantity(BigDecimal quantity, BigDecimal conversion, String unit) {
        return calculateTheOtherQuantity(withDefaultMathContext(quantity::multiply), conversion, unit);
    }

    private Function<BigDecimal, BigDecimal> withDefaultMathContext(BiFunction<BigDecimal, MathContext, BigDecimal> operation) {
        return value -> operation.apply(value, numberService.getMathContext());
    }

}
