/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.search.restrictions.internal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public final class BelongsToRestriction extends BaseRestriction {

    public BelongsToRestriction(final String belongsToFieldName, final Long id) {
        super(belongsToFieldName, id);
    }

    @Override
    public Criteria addRestrictionToHibernateCriteria(final Criteria criteria) {
        return criteria.add(Restrictions.eq(getFieldName() + ".id", getValue()));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(43, 7).append(getFieldName()).append(getValue()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BelongsToRestriction)) {
            return false;
        }
        BelongsToRestriction other = (BelongsToRestriction) obj;
        return new EqualsBuilder().append(getFieldName(), other.getFieldName()).append(getValue(), other.getValue()).isEquals();
    }

    @Override
    public String toString() {
        return getFieldName() + ".id = " + getValue();
    }

}
