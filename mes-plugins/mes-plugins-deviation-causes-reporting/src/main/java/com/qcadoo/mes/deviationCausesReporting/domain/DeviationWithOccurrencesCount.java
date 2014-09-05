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
