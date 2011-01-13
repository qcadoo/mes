package com.qcadoo.mes.model.search.restrictions.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.qcadoo.mes.model.search.Restriction;
import com.qcadoo.mes.model.search.RestrictionLogicalOperator;

public class LogicalOperatorRestriction extends BaseRestriction {

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
    public final Criterion getHibernateCriteria() {
        List<Criterion> criterions = new LinkedList<Criterion>();
        for (Restriction r : restrictions) {
            criterions.add(((BaseRestriction) r).getHibernateCriteria());
        }

        switch (operator) {
            case NOT:
                checkState(criterions.size() == 1, "not can only have one argument");
                return Restrictions.not(criterions.get(0));
            case AND:
                return createAndRestriction(criterions);
            case OR:
                return createOrRestriction(criterions);
            default:
                throw new IllegalArgumentException("Unknown restriction operator");
        }
    }

    private Criterion createAndRestriction(List<Criterion> criterions) {
        if (criterions.size() == 1) {
            return criterions.get(0);
        } else {
            Criterion first = criterions.get(0);
            criterions.remove(0);
            return Restrictions.and(first, createAndRestriction(criterions));
        }
    }

    private Criterion createOrRestriction(List<Criterion> criterions) {
        if (criterions.size() == 1) {
            return criterions.get(0);
        } else {
            Criterion first = criterions.get(0);
            criterions.remove(0);
            return Restrictions.or(first, createOrRestriction(criterions));
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogicalOperatorRestriction other = (LogicalOperatorRestriction) obj;
        if (operator != other.operator)
            return false;
        if (!Arrays.equals(restrictions, other.restrictions))
            return false;
        return true;
    }

}
