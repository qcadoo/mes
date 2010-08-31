package com.qcadoo.mes.core.data.search;

import com.qcadoo.mes.core.data.internal.search.restrictions.BelongsToRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.IsNotNullRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.IsNullRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.LikeRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.RestrictionOperator;
import com.qcadoo.mes.core.data.internal.search.restrictions.SimpleRestriction;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.search.Restriction
 */
public final class Restrictions {

    private Restrictions() {
    }

    public static Restriction eq(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.EQ);
    }

    public static Restriction like(final String fieldName, final String expectedValue) {
        return new LikeRestriction(fieldName, expectedValue);
    }

    public static Restriction belongsTo(final String belongsToFieldName, final Long id) {
        return new BelongsToRestriction(belongsToFieldName, id);
    }

    public static Restriction ge(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.GE);
    }

    public static Restriction gt(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.GT);
    }

    public static Restriction isNotNull(final String fieldName) {
        return new IsNotNullRestriction(fieldName, RestrictionOperator.NOTNULL);
    }

    public static Restriction isNull(final String fieldName) {
        return new IsNullRestriction(fieldName, RestrictionOperator.NULL);
    }

    public static Restriction le(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.LE);
    }

    public static Restriction lt(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.LT);
    }

    public static Restriction ne(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, RestrictionOperator.NE);
    }
}
