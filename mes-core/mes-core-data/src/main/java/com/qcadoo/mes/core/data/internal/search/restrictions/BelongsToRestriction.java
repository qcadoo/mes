package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public final class BelongsToRestriction extends BaseRestriction {

    public BelongsToRestriction(final String belongsToFieldName, final Long id) {
        super(belongsToFieldName, id);
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        return criteria.add(Restrictions.eq(getFieldName() + ".id", (Long) getValue()));
    }

}
