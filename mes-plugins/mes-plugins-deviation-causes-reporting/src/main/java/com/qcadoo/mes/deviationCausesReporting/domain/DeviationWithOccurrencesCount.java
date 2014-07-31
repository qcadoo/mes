package com.qcadoo.mes.deviationCausesReporting.domain;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Preconditions;

public class DeviationWithOccurrencesCount implements DeviationCauseHolder {

    private final String deviationCause;

    private final Long totalNumberOfOccurrences;

    public DeviationWithOccurrencesCount(final String deviationCause, final Long totalNumberOfOccurrences) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(deviationCause), "Deviation cause (deviation type) cannot be empty!");
        this.deviationCause = deviationCause;
        this.totalNumberOfOccurrences = (Long) ObjectUtils.defaultIfNull(totalNumberOfOccurrences, 0L);
    }

    public String getDeviationCause() {
        return deviationCause;
    }

    public Long getTotalNumberOfOccurrences() {
        return totalNumberOfOccurrences;
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
        DeviationWithOccurrencesCount rhs = (DeviationWithOccurrencesCount) obj;
        return new EqualsBuilder().append(this.deviationCause, rhs.deviationCause)
                .append(this.totalNumberOfOccurrences, rhs.totalNumberOfOccurrences).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deviationCause).append(totalNumberOfOccurrences).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deviationCause", deviationCause)
                .append("totalNumberOfOccurrences", totalNumberOfOccurrences).toString();
    }
}
