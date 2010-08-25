package com.qcadoo.mes.core.data.search;

import com.qcadoo.mes.core.data.internal.search.restrictions.BelongsToRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.LikeRestriction;
import com.qcadoo.mes.core.data.internal.search.restrictions.SimpleRestriction;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.search.Restriction
 */
public final class Restrictions {

    private Restrictions() {
    }

    public static Restriction eq(final String fieldName, final Object expectedValue) {
        return new SimpleRestriction(fieldName, expectedValue, "==");
    }

    public static Restriction like(final String fieldName, final String expectedValue) {
        return new LikeRestriction(fieldName, expectedValue);
    }

    public static Restriction belongsTo(final String entityName, final Long id) {
        return new BelongsToRestriction(entityName, id);
    }

}
