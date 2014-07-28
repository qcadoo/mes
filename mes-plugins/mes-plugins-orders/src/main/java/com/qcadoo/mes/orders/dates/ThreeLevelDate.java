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
