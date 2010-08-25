package com.qcadoo.mes.core.data.internal.search.restrictions;

public final class BelongsToRestriction extends BaseRestriction {

    public BelongsToRestriction(final String entityName, final Long id) {
        super("id", id);
    }

}
