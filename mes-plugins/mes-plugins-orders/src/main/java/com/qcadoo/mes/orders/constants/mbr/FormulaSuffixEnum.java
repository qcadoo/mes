package com.qcadoo.mes.orders.constants.mbr;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum FormulaSuffixEnum implements FormulaSuffix {

    RIGHT_BRACKET("01rightBracket") {

        @Override
        public String getSuffix() {
            return ")";
        }
    };

    private final String value;

    private FormulaSuffixEnum(final String value) {
        this.value = value;
    }

    public static FormulaSuffixEnum of(final Entity formula) {
        Preconditions.checkArgument(formula != null, "Passed entity have not to be null.");
        return parseString(formula.getStringField(FormulaFields.SUFFIX));
    }

    public static FormulaSuffixEnum parseString(final String suffix) {
        for (FormulaSuffixEnum formulaSuffix : values()) {
            if (StringUtils.equalsIgnoreCase(suffix, formulaSuffix.getStringValue())) {
                return formulaSuffix;
            }
        }
        throw new IllegalArgumentException("Couldn't parse FormulaSuffixValue from string '" + suffix + "'");
    }

    public String getStringValue() {
        return this.value;
    }
}
