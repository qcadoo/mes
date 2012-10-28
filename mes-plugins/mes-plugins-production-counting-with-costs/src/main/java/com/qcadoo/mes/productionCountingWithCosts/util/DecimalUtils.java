package com.qcadoo.mes.productionCountingWithCosts.util;

import java.math.BigDecimal;
import java.math.MathContext;

public final class DecimalUtils {

    private static BigDecimal oneHundred = BigDecimal.valueOf(100L);

    private DecimalUtils() {
    }

    public static BigDecimal nullToZero(final BigDecimal decimalValue) {
        if (decimalValue == null) {
            return BigDecimal.ZERO;
        } else {
            return decimalValue;
        }
    }

    public static BigDecimal toPercent(final BigDecimal decimalValue, final MathContext mathCntext) {
        return nullToZero(decimalValue).divide(oneHundred, mathCntext);
    }
}
