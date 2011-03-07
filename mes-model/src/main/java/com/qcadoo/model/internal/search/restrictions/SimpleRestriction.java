/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.model.internal.search.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.qcadoo.model.api.search.RestrictionOperator;

public final class SimpleRestriction extends BaseRestriction {

    private final RestrictionOperator op;

    public SimpleRestriction(final String fieldName, final Object value, final RestrictionOperator op) {
        super(fieldName, value);
        this.op = op;
    }

    @Override
    public Criterion getHibernateCriteria(final String propertyName) {
        switch (op) {
            case EQ:
                return Restrictions.eq(propertyName, getValue());
            case GE:
                return Restrictions.ge(propertyName, getValue());
            case GT:
                return Restrictions.gt(propertyName, getValue());
            case LE:
                return Restrictions.le(propertyName, getValue());
            case LT:
                return Restrictions.lt(propertyName, getValue());
            case NE:
                return Restrictions.ne(propertyName, getValue());
            default:
                throw new IllegalArgumentException("Unknown restriction operator");
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 5).append(getFieldName()).append(getValue()).append(op).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleRestriction)) {
            return false;
        }
        SimpleRestriction other = (SimpleRestriction) obj;
        return new EqualsBuilder().append(getFieldName(), other.getFieldName()).append(getValue(), other.getValue())
                .append(op, other.op).isEquals();
    }

    @Override
    public String toString() {
        return getFieldName() + " " + op.getValue() + " " + getValue();
    }
}
