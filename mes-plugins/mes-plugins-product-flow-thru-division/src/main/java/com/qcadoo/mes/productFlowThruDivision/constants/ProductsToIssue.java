package com.qcadoo.mes.productFlowThruDivision.constants;

import org.apache.commons.lang3.StringUtils;

public enum ProductsToIssue {

    ALL_INPUT_PRODUCTS("01allInputProducts"), ONLY_MATERIALS("02onlyMaterials");

    private final String strValue;

    private ProductsToIssue(final String strValue) {
        this.strValue = strValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public static ProductsToIssue parseString(final String stringValue) {
        for (ProductsToIssue productsToIssue : values()) {
            if (StringUtils.equalsIgnoreCase(stringValue, productsToIssue.getStrValue())) {
                return productsToIssue;
            }
        }

        throw new IllegalArgumentException(String.format("Can't parse productsToIssue enum instance from '%s'", stringValue));
    }

}
