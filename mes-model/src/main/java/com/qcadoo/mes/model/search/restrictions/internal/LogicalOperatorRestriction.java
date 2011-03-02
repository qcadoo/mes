package com.qcadoo.mes.model.search.restrictions.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.RestrictionLogicalOperator;

public class LogicalOperatorRestriction implements Restriction {

    private final RestrictionLogicalOperator operator;

    private final Restriction[] restrictions;

    public LogicalOperatorRestriction(final RestrictionLogicalOperator operator, final Restriction... restrictions) {
        checkNotNull(operator);
        checkNotNull(restrictions);
        checkArgument(restrictions.length > 0);
        this.operator = operator;
        this.restrictions = restrictions;
    }

    @Override
    public Criterion addToHibernateCriteria(final Criteria criteria) {
        switch (operator) {
            case NOT:
                checkState(restrictions.length == 1, "not can only have one argument");
                return Restrictions.not(restrictions[0].addToHibernateCriteria(criteria));
            case AND:
                return createAndRestriction(criteria, Arrays.asList(restrictions));
            case OR:
                return createOrRestriction(criteria, Arrays.asList(restrictions));
            default:
                throw new IllegalArgumentException("Unknown restriction operator");
        }
    }

    private Criterion createAndRestriction(final Criteria criteria, final List<Restriction> innerRestrictions) {
        if (innerRestrictions.size() == 1) {
            return innerRestrictions.get(0).addToHibernateCriteria(criteria);
        } else {
            Criterion first = innerRestrictions.get(0).addToHibernateCriteria(criteria);
            return Restrictions.and(first,
                    createAndRestriction(criteria, innerRestrictions.subList(0, innerRestrictions.size() - 1)));
        }
    }

    private Criterion createOrRestriction(final Criteria criteria, final List<Restriction> innerRestrictions) {
        if (innerRestrictions.size() == 1) {
            return innerRestrictions.get(0).addToHibernateCriteria(criteria);
        } else {
            Criterion first = innerRestrictions.get(0).addToHibernateCriteria(criteria);
            return Restrictions.or(first,
                    createOrRestriction(criteria, innerRestrictions.subList(0, innerRestrictions.size() - 1)));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + Arrays.hashCode(restrictions);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LogicalOperatorRestriction other = (LogicalOperatorRestriction) obj;
        if (operator != other.operator) {
            return false;
        }
        if (!Arrays.equals(restrictions, other.restrictions)) {
            return false;
        }
        return true;
    }

}
