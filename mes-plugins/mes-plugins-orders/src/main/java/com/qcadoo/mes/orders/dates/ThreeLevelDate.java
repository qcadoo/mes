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
package com.qcadoo.mes.orders.dates;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import com.google.common.base.Optional;

public final class ThreeLevelDate {

    private final DateTime planned;

    private final Optional<DateTime> corrected;

    private final Optional<DateTime> effective;

    ThreeLevelDate(final DateTime plannedDate, final Optional<DateTime> maybeCorrectedDate,
            final Optional<DateTime> maybeEffectiveDate) {
        planned = plannedDate;
        corrected = maybeCorrectedDate;
        effective = maybeEffectiveDate;
    }

    public DateTime planned() {
        return planned;
    }

    public Optional<DateTime> corrected() {
        return corrected;
    }

    public DateTime correctedWithFallback() {
        return corrected.or(planned);
    }

    public Optional<DateTime> effective() {
        return effective;
    }

    public DateTime effectiveWithFallback() {
        return effective.or(correctedWithFallback());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ThreeLevelDate rhs = (ThreeLevelDate) obj;
        return new EqualsBuilder().append(this.planned, rhs.planned).append(this.corrected, rhs.corrected)
                .append(this.effective, rhs.effective).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(planned).append(corrected).append(effective).toHashCode();
    }
}
