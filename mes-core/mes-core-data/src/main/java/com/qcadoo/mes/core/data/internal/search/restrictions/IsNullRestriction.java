package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IsNullRestriction extends BaseRestriction {

    private final RestrictionOperator op;

    private static final Logger LOG = LoggerFactory.getLogger(IsNullRestriction.class);

    public IsNullRestriction(final String fieldName, final RestrictionOperator op) {
        super(fieldName, null);
        this.op = op;
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        criteria.add(Restrictions.isNull(getFieldName()));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Criteria added: " + op.getValue());
        }
        return criteria;
    }

}
