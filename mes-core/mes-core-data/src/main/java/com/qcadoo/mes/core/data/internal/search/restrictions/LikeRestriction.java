package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LikeRestriction extends BaseRestriction {

    private static final Logger LOG = LoggerFactory.getLogger(LikeRestriction.class);

    public LikeRestriction(final String fieldName, final String value) {
        super(fieldName, value);
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Criteria added: " + "like");
        }
        return criteria.add(Restrictions.like(getFieldName(), getValue()));
    }

}
