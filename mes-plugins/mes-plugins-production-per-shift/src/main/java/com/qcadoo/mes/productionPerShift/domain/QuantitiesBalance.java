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
import java.math.MathContext;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.qcadoo.model.api.BigDecimalUtils;

public class QuantitiesBalance {

    private final BigDecimal planned;

    private final BigDecimal registered;

    public QuantitiesBalance() {
        this(null, null);
    }

    public QuantitiesBalance(final BigDecimal planned, final BigDecimal registered) {
        this.planned = planned;
        this.registered = registered;
    }

    public BigDecimal getPlanned() {
        return planned;
    }

    public BigDecimal getRegistered() {
        return registered;
    }

    public BigDecimal getDifference() {
        if (planned == null || registered == null) {
            return null;
        }
        if (BigDecimalUtils.valueEquals(planned, BigDecimal.ZERO)) {
            return null;
        }
        return registered.subtract(planned);
    }

    public BigDecimal getPercentageDeviation() {
        BigDecimal difference = getDifference();
        if (difference == null) {
            return null;
        }
        return difference.divide(planned, MathContext.DECIMAL64).multiply(BigDecimal.valueOf(100L));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QuantitiesBalance that = (QuantitiesBalance) o;
        return new EqualsBuilder().append(planned, that.planned).append(registered, that.registered).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planned).append(registered).toHashCode();
    }
}
