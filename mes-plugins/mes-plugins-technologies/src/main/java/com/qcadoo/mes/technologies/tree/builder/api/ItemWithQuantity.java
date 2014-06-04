/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.technologies.tree.builder.api;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This is simple container for some value of arbitrary type with (BigDecimal) quantity.
 * 
 * If item's type is immutable one then whole container will be also immutable.
 * 
 * @param <T>
 *            type of the containing item
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public class ItemWithQuantity<T> {

    public final BigDecimal quantity;

    public final T item;

    public ItemWithQuantity(final T item, final BigDecimal quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * @return quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * @return item
     */
    public T getItem() {
        return item;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(item).append(quantity).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ItemWithQuantity)) {
            return false;
        }
        ItemWithQuantity<?> other = (ItemWithQuantity<?>) obj;
        return new EqualsBuilder().append(item, other.item).append(quantity, other.quantity).isEquals();
    }

}
