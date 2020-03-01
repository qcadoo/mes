/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.productionPerShift.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import com.google.common.base.Preconditions;

public class ProductionProgressScope {

    private final Order order;

    private final Shift shift;

    private final Product product;

    private final LocalDate day;

    public ProductionProgressScope(final LocalDate day, final Order order, final Shift shift, final Product product) {

        Preconditions.checkArgument(day != null, "day must be not null");
        Preconditions.checkArgument(order != null, "order must be not null");
        Preconditions.checkArgument(shift != null, "shift must be not null");
        Preconditions.checkArgument(product != null, "product must be not null");

        this.day = day;
        this.order = order;
        this.shift = shift;
        this.product = product;
    }

    public Order getOrder() {
        return order;
    }

    public Shift getShift() {
        return shift;
    }

    public LocalDate getDay() {
        return day;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProductionProgressScope that = (ProductionProgressScope) o;
        return new EqualsBuilder().append(order, that.order).append(shift, that.shift).append(day, that.day)
                .append(product, that.product).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(order).append(shift).append(day).toHashCode();
    }

    @Override
    public String toString() {
        return "ProdProgressScope{" + "order=" + order + ", shift=" + shift + ", day=" + day + '}';
    }
}
