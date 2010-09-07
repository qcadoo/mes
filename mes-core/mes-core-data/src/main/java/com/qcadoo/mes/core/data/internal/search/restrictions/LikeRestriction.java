package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LikeRestriction extends BaseRestriction {

    private static final Logger LOG = LoggerFactory.getLogger(LikeRestriction.class);

    public LikeRestriction(final String fieldName, final Object value) {
        super(fieldName, value);
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Criteria added: " + "like");
        }
        return criteria.add(Restrictions.like(getFieldName(), getValue()));
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

}
