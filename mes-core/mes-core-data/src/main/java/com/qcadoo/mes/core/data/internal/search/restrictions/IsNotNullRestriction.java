package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IsNotNullRestriction extends BaseRestriction {

    private static final Logger LOG = LoggerFactory.getLogger(IsNotNullRestriction.class);

    public IsNotNullRestriction(final String fieldName) {
        super(fieldName, null);
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        criteria.add(Restrictions.isNotNull(getFieldName()));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Criteria added: is not null");
        }
        return criteria;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(47, 5).append(getFieldName()).append(getValue()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IsNotNullRestriction)) {
            return false;
        }
        IsNotNullRestriction other = (IsNotNullRestriction) obj;
        return new EqualsBuilder().append(getFieldName(), other.getFieldName()).append(getValue(), other.getValue()).isEquals();
    }

}
