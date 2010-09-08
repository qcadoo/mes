package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public final class IsNullRestriction extends BaseRestriction {

    public IsNullRestriction(final String fieldName) {
        super(fieldName, null);
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        return criteria.add(Restrictions.isNull(getFieldName()));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 5).append(getFieldName()).append(getValue()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IsNullRestriction)) {
            return false;
        }
        IsNullRestriction other = (IsNullRestriction) obj;
        return new EqualsBuilder().append(getFieldName(), other.getFieldName()).append(getValue(), other.getValue()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fieldName", getFieldName()).toString();
    }

}
