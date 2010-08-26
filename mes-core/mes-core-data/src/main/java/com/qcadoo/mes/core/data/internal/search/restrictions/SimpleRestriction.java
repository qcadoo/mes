package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.hibernate.Criteria;

public final class SimpleRestriction extends BaseRestriction {

    private final String op;

    public SimpleRestriction(final String fieldName, final Object value, final String op) {
        super(fieldName, value);
        this.op = op;
    }

    @Override
    public Criteria addToHibernateCriteria(Criteria criteria) {
        // TODO masz
        return criteria;
    }

}
