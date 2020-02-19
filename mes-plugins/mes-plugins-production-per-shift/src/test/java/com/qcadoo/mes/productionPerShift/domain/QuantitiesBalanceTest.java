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

import com.qcadoo.model.api.BigDecimalUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class QuantitiesBalanceTest {

    @Test
    public void shouldHasZeroDeviations() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(BigDecimal.ONE, BigDecimal.ONE);

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertTrue(BigDecimalUtils.valueEquals(difference, BigDecimal.ZERO));
        Assert.assertTrue(BigDecimalUtils.valueEquals(deviation, BigDecimal.ZERO));
    }

    @Test
    public void shouldHasDeviations() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(BigDecimal.ONE, BigDecimal.valueOf(2L));

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertTrue(BigDecimalUtils.valueEquals(difference, BigDecimal.ONE));
        Assert.assertTrue(BigDecimalUtils.valueEquals(deviation, BigDecimal.valueOf(100L)));
    }

    @Test
    public void shouldHasNegativeDeviationsForRegisteredQuantityEqualsZero() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(BigDecimal.ONE, BigDecimal.ZERO);

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertTrue(BigDecimalUtils.valueEquals(difference, BigDecimal.ZERO.subtract(BigDecimal.ONE)));
        Assert.assertTrue(BigDecimalUtils.valueEquals(deviation, BigDecimal.ZERO.subtract(BigDecimal.valueOf(100L))));
    }

    // TODO maku - confirm this behaviour
    @Test
    public void shouldTreatPlannedQuantityEqualsZeroAsALackOfPlan() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(BigDecimal.ZERO, BigDecimal.ONE);

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertNull(difference);
        Assert.assertNull(deviation);
    }

    @Test
    public void shouldHasNullDeviationsIfBothValuesAreNull() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(null, null);

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertNull(difference);
        Assert.assertNull(deviation);
    }

    @Test
    public void shouldHasNullDeviationsIfPlannedValueIsNull() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(null, BigDecimal.ONE);

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertNull(difference);
        Assert.assertNull(deviation);
    }

    @Test
    public void shouldHasNullDeviationsIfRegisteredValueIsNull() {
        // given
        QuantitiesBalance balance = new QuantitiesBalance(BigDecimal.ONE, null);

        // when
        BigDecimal difference = balance.getDifference();
        BigDecimal deviation = balance.getPercentageDeviation();

        // then
        Assert.assertNull(difference);
        Assert.assertNull(deviation);
    }

}
