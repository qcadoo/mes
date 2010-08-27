package com.qcadoo.mes.core.data.internal.search.restrictions;

public enum RestrictionOperator {
    EQ("="), GE(">="), GT(">"), LE("<="), LT("<"), NE("<>"), NOTNULL("is not null"), NULL("is null");

    private String value;

    private RestrictionOperator(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
