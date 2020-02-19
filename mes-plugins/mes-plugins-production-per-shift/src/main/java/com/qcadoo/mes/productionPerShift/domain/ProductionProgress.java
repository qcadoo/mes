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

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;

public class ProductionProgress {

    // required
    private final ProductionProgressScope scope;

    // null allowed
    private final BigDecimal quantity;

    public ProductionProgress(final ProductionProgressScope scope, final BigDecimal quantity) {
        Preconditions.checkArgument(scope != null, "Progress scope can't be null.");
        this.scope = scope;
        this.quantity = quantity;
    }

    public ProductionProgressScope getScope() {
        return scope;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProductionProgress that = (ProductionProgress) o;
        return new EqualsBuilder().append(scope, that.scope).append(quantity, that.quantity).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(scope).append(quantity).toHashCode();
    }
}
