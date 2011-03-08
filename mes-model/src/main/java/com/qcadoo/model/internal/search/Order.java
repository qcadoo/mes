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

package com.qcadoo.model.internal.search;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Object holds order for search criteria.
 */
public final class Order {

    private static final Order DEFAULT_ORDER = Order.asc("id");

    private final String fieldName;

    private final boolean asc;

    private Order(final String fieldName, final boolean asc) {
        this.fieldName = fieldName;
        this.asc = asc;
    }

    /**
     * Create asc order for given field.
     * 
     * @param fieldName
     *            field's name
     * @return order
     */
    public static Order asc(final String fieldName) {
        return new Order(fieldName, true);
    }

    /**
     * Create desc order for given field.
     * 
     * @param fieldName
     *            field's name
     * @return order
     */
    public static Order desc(final String fieldName) {
        return new Order(fieldName, false);
    }

    /**
     * Create asc order using id field.
     * 
     * @return order
     */
    public static Order asc() {
        return DEFAULT_ORDER;
    }

    /**
     * Return true if order is asc.
     * 
     * @return is asc
     */
    public boolean isAsc() {
        return asc;
    }

    /**
     * Return true if order is desc.
     * 
     * @return is desc
     */
    public boolean isDesc() {
        return !asc;
    }

    /**
     * Return field's name use for ordering.
     * 
     * @return field's name
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return fieldName + (asc ? " asc" : " desc");
    }

}
