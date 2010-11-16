/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.search.restrictions.internal;

import org.hibernate.Criteria;

import com.qcadoo.mes.model.search.Restriction;

public abstract class BaseRestriction implements Restriction {

    private final String fieldName;

    private final Object value;

    public BaseRestriction(final String fieldName, final Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    public final String getFieldName() {
        return fieldName;
    }

    public final Object getValue() {
        return value;
    }

    public abstract Criteria addRestrictionToHibernateCriteria(final Criteria criteria);

    @Override
    public final Criteria addToHibernateCriteria(final Criteria criteria) {
        String[] path = fieldName.split("\\.");

        if (path.length > 2) {
            throw new IllegalStateException("Cannot order using multiple assosiations - " + fieldName);
        } else if (path.length == 2 && !criteria.toString().matches(".*Subcriteria\\(" + path[0] + ":" + path[0] + "\\).*")) {
            criteria.createAlias(path[0], path[0]);
        }

        return addRestrictionToHibernateCriteria(criteria);
    }

}
