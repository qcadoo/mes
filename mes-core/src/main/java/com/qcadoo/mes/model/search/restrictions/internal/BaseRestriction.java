/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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
