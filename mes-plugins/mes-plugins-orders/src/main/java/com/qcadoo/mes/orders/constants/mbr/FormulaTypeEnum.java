package com.qcadoo.mes.orders.constants.mbr;

import org.apache.commons.lang3.StringUtils;

public enum FormulaTypeEnum implements FormulaType {

    ADDITION("01addition") {

        @Override
        public String getOperator() {
            return "+";
        }
    },
    SUBTRACTION("02subtraction") {

        @Override
        public String getOperator() {
            return "-";
        }
    },
    DIVISION("03division") {

        @Override
        public String getOperator() {
            return "/";
        }
    },
    MULTIPLICATION("04multiplication") {

        @Override
        public String getOperator() {
            return "*";
        }
    };

    private final String value;

    private FormulaTypeEnum(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static FormulaTypeEnum parseString(final String type) {
        for (FormulaTypeEnum documentType : values()) {
            if (StringUtils.equalsIgnoreCase(type, documentType.getStringValue())) {
                return documentType;
            }
        }
        throw new IllegalArgumentException("Couldn't parse FormulaType from string '" + type + "'");
    }

}
