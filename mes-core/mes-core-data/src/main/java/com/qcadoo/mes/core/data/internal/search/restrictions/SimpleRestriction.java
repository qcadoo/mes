package com.qcadoo.mes.core.data.internal.search.restrictions;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleRestriction extends BaseRestriction {

    private final RestrictionOperator op;

    private static final Logger LOG = LoggerFactory.getLogger(SimpleRestriction.class);

    public SimpleRestriction(final String fieldName, final Object value, final RestrictionOperator op) {
        super(fieldName, value);
        this.op = op;
    }

    @Override
    public Criteria addToHibernateCriteria(final Criteria criteria) {
        switch (op) {
            case EQ:
                criteria.add(Restrictions.eq(getFieldName(), getValue()));
                break;
            case GE:
                criteria.add(Restrictions.ge(getFieldName(), getValue()));
                break;
            case GT:
                criteria.add(Restrictions.gt(getFieldName(), getValue()));
                break;
            case LE:
                criteria.add(Restrictions.le(getFieldName(), getValue()));
                break;
            case LT:
                criteria.add(Restrictions.lt(getFieldName(), getValue()));
                break;
            case NE:
                criteria.add(Restrictions.ne(getFieldName(), getValue()));
            default:
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Criteria added: " + op.getValue());
        }
        return criteria;
    }
}
