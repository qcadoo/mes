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
package com.qcadoo.mes.productionPerShift;

import java.math.BigDecimal;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.Interval;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.qcadoo.mes.productionPerShift.domain.ProductionProgressScope;
import com.qcadoo.mes.productionPerShift.domain.QuantitiesBalance;
import com.qcadoo.model.api.BigDecimalUtils;

public final class BalanceGenerationStrategy {

    private final Interval searchInterval;

    private final boolean plannedQuantityRequired;

    private final boolean deviationRequired;

    private final BigDecimal deviationThreshold;

    public static BalanceGenerationStrategy forInterval(final Interval searchInterval) {
        return new BalanceGenerationStrategy(searchInterval, false, false, null);
    }

    public BalanceGenerationStrategy withPlannedQuantityRequired(final boolean plannedQuantityRequired) {
        boolean isDeviationRequired = plannedQuantityRequired && this.deviationRequired;
        return new BalanceGenerationStrategy(this.searchInterval, plannedQuantityRequired, isDeviationRequired,
                this.deviationThreshold);
    }

    public BalanceGenerationStrategy withDeviationRequired(final boolean deviationRequired) {
        boolean isPlannedRequired = deviationRequired || this.plannedQuantityRequired;
        return new BalanceGenerationStrategy(this.searchInterval, isPlannedRequired, deviationRequired, this.deviationThreshold);
    }

    public BalanceGenerationStrategy withDeviationThreshold(final BigDecimal threshold) {
        boolean isPlannedRequired = threshold != null || this.plannedQuantityRequired;
        boolean isDeviationRequired = threshold != null || this.deviationRequired;
        return new BalanceGenerationStrategy(this.searchInterval, isPlannedRequired, isDeviationRequired, threshold);
    }

    private BalanceGenerationStrategy(final Interval interval, final boolean plannedQuantityRequired,
            final boolean deviationRequired, final BigDecimal deviationThreshold) {
        Preconditions.checkArgument(interval != null, "Given searchInterval must be not null.");
        this.searchInterval = interval;
        this.plannedQuantityRequired = plannedQuantityRequired;
        this.deviationRequired = deviationRequired;
        this.deviationThreshold = deviationThreshold;
    }

    public Interval getSearchInterval() {
        return searchInterval;
    }

    public Set<ProductionProgressScope> combineScopes(final Set<ProductionProgressScope> scopesForPlanned,
            final Set<ProductionProgressScope> scopesForRegistered) {
        if (plannedQuantityRequired) {
            return scopesForPlanned;
        }
        return Sets.union(scopesForPlanned, scopesForRegistered);
    }

    public boolean balanceMatchesRequirements(final QuantitiesBalance balance) {
        if (balance.getPlanned() == null && balance.getRegistered() == null) {
            return false;
        }
        if (plannedQuantityRequired && balance.getPlanned() == null) {
            return false;
        }
        if (!deviationRequired) {
            return true;
        }
        BigDecimal percentageDeviation = BigDecimalUtils.convertNullToZero(balance.getPercentageDeviation());
        if (deviationThreshold == null) {
            return percentageDeviation.compareTo(BigDecimal.ZERO) != 0;
        }
        return percentageDeviation.abs().compareTo(BigDecimalUtils.convertNullToZero(deviationThreshold)) >= 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BalanceGenerationStrategy that = (BalanceGenerationStrategy) o;
        return new EqualsBuilder().append(searchInterval, that.searchInterval)
                .append(plannedQuantityRequired, that.plannedQuantityRequired).append(deviationRequired, that.deviationRequired)
                .append(deviationThreshold, that.deviationThreshold).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(searchInterval).append(plannedQuantityRequired).append(deviationRequired)
                .append(deviationThreshold).toHashCode();
    }
}
