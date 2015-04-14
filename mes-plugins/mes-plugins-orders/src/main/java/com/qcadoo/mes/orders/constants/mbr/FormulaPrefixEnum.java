package com.qcadoo.mes.orders.constants.mbr;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum FormulaPrefixEnum implements FormulaPrefix {

    LEFT_BRACKET("01leftBracket") {

        @Override
        public String getPrefix() {
            return "(";
        }
    };

    private final String value;

    private FormulaPrefixEnum(final String value) {
        this.value = value;
    }

    public static FormulaPrefixEnum of(final Entity formula) {
        Preconditions.checkArgument(formula != null, "Passed entity have not to be null.");
        return parseString(formula.getStringField(FormulaFields.PREFIX));
    }

    public static FormulaPrefixEnum parseString(final String prefix) {
        for (FormulaPrefixEnum formulaPrefix : values()) {
            if (StringUtils.equalsIgnoreCase(prefix, formulaPrefix.getStringValue())) {
                return formulaPrefix;
            }
        }
        throw new IllegalArgumentException("Couldn't parse FormulaPrefixValue from string '" + prefix + "'");
    }

    public String getStringValue() {
        return this.value;
    }
}
