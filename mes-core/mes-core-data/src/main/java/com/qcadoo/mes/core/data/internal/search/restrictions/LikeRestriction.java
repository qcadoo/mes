package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public final class LikeRestriction extends BaseRestriction {

    public LikeRestriction(final String fieldName, final String value) {
        super(fieldName, value);
    }

    @Override
    public Criteria addToHibernateCriteria(Criteria criteria) {
        return criteria.add(Restrictions.like(getFieldName(), (String) getValue()));
    }

}
