package com.qcadoo.mes.core.data.internal.search.restrictions;

public final class SimpleRestriction extends BaseRestriction {

    private final String op;

    public SimpleRestriction(final String fieldName, final Object value, final String op) {
        super(fieldName, value);
        this.op = op;
    }

}
