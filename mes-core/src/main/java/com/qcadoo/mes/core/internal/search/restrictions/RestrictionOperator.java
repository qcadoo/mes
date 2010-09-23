package com.qcadoo.mes.core.internal.search.restrictions;

public enum RestrictionOperator {

    EQ("="), GE(">="), GT(">"), LE("<="), LT("<"), NE("<>");

    private String value;

    private RestrictionOperator(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
