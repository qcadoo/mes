package com.qcadoo.mes.model.search.restrictions.internal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.qcadoo.mes.enums.RestrictionOperator;

public final class SimpleRestriction extends BaseRestriction {

    private final RestrictionOperator op;

    public SimpleRestriction(final String fieldName, final Object value, final RestrictionOperator op) {
        super(fieldName, value);
        this.op = op;
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        Criterion hibernateRestriction = getHibernateRestriction();

        if (hibernateRestriction != null) {
            criteria.add(hibernateRestriction);
        }

        return criteria;
    }

    private Criterion getHibernateRestriction() {
        switch (op) {
            case EQ:
                return Restrictions.eq(getFieldName(), getValue());
            case GE:
                return Restrictions.ge(getFieldName(), getValue());
            case GT:
                return Restrictions.gt(getFieldName(), getValue());
            case LE:
                return Restrictions.le(getFieldName(), getValue());
            case LT:
                return Restrictions.lt(getFieldName(), getValue());
            case NE:
                return Restrictions.ne(getFieldName(), getValue());
            default:
                throw new IllegalArgumentException("Unknown restriction operator");
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 5).append(getFieldName()).append(getValue()).append(op).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleRestriction)) {
            return false;
        }
        SimpleRestriction other = (SimpleRestriction) obj;
        return new EqualsBuilder().append(getFieldName(), other.getFieldName()).append(getValue(), other.getValue())
                .append(op, other.op).isEquals();
    }

    @Override
    public String toString() {
        return getFieldName() + " " + op.getValue() + " " + getValue();
    }
}
