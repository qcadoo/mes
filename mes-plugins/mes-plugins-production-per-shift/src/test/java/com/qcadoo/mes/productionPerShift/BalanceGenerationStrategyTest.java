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

import com.google.common.collect.Sets;
import com.qcadoo.mes.productionPerShift.domain.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class BalanceGenerationStrategyTest {

    private static final Interval SEARCH_INTERVAL = new Interval(new DateTime(2014, 1, 1, 0, 0, 0), new DateTime(2014, 12, 31,
            23, 59, 59));

    private static final BalanceGenerationStrategy BASE_STRATEGY = BalanceGenerationStrategy.forInterval(SEARCH_INTERVAL);

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private ProductionProgressScope buildScope(final Long id) {
        LocalDate localDate = SEARCH_INTERVAL.getStart().plusHours(id.intValue()).toLocalDate();

        Order order = new Order(id, "ord-" + id);
        Shift shift = new Shift(id, "Shift " + id);
        Product product = new Product(id, "prod-" + id, "kg");

        return new ProductionProgressScope(localDate, order, shift, product);
    }

    private QuantitiesBalance buildBalance(final BigDecimal planned, final BigDecimal registered) {
        return new QuantitiesBalance(planned, registered);
    }

    @Test
    public void shouldReturnSearchInterval() {
        assertEquals(SEARCH_INTERVAL, BASE_STRATEGY.getSearchInterval());
    }

    @Test
    public void shouldBaseStrategyReturnsScopesUnion() {
        // given
        BalanceGenerationStrategy strategy = BASE_STRATEGY;
        Set<ProductionProgressScope> scopesForPlanned = Sets.newHashSet(buildScope(1L), buildScope(2L), buildScope(3L));
        Set<ProductionProgressScope> scopesForRegistered = Sets.newHashSet(buildScope(2L), buildScope(3L), buildScope(4L),
                buildScope(5L));

        // when
        Set<ProductionProgressScope> combinedScopes = strategy.combineScopes(scopesForPlanned, scopesForRegistered);

        // then
        assertEquals(5, combinedScopes.size());
        assertTrue(combinedScopes.containsAll(Sets.union(scopesForPlanned, scopesForRegistered)));
    }

    @Test
    public void shouldStrategyWithPlannedQuantitiesRequiredReturnsPlannedScopesSet() {
        // given
        BalanceGenerationStrategy strategy = BASE_STRATEGY.withPlannedQuantityRequired(true);
        Set<ProductionProgressScope> scopesForPlanned = Sets.newHashSet(buildScope(1L), buildScope(2L), buildScope(3L));
        Set<ProductionProgressScope> scopesForRegistered = Sets.newHashSet(buildScope(2L), buildScope(3L), buildScope(4L),
                buildScope(5L));

        // when
        Set<ProductionProgressScope> combinedScopes = strategy.combineScopes(scopesForPlanned, scopesForRegistered);

        // then
        assertEquals(3, combinedScopes.size());
        assertTrue(combinedScopes.containsAll(scopesForPlanned));
    }

    @Test
    public void shouldBalanceMatchesRequirementsReturnsCorrectValues() {
        QuantitiesBalance emptyBalance = buildBalance(null, null);
        QuantitiesBalance onlyPlannedBalance = buildBalance(BigDecimal.TEN, null);
        QuantitiesBalance onlyRegisteredBalance = buildBalance(null, BigDecimal.TEN);
        QuantitiesBalance fullBalance = buildBalance(BigDecimal.TEN, BigDecimal.TEN);
        QuantitiesBalance positiveDeviationBalance = buildBalance(BigDecimal.ONE, BigDecimal.TEN);
        QuantitiesBalance negativeDeviationBalance = buildBalance(BigDecimal.TEN, BigDecimal.ONE);

        BalanceGenerationStrategy strategy = BASE_STRATEGY;
        performBalanceRequirementsTest(strategy, emptyBalance, false);
        performBalanceRequirementsTest(strategy, onlyRegisteredBalance, true);
        performBalanceRequirementsTest(strategy, onlyPlannedBalance, true);
        performBalanceRequirementsTest(strategy, fullBalance, true);

        strategy = BASE_STRATEGY.withPlannedQuantityRequired(true);
        performBalanceRequirementsTest(strategy, emptyBalance, false);
        performBalanceRequirementsTest(strategy, onlyRegisteredBalance, false);
        performBalanceRequirementsTest(strategy, onlyPlannedBalance, true);
        performBalanceRequirementsTest(strategy, fullBalance, true);
        performBalanceRequirementsTest(strategy, positiveDeviationBalance, true);
        performBalanceRequirementsTest(strategy, negativeDeviationBalance, true);

        strategy = BASE_STRATEGY.withDeviationRequired(true);
        performBalanceRequirementsTest(strategy, emptyBalance, false);
        performBalanceRequirementsTest(strategy, onlyRegisteredBalance, false);
        performBalanceRequirementsTest(strategy, onlyPlannedBalance, false);
        performBalanceRequirementsTest(strategy, fullBalance, false);
        performBalanceRequirementsTest(strategy, positiveDeviationBalance, true);
        performBalanceRequirementsTest(strategy, negativeDeviationBalance, true);

        strategy = BASE_STRATEGY.withDeviationThreshold(BigDecimal.ONE);
        performBalanceRequirementsTest(strategy, emptyBalance, false);
        performBalanceRequirementsTest(strategy, onlyRegisteredBalance, false);
        performBalanceRequirementsTest(strategy, onlyPlannedBalance, false);
        performBalanceRequirementsTest(strategy, fullBalance, false);
        performBalanceRequirementsTest(strategy, positiveDeviationBalance, true);
        performBalanceRequirementsTest(strategy, negativeDeviationBalance, true);

        strategy = BASE_STRATEGY.withDeviationThreshold(BigDecimal.valueOf(901L));
        performBalanceRequirementsTest(strategy, emptyBalance, false);
        performBalanceRequirementsTest(strategy, onlyRegisteredBalance, false);
        performBalanceRequirementsTest(strategy, onlyPlannedBalance, false);
        performBalanceRequirementsTest(strategy, fullBalance, false);
        performBalanceRequirementsTest(strategy, positiveDeviationBalance, false);
        performBalanceRequirementsTest(strategy, negativeDeviationBalance, false);

        // Filtering by deviation should be disabled when planned quantities aren't mandatory.
        strategy = BASE_STRATEGY.withDeviationThreshold(BigDecimal.valueOf(901L)).withPlannedQuantityRequired(false);
        performBalanceRequirementsTest(strategy, emptyBalance, false);
        performBalanceRequirementsTest(strategy, onlyRegisteredBalance, true);
        performBalanceRequirementsTest(strategy, onlyPlannedBalance, true);
        performBalanceRequirementsTest(strategy, fullBalance, true);
        performBalanceRequirementsTest(strategy, positiveDeviationBalance, true);
        performBalanceRequirementsTest(strategy, negativeDeviationBalance, true);
    }

    private void performBalanceRequirementsTest(final BalanceGenerationStrategy strategy, final QuantitiesBalance balance,
                                                final boolean expectedResult) {
        // when
        boolean balanceMatches = strategy.balanceMatchesRequirements(balance);

        // then
        assertEquals(expectedResult, balanceMatches);
    }

}
