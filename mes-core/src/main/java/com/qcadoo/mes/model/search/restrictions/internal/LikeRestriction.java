/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.search.restrictions.internal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public final class LikeRestriction extends BaseRestriction {

    public LikeRestriction(final String fieldName, final Object value) {
        super(fieldName, value);
    }

    @Override
    public Criteria addRestrictionToHibernateCriteria(final Criteria criteria) {
        return criteria.add(Restrictions.ilike(getFieldName(), getValue()));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(31, 5).append(getFieldName()).append(getValue()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LikeRestriction)) {
            return false;
        }
        LikeRestriction other = (LikeRestriction) obj;
        return new EqualsBuilder().append(getFieldName(), other.getFieldName()).append(getValue(), other.getValue()).isEquals();
    }

    @Override
    public String toString() {
        return getFieldName() + " like " + getValue();
    }

}
